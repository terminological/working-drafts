package uk.co.terminological.simplechart;

public enum OutputTarget {
	SCREEN("png"),
	SINGLE_COLUMN_FIGURE("svg"),
	DOUBLE_COLUMN_FIGURE("svg")
	;
	
	String filetype;
	OutputTarget(String filetype) {
		this.filetype = filetype;
	}
	public String getFileType() {
		return filetype;
	}
}