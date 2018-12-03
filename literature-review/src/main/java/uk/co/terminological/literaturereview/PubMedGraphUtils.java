package uk.co.terminological.literaturereview;


import uk.co.terminological.literaturereview.PubMedGraphSchema.Labels;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Rel;
import uk.co.terminological.literaturereview.PubMedGraphSchema.Prop;

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
import java.util.stream.Stream;

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
import uk.co.terminological.pubmedclient.EntrezResult.PubMedEntry;
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
	
	
	public static List<Node> mapEntriesToNode(Stream<PubMedEntry> entries, GraphDatabaseApi graph, LocalDate earliest, LocalDate latest, Label... additional) {

		List<Node> out = new ArrayList<>();

		try ( Transaction tx = graph.get().beginTx() ) {
			tx.acquireWriteLock(lockNode);
			
			entries.forEach(entry -> {
				Node tmp = null;
				Node tmp1 = entry.getPMID().isPresent() ? graph.get().findNode(Labels.ARTICLE, Prop.PMID, entry.getPMID().get()) : null;
				Node tmp2 = entry.getDoi().isPresent() ? graph.get().findNode(Labels.ARTICLE, Prop.DOI, entry.getDoi().get().toLowerCase()) : null;
				//Node tmp3 = entry.getPMCID().isPresent() ? graph.get().findNode(ARTICLE, Prop.PMCID, entry.getPMCID().get()) : null;
				
				if (tmp1 == null && tmp2 == null) {// && tmp3 == null) {
					logger.debug("Creating new article record: pmid"+entry.getPMID().orElse("none")+" doi:"+entry.getDoi().orElse("none"));
					Node newNode = graph.get().createNode(Labels.ARTICLE);
					entry.getPMID().ifPresent(pmid -> newNode.setProperty(Prop.PMID, pmid));
					entry.getDoi().ifPresent(doi -> newNode.setProperty(Prop.DOI, doi.toLowerCase()));
					entry.getPMCID().ifPresent(pmcid -> newNode.setProperty(Prop.PMCID, pmcid));
					tmp = newNode;
				} else {
				
					//Merge pubmed and doi stubs
					if (tmp1 != null && tmp2 != null && tmp1.getId() != tmp2.getId()) {
						logger.debug("Merging article pubmed: "+entry.getPMID().get()+" with doi: "+entry.getDoi().get());
						//merge tmp1 and tmp2
						entry.getPMCID().ifPresent(pmcid -> tmp1.setProperty(Prop.PMCID, pmcid));
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
						entry.getDOI().ifPresent(pmcid -> tmp1.setProperty(Prop.PMCID, pmcid));
						tmp3.getLabels().forEach(l -> tmp1.addLabel(l));
						tmp3.delete();
						tmp = tmp1;
					}
					*/
					
					if (tmp == null) {
						if (tmp1!=null) {
							logger.debug("Updating pubmed article: "+entry.getPMID().get());
							entry.getDoi().ifPresent(doi -> tmp1.setProperty(Prop.DOI, doi.toLowerCase()));
							entry.getPMCID().ifPresent(pmcid -> tmp1.setProperty(Prop.PMCID, pmcid));
							tmp = tmp1;
						} else if (tmp2!=null) {
							logger.debug("Updating doi article: "+entry.getDoi().get());
							entry.getPMID().ifPresent(pmid -> tmp2.setProperty(Prop.PMID, pmid));
							entry.getPMCID().ifPresent(pmcid -> tmp2.setProperty(Prop.PMCID, pmcid));
							tmp = tmp2;
						/*} else if (tmp3!=null) {
							logger.debug("Updating pmc article: "+entry.getPMCID().get());
							entry.getDoi().ifPresent(doi -> tmp3.setProperty(Prop.DOI, doi));
							entry.getPMID().ifPresent(pmid -> tmp3.setProperty(Prop.PMID, pmid));
							tmp = tmp3;*/
						} else {
							//cannot happen because of check at beginning
						}
					}
				}
				Node node = tmp;
				
				entry.getPMCID().ifPresent(pmc -> node.setProperty(Prop.PMCID, pmc));
				entry.getPMCPdfUrl().ifPresent(url -> node.setProperty(Prop.PDF_URL, url));
				entry.getPubMedDate().ifPresent(dt -> {
					node.setProperty(Prop.DATE, dt);
					/*if (dt.isAfter(earliest) && dt.isBefore(latest)) {
						node.addLabel(EXPAND);
					} else {
						logger.debug("not expanding: date="+dt.format(DateTimeFormatter.ISO_LOCAL_DATE)+" title="+entry.getTitle() );
					}*/
				});
				node.setProperty(Prop.ABSTRACT, entry.getAbstract());
				node.setProperty(Prop.TITLE, entry.getTitle());
				node.removeLabel(Labels.DOI_STUB);
				node.removeLabel(Labels.PMID_STUB);
				node.removeLabel(Labels.PMCENTRAL_STUB);
				
				Arrays.asList(additional).forEach(l -> node.addLabel(l));
					
				entry.getKeyWords().forEach(kw -> {
					Node target = doMerge(Labels.KEYWORD, Prop.TERM, kw, graph.get());
					node.createRelationshipTo(target, Rel.HAS_KEYWORD);
				});
				entry.getAuthors().forEach(au -> {
					Optional<Node> targetNode = mapAuthorToNode(au,graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, Rel.HAS_AUTHOR));
				});
				entry.getMeshHeadings().forEach(mh -> {
					Optional<Node> targetNode = mapMeshCodeToNode(mh.getDescriptor(),graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, Rel.HAS_MESH));
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
				author.lastName(),
				author.firstName(), 
				author.initials(), 
				author.affiliations().collect(Collectors.toSet()),
				author.orcid(), graph, tx)
				;
	}
		
	public static Optional<Node> mapAuthorToNode(Contributor author, GraphDatabaseApi graph, Transaction tx) {
		return mapAuthorToNode(
				author.getIdentifier(),
				author.family, author.given, Optional.empty(),
				author.affiliation.stream().flatMap(af -> af.name.stream()).collect(Collectors.toSet()),
				author.ORCID.map(url -> url.toString()), graph, tx
				);
	}
	
	
	public static Optional<Node> mapAuthorToNode(String identifier, Optional<String> lastName, Optional<String> firstName, Optional<String> initials, Set<String> affiliations, Optional<String> orcid, GraphDatabaseApi graph, Transaction tx) {

		Node out = null;

			Node node = graph.get().createNode(Labels.AUTHOR);// doMerge(AUTHOR, "identifier", identifier, graph.get());
			firstName.ifPresent(fn -> node.setProperty(Prop.FIRST_NAME, fn));
			lastName.ifPresent(fn -> node.setProperty(Prop.LAST_NAME, fn));
			initials.ifPresent(fn -> node.setProperty(Prop.INITIALS, fn));
			orcid.ifPresent(fn -> node.setProperty(Prop.ORCID, fn));
			if (!affiliations.isEmpty()) node.setProperty(Prop.AFFILIATIONS, affiliations.toArray(new String[] {}));
			out = node;

		return Optional.ofNullable(out);

	}

	public static Optional<Node> mapMeshCodeToNode(MeshCode meshCode, GraphDatabaseApi graph, Transaction tx) {
		Node out = null;

			Node tmp = doMerge(Labels.MESH_CODE, Prop.CODE, meshCode.getCode(), graph.get());
			tmp.setProperty(Prop.CODE, meshCode.getCode());
			tmp.setProperty(Prop.TERM, meshCode.getTerm());
			out = tmp;

		return Optional.ofNullable(out);
	}
	
	public static List<Relationship> mapCrossRefReferences(String citingDoi, List<Reference> citedDois, GraphDatabaseApi graph) {
		return mapHasReferences(Prop.DOI,citingDoi,Labels.DOI_STUB,Prop.DOI,citedDois,Labels.DOI_STUB, Rel.HAS_REFERENCE,graph);
	}
	
	public static List<Relationship> mapHasReferences(String citingType, String citingDoi, Label citingStubLabel, String citedType, List<Reference> citedDois, Label citedStubLabel, RelationshipType relType, GraphDatabaseApi graph) {
		List<Relationship> out = new ArrayList<>();
			
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			//Node start = 
					doMerge(Labels.ARTICLE, citingType, citingDoi.toLowerCase(), graph.get(), citingStubLabel);
			citedDois.forEach(cite -> {
				cite.DOI.ifPresent(citedDoi -> {
					
					Node end = doMerge(Labels.ARTICLE, citedType, citedDoi.toLowerCase(),graph.get(), citedStubLabel);
					/*Relationship tmp = null;
					for (Relationship r :start.getRelationships(Direction.OUTGOING,relType)) {
						if (r.getEndNode().equals(end)) tmp=r;
					};
					if (tmp == null) {
						tmp = start.createRelationshipTo(end, relType);						
					}*/
					Relationship tmp = doMerge(Labels.ARTICLE, citingType, citingDoi.toLowerCase() ,relType, Labels.ARTICLE, citedType, citedDoi.toLowerCase(),graph.get() );
					tmp.setProperty(Prop.CROSSREF, true);
					if (end.hasLabel(citedStubLabel)) {
						cite.articleTitle.ifPresent(t -> end.setProperty(Prop.TITLE, t));
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
			return mapCermineReference(Prop.DOI,citingDoi,Labels.DOI_STUB,Prop.DOI,work,Labels.DOI_STUB, Rel.HAS_REFERENCE,graph).stream();
		}).collect(Collectors.toSet());
	}
	
	public static Optional<String> mapCermineReference(String citingType, String citingDoi, Label citingStubLabel, String citedType, Work cite, Label citedStubLabel, RelationshipType relType, GraphDatabaseApi graph) {
		updateCrossRefMetadata(cite, graph);
		Optional<String> out = Optional.empty();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);
			//Node start = 
			doMerge(Labels.ARTICLE, citingType, citingDoi.toLowerCase(), graph.get(), citingStubLabel);
			out = cite.DOI.map(citedDoi -> {
					Relationship tmp = doMerge(Labels.ARTICLE, citingType, citingDoi.toLowerCase() ,relType, Labels.ARTICLE, citedType, citedDoi.toLowerCase(),graph.get() );
					tmp.setProperty(Prop.HAS_PDF, true);
					return citedDoi;
			});
			tx.success();
		}
		return out;
		
	}

	public static List<Relationship> mapPubmedRelated(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, Prop.PMID, Labels.PMID_STUB, Prop.PMID, Labels.PMID_STUB, Rel.HAS_RELATED, graph, false);
	}
	
	public static List<Relationship> mapPubMedCentralReferences(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, Prop.PMID, Labels.PMID_STUB, Prop.PMID, Labels.PMID_STUB, Rel.HAS_REFERENCE, graph, false);
	}
	
	public static List<Relationship> mapPubMedCentralCitedBy(List<Link> links, GraphDatabaseApi graph) {
		return mapEntrez(links, Prop.PMID, Labels.PMID_STUB, Prop.PMID, Labels.PMID_STUB, Rel.HAS_REFERENCE, graph, true);
	}
	
	public static List<Relationship> mapEntrez(List<Link> links, String inIdType, Label inLabel, String outIdType, Label outLabel, RelationshipType relType, GraphDatabaseApi graph, boolean invert) {
		logger.info("Adding {} entries {}:{} <-{}- {}:{}",links.size(), outIdType, outLabel, relType, inIdType, inLabel);
		List<Relationship> out = new ArrayList<>();
		try (Transaction tx = graph.get().beginTx()) {
			tx.acquireWriteLock(lockNode);

			links.forEach(link -> { 
				link.toId.ifPresent(toId -> {
					//Node start = 
							doMerge(Labels.ARTICLE, inIdType, link.fromId, graph.get(), inLabel);
					//Node end = 
							doMerge(Labels.ARTICLE, outIdType, toId, graph.get(), outLabel);
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
						tmp = doMerge(Labels.ARTICLE, outIdType, toId, relType, Labels.ARTICLE, inIdType, link.fromId, graph.get());
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
						tmp = doMerge(Labels.ARTICLE, inIdType, link.fromId, relType, Labels.ARTICLE, outIdType, toId, graph.get());
					}
					if (link.score.isPresent()) tmp.setProperty(Prop.RELATEDNESS, link.score.get());
					tmp.setProperty(Prop.ENTREZ, true);
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

				Node node = doMerge(Labels.ARTICLE, Prop.DOI, work.DOI.get().toLowerCase(), graph.get());
				node.setProperty(Prop.TITLE, work.title.stream().collect(Collectors.joining("\n")));
				work.author.forEach(as -> {
					Optional<Node> targetNode = mapAuthorToNode(as, graph, tx);
					targetNode.ifPresent(target -> node.createRelationshipTo(target, Rel.HAS_AUTHOR));
				});
				work.journalAbstract.ifPresent(abs -> node.setProperty(Prop.ABSTRACT, abs));
				if (work.publishedOnline.isPresent()) {
					work.publishedOnline.ifPresent(po -> {
					try {
						node.setProperty(Prop.DATE,LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
					} catch (Exception e) {
						// date is not well formed
					}
				});
				} else if (work.publishedPrint.isPresent()) {
				work.publishedPrint.ifPresent(po -> {
					try {
						node.setProperty(Prop.DATE,LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
					} catch (Exception e) {
						// date is not well formed
					}
				});
				} else if (work.issued.isPresent()) {
					work.issued.ifPresent(po -> {
						try {
							node.setProperty(Prop.DATE,LocalDate.of(po.dateParts.get(0).get(0), po.dateParts.get(0).get(1), po.dateParts.get(0).get(2)));
						} catch (Exception e) {
							// date is not well formed
						}
					});
				}
				node.removeLabel(Labels.DOI_STUB);
				node.removeLabel(Labels.PMID_STUB);
				node.removeLabel(Labels.PMCENTRAL_STUB);
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
				Node node = doMerge(Labels.ARTICLE, Prop.DOI, work.doi.get().toLowerCase(), graph.get());
				work.pdfUrl().ifPresent(url -> node.setProperty(Prop.PDF_URL, url));
				work.getPublishedDate().ifPresent(date -> node.setProperty(Prop.DATE, date));
				work.title.ifPresent(title -> node.setProperty(Prop.TITLE, title));
				node.removeLabel(Labels.DOI_STUB);
				node.removeLabel(Labels.PMID_STUB);
				node.removeLabel(Labels.PMCENTRAL_STUB);
				tx.success();
			}
			return Optional.of(work.doi.get().toLowerCase());
		}
		return Optional.empty();
	}
	
	public static Optional<String> updatePdfLink(Result work, GraphDatabaseApi graph) {
		if (work.doi.isPresent()) {
			try (Transaction tx = graph.get().beginTx()) {
				tx.acquireWriteLock(lockNode);
				Node node = doMerge(Labels.ARTICLE, Prop.DOI, work.doi.get().toLowerCase(), graph.get());
				work.pdfUrl().ifPresent(url -> node.setProperty(Prop.PDF_URL, url));
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
