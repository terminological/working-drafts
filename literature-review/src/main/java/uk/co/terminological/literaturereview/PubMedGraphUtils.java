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
import static uk.co.terminological.literaturereview.PubMedGraphUtils.mapHasRelated;

import java.time.format.DateTimeFormatter;
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
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oracle.tools.packager.Log;

import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;

public class PubMedGraphUtils {

	private static final Logger logger = LoggerFactory.getLogger(PubMedGraphUtils.class);

	public static Node doMerge(Label label, String indexName, Object indexValue, GraphDatabaseService graphDb) {
		return doMerge(label,indexName,indexValue,graphDb, null);
	}
	public static Node doMerge(Label label, String indexName, Object indexValue, GraphDatabaseService graphDb, Label label2) {
		// logger.debug("Looking for: {} with {}={}",label.name(),indexName,indexValue.toString());
		/*String queryString = "MERGE (n:"+label.name()+" {"+indexName+": $"+indexName+"})"+
				(label2!=null ? " ON CREATE SET n:"+label2.name():"")+" RETURN n";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put( indexName, indexValue );
		ResourceIterator<Node> resultIterator = graphDb.execute( queryString, parameters ).columnAs( "n" );
		Node result = resultIterator.next();
		return result;*/
		Node out = graphDb.findNode(label, indexName, indexValue);
		if (out == null) {
			if (label2 != null) {
				out =graphDb.createNode(label,label2);
			} else {
				out =graphDb.createNode(label);
			}
			out.setProperty(indexName, indexValue);
		}
		return out;
		
		// if (label2 != null && result.hasLabel(label2)) logger.debug("Created node: {} with {}={}",label.name(),indexName,indexValue.toString());
		
	}


	/*public static Optional<Node> mapDoiToNode(String doi, GraphDatabaseApi graph) {
		Node out = null;

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			Node tmp = doMerge(ARTICLE, "doi", doi, graph.get());
			tmp.addLabel(DOI_STUB);
			tx.success();
			out = tmp;

		}
		return Optional.ofNullable(out);
	}


	public static Optional<Node> mapPMIDToNode(String pmid, GraphDatabaseApi graph) {
		Node out = null;

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			Node tmp = doMerge(ARTICLE, "pmid", pmid,graph.get());
			tmp.addLabel(PMID_STUB);
			tx.success();
			out = tmp;
			tx.success();
		}
		return Optional.ofNullable(out);
	}*/


	public static List<Node> mapEntriesToNode(PubMedEntries entries, GraphDatabaseApi graph, Integer maxDepth) {

		List<Node> out = new ArrayList<>();


		/*entries.stream().forEach(entry -> {
			Node tmp = null;
			Node tmp3 = null;
			Node tmp4 = null;
			try ( Transaction tx = graph.get().beginTx() ) {
				tx.acquireWriteLock(lockNode);
				tmp = null;
				tmp3 = entry.getPMID().isPresent() ? doMerge(ARTICLE, "pmid", entry.getPMID().get(), graph.get()) : null;
				tmp4 = entry.getDoi().isPresent() ? doMerge(ARTICLE, "doi", entry.getDoi().get(), graph.get()) : null;
				tx.success();
			}
			Node tmp1=tmp3;
			Node tmp2=tmp4;
			if (tmp1 != null && tmp2 != null && tmp1.getId() != tmp2.getId()) {
				//merge tmp1 and tmp2
				try ( Transaction tx = graph.get().beginTx() ) {
					tx.acquireWriteLock(lockNode);
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
					tx.success();
				}
				tmp = tmp1;
			} else if (tmp1!=null) {
				tmp = tmp1;
			} else if (tmp2!=null) {
				tmp = tmp2;
			} else {
				try ( Transaction tx = graph.get().beginTx() ) {
					tx.acquireWriteLock(lockNode);

					Node newNode = graph.get().createNode(ARTICLE);
					entry.getPMID().ifPresent(pmid -> newNode.setProperty("pmid", pmid));
					entry.getDoi().ifPresent(doi -> newNode.setProperty("doi", doi));
					tmp = newNode;
					tx.success();
				}
			}

			Node node = tmp;
			try ( Transaction tx = graph.get().beginTx() ) {
				tx.acquireWriteLock(lockNode);

				Arrays.stream(additionalLabels).forEach(label -> node.addLabel(label));
				entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
				//TODO:: DEadlock here
				node.setProperty("abstract", entry.getAbstract());
				node.setProperty("title", entry.getTitle());
				tx.success();
			}
			Integer depth = null;
			try ( Transaction tx = graph.get().beginTx() ) {
				tx.acquireWriteLock(lockNode);

				for (Relationship r: node.getRelationships(Direction.INCOMING, HAS_REFERENCE, HAS_RELATED)) {
					Node other = r.getOtherNode(node);
					Integer tmpDepth = (Integer) other.getProperty("depth", null);
					if (depth == null) depth=tmpDepth;
					else if (tmpDepth!=null && tmpDepth<depth) depth=tmpDepth;
				};
				if (depth==null) depth = 0; else depth = depth+1;
				tx.acquireWriteLock(lockNode);
				node.setProperty("depth", depth);
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				entry.getPubMedDate().ifPresent(dt -> node.setProperty("date", dt));
				if (depth<maxDepth) {
					node.addLabel(EXPAND);
				} else {
					logger.debug("not expanding at depth "+depth+": "+entry.getTitle());
				}
				tx.success();
			}

			try ( Transaction tx = graph.get().beginTx() ) {
				tx.acquireWriteLock(lockNode);

				entry.getAuthors().forEach(au -> {
					Optional<Node> targetNode = mapAuthorToNode(au,graph);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_AUTHOR));
				});
				entry.getMeshHeadings().forEach(mh -> {
					Optional<Node> targetNode = mapMeshCodeToNode(mh.getDescriptor(),graph);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_MESH));
				});
				tx.success();
			}
			out.add(node);
		});*/

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);

			
			
			entries.stream().forEach(entry -> {
				Node tmp = null;
				Node tmp1 = entry.getPMID().isPresent() ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID().get()) : null;
				Node tmp2 = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID().get()) : null;
				if (tmp1 != null && tmp2 != null && tmp1.getId() != tmp2.getId()) {
					logger.info("Merging article pubmed: "+entry.getPMID().get()+" with doi: "+entry.getDoi().get());
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
					logger.info("Updating pubmed article: "+entry.getPMID().get());
					tmp = tmp1;
				} else if (tmp2!=null) {
					logger.info("Updating doi article: "+entry.getDoi().get());
					tmp = tmp2;
				} else {
					logger.info("Creating new article record: pmid"+entry.getPMID().orElse("none")+" doi:"+entry.getDoi().orElse("none"));
					Node newNode = graph.get().createNode(ARTICLE);
					entry.getPMID().ifPresent(pmid -> newNode.setProperty("pmid", pmid));
					entry.getDoi().ifPresent(doi -> newNode.setProperty("doi", doi));
					tmp = newNode;
				}

				Node node = tmp;
				
				entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
				entry.getPubMedDate().ifPresent(dt -> node.setProperty("date", dt.format(
					      DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
				node.setProperty("abstract", entry.getAbstract());
				node.setProperty("title", entry.getTitle());

				Integer depth = null;
				for (Relationship r: node.getRelationships(Direction.INCOMING, HAS_REFERENCE, HAS_RELATED)) {
					Node other = r.getOtherNode(node);
					Integer tmpDepth = (Integer) other.getProperty("depth", null);
					if (depth == null) depth=tmpDepth;
					else if (tmpDepth!=null && tmpDepth<depth) depth=tmpDepth;
					
				};
				if (depth==null) depth = 0; else depth = depth+1;
				node.setProperty("depth", depth);
				logger.debug("depth for node: "+depth+": "+entry.getTitle());
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				
				if (depth<maxDepth) {
					node.addLabel(EXPAND);
				} else {
					logger.debug("not expanding at depth "+depth+": "+entry.getTitle());
				}
				entry.getAuthors().forEach(au -> {
					Optional<Node> targetNode = mapAuthorToNode(au,graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_AUTHOR));
				});
				entry.getMeshHeadings().forEach(mh -> {
					Optional<Node> targetNode = mapMeshCodeToNode(mh.getDescriptor(),graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_MESH));
				});
				out.add(node);
			});
			tx.success();
		}


		return out;

	}

	public static Optional<Node> mapAuthorToNode(Author author, GraphDatabaseApi graph, Transaction tx) {

		Node out = null;

			Node node = doMerge(AUTHOR, "identifier", author.getIdentifier(), graph.get());
			author.firstName().ifPresent(fn -> node.setProperty("firstName", fn));
			author.lastName().ifPresent(fn -> node.setProperty("lastName", fn));
			author.initials().ifPresent(fn -> node.setProperty("initials", fn));
			String[] affiliations = author.affiliations().collect(Collectors.toList()).toArray(new String[] {});
			if (affiliations.length > 0) node.setProperty("affiliations", affiliations);

			out = node;

		return Optional.ofNullable(out);

	}

	public static Optional<Node> mapMeshCodeToNode(MeshCode meshCode, GraphDatabaseApi graph, Transaction tx) {
		Node out = null;

			Node tmp = doMerge(MESH_CODE, "code", meshCode.getCode(), graph.get());
			tmp.setProperty("code", meshCode.getCode());
			tmp.setProperty("term", meshCode.getTerm());
			out = tmp;

		return Optional.ofNullable(out);
	}

	public static List<Relationship> mapHasReferences(String citingDoi, List<String> citedDois, GraphDatabaseApi graph) {
		List<Relationship> out = new ArrayList<>();
		try {
			graph.get().getNodeById(lockNode.getId());
			throw new RuntimeException("Was in transaction already");
		} catch (NotInTransactionException e) {
			
		}
			
			
		logger.info("Adding "+citedDois.size()+" references to "+citingDoi);
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			Node start = doMerge(ARTICLE, "doi", citingDoi, graph.get(), DOI_STUB);
			citedDois.forEach(citedDoi -> {
				Node end = doMerge(ARTICLE, "doi", citedDoi,graph.get(), DOI_STUB);
				out.add(start.createRelationshipTo(end, HAS_REFERENCE));
			});
			tx.success();
			logger.info(out.size()+" relationships added in transaction: ");
		}

		return out;
	}

	public static List<Relationship> mapHasRelated(List<Link> links, GraphDatabaseApi graph) {
		
		try {
			graph.get().getNodeById(lockNode.getId());
			throw new RuntimeException("Was in transaction already");
		} catch (NotInTransactionException e) {
			
		}
		
		logger.info("Adding "+links.size()+" as related content");
		List<Relationship> out = new ArrayList<>();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);

			

			links.forEach(link -> { 
				link.toId.ifPresent(toId -> {
					Node start = doMerge(ARTICLE, "pmid", link.fromId,graph.get(), PMID_STUB);
					Node end = doMerge(ARTICLE, "pmid", link.toId.get(), graph.get(), PMID_STUB);
					Relationship tmp = start.createRelationshipTo(end, HAS_RELATED);
					link.score.ifPresent(s -> tmp.setProperty("relatedness", s));
				});
			});
			tx.success();
		}

		return out;
	}

}
