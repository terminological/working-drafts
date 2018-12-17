package uk.co.terminological.simplechart;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.Chart.Dimension;

public class WordcloudWriter extends Writer {

	List<String> text = new ArrayList<>();
	ColorPalette pallette;
	
	public WordcloudWriter(Chart chart) {
		super(chart);
	}
	
	@Override
	protected void process() throws IOException, TemplateException {
		
		File f = getChart().getFile("png");
		final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(text);
		final java.awt.Dimension dimension = new java.awt.Dimension(600, 600);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new CircleBackground(300));
		
		wordCloud.setColorPalette(pallette);
		wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile(f.getAbsolutePath());
		
		Chart.log.info("Writing word cloud to: "+f.getAbsolutePath());
		
	}

	@Override
	protected String extractData() {
		return extractData(this.getChart().getSeries().get(0));
	}

	protected <Y> String extractData(Series<Y> series) {
		List<Color> colors = series.getScheme().values(8).stream().map(c -> c.toAwt()).collect(Collectors.toList());
		pallette = new ColorPalette(colors);
		Function<Y, Object> xGenerator = series.functionFor(Dimension.TEXT);
		series.getData().stream().map(xGenerator).map(o -> o.toString()).forEach(s-> text.add(s));
		return "";
	}
	
}
