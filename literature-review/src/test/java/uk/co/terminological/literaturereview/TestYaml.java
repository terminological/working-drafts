package uk.co.terminological.literaturereview;

import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;



public class TestYaml {

	public static void main(String[] args) {
	Yaml yaml = new Yaml();
	InputStream inputStream = TestYaml.class
	  .getClassLoader()
	  .getResourceAsStream("cypherQuery.yaml");
	Map<String, Object> obj = yaml.load(inputStream);
	
	System.out.println(obj);
	}
	
}
