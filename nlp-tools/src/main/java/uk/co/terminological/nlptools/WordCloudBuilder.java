package uk.co.terminological.nlptools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.bg.RectangleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.palette.ColorPalette;

import uk.co.terminological.datatypes.Tuple;
import uk.co.terminological.simplechart.ColourScheme;

public class WordCloudBuilder {

	private WordCloudBuilder() {}
	
	WordCloud wordCloud;
	List<WordFrequency> wordFrequencies = new ArrayList<>();
	Path output;
	ColorPalette pallette;
	Function<Term, Integer> statisticMapper = t -> t.countOccurrences();
	Corpus corpus;
	int maxNumber;
	Dimension dimension;
	
	public static WordCloudBuilder from(Corpus corpus, int maxNumber, int x, int y) {
		WordCloudBuilder out = new WordCloudBuilder();
		out.maxNumber = maxNumber;
		out.withColourScheme(ColourScheme.Greys);
		out.dimension = new Dimension(x, y);
		out.wordCloud = new WordCloud(out.dimension, CollisionMode.PIXEL_PERFECT);
		out.wordCloud.setPadding(2);
		out.wordCloud.setKumoFont(new KumoFont(new Font("Lucida Sans", Font.PLAIN, 14)));
		out.wordCloud.setBackground(new CircleBackground(Math.min(x, y)/2));
		out.wordCloud.setFontScalar(new SqrtFontScalar(10, 50));
		return out;
	}
	
	public WordCloudBuilder withStatistic(Function<Term,Integer> mapper) {
		this.statisticMapper = mapper;
		return this;
	}
 	
	public WordCloudBuilder withOutputPath(Path path) {
		this.output = path;
		return this;
	}
	
	public WordCloudBuilder withColourScheme(ColourScheme scheme) {
		List<Color> colors = scheme.values(8).stream().map(c -> c.toAwt()).collect(Collectors.toList());
		wordCloud.setColorPalette(new ColorPalette(colors));
		return this;
	}
	
	public WordCloudBuilder rectangular() {
		wordCloud.setBackground(new RectangleBackground(dimension));
		return this;
	}
	
	public WordCloudBuilder circular() {
		wordCloud.setBackground(new CircleBackground( (int) Math.floor(Math.min(dimension.getHeight(),dimension.getWidth())/2) ));
		return this;
	}
	
	public void execute() {
		corpus.streamTerms().map(t -> Tuple.create(t, statisticMapper.apply(t)))
	       .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	       .limit(maxNumber)
	       .forEach((ti) -> {
	    	   wordFrequencies.add(new WordFrequency(ti.getKey().tag, ti.getValue()));
		});
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile(output.toString());
	}
	
}
