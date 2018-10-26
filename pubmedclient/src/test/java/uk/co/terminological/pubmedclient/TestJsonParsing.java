package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Properties;

public class TestJsonParsing {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub

		ObjectMapper objectMapper = new ObjectMapper();
		InputStream is = ClassLoader.getSystemResourceAsStream("ontologiesExample.json");
		CrossRefResult.ListResult  response = objectMapper.readValue(is, CrossRefResult.ListResult.class);
		response.message.items.forEach(
				item -> {
					item.title.forEach(System.out::println);
					item.link.forEach(
							rl -> System.out.println(rl.intendedApplication+" => "+rl.URL)
							);
				}
				);
		
		
		
	}

}
