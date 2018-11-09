package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ARTICLE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.MESH_CODE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_MESH;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_REFERENCE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_RELATED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;

public class PubMedGraphUtils {

	
	
	public static Optional<Node> mapDoiToNode(String doi, GraphDatabaseApi graph, Label... additionalLabels) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = graph.get().findNode(ARTICLE, "doi", doi);
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE, DOI_STUB);
			}
			tmp.setProperty("doi", doi);
			tx.success();
			out = tmp;
		}
		return Optional.ofNullable(out);
	}
	
	
	public static Optional<Node> mapPMIDToNode(String pmid, GraphDatabaseApi graph, Label... additionalLabels) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = graph.get().findNode(ARTICLE, "pmid", pmid);
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE, PMID_STUB);
			}
			tmp.setProperty("pmid", pmid);
			tx.success();
			out = tmp;
		}
		return Optional.ofNullable(out);
	}


	public static List<Node> mapEntriesToNode(PubMedEntries entries, Integer depth, GraphDatabaseApi graph, Label... additionalLabels) {

		List<Node> out = new ArrayList<>();

		try ( Transaction tx = graph.get().beginTx() ) {
			entries.stream().forEach(entry -> {
				Node tmp = null;
				Node tmp1 = entry.getPMID().isPresent() ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID().get()) : null;
				Node tmp2 = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "doi", entry.getDoi().get()) : null;
				if (tmp1 != null && tmp2 != null && tmp1.getId() != tmp2.getId()) {
					//merge tmp1 and tmp2
					tmp2.getAllProperties().forEach((k,v) -> tmp1.setProperty(k, v));
					tmp2.getRelationships(Direction.INCOMING).forEach(r -> {
						Node other = r.getOtherNode(tmp2);
						Relationship r2 = other.createRelationshipTo(tmp1, r.getType());
						r.getAllProperties().forEach((k,v) -> r2.setProperty(k, v));
						r.delete();
					});
					tmp2.getRelationships(Direction.OUTGOING).forEach(r -> {
						Node other = r.getOtherNode(tmp2);
						Relationship r2 = tmp1.createRelationshipTo(other, r.getType());
						r.getAllProperties().forEach((k,v) -> r2.setProperty(k, v));
						r.delete();
					});
					tmp2.getLabels().forEach(l -> tmp1.addLabel(l));
					tmp2.delete();
				} else if (tmp1!=null) {
					tmp = tmp1;
				} else if (tmp2!=null) {
					tmp = tmp2;
				} else {
					tmp = graph.get().createNode(ARTICLE);
				}
				
				Node node = tmp;
				Arrays.stream(additionalLabels).forEach(label -> node.addLabel(label));
				entry.getPMID().ifPresent(pmid -> node.setProperty("pmid", pmid));
				entry.getDoi().ifPresent(doi -> node.setProperty("doi", doi));
				entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
				node.setProperty("abstract", entry.getAbstract());
				node.setProperty("title", entry.getTitle());
				node.setProperty("depth", depth);
				entry.getPubMedDate().ifPresent(dt -> node.setProperty("date", dt));
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				entry.getAuthors().forEach(au -> {
					Optional<Node> targetNode = mapAuthorToNode(au,graph);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_AUTHOR));
				});
				entry.getMeshHeadings().forEach(mh -> {
					Optional<Node> targetNode = mapMeshCodeToNode(mh.getDescriptor(),graph);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_MESH));
				});
				out.add(node);
			});
			tx.success();

		}

		return out;

	}

	public static Optional<Node> mapAuthorToNode(Author author, GraphDatabaseApi graph) {
		
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			
			Node tmp = graph.get().findNode(AUTHOR, "identifier", author.getIdentifier());
			if (tmp == null) {
				tmp = graph.get().createNode(AUTHOR);
				tmp.setProperty("identifier", author.getIdentifier());
			}
			
			Node node = tmp;
			author.firstName().ifPresent(fn -> node.setProperty("firstName", fn));
			author.lastName().ifPresent(fn -> node.setProperty("lastName", fn));
			author.initials().ifPresent(fn -> node.setProperty("initials", fn));
			String[] affiliations = author.affiliations().collect(Collectors.toList()).toArray(new String[] {});
			if (affiliations.length > 0) node.setProperty("affiliations", affiliations);
			
			out = node;
		    tx.success();
		    
		}
		
		return Optional.ofNullable(out);
		
	}
	
	public static Optional<Node> mapMeshCodeToNode(MeshCode meshCode, GraphDatabaseApi graph) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			
			Node tmp = graph.get().findNode(MESH_CODE, "code", meshCode.getCode());
			if (tmp == null) {
				tmp = graph.get().createNode(MESH_CODE);
				tmp.setProperty("code", meshCode.getCode());
				tmp.setProperty("term", meshCode.getTerm());
			}
			out = tmp;
		    tx.success();
		    
		}
		
		return Optional.ofNullable(out);
	}
	
	public static List<Relationship> mapHasReferences(String citingDoi, List<String> citedDois, Integer depth, GraphDatabaseApi graph) {
		List<Relationship> out = new ArrayList<>();
		
		try (Transaction tx = graph.get().beginTx()) {
			Node start = 
				Optional.ofNullable(
					graph.get().findNode(ARTICLE, "doi", citingDoi)
				).orElseGet(() -> {
					Node other = graph.get().createNode(ARTICLE,DOI_STUB);
					other.setProperty("doi", citingDoi);
					return other;
				}); 
			citedDois.forEach(citedDoi -> {
			Node end = graph.get().findNode(ARTICLE, "doi", citedDoi);
			if (end==null) {
				end = graph.get().createNode(ARTICLE,DOI_STUB);
				end.setProperty("doi", citedDoi);
				end.setProperty("depth", depth);
			}
			out.add(start.createRelationshipTo(end, HAS_REFERENCE));
			
			});
			tx.success();
		}
		
		return out;
	}
	
	public static Optional<Relationship> mapHasRelated(String sourcePMID, String targetPMID, Long relatedness, Integer depth, GraphDatabaseApi graph) {
		Relationship out = null;
		
		try (Transaction tx = graph.get().beginTx()) {
			Node start = graph.get().findNode(ARTICLE, "pmid", sourcePMID);
			if (start==null) {
				start = graph.get().createNode(ARTICLE,PMID_STUB);
				start.setProperty("pmid", sourcePMID);
			}
			Node end = graph.get().findNode(ARTICLE, "pmid", targetPMID);
			if (end==null) {
				end = graph.get().createNode(ARTICLE,PMID_STUB);
				end.setProperty("pmid", targetPMID);
			}
			out = start.createRelationshipTo(end, HAS_RELATED);
			out.setProperty("relatedness", relatedness);
			out.setProperty("depth", depth);
			tx.success();
		}
		
		return Optional.ofNullable(out);
	}
	
}
