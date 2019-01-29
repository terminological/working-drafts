package uk.co.terminological.bibliography.record;

import java.util.Optional;

public interface Citation {

	Optional<RecordReference> getIdentifier();
	Optional<String> getTitle();
	Optional<Print> getBibliographicId();
	
	public static Citation create(
			final RecordReference identifier,
			final String title,
			final Print bibliographicInfo) {
		return new Citation() {

			@Override
			public Optional<RecordReference> getIdentifier() {
				return Optional.ofNullable(identifier);
			}

			@Override
			public Optional<String> getTitle() {
				return Optional.ofNullable(title);
			}

			@Override
			public Optional<Print> getBibliographicId() {
				return Optional.ofNullable(bibliographicInfo);
			}
			
		};
	}
	
}
