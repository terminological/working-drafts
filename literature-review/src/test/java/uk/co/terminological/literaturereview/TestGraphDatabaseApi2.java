package uk.co.terminological.literaturereview;

import java.io.IOException;
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
import uk.co.terminological.nlptools.Corpus;
import uk.co.terminological.nlptools.Similarity;
import uk.co.terminological.nlptools.StringCrossMapper;
import uk.co.terminological.pubmedclient.BibliographicApiException;



public class TestGraphDatabaseApi2 {

	private static final Logger logger = LoggerFactory.getLogger(TestGraphDatabaseApi2.class);
	
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
		
		graphApi.waitAndShutdown();
		
		logger.info("loading affiliations from graph");
		Corpus mapper = Corpus.create();
		
		try (Transaction tx = graphApi.get().beginTx()) {
			
			graphApi.get().findNodes(Labels.AFFILIATION).stream().forEach( //.limit(30).forEach(
				n -> {
					String affil = n.getProperty(Prop.ORGANISATION_NAME).toString();
					mapper.addDocument(doc);.addSource(Long.toString(n.getId()),affil.toString()); 
					mapper.addTarget(Long.toString(n.getId()),affil.toString());
			});
			
			log.info(mapper.summaryStats());
			mapper.getAllMatchesBySimilarity(0.9D, d -> d.termsByTfIdf(), Similarity::getCosineDifference).forEach(triple -> {
				if (!triple.getFirst().equals(triple.getSecond())) {
					Node in = graphApi.get().getNodeById(Long.parseLong(triple.getFirst().getIdentifier()));
					Node out = graphApi.get().getNodeById(Long.parseLong(triple.getSecond().getIdentifier()));
					Relationship r = in.createRelationshipTo(out, Rel.SIMILAR_TO);
					r.setProperty(Prop.SCORE, triple.getThird());
				}
				tx.success();
			});
			
		}
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
