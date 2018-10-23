package uk.co.terminological.pubmedclient;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJsonParsing {

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub

		ObjectMapper objectMapper = new ObjectMapper();
		InputStream is = ClassLoader.getSystemResourceAsStream("ontologiesExample.json");
		CrossRefApiResponse.Response  response = objectMapper.readValue(is, CrossRefApiResponse.Response.class);
		response.message.items.stream().forEach(
				item -> {
						if (item.originalTitle != null) item.originalTitle.forEach(System.out::println);
					}
				);
		
	}

}
