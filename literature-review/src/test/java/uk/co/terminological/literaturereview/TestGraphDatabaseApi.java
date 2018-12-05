package uk.co.terminological.literaturereview;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.edu.icm.cermine.exception.AnalysisException;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Labels;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Prop;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Rel;
import uk.co.terminological.nlptools.Similarity;
import uk.co.terminological.nlptools.StringCrossMapper;
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
		
		
		//Map<String,StringCrossMapper> surnameMapper = new HashMap<>();
		StringCrossMapper mapper = new StringCrossMapper("University","Institute","Department", "Research","of","at","is","a","for", "Dept");
		//TODO: Cross map affiliation strings not names
		
		
		logger.info("loading from graph");
		
		try (Transaction tx = graphApi.get().beginTx()) {
			
			graphApi.get().findNodes(Labels.AFFILIATION).stream().forEach( //.limit(30).forEach(
				n -> {
					
					/*String lastName = (n.getProperty(Prop.LAST_NAME, "unknown").toString()+"_"+
							(n.getProperty(Prop.INITIALS, "").toString().isEmpty() ?
									n.getProperty(Prop.FIRST_NAME, "unknown").toString().substring(0,1):
									n.getProperty(Prop.INITIALS, "unknown").toString().substring(0,1)
									)
							).toLowerCase();*/
					
					// StringBuilder nameAffiliation = new StringBuilder(n.getProperty(Prop.FIRST_NAME, "").toString());
					
					String affil = n.getProperty(Prop.ORGANISATION_NAME).toString();
					// if (affils.length > 0) {
						/*StringCrossMapper mapper = Optional.ofNullable(surnameMapper.get(lastName)).orElseGet(() -> {
							StringCrossMapper tmp = new StringCrossMapper("University","Institute","Department","of","at","is","a","for");
							surnameMapper.put(lastName,tmp);
							return tmp;
						});*/
										
					// for (int i=0; i<affils.length; i++) {
						
						// nameAffiliation.append(" "+affils[i]);
						// System.out.println(n.getId()+"\t"+nameAffiliation.toString());
						// String nameAffiliation = lastName+" "+n.getProperty(Prop.FIRST_NAME, "")+" "+affils[i];
						mapper.addSource(Long.toString(n.getId()),affil.toString()); 
						mapper.addTarget(Long.toString(n.getId()),affil.toString());
					
					}
					
					
				
			);			
		}
		
		//PrintStream out = new PrintStream(Files.newOutputStream(outputDir.resolve("authorSim.tsv")));
		
		
		//surnameMapper.forEach((surname,mapper)-> {
		
		logger.info(mapper.summaryStats());
		
		//if (mapper.getSource().countCorpusDocuments() > 20) {
		
		try (Transaction tx = graphApi.get().beginTx()) {
		mapper.getAllMatchesBySimilarity(0.9D, d -> d.termsByTfIdf(), Similarity::getCosineDifference).forEach(triple -> {
			if (!triple.getFirst().equals(triple.getSecond())) {
				Node in = graphApi.get().getNodeById(Long.parseLong(triple.getFirst().getIdentifier()));
				Node out2 = graphApi.get().getNodeById(Long.parseLong(triple.getSecond().getIdentifier()));
				Relationship r = in.createRelationshipTo(out2, Rel.SIMILAR_TO);
				r.setProperty(Prop.SCORE, triple.getThird());
				System.out.print(".");
			} else {
				System.out.print("x");
			}
			
		});
		}
		
		/*mapper.getAllMatchesBySimilarity(0.9D, d-> d.termsByEntropy(), Similarity::getCosineDifference).forEach(
			t -> {
				out.println(
						t.getThird()+"\t"+
						t.getFirst().getIdentifier()+"\t"+
						t.getSecond().getIdentifier()+"\t"+
						t.getFirst().getString()+"\t"+
						t.getSecond().getString()
				);
			});*/
		
		
		//}
		
		//});
		
		
		logger.info("file written");
		out.close();
		
		// graphApi.shutdown();
		graphApi.waitAndShutdown();
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
