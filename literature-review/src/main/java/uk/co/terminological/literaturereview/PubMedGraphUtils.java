package uk.co.terminological.literaturereview;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.Schema;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;

import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.*;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.*;

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
	
	
	public static Optional<Node> mapEntryToNode(PubMedEntry entry, GraphDatabaseApi graph, Label... additionalLabels) {
		
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = entry.getPMID().isPresent() ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID().get()) : null;
			if (tmp == null) tmp = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "doi", entry.getDoi().get()) : null;
			if (tmp == null) {
				tmp = graph.get().createNode(ARTICLE);
			}
			
			Node node = tmp;
			Arrays.stream(additionalLabels).forEach(label -> node.addLabel(label));
			entry.getPMID().ifPresent(pmid -> node.setProperty("pmid", pmid));
			entry.getDoi().ifPresent(doi -> node.setProperty("doi", doi));
			entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
			node.setProperty("abstract", entry.getAbstract());
			node.setProperty("title", entry.getTitle());
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
			out = node;
		    tx.success();
		}
		
		return Optional.ofNullable(out);
		
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
	
	public static Optional<Relationship> mapHasReference(String citingDoi, String citedDoi, Integer depth, GraphDatabaseApi graph) {
		Relationship out = null;
		
		try (Transaction tx = graph.get().beginTx()) {
			Node start = graph.get().findNode(ARTICLE, "doi", citingDoi);
			if (start==null) {
				start = graph.get().createNode(ARTICLE,DOI_STUB);
				start.setProperty("doi", citingDoi);
			}
			Node end = graph.get().findNode(ARTICLE, "doi", citedDoi);
			if (end==null) {
				end = graph.get().createNode(ARTICLE,DOI_STUB);
				end.setProperty("doi", citedDoi);
			}
			out = start.createRelationshipTo(end, HAS_REFERENCE);
			out.setProperty("depth", depth);
			tx.success();
		}
		
		return Optional.ofNullable(out);
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
