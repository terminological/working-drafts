package uk.co.terminological.bibliography.europepmc;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.record.Author;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.PrintRecord;
import uk.co.terminological.bibliography.record.RecordReference;

public class CoreResult extends LiteResult implements PrintRecord {

	//https://www.ebi.ac.uk/europepmc/webservices/rest/search?query=DOI:10.1073/pnas.0506580102&sort=CITED%20desc&format=json&resultType=core
	
	public CoreResult(JsonNode node) { super(node); }

	@Override
	public List<RecordReference> getOtherIdentifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Author> getAuthors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<String> getLicenses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<String> getAbstract() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<LocalDate> getDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<URI> getPdfUri() {
		// TODO Auto-generated method stub
		return null;
	}

	
}
