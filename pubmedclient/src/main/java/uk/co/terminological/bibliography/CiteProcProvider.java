package uk.co.terminological.bibliography;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

public class CiteProcProvider extends ArrayList<CSLItemData> implements ItemDataProvider  {
    
	private List<String> ids = new ArrayList<>();
	private Bibliography bib = null;
	private Format format = Format.text;
	private String style = "ieee";
	
	public static CiteProcProvider create() {
		return create("ieee", Format.text);
	}
	
	public static CiteProcProvider create(String style, Format output) {
		CiteProcProvider out = new CiteProcProvider();
		out.format = output;
		out.style = style;
		return out;
	}
	
	@Override
    public CSLItemData retrieveItem(String id) {
		try {
			int i = ids.indexOf(id);
			if (i==-1) return null;
			CSLItemData out = this.get(i);
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
    
	public void add(Record rec) {
		bib = null;
		add(fromRecord(rec,Integer.toString(size())));
		ids.add(Integer.toString(size()));
	}
	
	public void add(Record rec, String id) {
		bib = null;
		if (ids.contains(id)) {
			this.set(ids.indexOf(id), fromRecord(rec,id));
		} else {
			add(fromRecord(rec,id));
			ids.add(id);
		}
	}
	
	public static CSLItemData fromRecord(Record record) {
		return fromRecord(record, record.getIdentifier().orElse(UUID.randomUUID().toString()));
	}
	
	public static CSLItemData fromRecord(Record record, String id) {
		CSLItemDataBuilder builder = new CSLItemDataBuilder()
	            .id(id)
	            .type(CSLType.ARTICLE_JOURNAL);
	         record.getTitle().ifPresent(t -> builder.title(t.replace('\n', ' ').trim()));
	         if (record.getFirstAuthor().isPresent()) {
	        	 Author a = record.getFirstAuthor().get();
	        	 builder.author(
	        		a.getFirstName().or(() -> a.getInitials()).orElse("").replace("\n", " ").trim(),
	        		a.getLastName().replace("\n", " ").trim()
	        	); 
	         } else if (record.getFirstAuthorLastName().isPresent()) {
	        	 if (record.getFirstAuthorFirstName().isPresent()) {
	        		 builder.author(
	        				 record.getFirstAuthorFirstName().get(), 
	        				 record.getFirstAuthorLastName().get());
	        	 }
	         }
	         
	         record.getDate().ifPresent(d -> builder.issued(
	        		 d.getYear(),
	        		 d.getMonthValue(),
	        		 d.getDayOfMonth()));
	         record.getJournal().ifPresent(j -> builder.containerTitle(j));
	         
	         buildIds(builder, record.getIdentifierType(), record.getIdentifier());
	         record.getOtherIdentifiers().forEach(rr -> buildIds(builder, rr.getIdentifierType(), rr.getIdentifier()));
	         
	         record.getAbstract().ifPresent(a -> builder.abstrct(a.replace('\n', ' ').trim()));
	         record.getPdfUri().ifPresent(p -> builder.URL(p.toString()));
	         
	         if (record instanceof Print) {
	        	 Print print = (Print) record;
	        	 
	        	 print.getIssue().ifPresent(i -> builder.issue(i));
	        	 print.getPage().ifPresent(p -> builder.page(p));
	        	 print.getVolume().ifPresent(v -> builder.volume(v));
	        	 print.getYear().ifPresent(y -> builder.issued(y.intValue()));
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
        return IntStream.range(0, size()).mapToObj(r -> Integer.toString(r))
        		.collect(Collectors.toList()).toArray(new String[] {});
    }
	
	public Bibliography orderedCitations(String style, Format format) throws IOException {
		bib = CSL.makeAdhocBibliography(style, format.toString(), this.toArray(new CSLItemData[] {}));
		return bib;
	}
	
	public Bibliography orderedCitations() throws IOException {
		bib = CSL.makeAdhocBibliography(style, format.toString(), this.toArray(new CSLItemData[] {}));
		return bib;
	}
	
	public Optional<String> fetchReference(String id) {
		
			try {
				if (bib == null) orderedCitations();
				int index = this.ids.indexOf(id);
				return Optional.ofNullable(bib.getEntries()[index]);
			} catch (Exception e) {
				return Optional.empty();
			}
	}
	
	public static Bibliography convert(String style, Format format, Record... record) throws IOException {
		return CSL.makeAdhocBibliography(style, format.toString(), 
				Stream.of(record).map(r -> fromRecord(r)).collect(Collectors.toList()).toArray(new CSLItemData[] {}))
				;
	}
	
	public static enum Format {
		html, text, asciidoc, fo, rtf
	}
}