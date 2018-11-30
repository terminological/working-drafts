package uk.co.terminological.literaturereview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.internal.frontend.v2_3.perty.recipe.Pretty.doc;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import pl.edu.icm.cermine.exception.AnalysisException;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Labels;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Prop;
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
		
		GraphDatabaseApi graphApi = GraphDatabaseApi.create(graphDbPath, graphConfPath);
		StringCrossMapper mapper = new StringCrossMapper("University","Institute","Informatics","Computer","Science","Medicine");
		
		try (Transaction tx = graphApi.get().beginTx()) {
			
			graphApi.get().findNodes(Labels.AUTHOR).forEachRemaining(
				n -> {
					String[] affils = (String[]) n.getProperty(Prop.AFFILITATIONS, new String[] {});
					for (String affil: affils) {
						String doc = repeat(n.getProperty(Prop.FIRST_NAME, "").toString(),5)+" "+
								repeat(n.getProperty(Prop.LAST_NAME, "").toString(),5)+" "+
								repeat(n.getProperty(Prop.INITIALS, "").toString(),5)+" "+
								affil; 
						mapper.addSource(Long.toString(n.getId()),doc); 
						mapper.addTarget(Long.toString(n.getId()),doc);
					}
				}
			);			
		}
		
		System.out.println(mapper.summaryStats());
		
		mapper.getAllMatchesBySignificance(0D).forEach(
			(src,match) -> {
				match.forEach((target,score) -> {
					
					System.out.println(
							src.identifier+"\t"+src.string+"\t"
							+target.identifier+"\t"+target.string
							+score
							);
					
				});
			});
		
		
		graphApi.waitAndShutdown();
	}
	
	static String repeat(String s, int count) {
		StringBuilder out = new StringBuilder();
		for (int i=0; i<count; i++) out.append(s);
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
