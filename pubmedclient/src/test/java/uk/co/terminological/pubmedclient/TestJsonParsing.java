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
					System.out.println("cited by: "+item.getCitedByCount());
					System.out.println("date: "+item.getDate());
					System.out.println("first author: "+item.getFirstAuthorName());
					System.out.println("id: "+item.getIdentifier());
					System.out.println("idtype: "+item.getIdentifierType());
					System.out.println("issue: "+item.getIssue());
					System.out.println("journal: "+item.getJournal());
					System.out.println("page: "+item.getPage());
					System.out.println("pdf uri: "+item.getPdfUri());
					System.out.println("references: "+item.getReferencesCount());
					System.out.println("score: "+item.getScore());
					System.out.println("title: "+item.getTitle());
					System.out.println("tect mining uri: "+item.getTextMiningUri());
					System.out.println("vol: "+item.getVolume());
					System.out.println("year: "+item.getYear());
					System.out.println("authors:");
					item.getAuthors().forEach(a -> {
						System.out.println("first name: "+a.getFirstName());
						System.out.println("first name: "+a.getLastName());
						System.out.println("first name: "+a.getInitials());
						System.out.println("first name: "+a.getORCID());
					});
				}
				);
		
		
		
	}

}
