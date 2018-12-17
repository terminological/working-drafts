package uk.co.terminological.literaturereview;

import static uk.co.terminological.simplechart.Chart.Dimension.ID;
import static uk.co.terminological.simplechart.Chart.Dimension.STRENGTH;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.Figure;

public class PubMedGraphAnalysis {

	static Logger log = LoggerFactory.getLogger(PubMedGraphAnalysis.class);
	
	public static void main(String[] args) {
		
		BasicConfigurator.configure();
		
		Config config = Config.builder()
	            .withMaxConnectionLifetime( 30, TimeUnit.MINUTES )
	            .withMaxConnectionPoolSize( 50 )
	            .withConnectionAcquisitionTimeout( 2, TimeUnit.MINUTES )
	            .build();
		
		final Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo4j" ), config );
		
		Yaml yaml = new Yaml();
		InputStream inputStream = PubMedGraphAnalysis.class.getClassLoader().getResourceAsStream("cypherQuery.yaml");
		Map<String, Object> obj = yaml.load(inputStream);
		
		@SuppressWarnings("unchecked")
		Map<String,String> queries = (Map<String,String> ) obj.get("analyse");
		
		try ( Session session = driver.session() ) {
	        
			//Plot by age
			session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getArticlesByAge");
	        	List<Record> res = tx.run( qry ).list();
	        	
	        	try {
	        	Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/lit-review"))
					.withNewChart("Articles by age", ChartType.XYSCATTER)
					.withSeries(res)
						.bind(X, t -> t.get("qtr").asDouble()/4)
						.bind(Y, t -> t.get("articles").asInt())
					.done()
					.config()
						.withXLabel("years elapsed")
						.withYLabel("articles")
					.done().render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	
	            return true;
	        });
	        
	        session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getMeshCodeCooccurMutualInformation");
	        	List<Record> res = tx.run( qry ).list();
	        	//List<Triple<String,Double,String>> links = new ArrayList<>();
	        	Map<String, Integer> nodes = new HashMap<>();
	        	Map<String, DataEntry> links = new HashMap<>();
	        	
	        	res.forEach(r -> {
	        		DataEntry tmp = new DataEntry();
	        		tmp.sourceTerm = r.get("sourceTerm").asString();
	        		tmp.sourceOccurrences = r.get("sourceOccurrences").asInt();
	        		tmp.targetTerm = r.get("targetTerm").asString();
	        		tmp.targetOccurrences = r.get("targetOccurrences").asInt();
	        		tmp.cooccurrenceCount = r.get("cooccurrences").asInt();
	        		tmp.totalOccurrence = r.get("totalOccurrences").asInt();
	        		
	        		if (!nodes.containsKey(tmp.sourceTerm) && nodes.size() < 50) {
	        			nodes.put(tmp.sourceTerm, tmp.sourceOccurrences);
	        		} else {
	        			tmp.sourceTerm = "Other";
	        		}
	        		
	        		if (!nodes.containsKey(tmp.targetTerm) && nodes.size() < 50) {
	        			nodes.put(tmp.targetTerm, tmp.targetOccurrences);
	        		} else {
	        			tmp.targetTerm = "Other";
	        		}
	        		
	        		DataEntry previous = links.get(tmp.key());
	        		if (previous != null) 
	        			tmp.cooccurrenceCount += previous.cooccurrenceCount;
	        		links.put(tmp.key(), tmp);
	        			        		
	        	});
	        	
	        	try {
	        	Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/lit-review"))
					.withNewChart("Articles by age", ChartType.CHORD)
					.withSeries(new ArrayList<>(links.values()))
						.bind(ID, t -> t.sourceTerm, "source")
						.bind(STRENGTH, t -> t.cooccurrenceCount)
						.bind(ID, t -> t.targetTerm, "term")
					.done()
					.render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	
	            return true;
	        });
	        
	    } catch ( ServiceUnavailableException ex ) {
	        log.error(ex.getMessage());
	    }
		
		driver.close();
		
	}
	
	private static class DataEntry {
		String sourceTerm;
		String targetTerm;
		Integer sourceOccurrences;
		Integer targetOccurrences;
		Integer cooccurrenceCount;
		Integer totalOccurrence;
		String key() {return sourceTerm+"_"+targetTerm;}
	}
	
}
