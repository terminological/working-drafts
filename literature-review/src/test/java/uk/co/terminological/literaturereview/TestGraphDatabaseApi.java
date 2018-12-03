package uk.co.terminological.literaturereview;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.text.similarity.CosineDistance;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.icm.cermine.exception.AnalysisException;

import uk.co.terminological.literaturereview.PubMedGraphSchema.Labels;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Prop;
import uk.co.terminological.nlptools.Similarity;
import uk.co.terminological.nlptools.StringCrossMapper;
import uk.co.terminological.nlptools.Term;
import uk.co.terminological.pubmedclient.BibliographicApiException;



public class TestGraphDatabaseApi {

	private static final Logger logger = LoggerFactory.getLogger(TestGraphDatabaseApi.class);
	
	public static void main(String args[]) throws IOException, BibliographicApiException, AnalysisException {
		BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);

		String propFilename = args.length ==1? args[0]: "~/Dropbox/litReview/project.prop";
		Path propPath= Paths.get(propFilename.replace("~", System.getProperty("user.home")));
		Properties prop = System.getProperties();
		prop.load(Files.newInputStream(propPath));

		prop.forEach((k,v) -> prop.put(k, v.toString().replace("~", System.getProperty("user.home")))); 
		Path graphDbPath = fromProperty(prop,"graph-db-directory");
		Path graphConfPath = fromProperty(prop,"graph-conf-file");
		Path outputDir = fromProperty(prop,"output-directory");
		
		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphDbPath, graphConfPath);
		
		
		Map<String,StringCrossMapper> surnameMapper = new HashMap<>();
		
		logger.info("loading from graph");
		
		try (Transaction tx = graphApi.get().beginTx()) {
			
			graphApi.get().findNodes(Labels.AUTHOR).stream().forEach( //.limit(30).forEach(
				n -> {
					
					String lastName = (n.getProperty(Prop.LAST_NAME, "unknown").toString()+"_"+
							(n.getProperty(Prop.INITIALS, "").toString().isEmpty() ?
									n.getProperty(Prop.FIRST_NAME, "unknown").toString().substring(0,1):
									n.getProperty(Prop.INITIALS, "unknown").toString().substring(0,1)
									)
							).toLowerCase();
					StringCrossMapper mapper = Optional.ofNullable(surnameMapper.get(lastName)).orElseGet(() -> {
						StringCrossMapper tmp = new StringCrossMapper("University","Institute","Department","of","at","is","a","for");
						surnameMapper.put(lastName,tmp);
						return tmp;
					});
					
					StringBuilder nameAffiliation = new StringBuilder(n.getProperty(Prop.FIRST_NAME, "").toString());
					
					String[] affils = (String[]) n.getProperty(Prop.AFFILIATIONS, new String[] {""});
					for (int i=0; i<affils.length; i++) {
						nameAffiliation.append(" "+affils[i]);
					}
					
					System.out.println(n.getId()+"\t"+nameAffiliation.toString());
					mapper.addSource(Long.toString(n.getId()),nameAffiliation.toString()); 
					mapper.addTarget(Long.toString(n.getId()),nameAffiliation.toString());
					
				}
			);			
		}
		
		PrintStream out = new PrintStream(Files.newOutputStream(outputDir.resolve("authorSim.tsv")));
		
		
		surnameMapper.forEach((surname,mapper)-> {
		
		logger.info(mapper.summaryStats());
		
		if (mapper.getSource().countCorpusDocuments() > 20) {
		
		mapper.getAllMatchesBySimilarity(0D, d-> d.termsByEntropy(), Similarity::getEuclideanDistance).forEach(
			(src,match) -> {
				match.forEach((target,score) -> {
					out.println(
							score+"\t"+
						src.getIdentifier()+"\t"+
						target.getIdentifier()+"\t"+
						src.getString()+"\t"+
						target.getString()
				);
			});
		});
		
		}
		
		});
		
		
		logger.info("file written");
		out.close();
		
		graphApi.shutdown();
		// graphApi.waitAndShutdown();
	}
	
	static String repeat(String s, int count) {
		StringBuilder out = new StringBuilder();
		for (int i=0; i<count; i++) out.append(s+" ");
		return out.toString().trim();
	}
	
	static Path fromProperty(Properties prop, String name) {
		return Paths.get(prop.getProperty(name).replace("~", System.getProperty("user.home")));
	}
	
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
	}

	@Test
	public final void testCreate() throws IOException {
		
		Path tmp = Files.createTempDirectory("neo4j");
		
		GraphDatabaseApi api = GraphDatabaseApi.create(tmp);
		logger.info("graph Db available in "+tmp);
		api.shutdown();
		
		Files.deleteIfExists(tmp);
	}

	
}
