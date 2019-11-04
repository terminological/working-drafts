package uk.co.terminological.bibliography.record;

import java.util.Optional;

public class Builder {

	public static CitationLink citationLink(CitationReference source, CitationReference target, Integer index) {
		return citationLink(source,target,Optional.ofNullable(index));
	}
	
	public static CitationLink citationLink(CitationReference source, CitationReference target, Optional<Integer> index) {
		return new CitationLink() {
			@Override
			public CitationReference getSource() {
				return source;
			}
			@Override
			public CitationReference getTarget() {
				return target;
			}
			@Override
			public Optional<Integer> getIndex() {
				return index;
			};
		};
	}
	
	public static CitationReference citationReference(
			Optional<RecordReference> identifier,
			Optional<String> title,
			Optional<Print> bibliographicId
			) {
		return new CitationReference() {

			@Override
			public Optional<RecordReference> getIdentifier() {
				return identifier;
			}

			@Override
			public Optional<String> getTitle() {
				return title;
			}

			@Override
			public Optional<Print> getBibliographicId() {
				return bibliographicId;
			}
			
		};
	}
	
	public static CitationReference citationReference(
			RecordReference identifier,
			String title,
			Print bibliographicId
			) {
		return citationReference(
				Optional.ofNullable(identifier),
				Optional.ofNullable(title),
				Optional.ofNullable(bibliographicId)
		);
	}
	
	public static RecordReference recordReference(IdType idType, String identifier) {
		return new RecordReference() {

			@Override
			public Optional<String> getIdentifier() {
				return Optional.ofNullable(identifier);
			}

			@Override
			public IdType getIdentifierType() {
				return idType;
			}
			
		};
	}
}
