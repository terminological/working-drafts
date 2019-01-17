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
					System.out.println(item.getAbstract());
					System.out.println(item.getCitedByCount());
					System.out.println(item.getDate());
					System.out.println(item.getFirstAuthorName());
					System.out.println(item.getIdentifier());
					System.out.println(item.getIdentifierType());
					System.out.println(item.getIssue());
					System.out.println(item.getJournal());
					System.out.println(item.getLabel());
					System.out.println(item.getPage());
					System.out.println(item.getPdfUri());
					System.out.println(item.getReferencesCount());
					System.out.println(item.getScore());
					System.out.println(item.getTitle());
					System.out.println(item.getTextMiningUri());
					System.out.println(item.getVolume());
					System.out.println(item.getYear());
				}
				);
		
		
		
	}

}
