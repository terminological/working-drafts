package uk.co.terminological.charts;

public enum Geometry {

	LINE, //(1,new Dimension[] {X,Y,Z}),
	POINT, //(1),
	SHAPE,
	BAR,
	RECTANGLE,
	AREA,
	RIBBON,
	ERROR_BAR
	;
	// WHISKER,
	
	/*int dataPoints;
	Dimension[] validDimensions;
	Geometry(int dataPoints, Dimension[] validDimensions) {
	 this.dataPoints = dataPoints;
	 this.validDimensions = validDimensions;
	}*/
}
