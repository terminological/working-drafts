package uk.co.terminological.simplechart;

import java.awt.Color;
import java.awt.Font;
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
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.Chart.Dimension;

public class WordcloudWriter extends Writer {

	List<String> text;
	List<String> stopWords;
	ColorPalette pallette;
	
	public WordcloudWriter(Chart chart) {
		super(chart);
	}
	
	@Override
	protected void process() throws IOException, TemplateException {
		
		File f = getChart().getFile("png");
		final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
		frequencyAnalyzer.setWordFrequenciesToReturn(300);
		frequencyAnalyzer.setMinWordLength(4);
		frequencyAnalyzer.setStopWords(stopWords);
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(text);
		final java.awt.Dimension dimension = new java.awt.Dimension(600, 600);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
		wordCloud.setPadding(2);
		wordCloud.setKumoFont(new KumoFont(new Font("Lucida Sans", Font.PLAIN, 14)));
		wordCloud.setBackground(new CircleBackground(300));
		
		wordCloud.setColorPalette(pallette);
		wordCloud.setFontScalar(new SqrtFontScalar(10, 50));
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile(f.getAbsolutePath());
		
		Chart.log.info("Writing word cloud to: "+f.getAbsolutePath());
		
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String extractData() {
		extractData(this.getChart().getSeries().get(0));
		if (this.getChart().getSeries().size() > 1) extractStopwords((Series<String>) this.getChart().getSeries().get(1));
		return "";
	}

	protected <Y> String extractData(Series<Y> series) {
		List<Color> colors = series.getScheme().values(8).stream().map(c -> c.toAwt()).collect(Collectors.toList());
		pallette = new ColorPalette(colors);
		Function<Y, Object> xGenerator = series.functionFor(Dimension.TEXT);
		text = new ArrayList<>();
		series.getData().stream().map(xGenerator).map(o -> o.toString()).forEach(s-> text.add(s));
		return "";
	}
	
	protected String extractStopwords(Series<String> series) {
		List<Color> colors = series.getScheme().values(8).stream().map(c -> c.toAwt()).collect(Collectors.toList());
		pallette = new ColorPalette(colors);
		Function<String, Object> xGenerator = series.functionFor(Dimension.TEXT);
		stopWords = new ArrayList<>();
		series.getData().stream().map(xGenerator).map(o -> o.toString()).forEach(s-> stopWords.add(s));
		return "";
	}
}
