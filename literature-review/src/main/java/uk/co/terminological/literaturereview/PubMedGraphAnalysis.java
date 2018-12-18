package uk.co.terminological.literaturereview;

import static uk.co.terminological.simplechart.Chart.Dimension.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Values;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
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
		
		Figure fig = Figure.outputTo(new File(System.getProperty("user.home")+"/Dropbox/litReview/output"));
		
		@SuppressWarnings("unchecked")
		Map<String,String> queries = (Map<String,String> ) obj.get("analyse");
		
		try ( Session session = driver.session() ) {
	        
			//Plot by age
			session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getArticlesByAge");
	        	List<Record> res = tx.run( qry ).list();
	        	
	        	try {
	        	fig.withNewChart("Articles by age", ChartType.XYSCATTER)
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
	        	Set<String> nodes = new HashSet<>();
	        	List<Triple<String,Double,String>> links = new ArrayList<>();
	        	List<Triple<String,Integer,String>> links2 = new ArrayList<>();
	        	
	        	res.forEach(r -> {
	        		String sourceTerm = r.get("sourceTerm").asString();
	        		String targetTerm = r.get("targetTerm").asString();
	        		Integer cooccurrenceCount = r.get("cooccurrences").asInt();
	        		Double npmi = r.get("npmi").asDouble();
	        		
	        		if (nodes.size() < 50) {
	        			nodes.add(sourceTerm);
	        			nodes.add(targetTerm);
	        		}
	        		
	        		if (nodes.contains(sourceTerm) && nodes.contains(targetTerm)) {
	        			links.add(Triple.create(sourceTerm, npmi, targetTerm));
	        			links2.add(Triple.create(sourceTerm, cooccurrenceCount, targetTerm));
	        		}	
		        	
	        		
	        			        		
	        	});
	        	
	        	try {
	        	fig.withNewChart("Mesh codes by npmi", ChartType.CHORD)
					.withSeries(links)
						.bind(ID, t -> t.getFirst(), "source")
						.bind(STRENGTH, t -> t.getSecond())
						.bind(ID, t -> t.getThird(), "target")
						.withColourScheme(ColourScheme.Accent)
					.done()
					.render();
	        	
	        	fig.withNewChart("Mesh codes by cooccurrence", ChartType.CHORD)
				.withSeries(links2)
					.bind(ID, t -> t.getFirst(), "source")
					.bind(STRENGTH, t -> t.getSecond())
					.bind(ID, t -> t.getThird(), "target")
					.withColourScheme(ColourScheme.Set1)
				.done()
				.render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	
	            return true;
	        });
	        
	        session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getKeywordCooccurMutualInformation");
	        	List<Record> res = tx.run( qry ).list();
	        	//List<Triple<String,Double,String>> links = new ArrayList<>();
	        	Set<String> nodes = new HashSet<>();
	        	List<Triple<String,Double,String>> links = new ArrayList<>();
	        	List<Triple<String,Integer,String>> links2 = new ArrayList<>();
	        	
	        	res.forEach(r -> {
	        		String sourceTerm = r.get("sourceTerm").asString();
	        		String targetTerm = r.get("targetTerm").asString();
	        		Integer cooccurrenceCount = r.get("cooccurrences").asInt();
	        		Double npmi = r.get("npmi").asDouble();
	        		
	        		if (nodes.size() < 50) {
	        			nodes.add(sourceTerm);
	        			nodes.add(targetTerm);
	        		}
	        		
	        		if (nodes.contains(sourceTerm) && nodes.contains(targetTerm)) {
	        			links.add(Triple.create(sourceTerm, npmi, targetTerm));
	        			links2.add(Triple.create(sourceTerm, cooccurrenceCount, targetTerm));
	        		}	
		        	
	        		
	        			        		
	        	});
	        	
	        	try {
	        	fig.withNewChart("Keywords by npmi", ChartType.CHORD)
					.withSeries(links)
						.bind(ID, t -> t.getFirst(), "source")
						.bind(STRENGTH, t -> t.getSecond())
						.bind(ID, t -> t.getThird(), "target")
						.withColourScheme(ColourScheme.Set2)
					.done()
					.render();
	        	
	        	fig.withNewChart("Keywords by cooccurrence", ChartType.CHORD)
				.withSeries(links2)
					.bind(ID, t -> t.getFirst(), "source")
					.bind(STRENGTH, t -> t.getSecond())
					.bind(ID, t -> t.getThird(), "target")
					.withColourScheme(ColourScheme.Set3)
				.done()
				.render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	
	            return true;
	        });
	        
	        List<Integer> communityIndex = new ArrayList<>();
	        
	        
	        session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getAuthorCommunityAffiliations");
	        	List<Record> res = tx.run( qry ).list();
	        	
	        	
	        	BiConsumer<List<String>,Integer> plot = (list,community) -> {try {
	        	
	        		Integer i = communityIndex.indexOf(community);
	        		if (i == -1) {
	        			i = communityIndex.size();
	        			communityIndex.add(community);
	        		}
	        		
	        		fig.withNewChart("Community affiliations "+i, ChartType.WORDCLOUD)
					.withSeries(list)
						.bind(TEXT, t -> t)
						.withColourScheme(ColourScheme.sequential(community))
					.done()	
					.withSeries(Arrays.asList(
							"university","of","the","college", "department", "division", "research"
							)).bind(TEXT, t -> t).done()
					.render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	};
	        	
	        	List<String> texts = new ArrayList<>();
	        	Integer community = null;
	        	for( Record r : res) {
	        		Integer next = r.get("community").asInt();
	        		if (community == null) community = next;
	        		if (community != next) {
	        			plot.accept(texts, community);
	        			texts = new ArrayList<>();
	        		}
	        		texts.addAll(r.get("affiliations").asList(Values.ofString()));
	        		community = next;
	        	}
	        	plot.accept(texts, community);
	        	return true;
	        });
	        
	        // 
	        
	        session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getAuthorCommunityKeywords");
	        	List<Record> res = tx.run( qry ).list();
	        	
	        	
	        	BiConsumer<List<String>,Integer> plot = (list,community) -> {try {
	        	
	        		Integer i = communityIndex.indexOf(community);
	        		if (i == -1) {
	        			i = communityIndex.size();
	        			communityIndex.add(community);
	        		}
	        		
	        		fig.withNewChart("Community keywords "+i, ChartType.WORDCLOUD)
					.withSeries(list)
						.bind(TEXT, t -> t)
						.withColourScheme(ColourScheme.sequential(community))
					.done()	
					//.withSeries(Arrays.asList(
					//		)).bind(TEXT, t -> t).done()
					.render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	};
	        	
	        	List<String> texts = new ArrayList<>();
	        	Integer community = null;
	        	for( Record r : res) {
	        		Integer next = r.get("community").asInt();
	        		if (community == null) community = next;
	        		if (community != next) {
	        			plot.accept(texts, community);
	        			texts = new ArrayList<>();
	        		}
	        		texts.addAll(r.get("terms").asList(Values.ofString()));
	        		community = next;
	        	}
	        	plot.accept(texts, community);
	        	return true;
	        });
	        
	    } catch ( ServiceUnavailableException ex ) {
	        log.error(ex.getMessage());
	    }
		
		driver.close();
		
	}
	
	
}
