package uk.co.terminological.literaturereview;


import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ARTICLE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.DOI_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.EXPAND;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.MESH_CODE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.ORIGINAL_SEARCH;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMCENTRAL_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Labels.PMID_STUB;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_AUTHOR;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_MESH;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_REFERENCE;
import static uk.co.terminological.literaturereview.PubMedGraphSchema.Rel.HAS_RELATED;
//TODO: import static uk.co.terminological.literaturereview.PubMedGraphSchema.Props.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.pubmedclient.CrossRefResult.Contributor;
import uk.co.terminological.pubmedclient.CrossRefResult.Reference;
import uk.co.terminological.pubmedclient.CrossRefResult.Work;
import uk.co.terminological.pubmedclient.EntrezResult.Author;
import uk.co.terminological.pubmedclient.EntrezResult.Link;
import uk.co.terminological.pubmedclient.EntrezResult.MeshCode;
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntries;
import uk.co.terminological.pubmedclient.UnpaywallClient.Result;

public class PubMedGraphUtils {

	static Node lockNode;
	private static final Logger logger = LoggerFactory.getLogger(PubMedGraphUtils.class);

	public static Node doMerge(Label label, String indexName, String indexValue, GraphDatabaseService graphDb) {
		return doMerge(label,indexName,indexValue,graphDb, null);
	}
	public static Node doMerge(Label label, String indexName, String indexValue, GraphDatabaseService graphDb, Label label2) {
		String queryString = "MERGE (n:"+label.name()+" {"+indexName+": $"+indexName+"})"+
				(label2!=null ? " ON CREATE SET n:"+label2.name():"")+" RETURN n";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put( indexName, indexValue );
		ResourceIterator<Node> resultIterator = graphDb.execute( queryString, parameters ).columnAs( "n" );
		Node result = resultIterator.next();
		return result;	
	}
	
	public static Relationship doMerge(Label srcLabel, String srcIndexName, String srcIndexValue,RelationshipType relType, Label targetLabel, String targetIndexName, String targetIndexValue, GraphDatabaseService graphDb) {
		String queryString = 
				"MATCH "+
						"(n:"+srcLabel.name()+" {"+srcIndexName+": $src_"+srcIndexName+"}), "+
						"(m:"+targetLabel.name()+" {"+targetIndexName+": $target_"+targetIndexName+"}) "+
				"MERGE "+
						"(n)-[r:"+relType.name()+"]->(m) "+
				"RETURN r";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put( "src_"+srcIndexName, srcIndexValue );
		parameters.put( "target_"+targetIndexName, targetIndexValue );
		ResourceIterator<Relationship> resultIterator = graphDb.execute( queryString, parameters ).columnAs( "r" );
		Relationship result = resultIterator.next();
		return result;	
	}

	public static void addLabelsByIds(Label existingLabel, String indexProp, Set<?> values, Label newLabel, GraphDatabaseApi graph) {
		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			values.forEach(v -> {
				Node tmp = graph.get().findNode(existingLabel, indexProp, v);
				if (tmp != null) {	
					tmp.addLabel(newLabel);
				} else {
					logger.warn("No {} found for {} with value of {}", existingLabel.name(),indexProp,v.toString());
				}
			});
			tx.success();
		}
	}
	
	private static Node mergeNodes(Node tmp1, Node tmp2, Transaction tx) {
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
		return tmp1;
	}
	
	
	public static List<Node> mapEntriesToNode(PubMedEntries entries, GraphDatabaseApi graph, LocalDate earliest, LocalDate latest, Label... additional) {

		List<Node> out = new ArrayList<>();

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			
			entries.stream().forEach(entry -> {
				Node tmp = null;
				Node tmp1 = entry.getPMID().isPresent() ? graph.get().findNode(ARTICLE, "pmid", entry.getPMID().get()) : null;
				Node tmp2 = entry.getDoi().isPresent() ? graph.get().findNode(ARTICLE, "doi", entry.getDoi().get().toLowerCase()) : null;
				//Node tmp3 = entry.getPMCID().isPresent() ? graph.get().findNode(ARTICLE, "pmcid", entry.getPMCID().get()) : null;
				
				if (tmp1 == null && tmp2 == null) {// && tmp3 == null) {
					logger.debug("Creating new article record: pmid"+entry.getPMID().orElse("none")+" doi:"+entry.getDoi().orElse("none"));
					Node newNode = graph.get().createNode(ARTICLE);
					entry.getPMID().ifPresent(pmid -> newNode.setProperty("pmid", pmid));
					entry.getDoi().ifPresent(doi -> newNode.setProperty("doi", doi.toLowerCase()));
					entry.getPMCID().ifPresent(pmcid -> newNode.setProperty("pmcid", pmcid));
					tmp = newNode;
				} else {
				
					//Merge pubmed and doi stubs
					if (tmp1 != null && tmp2 != null && tmp1.getId() != tmp2.getId()) {
						logger.debug("Merging article pubmed: "+entry.getPMID().get()+" with doi: "+entry.getDoi().get());
						//merge tmp1 and tmp2
						entry.getPMCID().ifPresent(pmcid -> tmp1.setProperty("pmcid", pmcid));
						tmp = mergeNodes(tmp1,tmp2,tx);
					} 
					
					/*
					//Merge pubmed and pmc stubs
					if (tmp1 != null && tmp3 != null && tmp1.getId() != tmp3.getId()) {
						logger.debug("Merging article pubmed: "+entry.getPMID().get()+" with PMCID: "+entry.getPMCID().get());
						//merge tmp1 and tmp2
						tmp3.getAllProperties().forEach((k,v) -> tmp1.setProperty(k, v));
						tmp3.getRelationships(Direction.INCOMING).forEach(r -> {
							Node other = r.getOtherNode(tmp3);
							Relationship r2 = other.createRelationshipTo(tmp1, r.getType());
							r.getAllProperties().forEach((k,v) -> r2.setProperty(k, v));
							r.delete();
						});
						tmp3.getRelationships(Direction.OUTGOING).forEach(r -> {
							Node other = r.getOtherNode(tmp3);
							Relationship r2 = tmp1.createRelationshipTo(other, r.getType());
							r.getAllProperties().forEach((k,v) -> r2.setProperty(k, v));
							r.delete();
						});
						entry.getDOI().ifPresent(pmcid -> tmp1.setProperty("pmcid", pmcid));
						tmp3.getLabels().forEach(l -> tmp1.addLabel(l));
						tmp3.delete();
						tmp = tmp1;
					}
					*/
					
					if (tmp == null) {
						if (tmp1!=null) {
							logger.debug("Updating pubmed article: "+entry.getPMID().get());
							entry.getDoi().ifPresent(doi -> tmp1.setProperty("doi", doi.toLowerCase()));
							entry.getPMCID().ifPresent(pmcid -> tmp1.setProperty("pmcid", pmcid));
							tmp = tmp1;
						} else if (tmp2!=null) {
							logger.debug("Updating doi article: "+entry.getDoi().get());
							entry.getPMID().ifPresent(pmid -> tmp2.setProperty("pmid", pmid));
							entry.getPMCID().ifPresent(pmcid -> tmp2.setProperty("pmcid", pmcid));
							tmp = tmp2;
						/*} else if (tmp3!=null) {
							logger.debug("Updating pmc article: "+entry.getPMCID().get());
							entry.getDoi().ifPresent(doi -> tmp3.setProperty("doi", doi));
							entry.getPMID().ifPresent(pmid -> tmp3.setProperty("pmid", pmid));
							tmp = tmp3;*/
						} else {
							//cannot happen because of check at beginning
						}
					}
				}
				Node node = tmp;
				
				entry.getPMCID().ifPresent(pmc -> node.setProperty("pmcid", pmc));
				entry.getPubMedDate().ifPresent(dt -> {
					node.setProperty("date", dt);
					/*if (dt.isAfter(earliest) && dt.isBefore(latest)) {
						node.addLabel(EXPAND);
					} else {
						logger.debug("not expanding: date="+dt.format(DateTimeFormatter.ISO_LOCAL_DATE)+" title="+entry.getTitle() );
					}*/
				});
				node.setProperty("abstract", entry.getAbstract());
				node.setProperty("title", entry.getTitle());
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				node.removeLabel(PMCENTRAL_STUB);
				
				Arrays.asList(additional).forEach(l -> node.addLabel(l));
								
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
		return mapAuthorToNode(
				author.getIdentifier(),
				author.lastName(),author.firstName(), author.initials(), author.affiliations().collect(Collectors.toList()), graph, tx);
	}
		
	public static Optional<Node> mapAuthorToNode(Contributor author, GraphDatabaseApi graph, Transaction tx) {
		return mapAuthorToNode(
				author.getIdentifier(),
				author.family, author.given, Optional.empty(),
				author.affiliation.stream().flatMap(af -> af.name.stream()).collect(Collectors.toList()), graph, tx
				);
	}
	
	
	public static Optional<Node> mapAuthorToNode(String identifier, Optional<String> lastName, Optional<String> firstName, Optional<String> initials, List<String> affiliations, GraphDatabaseApi graph, Transaction tx) {

		Node out = null;

			Node node = doMerge(AUTHOR, "identifier", identifier, graph.get());
			firstName.ifPresent(fn -> node.setProperty("firstName", fn));
			lastName.ifPresent(fn -> node.setProperty("lastName", fn));
			initials.ifPresent(fn -> node.setProperty("initials", fn));
			if (!affiliations.isEmpty()) node.setProperty("affiliations", affiliations.toArray(new String[] {}));
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
	
	public static List<Relationship> mapCrossRefReferences(String citingDoi, List<Reference> citedDois, GraphDatabaseApi graph) {
		return mapHasReferences("doi",citingDoi,DOI_STUB,"doi",citedDois,DOI_STUB, HAS_REFERENCE,graph);
	}
	
	public static List<Relationship> mapHasReferences(String citingType, String citingDoi, Label citingStubLabel, String citedType, List<Reference> citedDois, Label citedStubLabel, RelationshipType relType, GraphDatabaseApi graph) {
		List<Relationship> out = new ArrayList<>();
			
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			//Node start = 
					doMerge(ARTICLE, citingType, citingDoi.toLowerCase(), graph.get(), citingStubLabel);
			citedDois.forEach(cite -> {
				cite.DOI.ifPresent(citedDoi -> {
					
					Node end = doMerge(ARTICLE, citedType, citedDoi.toLowerCase(),graph.get(), citedStubLabel);
					/*Relationship tmp = null;
					for (Relationship r :start.getRelationships(Direction.OUTGOING,relType)) {
						if (r.getEndNode().equals(end)) tmp=r;
					};
					if (tmp == null) {
						tmp = start.createRelationshipTo(end, relType);						
					}*/
					Relationship tmp = doMerge(ARTICLE, citingType, citingDoi.toLowerCase() ,relType, ARTICLE, citedType, citedDoi.toLowerCase(),graph.get() );
					tmp.setProperty("crossref", true);
					if (end.hasLabel(citedStubLabel)) {
						cite.articleTitle.ifPresent(t -> end.setProperty("title", t));
					}
					out.add(tmp);
				});
			});
			tx.success();
		}
		logger.debug("Adding {}:{} <-{}- {}:{}}",citedDois.size(),citedStubLabel,relType,citingDoi,citingType);
		return out;
	}
	
	public static Set<String> mapCermineReferences(String citingDoi, Set<Work> citedDois, GraphDatabaseApi graph) {
		return citedDois.stream().flatMap(work -> {
			return mapCermineReference("doi",citingDoi,DOI_STUB,"doi",work,DOI_STUB, HAS_REFERENCE,graph).stream();
		}).collect(Collectors.toSet());
	}
	
	public static Optional<String> mapCermineReference(String citingType, String citingDoi, Label citingStubLabel, String citedType, Work cite, Label citedStubLabel, RelationshipType relType, GraphDatabaseApi graph) {
		updateCrossRefMetadata(cite, graph);
		Optional<String> out = Optional.empty();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			//Node start = 
			doMerge(ARTICLE, citingType, citingDoi.toLowerCase(), graph.get(), citingStubLabel);
			out = cite.DOI.map(citedDoi -> {
					Relationship tmp = doMerge(ARTICLE, citingType, citingDoi.toLowerCase() ,relType, ARTICLE, citedType, citedDoi.toLowerCase(),graph.get() );
					tmp.setProperty("cermine", true);
					return citedDoi;
			});
			tx.success();
		}
		return out;
		
	}

	public static List<Relationship> mapPubmedRelated(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, "pmid", PMID_STUB, "pmid", PMID_STUB, HAS_RELATED, graph, false);
	}
	
	public static List<Relationship> mapPubMedCentralReferences(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, "pmid", PMID_STUB, "pmid", PMID_STUB, HAS_REFERENCE, graph, false);
	}
	
	public static List<Relationship> mapPubMedCentralCitedBy(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, "pmid", PMID_STUB, "pmid", PMID_STUB, HAS_REFERENCE, graph, true);
	}
	
	public static List<Relationship> mapEntrez(List<Link> links, String inIdType, Label inLabel, String outIdType, Label outLabel, RelationshipType relType, GraphDatabaseApi graph, boolean invert) {
		logger.info("Adding {} entries {}:{} <-{}- {}:{}",links.size(), outIdType, outLabel, relType, inIdType, inLabel);
		List<Relationship> out = new ArrayList<>();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);

			links.forEach(link -> { 
				link.toId.ifPresent(toId -> {
					//Node start = 
							doMerge(ARTICLE, inIdType, link.fromId, graph.get(), inLabel);
					//Node end = 
							doMerge(ARTICLE, outIdType, toId, graph.get(), outLabel);
					Relationship tmp = null;
					if (invert) {
						/*
						for (Relationship r :end.getRelationships(Direction.OUTGOING,relType)) {
							if (r.getEndNode().equals(start)) {
								tmp=r;
								break;
							}
						};
						if (tmp == null) {
							tmp = end.createRelationshipTo(start, relType);						
						}*/
						tmp = doMerge(ARTICLE, outIdType, toId, relType, ARTICLE, inIdType, link.fromId, graph.get());
					} else {
						/*for (Relationship r :start.getRelationships(Direction.OUTGOING,relType)) {
							if (r.getEndNode().equals(end)) {
								tmp=r;
								break;
							}
						};
						if (tmp == null) {
							tmp = start.createRelationshipTo(end, relType);						
						}*/
						tmp = doMerge(ARTICLE, inIdType, link.fromId, relType, ARTICLE, outIdType, toId, graph.get());
					}
					if (link.score.isPresent()) tmp.setProperty("relatedness", link.score.get());
					tmp.setProperty("entrez", true);
				});
			});
			tx.success();
		}

		return out;
	}
	
	public static Optional<String> updateCrossRefMetadata(Work work, GraphDatabaseApi graph) {
		if (work.DOI.isPresent()) {
			
			try (Transaction tx = graph.get().beginTx()) {
				tx.acquireWriteLock(lockNode);

				Node node = doMerge(ARTICLE, "doi", work.DOI.get().toLowerCase(), graph.get());
				node.setProperty("title", work.title.stream().collect(Collectors.joining("\n")));
				work.author.forEach(as -> {
					Optional<Node> targetNode = mapAuthorToNode(as, graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, HAS_AUTHOR));
				});
				work.journalAbstract.ifPresent(abs -> node.setProperty("abstract", abs));
				if (work.publishedOnline.isPresent()) {
					work.publishedOnline.ifPresent(po -> {
					try {
						node.setProperty("date",LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
					} catch (Exception e) {
						// date is not well formed
					}
				});
				} else if (work.publishedPrint.isPresent()) {
				work.publishedPrint.ifPresent(po -> {
					try {
						node.setProperty("date",LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
					} catch (Exception e) {
						// date is not well formed
					}
				});
				} else if (work.issued.isPresent()) {
					work.issued.ifPresent(po -> {
						try {
							node.setProperty("date",LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
						} catch (Exception e) {
							// date is not well formed
						}
					});
				}
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				node.removeLabel(PMCENTRAL_STUB);
				tx.success();
			}

			return Optional.of(work.DOI.get().toLowerCase());
		}
		return Optional.empty();
	}

	public static Optional<String> updateUnpaywallMetadata(Result work, GraphDatabaseApi graph) {
		if (work.doi.isPresent()) {
			try (Transaction tx = graph.get().beginTx()) {
				tx.acquireWriteLock(lockNode);
				Node node = doMerge(ARTICLE, "doi", work.doi.get().toLowerCase(), graph.get());
				work.pdfUrl().ifPresent(url -> node.setProperty("pdfUrl", url));
				work.getPublishedDate().ifPresent(date -> node.setProperty("date", date));
				work.title.ifPresent(title -> node.setProperty("title", title));
				node.removeLabel(DOI_STUB);
				node.removeLabel(PMID_STUB);
				node.removeLabel(PMCENTRAL_STUB);
				tx.success();
			}
			return Optional.of(work.doi.get().toLowerCase());
		}
		return Optional.empty();
	}
	
	public static Optional<String> updatePdfLink(Result work, GraphDatabaseApi graph) {
		if (work.doi.isPresent() && work.pdfUrl().isPresent()) {
			try (Transaction tx = graph.get().beginTx()) {
				tx.acquireWriteLock(lockNode);
				Node node = doMerge(ARTICLE, "doi", work.doi.get().toLowerCase(), graph.get());
				node.setProperty("pdfUrl", work.pdfUrl().get());
				tx.success();
			}
			return Optional.of(work.doi.get().toLowerCase());
		}
		return Optional.empty();
	}
	
	public static Set<String> lookupDoisForUnreferenced(GraphDatabaseApi graph) {
		Set<String> out = new HashSet<>();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			ResourceIterator<String> resultIterator = graph.get().execute("MATCH (source:Expand) WHERE NOT (source)-[:HAS_REFERENCE]->() AND source.doi IS NOT NULL RETURN source.doi AS out").columnAs("out"); 
			resultIterator.forEachRemaining(doi -> out.add(doi));
			tx.success();
		}
		return out;
	}; 
	
}
