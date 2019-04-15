use omop

select relationship_id, count(*) as num from concept_relationship
group by relationship_id order by num desc


select c1.vocabulary_id as source, c2.domain_id as target, count(*) as mappings from concept_relationship r, concept c1, concept c2
where relationship_id like 'Maps to' and r.concept_id_1 = c1.concept_id and r.concept_id_2 = c2.concept_id
group by c1.vocabulary_id, c2.domain_id order by mappings desc


select c1.*, r.relationship_id, c2.* from concept_relationship r, concept c1, concept c2
where r.concept_id_1 = c1.concept_id and r.concept_id_2 = c2.concept_id
and c1.vocabulary_id = 'HES Specialty'

select c1.vocabulary_id, count(*) as number from concept c1 group by c1.vocabulary_id order by number desc

select c1.domain_id, count(*) as number from concept c1 group by c1.domain_id order by number desc


-- #############################################

use omopBuild

DECLARE @indexInput2 AS dbo.IndexInputType;
INSERT INTO @indexInput2 
SELECT c.concept_id, c.concept_name FROM 
[omop].[dbo].[concept] c
where c.domain_id = 'Procedure'

EXEC dbo.fn4gramIndex 4, @indexInput2

DECLARE @bulkSearchInput2 AS dbo.BulkSearchInputType
INSERT INTO @bulkSearchInput2
SELECT c.concept_id, c.concept_name FROM 
[omop].[dbo].[concept] c
where c.vocabulary_id = 'OPCS4'

DROP TABLE IF EXISTS #tmpBulkSearchOutput2
SELECT * 
INTO #tmpBulkSearchOutput2
FROM dbo.fnBulkSearch4Gram(
	@bulkSearchInput2,
	4,0,20)


SELECT 
	s.concept_id, s.concept_name, m.similarity, m.concept_id, c.concept_name
INTO
	RobsDatabase.dbo.mappingTestOPCS4
FROM 
[omop].[dbo].[concept] s LEFT OUTER JOIN
#tmpBulkSearchOutput2 m ON s.concept_id = m.local_id
LEFT OUTER JOIN [omop].[dbo].[concept] c on c.concept_id = m.concept_id
order by s.concept_code ASC, similarity DESC

SELECT s.concept_id, s.concept_name, c.concept_id, c.concept_name
INTO
	RobsDatabase.dbo.mappingGoldStandardOPCS4
FROM
[omop].[dbo].[concept] s 
LEFT OUTER JOIN [omop].[dbo].[concept_relationship] r ON (r.concept_id_1 = s.concept_id  and r.relationship_id like 'Maps to')
LEFT OUTER JOIN [omop].[dbo].[concept] c on (r.concept_id_2 = c.concept_id and c.domain_id like 'Procedure')
WHERE s.vocabulary_id = 'OPCS4'

