package uk.co.terminological.literaturereview;

import static uk.co.terminological.simplechart.Chart.Dimension.ID;
import static uk.co.terminological.simplechart.Chart.Dimension.LABEL;
import static uk.co.terminological.simplechart.Chart.Dimension.STRENGTH;
import static uk.co.terminological.simplechart.Chart.Dimension.TEXT;
import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Values;
import org.yaml.snakeyaml.Yaml;

import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.nlptools.Corpus;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Figure;

public class LitReviewAnalysis {

	Driver driver;
	Map<String, Object> obj;
	Figure fig;
	List<String> affiliationStopwords;
	List<String> textStopwords;
	Map<String,String> queries;
	List<Integer> communityIndex = new ArrayList<>();

	void plot(Figure fig, String name, List<String> list, Integer community, List<String> stopwords) {
		try {
			Integer i = communityIndex.indexOf(community);
			if (i == -1) {
				i = communityIndex.size();
				communityIndex.add(community);
			}

			fig.withNewChart(name+" "+i, ChartType.WORDCLOUD)
			.withSeries(list)
			.bind(TEXT, t -> t)
			.withColourScheme(ColourScheme.sequential(i))
			.done()	
			.withSeries(stopwords).bind(TEXT, t -> t).done()
			.render();
		} catch (Exception e) {throw new RuntimeException(e);}
	};

	@SuppressWarnings("unchecked")
	@Before
	public void setUpBeforeClass() throws Exception {
		BasicConfigurator.configure();

		Config config = Config.builder()
				.withMaxConnectionLifetime( 30, TimeUnit.MINUTES )
				.withMaxConnectionPoolSize( 50 )
				.withConnectionAcquisitionTimeout( 2, TimeUnit.MINUTES )
				.build();

		driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "neo4j" ), config );

		Yaml yaml = new Yaml();
		InputStream inputStream = PubMedGraphAnalysis.class.getClassLoader().getResourceAsStream("cypherQuery.yaml");
		obj = yaml.load(inputStream);
		fig = Figure.outputTo(new File(System.getProperty("user.home")+"/Dropbox/litReview/output"));

		affiliationStopwords = Arrays.asList(((Map<String,String>) obj.get("config")).get("stopwordsForAffiliation").split("\n"));
		textStopwords = Arrays.asList(((Map<String,String>) obj.get("config")).get("stopwordsForText").split("\n"));
		queries = (Map<String,String> ) obj.get("analyse");
	}

	@After
	public void tearDownAfterClass() throws Exception {
		driver.close();
	}

	@Test
	public void plotAgeOfArticles() {
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
		}
	}

	@Test
	public void plotCooccurrenceOfMeshCodes() {
		try ( Session session = driver.session() ) {
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
		}
	}

	@Test
	public void plotCooccurrenceOfKeywords() {
		try ( Session session = driver.session() ) {
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
					.withSeries(new ArrayList<>(nodes))
					.bind(ID, t -> t)
					.bind(LABEL, t -> t)
					.done()
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

		}
	}

	@Test
	public void plotCommunityAffiliations() {
		try ( Session session = driver.session() ) {

			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityAffiliations");
				List<Record> res = tx.run( qry ).list();
				List<String> texts = new ArrayList<>();
				Integer community = null;
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (community == null) community = next;
					if (community != next) {
						plot(fig, "Community affiliations", texts, community, affiliationStopwords);
						texts = new ArrayList<>();
					}
					texts.addAll(r.get("affiliations").asList(Values.ofString()));
					community = next;
				}
				plot(fig, "Community affiliations", texts, community, affiliationStopwords);
				return true;
			});
		}
	}

	@Test
	public void plotCommunityKeywords() {
		try ( Session session = driver.session() ) {			// 

			session.readTransaction( tx -> {
				String qry = queries.get("getAuthorCommunityKeywords");
				List<Record> res = tx.run( qry ).list();
				List<String> texts = new ArrayList<>();
				Integer community = null;
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (community == null) community = next;
					if (community != next) {
						plot(fig, "Community keywords", texts, community, textStopwords);
						texts = new ArrayList<>();
					}
					texts.addAll(r.get("terms").asList(Values.ofString()));
					community = next;
				}
				plot(fig, "Community keywords", texts, community, textStopwords);
				return true;
			});
		}
	}

	@Test
	public void plotCommunityContent() {
		try ( Session session = driver.session() ) {
			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityTitlesAbstracts");
				List<Record> res = tx.run( qry ).list();
				Corpus texts = Corpus.create();
				Integer community = null;
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (community == null) community = next;
					if (community != next) {
						plot(fig, "Community content", texts, community, textStopwords);
						texts = new ArrayList<>();
					}
					texts.addAll(r.get("abstracts").asList(Values.ofString()));
					texts.addAll(r.get("titles").asList(Values.ofString()));
					community = next;
				}

				plot(fig, "Community content", texts, community, textStopwords);
				return true;
			});

		}
	}
}
