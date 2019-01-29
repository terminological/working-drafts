package uk.co.terminological.bibliography;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;
import de.undercouch.citeproc.output.Bibliography;
import uk.co.terminological.bibliography.record.Author;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.Print;
import uk.co.terminological.bibliography.record.Record;
import uk.co.terminological.datatypes.FluentList;

public class CiteProcProvider extends FluentList<Record> implements ItemDataProvider  {
    
	@Override
    public CSLItemData retrieveItem(String id) {
		Record record = this.stream()
				.filter(r -> r.getIdentifier().orElse("").equals(id))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("CSL item not found"));
		return fromRecord(record);
    }
    
	public static CSLItemData fromRecord(Record record) {
		return fromRecord(record, record.getIdentifier().orElse(UUID.randomUUID().toString()));
	}
	
	public static CSLItemData fromRecord(Record record, String id) {
		CSLItemDataBuilder builder = new CSLItemDataBuilder()
	            .id(id)
	            .type(CSLType.ARTICLE_JOURNAL);
	         record.getTitle().ifPresent(t -> builder.title(t));
	         if (record.getFirstAuthor().isPresent()) {
	        	 Author a = record.getFirstAuthor().get();
	        	 builder.author(
	        		a.getFirstName().or(() -> a.getInitials()).orElse(""),
	        		a.getLastName()
	        	); 
	         } else if (record.getFirstAuthorLastName().isPresent()) {
	        	 if (record.getFirstAuthorFirstName().isPresent()) {
	        		 builder.author(
	        				 record.getFirstAuthorFirstName().get(), 
	        				 record.getFirstAuthorLastName().get());
	        	 }
	         }
	         
	         record.getDate().ifPresent(d -> builder.originalDate(
	        		 d.getYear(),
	        		 d.getMonthValue(),
	        		 d.getDayOfMonth()));
	         record.getJournal().ifPresent(j -> builder.containerTitle(j));
	         
	         buildIds(builder, record.getIdentifierType(), record.getIdentifier());
	         record.getOtherIdentifiers().forEach(rr -> buildIds(builder, rr.getIdentifierType(), rr.getIdentifier()));
	         
	         record.getAbstract().ifPresent(a -> builder.abstrct(a));
	         record.getPdfUri().ifPresent(p -> builder.URL(p.toString()));
	         
	         if (record instanceof Print) {
	        	 Print print = (Print) record;
	        	 
	        	 print.getIssue().ifPresent(i -> builder.issue(i));
	        	 print.getPage().ifPresent(p -> builder.page(p));
	        	 print.getVolume().ifPresent(v -> builder.volume(v));
	        	 print.getYear().ifPresent(y -> builder.originalDate(y.intValue()));
	         }
	         
	         return builder.build();
	}
	
	private static void buildIds(CSLItemDataBuilder builder, IdType idType, Optional<String> id) {
		if (idType.equals(IdType.DOI)) {
			id.ifPresent(doi -> builder.DOI(doi));
        } else if (idType.equals(IdType.PMCID)) {
        	id.ifPresent(doi -> builder.PMCID(doi));
        } else if (idType.equals(IdType.PMID)) {
        	id.ifPresent(doi -> builder.PMID(doi));
        }
	}
	
	public String[] getIds() {
        return this.stream().flatMap(r -> r.getIdentifier().stream())
        		.collect(Collectors.toList()).toArray(new String[] {});
    }
	
	public Bibliography orderedCitations(String style, Output format) throws IOException {
		CSL citeproc = new CSL(this, style);
		citeproc.setOutputFormat(format.toString());
		citeproc.registerCitationItems(getIds());
		return citeproc.makeBibliography();
	}
	
	public static String convert(String style, Output format, Record... record) throws IOException {
		return CSL.makeAdhocBibliography(style, format.toString(), 
				Stream.of(record).map(r -> fromRecord(r)).collect(Collectors.toList()).toArray(new CSLItemData[] {}))
				.makeString();
	}
	
	public static enum Output {
		html, text, asciidoc, fo, rtf
	}
}