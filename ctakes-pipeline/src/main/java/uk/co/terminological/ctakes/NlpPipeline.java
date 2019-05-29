package uk.co.terminological.ctakes;





import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
import org.apache.ctakes.lvg.ae.LvgAnnotator;
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
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;

public class NlpPipeline {

	AnalysisEngine aaeInst = null;
	static Path CACHE_DIR = Paths.get(System.getProperty("user.home"),"tmp/resources");

	public NlpPipeline(String umlsUser, String umlsPw) throws ResourceInitializationException, MalformedURLException {
		
		/*
If you plan to use the UMLS Resources, set/export env variables
export ctakes.umlsuser=[username], ctakes.umlspw=[password]
for example
export ctakes.umlsuser=MYUSERNAME, ctakes.umlspw=MYPASSWORD
or add the system properties to the java args
-Dctakes.umlsuser=[username] -Dctakes.umlspw=[password]
for example
-Dctakes.umlsuser=MYUSERNAME -Dctakes.umlspw=MYPASSWORD
		 */
		
		System.setProperty("ctakes.umlsuser", umlsUser);
		System.setProperty("ctakes.umlspw", umlsPw);
		
		AnalysisEngineDescription aed;
		
		AggregateBuilder builder = new AggregateBuilder();
		// from ClinicalPipelineFactory.getTokenProcessingPipeline()
		 builder.add( SimpleSegmentAnnotator.createAnnotatorDescription() );
	      builder.add( SentenceDetector.createAnnotatorDescription() );
	      builder.add( TokenizerAnnotatorPTB.createAnnotatorDescription() );
	      builder.add( LvgAnnotator.createAnnotatorDescription() );
	      //builder.add( AlternateLvgAnnotator.createAnnotatorDescription(CACHE_DIR) );
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
	}

	public String run(String sentence) throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText(sentence);
		aaeInst.process( jcas );

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
		return sb.toString();
	}

	public String runDocument(String doc) throws UIMAException {
		final JCas jcas = JCasFactory.createJCas();
		jcas.setDocumentText( doc );
		aaeInst.process( jcas );

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
		return sb.toString();
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



}
