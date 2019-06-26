package uk.co.terminological.simplechart.aesthetics;

public interface Cartesian {
	
	public static interface Z {
		public Double getZ();
	}
	
	public static interface DyDx {
		public Double getDyDx();
	}
	
	public static interface DzDx {
		public Double getDzDx();
	}
	
	public static interface DzDy {
		public Double getDzDy();
	}
	
	
	public static interface XY extends X,Y {}
	public static interface XYZ extends X,Y,Z {}
	
	public static interface XYwithDiff extends X,Y,DyDx {}
	public static interface XYZwithDiff extends X,Y,Z,DzDx,DzDy {}
}
