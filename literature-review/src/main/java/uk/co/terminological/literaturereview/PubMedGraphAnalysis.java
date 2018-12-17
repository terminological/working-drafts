package uk.co.terminological.literaturereview;

import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.File;
import java.io.InputStream;
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
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

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
	        session.readTransaction( tx -> {
	        	
	        	String qry = queries.get("getArticlesByAge");
	        	List<Record> res = tx.run( qry ).list();
	        	
	        	try {
	        	Figure.outputTo(new File(System.getProperty("user.home")+"/tmp/ggplot"))
					.withNewChart("Hello", ChartType.XYBAR)
					.withSeries(res)
						.bind(X, t -> t.get("qtr"))
						.bind(Y, t -> t.get("articles"))
					.done()
					.config()
						.withXLabel("time ago")
						.withYLabel("articles")
					.done().render();
	        	} catch (Exception e) {throw new RuntimeException(e);}
	        	
	            return true;
	        });
	    } catch ( ServiceUnavailableException ex ) {
	        log.error(ex.getMessage());
	    }
		
	}
	
}
