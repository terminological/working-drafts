package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

public class TestJsonParsing {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub

		ObjectMapper objectMapper = new ObjectMapper().registerModule(new Jdk8Module());;
		InputStream is = ClassLoader.getSystemResourceAsStream("ontologiesExample.json");
		CrossRefResult.ListResult  response = objectMapper.readValue(is, CrossRefResult.ListResult.class);
		response.getMessage().getItems().forEach(
				item -> {
					System.out.println(item.getTitle());
					item.link.forEach(
							rl -> System.out.println(rl.intendedApplication+" => "+rl.URL)
							);
				}
				);
		
		
		
	}

}
