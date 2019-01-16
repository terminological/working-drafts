package uk.co.terminological.pubmedclient.record;

import java.util.Optional;

public interface Citation {

	Optional<RecordReference> getIdentifier();
	Optional<String> getTitle();
	Optional<PrintRecord> getBibliographicId();
	
	public static Citation create(
			final RecordReference identifier,
			final String title,
			final PrintRecord bibliographicInfo) {
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
			public Optional<PrintRecord> getBibliographicId() {
				return Optional.ofNullable(bibliographicInfo);
			}
			
		};
	}
	
}
