package uk.co.terminological.ctakes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.ctakes.core.resource.FileLocator;
import org.apache.ctakes.lvg.ae.LvgAnnotator;
import org.apache.ctakes.lvg.resource.LvgCmdApiResourceImpl;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.datatypes.StreamExceptions;

public class AlternateLvgAnnotator {

	static Logger logger = LoggerFactory.getLogger(LvgAnnotator.class.getName());
	/**
	 * Copy to under /tmp/ (or some other specified directory) the files needed for EventAnnotatorTest and anyone else 
	 * using UIMAfit to create a pipeline.
	 * Localize to this method all hardcoded file names and subdirs related to copying files to under /tmp (or C:\tmp).
	 * @param absolutePath - Where to copy the LVG data/config subtree. Typically "/tmp/".
	 * @return The full path to the copy of lvg.properties file
	 */
	public static void copyLvgFiles(Path cachePath) {
		try {
			final String prefix = "org/apache/ctakes/lvg/";
			final String [] filesToCopy = { 
					"data/config/lvg.properties",
					"data/HSqlDb/lvg2008.backup",
					"data/HSqlDb/lvg2008.data",
					"data/HSqlDb/lvg2008.properties",
					"data/HSqlDb/lvg2008.script",
					"data/misc/conjunctionWord.data",
					"data/misc/nonInfoWords.data",
					"data/misc/removeS.data",
					"data/misc/stopWords.data",
					// "data/misc/symbolSynonyms.data", <-appears to be missing
					"data/rules/dm.rul",
					"data/rules/im.rul",
					"data/rules/plural.rul",
					"data/rules/verbinfl.rul",
					"data/rules/exceptionD.data",
					"data/rules/exceptionI.data",
					"data/rules/ruleD.data",
					"data/rules/ruleI.data",
					"data/rules/trieD.data",
					"data/rules/trieI.data",
					"data/Unicode/diacriticMap.data",
					"data/Unicode/ligatureMap.data",
					"data/Unicode/nonStripMap.data",
					"data/Unicode/synonymMap.data",
					"data/Unicode/symbolMap.data",
					"data/Unicode/unicodeMap.data",

			};

			for (String tmp:filesToCopy) {
				String path = prefix+tmp;
				InputStream stream =  LvgAnnotator.class.getClassLoader().getResourceAsStream(path);
				if (stream==null) throw new IOException("could not open: "+path);

				logger.info("copying lvg-related file to " + cachePath.resolve(path));

				Files.createDirectories(cachePath.resolve(path).getParent());
				OutputStream os = Files.newOutputStream(cachePath.resolve(path));

				try {
					IOUtils.copy(stream, os);
				} catch (IOException e) {

					
					throw new IOException("error copying " + path + " to " + cachePath.resolve(path) + ".", e);
				}

			}

		} catch (IOException ioe) {
			
			try {
				Files.walk(cachePath).sorted(Comparator.reverseOrder())
				.peek(p -> logger.info("deleting "+p))
				.forEach(AlternateLvgAnnotator::deleteIfPossible);
			} catch (Exception e) {}
			
			throw new RuntimeException("fatal IO error occurred copying files to cache: ",ioe);
			
		}

	}

	static void deleteIfPossible(Path p) {
		try {
			Files.deleteIfExists(p);
		} catch (IOException e) {
			logger.warn("could not delete "+p);
		}
	}

	public static AnalysisEngineDescription createAnnotatorDescription(Path cacheDir) throws ResourceInitializationException, MalformedURLException {

		// Here if a pipeline is run from source, for example in Eclipse using a Run Configuration for project ctakes-clinical-pipeline,  
		// the cwd might be, for example, C:\workspaces\cTAKES\ctakes\ctakes-clinical-pipeline
		// Therefore we can no longer let LvgCmdApiResourceImpl use the current working directory to look for 
		// the lvg properties file or the lvg resources (plural.rul etc.)
		// Instead we use getResource to find the URL for the lvg.properties file. 

		final String lvgProperties = "org/apache/ctakes/lvg/data/config/lvg.properties";
		if (!Files.exists(cacheDir.resolve(lvgProperties))) { 
			java.net.URL url = LvgAnnotator.class.getClassLoader().getResource(lvgProperties);
			if (url==null) {
				throw new RuntimeException("no cached lvg.properties and none found in jar files");
			}
			//copy the contents of the jar file to the cache directory
			logger.info("copying files and directories to under " + cacheDir);
			copyLvgFiles(cacheDir);

		}

		return AnalysisEngineFactory.createEngineDescription(LvgAnnotator.class,
				LvgAnnotator.PARAM_USE_CMD_CACHE,
				false,
				LvgAnnotator.PARAM_USE_LEMMA_CACHE,
				false,
				LvgAnnotator.PARAM_USE_SEGMENTS,
				false,
				LvgAnnotator.PARAM_LEMMA_CACHE_FREQUENCY_CUTOFF,
				20,
				LvgAnnotator.PARAM_LEMMA_FREQ_CUTOFF,
				20,
				LvgAnnotator.PARAM_POST_LEMMAS,
				false,
				LvgAnnotator.PARAM_LVGCMDAPI_RESRC_KEY,
				ExternalResourceFactory.createExternalResourceDescription(
						LvgCmdApiResourceImpl.class, cacheDir.resolve(lvgProperties).toUri().toURL()));
	}

	static final String CTAKES_HOME = "CTAKES_HOME";
	
	public static AnalysisEngineDescription createAnnotatorDescription() throws ResourceInitializationException, MalformedURLException {

		final String relativePath = "org/apache/ctakes/lvg/data/config/lvg.properties";
		String lvgProperties = null;
	final String cTakesHome = System.getenv( CTAKES_HOME );
	logger.info("Looking for lvgProperties at {}, {}",cTakesHome,relativePath);
    if ( cTakesHome != null && !cTakesHome.isEmpty() ) {
    	
       File file = new File( cTakesHome, relativePath );
       if ( file.exists() ) {
          lvgProperties = createDiscoveredPath( relativePath, file, "under $CTAKES_HOME" );
       }
       // in an ide the resources/ dir may not be in classpath
       file = new File( cTakesHome, "resources/" + relativePath );
       if ( file.exists() ) {
    	  lvgProperties =  createDiscoveredPath( relativePath, file, "under $CTAKES_HOME resources" );
       }
    }
    
    
    
    try {
		return AnalysisEngineFactory.createEngineDescription(LvgAnnotator.class,
				LvgAnnotator.PARAM_USE_CMD_CACHE,
				false,
				LvgAnnotator.PARAM_USE_LEMMA_CACHE,
				false,
				LvgAnnotator.PARAM_USE_SEGMENTS,
				false,
				LvgAnnotator.PARAM_LEMMA_CACHE_FREQUENCY_CUTOFF,
				20,
				LvgAnnotator.PARAM_LEMMA_FREQ_CUTOFF,
				20,
				LvgAnnotator.PARAM_POST_LEMMAS,
				false,
				LvgAnnotator.PARAM_LVGCMDAPI_RESRC_KEY,
				ExternalResourceFactory.createExternalResourceDescription(
						LvgCmdApiResourceImpl.class, new File(lvgProperties).toURI().toURL()));
	} catch (NullPointerException e) {
		throw new ResourceInitializationException(e);
	}
    
	}
	
	static private String createDiscoveredPath( final String relativePath, final File file, final String locationText ) {
	      try {
	         logger.debug( relativePath + " discovered " + locationText + " as: " + file.getCanonicalPath() );
	         return file.getCanonicalPath();
	      } catch ( IOException ioE ) {
	         logger.debug( relativePath + " discovered " + locationText + " as: " + file.getPath() );
	         return file.getPath();
	      }
	   }
}
