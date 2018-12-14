package uk.co.terminological.simplechart;

public class TestColourScheme {
	public static void main(String[] args) {
		System.out.println(
			ColourScheme.Accent.getGnuplotPalette(2)+"\n====\n"
				);
		
		System.out.println(
				ColourScheme.Accent.getGnuplotPalette(8)+"\n====\n"
					);
		
		System.out.println(
				ColourScheme.Accent.getGnuplotPalette(20)+"\n====\n"
					);
	}
}
