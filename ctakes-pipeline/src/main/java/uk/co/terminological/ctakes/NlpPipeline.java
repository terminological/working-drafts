package uk.co.terminological.ctakes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.ctakes.assertion.medfacts.cleartk.ConditionalCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.GenericCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.HistoryCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.PolarityCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.SubjectCleartkAnalysisEngine;
import org.apache.ctakes.assertion.medfacts.cleartk.UncertaintyCleartkAnalysisEngine;
import org.apache.ctakes.contexttokenizer.ae.ContextDependentTokenizerAnnotator;
import org.apache.ctakes.core.ae.SentenceDetector;
import org.apache.ctakes.core.ae.SimpleSegmentAnnotator;
import org.apache.ctakes.core.ae.TokenizerAnnotatorPTB;
import org.apache.ctakes.dependency.parser.ae.ClearNLPDependencyParserAE;
import org.apache.ctakes.dictionary.lookup2.ae.DefaultJCasTermAnnotator;
import org.apache.ctakes.postagger.POSTagger;
import org.apache.ctakes.typesystem.type.constants.CONST;
import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.ctakes.ClassFinder.Visitor;
import uk.co.terminological.omop.Input;
import uk.co.terminological.omop.NoteNlp;


public class NlpPipeline {

	static Logger log = LoggerFactory.getLogger(NlpPipeline.class);
	AnalysisEngine aaeInst = null;
	static Path CACHE_DIR = Paths.get(System.getProperty("user.home"),"tmp/resources");

	/*public NlpPipeline(String umlsUser, String umlsPw) throws ResourceInitializationException, MalformedURLException {

		/*
If you plan to use the UMLS Resources, set/export env variables
export ctakes.umlsuser=[username], ctakes.umlspw=[password]
for example
export ctakes.umlsuser=MYUSERNAME, ctakes.umlspw=MYPASSWORD
or add the system properties to the java args
-Dctakes.umlsuser=[username] -Dctakes.umlspw=[password]
for example
-Dctakes.umlsuser=MYUSERNAME -Dctakes.umlspw=MYPASSWORD
		 

		System.setProperty("ctakes.umlsuser", umlsUser);
		System.setProperty("ctakes.umlspw", umlsPw);

		AnalysisEngineDescription aed;

		AggregateBuilder builder = new AggregateBuilder();
		// from ClinicalPipelineFactory.getTokenProcessingPipeline()
		builder.add( SimpleSegmentAnnotator.createAnnotatorDescription() );
		builder.add( SentenceDetector.createAnnotatorDescription() );
		builder.add( TokenizerAnnotatorPTB.createAnnotatorDescription() );
		//builder.add( AlternateLvgAnnotator.createAnnotatorDescription() );
		builder.add( AlternateLvgAnnotator.createAnnotatorDescription(CACHE_DIR) );
		builder.add( ContextDependentTokenizerAnnotator.createAnnotatorDescription() );
		builder.add( POSTagger.createAnnotatorDescription() );
		// from ClinicalPipelineFactory.getFastPipeline()
		builder.add( DefaultJCasTermAnnotator.createAnnotatorDescription() );
		builder.add( ClearNLPDependencyParserAE.createAnnotatorDescription() );
		builder.add( PolarityCleartkAnalysisEngine.createAnnotatorDescription() );
		builder.add( UncertaintyCleartkAnalysisEngine.createAnnotatorDescription() );
		builder.add( HistoryCleartkAnalysisEngine.createAnnotatorDescription() );
		builder.add( ConditionalCleartkAnalysisEngine.createAnnotatorDescription() );
		builder.add( GenericCleartkAnalysisEngine.createAnnotatorDescription() );
		builder.add( SubjectCleartkAnalysisEngine.createAnnotatorDescription() );
		aed = builder.createAggregateDescription();

		ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
		aaeInst = UIMAFramework.produceAnalysisEngine(aed, resMgr, null);
	}*/

	public NlpPipeline(String umlsUser, String umlsPw, String ctakesResourceLocation,boolean tidyUp) {
		try {
			NlpPipeline.addCtakesResources(ctakesResourceLocation, tidyUp);
			System.setProperty("ctakes.umlsuser", umlsUser);
			System.setProperty("ctakes.umlspw", umlsPw);
			AnalysisEngineDescription aed;

			AggregateBuilder builder = new AggregateBuilder();
			// from ClinicalPipelineFactory.getTokenProcessingPipeline()
			builder.add( SimpleSegmentAnnotator.createAnnotatorDescription() );
			builder.add( SentenceDetector.createAnnotatorDescription() );
			builder.add( TokenizerAnnotatorPTB.createAnnotatorDescription() );
			builder.add( AlternateLvgAnnotator.createAnnotatorDescription(ctakesResourceLocation) );
			builder.add( ContextDependentTokenizerAnnotator.createAnnotatorDescription() );
			builder.add( POSTagger.createAnnotatorDescription() );
			// from ClinicalPipelineFactory.getFastPipeline()
			builder.add( DefaultJCasTermAnnotator.createAnnotatorDescription() );
			builder.add( ClearNLPDependencyParserAE.createAnnotatorDescription() );
			builder.add( PolarityCleartkAnalysisEngine.createAnnotatorDescription() );
			builder.add( UncertaintyCleartkAnalysisEngine.createAnnotatorDescription() );
			builder.add( HistoryCleartkAnalysisEngine.createAnnotatorDescription() );
			builder.add( ConditionalCleartkAnalysisEngine.createAnnotatorDescription() );
			builder.add( GenericCleartkAnalysisEngine.createAnnotatorDescription() );
			builder.add( SubjectCleartkAnalysisEngine.createAnnotatorDescription() );

			//TODO: ask how to do this

			//String path = this.getClass().getClassLoader().getResource("desc/analysis_engine/RelationExtractorAggregate.xml").toExternalForm();
			//AnalysisEngineDescription relationExtractor = AnalysisEngineFactory.createEngineDescriptionFromPath(path);
			//https://github.com/apache/ctakes/blob/trunk/ctakes-relation-extractor/desc/analysis_engine/RelationExtractorAggregate.xml
			//builder.add(relationExtractor);

			aed = builder.createAggregateDescription();


			ResourceManager resMgr = UIMAFramework.newDefaultResourceManager();
			aaeInst = UIMAFramework.produceAnalysisEngine(aed, resMgr, null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public NlpPipeline(CtakesProperties p, boolean b) {
		this(p.umlsUser(), p.umlsPw(), p.ctakesHome().toString(), b);
	}

	public String runDocument(String sentence, Path xmi) throws UIMAException, IOException {

		long time = System.currentTimeMillis();
		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(sentence);
		aaeInst.process( jcas );
		log.debug("Parsed document in "+(System.currentTimeMillis()-time)+" ms");
		for( TOP t: JCasUtil.selectAll(jcas)) {
			System.out.println(t.getClass());
		}
		StringBuilder sb = new StringBuilder();
		for (IdentifiedAnnotation entity : JCasUtil.select(jcas,
				IdentifiedAnnotation.class)) {
			for( String cui : getCUIs( entity ) ) {
				sb.append( "cTAKESFast\t" );
				sb.append( cui );
				sb.append( "\t" );
				sb.append( entity.getBegin() );
				sb.append( "\t" );
				sb.append( entity.getEnd() );
				sb.append( "\t" );
				sb.append( entity.getCoveredText() );
				sb.append( "\t" );
				sb.append( "polarity:[" );
				sb.append( entity.getPolarity() );
				sb.append( "], uncertain=[" );
				sb.append( entity.getUncertainty() == CONST.NE_UNCERTAINTY_PRESENT );
				sb.append( "], subject=[" );
				sb.append( entity.getSubject() );
				sb.append( "], generic=[" );
				sb.append( entity.getGeneric() == CONST.NE_GENERIC_TRUE );
				sb.append( "], condition=[" );
				sb.append( entity.getConditional() == CONST.NE_CONDITIONAL_TRUE );
				sb.append( "], history=[" );
				sb.append( entity.getHistoryOf() == CONST.NE_HISTORY_OF_PRESENT );
				sb.append( "]\n");

			}

		}
		//Files.createDirectories(xmi.getParent());
		// CasIOUtil.writeXCas(jcas, xmi.toFile());
		//System.out.println(xmi.toAbsolutePath().toString());
		// CasIOUtil.writeXCas(jcas, aFile);
		//Gson gson = new GsonBuilder().setPrettyPrinting().create();
		//return gson.toJson(jcas);
		return sb.toString();
	}

	public List<NoteNlp> runNote(Input note, JcasOmopMapper mapper) throws UIMAException {

		long time = System.currentTimeMillis();
		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText( note.getNoteText() );
		aaeInst.process( jcas );
		log.debug("Parsed document in "+(System.currentTimeMillis()-time)+" ms");
		List<NoteNlp> out = new ArrayList<>();

		for (IdentifiedAnnotation entity : JCasUtil.select(jcas, IdentifiedAnnotation.class)) {
			mapper.mapNote(note, entity).forEach(x -> out.add(x));
		}

		return out;
	}

	static private Set<String> getCUIs(
			final IdentifiedAnnotation identifiedAnnotation) {
		Set<String> cuiSet = new HashSet<String>();

		final FSArray fsArray = identifiedAnnotation.getOntologyConceptArr();
		if (fsArray == null) {
			return cuiSet;
		}
		final FeatureStructure[] featureStructures = fsArray.toArray();
		for (FeatureStructure featureStructure : featureStructures) {
			if (featureStructure instanceof UmlsConcept) {
				final UmlsConcept umlsConcept = (UmlsConcept) featureStructure;
				final String cui = umlsConcept.getCui();
				final String tui = umlsConcept.getTui();
				final double score = umlsConcept.getScore();
				//final String sem = umlsConcept.getPreferredText();
				if (tui != null && !tui.isEmpty()) {
					cuiSet.add( cui + "\t" + score );// + "_" + tui + "_{" + sem + "}" );
				} else {
					cuiSet.add( cui + "\t" + score );
				}
			}
		}
		/*String ret = "";
			for( String cui : cuiSet ) {
				if( ret.isEmpty() ) {
					ret += cui;
				} else {
					ret += "|" + cui;					
				}
			}*/
		return cuiSet;
	}

	public static void addCtakesResources(String s, boolean tidyUp) throws Exception {
		Path cachePath = Paths.get(s);
		
		Files.createDirectories(cachePath);
		
		if (tidyUp) {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				log.info("Deleting ctakes resources from temporary CTAKES_HOME: "+s);
				try {
					Files.walk(cachePath.resolve("resources")).sorted(Comparator.reverseOrder())
					.peek(p -> log.debug("deleting "+p))
					.forEach(NlpPipeline::deleteIfPossible);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}));
		}
		
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		log.info("Expanding ctakes resources to temporary CTAKES_HOME: "+s);

		ClassFinder.findResources(new Visitor<String>() {
			@Override
			public boolean visit(String filename) {
				if (filename.startsWith("org/apache/ctakes")) {
					//System.out.println(filename);
					Path path = cachePath.resolve("resources").resolve(filename);
					if (!Files.exists(path)) {
						InputStream stream =  urlClassLoader.getResourceAsStream(filename);
						if (stream==null) { 
							log.error("could not open: "+filename+": skipping");
						} else {
							log.debug("copying ctakes related file to " + path);
							try {
								Files.createDirectories(path.getParent());
								OutputStream os = Files.newOutputStream(path);
								IOUtils.copy(stream, os);
							} catch (IOException e) {
								throw new RuntimeException("error copying " + path + " to " + cachePath.resolve(path) + ".", e);
							}
						}
					}
				}
				return true; // return false if you don't want to see any more classes
			}
		});

		Class<URLClassLoader> urlClass = URLClassLoader.class;
		Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
		method.setAccessible(true);
		method.invoke(urlClassLoader, new Object[]{cachePath.toUri().toURL()});
	}

	public static class Status {
		static final String PENDING="PENDING";
		static final String PROCESSING="PROCESSING";
		static final String RETRY="RETRY";
		static final String COMPLETE="COMPLETE";
		static final String CANCELLED="CANCELLED";
		static final String FAILED="FAILED";
	}


	static void deleteIfPossible(Path p) {
		try {
			Files.deleteIfExists(p);
		} catch (IOException e) {
			log.warn("could not delete "+p);
		}
	}







}
