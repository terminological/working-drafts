package uk.co.terminological.bibliography;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import de.undercouch.citeproc.ItemDataProvider;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLItemDataBuilder;
import de.undercouch.citeproc.csl.CSLType;
import uk.co.terminological.bibliography.record.IdType;
import uk.co.terminological.bibliography.record.PrintRecord;
import uk.co.terminological.bibliography.record.Record;

public class CiteProcProvider extends ArrayList<Record> implements ItemDataProvider  {
    
	@Override
    public CSLItemData retrieveItem(String id) {
		Record record = this.stream()
				.filter(r -> r.getIdentifier().orElse("").equals(id))
				.findFirst()
				.orElseThrow(() -> new RuntimeException("CSL item not found"));
		return fromRecord(record);
    }
    
	public static CSLItemData fromRecord(Record record) {
		CSLItemDataBuilder builder = new CSLItemDataBuilder()
	            .id(record.getIdentifier().get())
	            .type(CSLType.ARTICLE_JOURNAL);
	         record.getTitle().ifPresent(t -> builder.title(t));
	         record.getAuthors().findFirst().ifPresent(a -> {
	        	builder.author(
	        		a.getFirstName().or(() -> a.getInitials()).orElse(""),
	        		a.getLastName()
	        	); 
	         });
	         record.getDate().ifPresent(d -> builder.originalDate(
	        		 d.getYear(),
	        		 d.getMonthValue(),
	        		 d.getDayOfMonth()));
	         record.getJournal().ifPresent(j -> builder.containerTitle(j));
	         
	         buildIds(builder, record.getIdentifierType(), record.getIdentifier());
	         record.getOtherIdentifiers().forEach(rr -> buildIds(builder, rr.getIdentifierType(), rr.getIdentifier()));
	         
	         record.getAbstract().ifPresent(a -> builder.abstrct(a));
	         record.getPdfUri().ifPresent(p -> builder.URL(p.toString()));
	         
	         if (record instanceof PrintRecord) {
	        	 PrintRecord print = (PrintRecord) record;
	        	 
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
}