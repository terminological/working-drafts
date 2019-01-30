package uk.co.terminological.literaturereview;

import static uk.co.terminological.simplechart.Chart.Dimension.ID;
import static uk.co.terminological.simplechart.Chart.Dimension.LABEL;
import static uk.co.terminological.simplechart.Chart.Dimension.STRENGTH;
import static uk.co.terminological.simplechart.Chart.Dimension.TEXT;
import static uk.co.terminological.simplechart.Chart.Dimension.X;
import static uk.co.terminological.simplechart.Chart.Dimension.Y;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Values;
import org.yaml.snakeyaml.Yaml;

import uk.co.terminological.bibliography.CiteProcProvider;
import uk.co.terminological.bibliography.CiteProcProvider.Output;
import uk.co.terminological.datatypes.EavMap;
import uk.co.terminological.datatypes.StreamExceptions;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.nlptools.Corpus;
import uk.co.terminological.nlptools.Document;
import uk.co.terminological.nlptools.Filters;
import uk.co.terminological.nlptools.TopicModelBuilder;
import uk.co.terminological.nlptools.WordCloudBuilder;
import uk.co.terminological.simplechart.ChartType;
import uk.co.terminological.simplechart.ColourScheme;
import uk.co.terminological.simplechart.Figure;
import uk.co.terminological.nlptools.Normaliser;
import uk.co.terminological.nlptools.Tokeniser;

/*
 * TODO:
 * What do I need to do to get this over the line.
 * 
 * Methods
 * 
 * Sample selection
 * Stats about the collection process
 * 
 * 
 * Results
 * 
 * Temporal composition; aticle network analysis islands / sizes - horizontal staked bar graph.
 * Top N articles by page rank 
 * Top N journals by aggregated page rank
 * 
 * Co-citation authors network
 * 
 * 
 * Communities
 * 
 * Top Y topics based on title / abstract
 * 
 * 
 */

public class LitReviewAnalysis {

	/*public static void main(String[] args) throws Exception {
		LitReviewAnalysis tmp = new LitReviewAnalysis();
		tmp.setUpBeforeClass();
		tmp.articlesCSL();
	}*/

	Driver driver;
	Map<String, Object> obj;
	Figure fig;
	List<String> affiliationStopwords;
	List<String> textStopwords;
	Map<String,String> queries;
	// List<Integer> communityIndex = new ArrayList<>();
	Path outDir;

	/*private String getCommunityName(int community) {
		int i = getCommunityIndex(community);
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(i % 26, i % 26 + 1);
	}

	private int getCommunityIndex(int community) {
		Integer i = communityIndex.indexOf(community);
		if (i == -1) {
			i = communityIndex.size();
			communityIndex.add(community);
		}
		return i;
	}*/
	
	/*void plot(Figure fig, String name, List<String> list, Integer community, List<String> stopwords) {
		try {
			Integer i = getCommunity(community);
			fig.withNewChart(name+" "+i, ChartType.WORDCLOUD)
			.withSeries(list)
			.bind(TEXT, t -> t)
			.withColourScheme(ColourScheme.sequential(i))
			.done()	
			.withSeries(stopwords).bind(TEXT, t -> t).done()
			.render();
		} catch (Exception e) {throw new RuntimeException(e);}
	};*/

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
		outDir = Paths.get(System.getProperty("user.home")+"/Dropbox/litReview/output");
		fig = Figure.outputTo(outDir.toFile());

		affiliationStopwords = Arrays.asList(((Map<String,String>) obj.get("config")).get("stopwordsForAffiliation").split("\n"));
		textStopwords = Arrays.asList(((Map<String,String>) obj.get("config")).get("stopwordsForText").split("\n"));
		queries = (Map<String,String> ) obj.get("analyse");
	}

	@After
	public void tearDownAfterClass() throws Exception {
		driver.close();
	}

	@Test
	public void articlesCSL() {
		try ( Session session = driver.session() ) {

			//Plot by age
			session.readTransaction( tx -> {

				String qry = queries.get("listArticlesByPagerank");
				List<Record> res = tx.run( qry ).list();
				CiteProcProvider out = new CiteProcProvider();

				res.forEach(r -> {
					uk.co.terminological.bibliography.record.Record rec = 
							Shim.recordFacade(r.get("node").asNode());
					out.add(rec);

				});

				try {
					System.out.println(out.orderedCitations("ieee", Output.text).makeString());
				} catch (IOException e) {
					e.printStackTrace();
				}

				return true;
			});
		}
	}

	@Test
	public void writeToCsv() {
		try ( Session session = driver.session() ) {
			queries.forEach((name,qry) -> {
				if (name.startsWith("get")) {
					System.out.println("Executing query: "+name);
					session.readTransaction( tx -> {

						StatementResult qryR = tx.run( qry );
						List<Record> res = qryR.list();

						Path path = outDir.resolve(name+".tsv");
						try {

							OutputStream writer = Files.newOutputStream(path);
							writer.write(qryR.keys().stream().collect(Collectors.joining("\t")).getBytes());
							for (Record r:res) {
								writer.write(("\n"+
										r.values().stream().map(v -> v.toString()).collect(Collectors.joining("\t"))).getBytes()
										);
							}
							writer.close();

						} catch (Exception e) {
							e.printStackTrace(System.out);
						}

						return true;
					});
				}
			});
		}

	}

	@Test
	@Deprecated
	// Probably better to export and do in R
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
	public void plotArticlesByJournal() {
		try ( Session session = driver.session() ) {

			//Plot by age
			session.readTransaction( tx -> {

				String qry = queries.get("getArticlesByJournal");
				List<Record> res = tx.run( qry ).list();

				try {
					fig.withNewChart("Articles by journal", ChartType.STACKEDYBAR)
					.withSeries(res)
					.bind(LABEL, t -> t.get("journal").asString())
					.bind(Y, t -> t.get("articles").asNumber().intValue())
					.done()
					.config()
					.withYLabel("articles")
					.done().render();
				} catch (Exception e) {throw new RuntimeException(e);}

				return true;
			});
		}
	}

	@Test
	@Deprecated
	//Export and do in R
	public void plotCommunityStats() {
		try ( Session session = driver.session() ) {

			//Plot by age
			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityStats");
				List<Record> res = tx.run( qry ).list();

				EavMap<String,String,Double> tmp = new EavMap<>();

				for (String key:Arrays.asList("authors","articles","avgPagerank")) {

					Double[] sum = {0D};

					res.stream().forEach(r -> {
						tmp.add(
								r.get("community").asNumber().toString(), key, r.get(key).asNumber().doubleValue());
						sum[0] += r.get(key).asNumber().doubleValue();
					});

					//Convert to percentage
					tmp.getEntitySet().forEach(e -> {
						tmp.put(e, key, tmp.get(e, key)/sum[0]*100);
					});

				}

				try {
					fig.withNewChart("Community stats", ChartType.MULTISTACKEDYBAR)
					.withSeries(tmp.stream())
					.bind(LABEL, t -> t.getFirst())
					.bind(X, t -> t.getSecond())
					.bind(Y, t -> t.getThird())
					.done()
					.config()
					.withYLabel("articles")
					.withXLabel("facet")
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

	private Corpus affiliationCorpus() {return 
			Corpus.create()
			.withStopwordFilter(affiliationStopwords)
			.withTokenFilter(Filters.number())
			.withTokenFilter(Filters.shorterThan(4));
	}
	
	private Corpus textCorpus() {return 
			Corpus.create()
			.withStopwordFilter(textStopwords)
			.withTokenFilter(Filters.number())
			.withTokenFilter(Filters.shorterThan(4));
	}
	
	private void plotCommunityWordcloud(Corpus texts, int community, String type) {
		WordCloudBuilder.from(texts, 200, 600, 600)
		.withSelector(c -> c.getTermsByTotalEntropy().map(wt -> wt.scale(100)))
		.withColourScheme(ColourScheme.sequential(community))
		.execute(outDir.resolve("Community"+type+community+".png"));
	}
	
	/*@Test
	public void plotCommunityAffiliations() {
		try ( Session session = driver.session() ) {

			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityAffiliations");
				List<Record> res = tx.run( qry ).list();
				Corpus texts = affiliationCorpus();
				
				Integer community = null;
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (community == null) community = next;
					if (community != next) {
						plotCommunityWordcloud(texts,community,"Affiliations");
						texts = affiliationCorpus();
					}
					r.get("affiliations").asList(Values.ofString()).forEach(texts::addDocument);
					community = next;
				}
				plotCommunityWordcloud(texts,community,"Affiliations");
				return true;
			});
		}
	}*/
	
	@Test
	public void plotCommunityAffiliations() {
		try ( Session session = driver.session() ) {

			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityAffiliations");
				List<Record> res = tx.run( qry ).list();
				Corpus texts = affiliationCorpus();
				List<Integer> communities = new ArrayList<>();
 				
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (!communities.contains(next)) communities.add(next);
					r.get("affiliations").asList(Values.ofString()).forEach(aff -> { 
						Document doc = texts.addDocument(aff);
						doc.addMetadata("community", next);
					});
				}
				
				for (Integer community: communities) {
					int id = communities.indexOf(community);
					WordCloudBuilder.from(texts, 200, 600, 600).circular()
						.withColourScheme(ColourScheme.sequential(id).darker(0.25F))
						.withSelector(c -> c.getTermsByMutualInformation(d -> community.equals(d.getMetadata("community").orElse(null)))
						.map(wt -> wt.scale(10000)))
						.execute(outDir.resolve("CommunityAffiliations"+id+".png"));
				}
				//plotCommunityWordcloud(texts,community,"Affiliations");
				return true;
			});
		}
	}

	@Test
	public void plotCommunityContent() {
		try ( Session session = driver.session() ) {

			session.readTransaction( tx -> {
				String qry = queries.get("getAuthorCommunityTitlesAbstracts2");
				List<Record> res = tx.run( qry ).list();
				Corpus texts = textCorpus();
				List<Integer> communities = new ArrayList<>();
 				
				for( Record r : res) {
					Integer next = r.get("community").asInt();
					if (!communities.contains(next)) communities.add(next);
					String nodeId = r.get("nodeId").asNumber().toString();
					String title = r.get("title").asString();
					String abstrct = r.get("abstract").asString();
					Document doc = texts.addDocument(nodeId, title+(abstrct != null ? "\n"+abstrct : ""));
					doc.addMetadata("community",next);
				}
				
				for (Integer community: communities) {
					int id = communities.indexOf(community);
					WordCloudBuilder.from(texts, 200, 600, 600).circular()
						.withColourScheme(ColourScheme.sequential(id).darker(0.25F))
						.withSelector(c -> c.getTermsByMutualInformation(d -> community.equals(d.getMetadata("community").orElse(null)))
						.map(wt -> wt.scale(10000)))
						.execute(outDir.resolve("CommunityContent"+id+".png"));
				}
				
				return true;
			});
		}
	}

	@Test
	public void plotTopicContent() {
		try ( Session session = driver.session() ) {
			session.readTransaction( tx -> {

				String qry = queries.get("getAuthorCommunityTitlesAbstracts2");
				List<Record> res = tx.run( qry ).list();
				Corpus texts = textCorpus();
				for( Record r : res) {

					Integer i = r.get("community").asInt();
					String nodeId = r.get("nodeId").asNumber().toString();
					String title = r.get("title").asString();
					String abstrct = r.get("abstract").asString();
					Document doc = texts.addDocument(nodeId, title+(abstrct != null ? "\n"+abstrct : ""));
					doc.addMetadata("community",i);
					//doc.addMetadata("qtr",r.get("qtr").asFloat()); //TODO: needs a think. sometimes null.

				}

				
				
				// texts.getCollocations(5).stream().forEach(System.out::println);

				TopicModelBuilder.Result result = TopicModelBuilder.create(texts).withTopics(10).execute(0.1,0.1);
				result.printTopics(10);
				
				EavMap<String,String,Double> topicCommunityCorrelation = new EavMap<>();
				
				result.getTopicsForDocuments().forEach(top -> {

					int id = top.getTopicId();

					WordCloudBuilder.from(texts, 200, 600, 600).circular()
						.withColourScheme(ColourScheme.sequential(id).darker(0.25F))
						.withSelector(c -> 
							c.streamTopics()
							.filter(t -> t.getTopicId() == id)
							.flatMap(t -> t.streamTerms())
							.map(wt -> wt.scale(10000)))
						.execute(outDir.resolve("TopicContent"+id+".png"));
				
					top.streamDocuments().forEach(wd -> {
						Optional<Integer> commId = wd.getTarget().getMetadata("community").map(o -> (int) o);
						commId.ifPresent( cid -> {
							Double score = topicCommunityCorrelation.get("Topic "+id, "Community "+cid);
							if (score == null) { score = 0D; }
							score += wd.getWeight();
							topicCommunityCorrelation.put("Topic "+id, "Community "+cid, score);
							topicCommunityCorrelation.put("Community "+cid, "Topic "+id, score);
						});
					});
				});
				
				try {
				fig.withNewChart("Topic community relationships", ChartType.CHORD)
				.withSeries(topicCommunityCorrelation.stream())
				.bind(ID, t -> t.getFirst(), "source")
				.bind(STRENGTH, t -> t.getThird())
				.bind(ID, t -> t.getSecond(), "target")
				.withColourScheme(ColourScheme.Set2)
				.done()
				.render();
				} catch (Exception e) {throw new RuntimeException(e);}
				//TODO: find meaningful export format for topic and corpus data e.g. some sort of CSV

				return true;
			});

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
