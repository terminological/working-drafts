package uk.co.terminological.literaturereview;

import static uk.co.terminological.literaturereview.PubMedGraphExperiment.lockNode;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ARTICLE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.MESH_CODE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_MESH;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_REFERENCE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_RELATED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;

public class PubMedGraphUtils {

	private static final Logger logger = LoggerFactory.getLogger(PubMedGraphUtils.class);
	
	public static Node doMerge(Label label, String indexName, Object indexValue, GraphDatabaseService graphDb) {
		return doMerge(label,indexName,indexValue,graphDb, null);
	}
	public static Node doMerge(Label label, String indexName, Object indexValue, GraphDatabaseService graphDb, Label label2) {
		String queryString = "MERGE (n:"+label.name()+" {"+indexName+": $"+indexName+"})"+
				(label2!=null ? " SET n:"+label2.name():"")+" RETURN n";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put( indexName, indexValue );
		ResourceIterator<Node> resultIterator = graphDb.execute( queryString, parameters ).columnAs( "n" );
		Node result = resultIterator.next();
		return result;
	}
        
	
	public static Optional<Node> mapDoiToNode(String doi, GraphDatabaseApi graph, Label... additionalLabels) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = doMerge(ARTICLE, "doi", doi, graph.get());
			tmp.addLabel(DOI_STUB);
			tx.success();
			out = tmp;
		}
		return Optional.ofNullable(out);
	}
	
	
	public static Optional<Node> mapPMIDToNode(String pmid, GraphDatabaseApi graph, Label... additionalLabels) {
		Node out = null;
		
		try ( Transaction tx = graph.get().beginTx() ) {
			Node tmp = doMerge(ARTICLE, "pmid", pmid,graph.get());
			tmp.addLabel(PMID_STUB);
			tx.success();
			out = tmp;
		}
		return Optional.ofNullable(out);
	}


	public static List<Node> mapEntriesToNode(PubMedEntries entries, GraphDatabaseApi graph, Integer maxDepth, Label... additionalLabels) {

		List<Node> out = new ArrayList<>();

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			entries.stream().forEach(entry -> {
				Node tmp = null;
				Node tmp1 = entry.getPMID().isPresent() ? doMerge(ARTICLE, "pmid", entry.getPMID().get(), graph.get()) : null;
				Node tmp2 = entry.getDoi().isPresent() ? doMerge(ARTICLE, "doi", entry.getDoi().get(), graph.get()) : null;
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
					tmp = tmp1;
				} else if (tmp1!=null) {
					tmp = tmp1;
				} else if (tmp2!=null) {
					tmp = tmp2;
				} else {
					Node newNode = graph.get().createNode(ARTICLE);
					entry.getPMID().ifPresent(pmid -> newNode.setProperty("pmid", pmid));
					entry.getDoi().ifPresent(doi -> newNode.setProperty("doi", doi));
					tmp = newNode;
				}
				
				Node node = tmp;
				Arrays.stream(additionalLabels).forEach(label -> node.addLabel(label));
				entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
				//TODO:: DEadlock here
				node.setProperty("abstract", entry.getAbstract());
				node.setProperty("title", entry.getTitle());
				Integer depth = null;
				for (Relationship r: node.getRelationships(Direction.INCOMING, HAS_REFERENCE, HAS_RELATED)) {
					Node other = r.getOtherNode(node);
					Integer tmpDepth = (Integer) other.getProperty("depth", null);
					if (depth == null) depth=tmpDepth;
					else if (tmpDepth!=null && tmpDepth<depth) depth=tmpDepth;
				};
				if (depth==null) depth = 0;
				node.setProperty("depth", depth);
				if (depth<maxDepth) {
					node.addLabel(EXPAND);
				} else {
					logger.debug("not expanding at depth "+depth+": "+entry.getTitle());
				}
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
			tx.acquireWriteLock(lockNode);
			
			Node node = doMerge(AUTHOR, "identifier", author.getIdentifier(), graph.get());
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
	
	public static List<Relationship> mapHasReferences(String citingDoi, List<String> citedDois, GraphDatabaseApi graph) {
		List<Relationship> out = new ArrayList<>();
		
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			Node start = doMerge(ARTICLE, "doi", citingDoi, graph.get(), DOI_STUB);
			citedDois.forEach(citedDoi -> {
				Node end = doMerge(ARTICLE, "doi", citedDoi,graph.get(), DOI_STUB);
				out.add(start.createRelationshipTo(end, HAS_REFERENCE));
			});
			tx.success();
		}
		
		return out;
	}
	
	public static Optional<Relationship> mapHasRelated(String sourcePMID, String targetPMID, Long relatedness, GraphDatabaseApi graph) {
		Relationship out = null;
		Node start;
		Node end;
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			//TODO:: DEadlock here
			start = doMerge(ARTICLE, "pmid", sourcePMID,graph.get(), PMID_STUB);
			tx.success();
		}
		try (Transaction tx = graph.get().beginTx()) {
			end = doMerge(ARTICLE, "pmid", targetPMID, graph.get(), PMID_STUB);
			tx.success();
		}
		try (Transaction tx = graph.get().beginTx()) {
			out = start.createRelationshipTo(end, HAS_RELATED);
			out.setProperty("relatedness", relatedness);
			tx.success();
		}
		
		return Optional.ofNullable(out);
	}
	
}
