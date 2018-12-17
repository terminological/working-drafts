package uk.co.terminological.simplechart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

import freemarker.template.TemplateException;
import uk.co.terminological.simplechart.Chart.Dimension;

public class WordcloudWriter extends Writer {

	@Override
	protected void process() throws IOException, TemplateException {
		
		File f = getChart().getFile("png");
		final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
		final List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(Collections.singletonList(getData()));
		final Dimension dimension = new Dimension(600, 600);
		final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
		wordCloud.setPadding(2);
		wordCloud.setBackground(new CircleBackground(300));
		wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
		wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
		wordCloud.build(wordFrequencies);
		wordCloud.writeToFile("kumo-core/output/datarank_wordcloud_circle_sqrt_font.png");
		
		PrintWriter out = new PrintWriter(new FileWriter(f));
		getTemplate().process(getRoot(), out);
		out.close();
		Chart.log.info("Writing html to: "+f.getAbsolutePath());
		
	}

	@Override
	protected String extractData() {
		return extractData(this.getChart().getSeries().get(0));
	}

	protected <Y> String extractData(Series<Y> series) {
		Function<Y, Object> xGenerator = series.functionFor(Dimension.TEXT);
		return series.getData().stream().map(xGenerator).map(o -> o.toString()).collect(Collectors.joining("\n"));		
	}
	
}
