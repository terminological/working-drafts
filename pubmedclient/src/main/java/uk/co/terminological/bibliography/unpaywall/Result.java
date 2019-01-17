package uk.co.terminological.bibliography.unpaywall;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.ExtensibleJson;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.Record;
import uk.co.terminological.bibliography.record.RecordReference;

public class Result extends ExtensibleJson implements Record {
	public Result(JsonNode node) {super(node);}
	
	public Optional<String> getIdentifier() {return this.asString("doi");}
	public Optional<String> getTitle() {return this.streamPath("title").findFirst().map(
			n -> n.asString()).or(() -> getJournal());}
	public String getFirstAuthorName() {
		return this.getAuthors().findFirst().map(o -> o.getLastName()).orElse("n/a");
	}
	public Optional<String> getJournal() {return this.asString("journal_name");}
	public Optional<Long> getYear() {return getDate().map(d -> (long) d.getYear());}
	
	public Optional<LocalDate> getDate() {
		return this.asString("published_date").map(LocalDate::parse);
	}
	
	public Stream<String> getLicenses() {
		return this.streamPath("oa_locations","URL").map(o -> o.asString());}
	
	//public Optional<Double> getScore() {return this.asDouble("score");}
	
	public Stream<Author> getAuthors() {return this.streamPath(Author.class, "z_authors");}
	public Optional<String> getAbstract() {return this.asString("abstract");}
	
	public Optional<URI> getTextMiningUri() {
		return Optional.empty();
	}
	
	public Optional<URI> getPdfUri() {
		return this.streamPath("best_oa_location","url_for_pdf").findFirst()
				.or(() -> this.streamPath("oa_locations","url_for_pdf").findFirst())
				.flatMap(n -> {
					try {
						return Optional.of(new URI(n.asString()));
					} catch (URISyntaxException e) {
						return Optional.empty();
					}
				});
	}

	@Override
	public IdType getIdentifierType() {
		return IdType.DOI;
	}

	@Override
	public Set<RecordReference> getOtherIdentifiers() {
		return Collections.emptySet();
	}
	
}