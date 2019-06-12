package uk.co.terminological.omop;

import javax.annotation.Generated;
import java.util.*;
import uk.co.terminological.omop.CuiOmopMap;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface CuiOmopMapFluent extends CuiOmopMap {

	// POJO setters
	// ==============

	public void setCui(String value);

	public void setCode(String value);

	public void setConceptId(Integer value);

	
	// Fluent setters
	// ==============
	
	public CuiOmopMapFluent withCui(String value);
	
	public CuiOmopMapFluent withCode(String value);
	
	public CuiOmopMapFluent withConceptId(Integer value);
	
}
