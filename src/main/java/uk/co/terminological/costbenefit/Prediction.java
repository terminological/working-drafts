package uk.co.terminological.costbenefit;

public class Prediction {
	Prediction(Boolean actual, Double predicted) {
		this.actual = actual;
		this.predicted = predicted;
	}

	Boolean actual;
	Double predicted;

	public Double getPredicted() {return predicted;}
	public Boolean getActual() {return actual;}

}