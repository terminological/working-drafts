package uk.co.terminological.bibliography.entrez;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import uk.co.terminological.bibliography.record.Raw;
import uk.co.terminological.fluentxml.XmlElement;
import uk.co.terminological.fluentxml.XmlException;

public class Links implements Raw<XmlElement> {

	private XmlElement raw;
	public Links(XmlElement raw) throws XmlException {
		this.raw=raw; 
		convert();
	}
	public XmlElement getRaw() {return raw;}

	private List<Link> links;

	public Stream<Link> stream() {
		return links.stream();
	}

	private void convert() throws XmlException {
		links = new ArrayList<Link>();
		
			for (XmlElement linkSet: raw.doXpath(".//LinkSet").getMany(XmlElement.class)) {

				Optional<String> dbFrom = linkSet.doXpath("./DbFrom").get(XmlElement.class).flatMap(el -> el.getTextContent());
				Optional<String> fromId = linkSet.doXpath("./IdList/Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
				
				for (XmlElement linkSetDb: linkSet.doXpath("./LinkSetDb").getMany(XmlElement.class)) {
					
					Optional<String> dbTo = linkSetDb.doXpath("./DbTo").get(XmlElement.class).flatMap(el -> el.getTextContent());
					Optional<String> linkName = linkSetDb.doXpath("./LinkName").get(XmlElement.class).flatMap(el -> el.getTextContent());
					
					for (XmlElement link: linkSetDb.doXpath("./Link").getMany(XmlElement.class)) {
						
						Optional<String> toId = link.doXpath("./Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
						Optional<Long> score = link.doXpath("./Score").get(XmlElement.class).flatMap(el -> el.getTextContent()).map(s -> Long.parseLong(s));
						
						if (dbFrom.isPresent() && fromId.isPresent() && toId.isPresent()) {
							links.add(new Link(dbFrom.get(),fromId.get(),linkName,dbTo,toId.get(), score));
						}
						
					}
					
				}
				
				for (XmlElement idUrlSet: linkSet.doXpath("./IdUrlList/IdUrlSet").getMany(XmlElement.class)) { 
					
					fromId = idUrlSet.doXpath("./Id").get(XmlElement.class).flatMap(el -> el.getTextContent());
					for (XmlElement objUrl: idUrlSet.doXpath("./ObjUrl").getMany(XmlElement.class)) {
						
						Optional<String> category = objUrl.doXpath("./Category").get(XmlElement.class).flatMap(el -> el.getTextContent());
						Optional<String> toUrl = objUrl.doXpath("./Url").get(XmlElement.class).flatMap(el -> el.getTextContent());
						
						if (dbFrom.isPresent() && fromId.isPresent() && toUrl.isPresent()) {
							links.add(new Link(dbFrom.get(),fromId.get(),category,toUrl.get()));
						}
					}
				}

				
			}
		
	}
	public List<Link> getLinks() {
		return links;
	}



	//public List<String> getSimilar

}