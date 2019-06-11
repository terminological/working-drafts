package uk.co.terminological.ctakes;

import java.sql.JDBCType;

public interface Typed {

	public static interface Reader {
		public Class<?> getType();
	}
	
	public static interface JDBCWriter {
		public JDBCType getType();
	}
}
