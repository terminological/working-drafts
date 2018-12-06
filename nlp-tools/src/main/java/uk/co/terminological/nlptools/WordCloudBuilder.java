package uk.co.terminological.nlptools;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.kennycason.kumo.*;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;

public class WordCloudBuilder {

	private WordCloudBuilder() {}
	
	WordCloud wordCloud;
	List<WordFrequency> wordFrequencies = new ArrayList<>();
	Path output;
	
	public static WordCloudBuilder from(Corpus corpus, int maxNumber) {
		WordCloudBuilder out = new WordCloudBuilder();
		corpus.getTermCounts().entrySet().stream()
	       .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	       .limit(maxNumber)
	       .forEach((ti) -> {
			out.wordFrequencies.add(new WordFrequency(ti.getKey().tag, ti.getValue()));
		});
		out.rectangular(600, 600);
		out.wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
		out.wordCloud.setFontScalar(new LinearFontScalar(10, 40));
		return out;
	}
	
	public WordCloudBuilder withOutputPath(Path path) {
		this.output = path;
		return this;
	}
	
	public WordCloudBuilder rectangular(int x, int y) {
		Dimension dimension = new Dimension(x, y);
		wordCloud = new WordCloud(dimension, CollisionMode.RECTANGLE);
		wordCloud.setPadding(0);
		wordCloud.setBackground(new RectangleBackground(dimension));
		return this;
	}
	
	public void execute() {
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile(output.toString());
	}
	
}
