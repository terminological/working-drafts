package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import uk.co.terminological.bibliography.crossref.ListResult;

public class TestJsonParsing {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub

		ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());;
		InputStream is = ClassLoader.getSystemResourceAsStream("ontologiesExample.json");
		ListResult  response = new ListResult(objectMapper.readTree(is));
		response.getMessage().getItems().forEach(
				item -> {
					System.out.println("\n========================================\n"+item.getTitle());
					System.out.println("abs: "+item.getAbstract());
					System.out.println("abs: "+item.getCitedByCount());
					System.out.println("abs: "+item.getDate());
					System.out.println("abs: "+item.getFirstAuthorName());
					System.out.println("abs: "+item.getIdentifier());
					System.out.println("abs: "+item.getIdentifierType());
					System.out.println("abs: "+item.getIssue());
					System.out.println("abs: "+item.getJournal());
					System.out.println("abs: "+item.getLabel());
					System.out.println("abs: "+item.getPage());
					System.out.println("abs: "+item.getPdfUri());
					System.out.println("abs: "+item.getReferencesCount());
					System.out.println("abs: "+item.getScore());
					System.out.println("abs: "+item.getTitle());
					System.out.println("abs: "+item.getTextMiningUri());
					System.out.println("abs: "+item.getVolume());
					System.out.println("abs: "+item.getYear());
				}
				);
		
		
		
	}

}
