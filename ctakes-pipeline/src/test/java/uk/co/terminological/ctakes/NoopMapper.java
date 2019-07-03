package uk.co.terminological.ctakes;

import java.util.stream.Stream;

import org.apache.ctakes.typesystem.type.refsem.UmlsConcept;
import org.apache.ctakes.typesystem.type.textsem.IdentifiedAnnotation;

import uk.co.terminological.omop.CuiOmopMap;
import uk.co.terminological.omop.Database;

public class NoopMapper extends JcasOmopMapper {

	public NoopMapper(Database db, String version) {
		super(null, null);
	}
	
	public Stream<CuiOmopMap> mapConcept(IdentifiedAnnotation jcas) {
		if (jcas == null || jcas.getOntologyConceptArr() == null) return Stream.empty();
		return Stream.of(jcas.getOntologyConceptArr().toArray())
			.filter(fs -> fs instanceof UmlsConcept)
			.map(u -> new CuiOmopMap() {

				@Override
				public String getCui() {
					return ((UmlsConcept) u).getCui());
				}

				@Override
				public Integer getSourceConceptId() {
					return -1;
				}

				@Override
				public Integer getConceptId() {
					return -1;
				}
				
			});
			
	}

}
