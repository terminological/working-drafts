﻿/****** Script for SelectTopNRows command from SSMS  ******/
--SELECT [Procedure Code System], COUNT(*)
--FROM  [TriNetX].[dbo].[tblTriNetXProcedure]
--GROUP BY [Procedure Code System];

-- ----------------------------------------------------
-- get mappings for OPCS4 procedure codes
-- These will be mapped into the procedure occcurence table
-- as main operations


DROP TABLE IF EXISTS #tmpProcedureMap

CREATE TABLE #tmpProcedureMap (
procedure_source_value VARCHAR(50), 
procedure_concept_id INT,
procedure_source_concept_id INT
INDEX X_procedure_code (procedure_source_value))


-- Get source procedure maps from the concept vocabularies for 
INSERT INTO #tmpProcedureMap
SELECT
	REPLACE(concept_code,'.','') as procedure_source_value,
	IIF(c.standard_concept='S',c.concept_id,r.concept_id_2) as procedure_concept_id,
	c.concept_id as procedure_source_concept_id
FROM 
	[omop].[dbo].[concept] c
	LEFT JOIN [omop].[dbo].[concept_relationship] r 
	ON c.concept_id = r.concept_id_1 AND r.relationship_id = 'Maps to'
WHERE 
	vocabulary_id LIKE 'OPCS4' AND
	c.domain_id = 'Procedure' AND
	IIF(c.standard_concept='S',c.concept_id,r.concept_id_2) IS NOT NULL
;

-- TODO: insert manually inserted maps from omopBuild

-- TODO: Check used procedure codes can be mapped to these
-- insert into omopBuild if not



-- ----------------------------------------------------
-- get mappings for OPCS4 procedure codes
-- These will be mapped into the procedure occcurence table
-- as qualifier


DROP TABLE IF EXISTS #tmpModifierMap

CREATE TABLE #tmpModifierMap (
modifier_source_value VARCHAR(50), 
modifier_concept_id INT,
modifier_source_concept_id INT,
INDEX X_modifier_code (modifier_source_value)
)

INSERT INTO #tmpModifierMap
SELECT
	REPLACE(concept_code,'.',''),
	IIF(c.standard_concept='S',c.concept_id,r.concept_id_2),
	c.concept_id
FROM 
	[omop].[dbo].[concept] c
	LEFT JOIN [omop].[dbo].[concept_relationship] r 
	ON c.concept_id = r.concept_id_1 AND r.relationship_id = 'Maps to'
WHERE 
	vocabulary_id LIKE 'OPCS4' AND
	c.domain_id <> 'Procedure' AND
	IIF(c.standard_concept='S',c.concept_id,r.concept_id_2) IS NOT NULL
;

INSERT INTO #tmpModifierMap
SELECT DISTINCT
	sourceId,
	omopConceptId,
	0 as sourceConceptId
FROM 
	omopBuild.dbo.ConceptMapping
WHERE 
	sourceDomain LIKE 'urn:code-system:OPCS4'
	AND omopDomainId = 'Procedure'
;

-- ----------------------------------------------------
-- So what is not mapped?

SELECT 
	'OPCS4' as sourceDomain,
	x.[Procedure Code] as sourceId,
	x.[Procedure Description] as sourceTerm,
	0 as certainty,
	0 as reviewStatus,
	'Procedure' as omopDomainId,
	0 as omopConceptId,
	'Unknown' as omopConceptName,
	x.number as usedCount
FROM (
	SELECT [Procedure Code], [Procedure Description], Count(*) as number
		FROM [TriNetX].[dbo].[tblTriNetXProcedure] p
		GROUP BY [Procedure Code], [Procedure Description]
		) x 
	LEFT JOIN #tmpProcedureMap pm ON x.[Procedure Code] collate Latin1_General_CI_AS = pm.procedure_source_value
	LEFT JOIN #tmpModifierMap mm ON x.[Procedure Code] collate Latin1_General_CI_AS = mm.modifier_source_value
	WHERE mm.modifier_concept_id IS NULL AND pm.procedure_concept_id IS NULL

-- ----------------------------------------------------
-- Map Trinetx table into a data structure that we can operate on
-- losing the duplicates generated by provider id.

DROP TABLE IF EXISTS #tmpProcedure;

CREATE TABLE #tmpProcedure (
	procedure_occurrence_id BIGINT PRIMARY KEY,
	person_id BIGINT NULL,
	visit_occurrence_id BIGINT NULL,
	procedure_source_value [varchar](50) NULL,
	description [varchar](500) NULL,
	adj_proc_date DATETIME NULL
)
GO

INSERT INTO #tmpProcedure
SELECT 
	procedure_occurrence_id,
	person_id,
	visit_occurrence_id,
	procedure_source_value,
	description,
	adj_proc_date
FROM 
	( SELECT
		omopBuild.dbo.getId(p.original_row,'tPRO') AS procedure_occurrence_id
		,s.groupId as person_id
		,omopBuild.dbo.getId(CONVERT(INT,[Encounter id]),'tENC') as visit_occurrence_id
		,[Procedure Code] as procedure_source_value
		,[Procedure Description] as description
		,CONVERT(DATETIME,[Procedure Date],112)+s.dateOffset as adj_proc_date
		,ROW_NUMBER() OVER(PARTITION BY 
			p.[Patient id],
			p.[Encounter id],
			p.[Procedure Code],
			p.[Procedure Date] ORDER BY original_row) as provider
	FROM 
		(SELECT *,
		ROW_NUMBER() OVER(ORDER BY (SELECT NULL)) as original_row
		FROM [TriNetX].[dbo].[tblTriNetXProcedure]) p
		INNER JOIN omopBuild.dbo.TrinetxLookup l on l.[Patient id]=p.[Patient id] COLLATE Latin1_General_CI_AS
		INNER JOIN omopBuild.dbo.StudyPopulation s on s.groupId = l.groupId
	) t
	WHERE t.provider = 1
	ORDER BY procedure_occurrence_id
GO

-- ----------------------------------------------------
-- IMplementation note: you have to find out what is the actual procedure and 
-- what is a qualifier. We can incorporate one qualifier as a procedure "modifier"
-- laterality is seen as a procedure .

DROP TABLE IF EXISTS  #tmpProcedure2;

SELECT
	p.*,
	CASE WHEN EXISTS(SELECT * FROM #tmpProcedureMap pm WHERE p.procedure_source_value=pm.procedure_source_value)
	THEN 1 ELSE 0 END AS isProcCode,
	CASE WHEN EXISTS(SELECT * FROM #tmpModifierMap mm WHERE p.procedure_source_value=mm.modifier_source_value)
	THEN 1 ELSE 0 END AS isAnatCode,
	IIF(  
		p.procedure_source_value like 'O%' OR
		p.procedure_source_value like 'Y%' OR
		p.procedure_source_value like 'Z%',
		1,0) isQualifier
INTO #tmpProcedure2
FROM
#tmpProcedure p 


-- ----------------------------------------------------

SELECT TOP(100) *
FROM #tmpProcedure2 
order by procedure_occurrence_id;

-- ----------------------------------------------------

DELETE FROM omop.dbo.procedure_occurrence;

INSERT INTO omop.dbo.procedure_occurrence
SELECT 
	-- TOP (1000) 
	x.procedure_occurrence_id,
	x.person_id,
	pm.procedure_concept_id,
	CONVERT(DATE,x.adj_proc_date) AS procedure_date,
	CONVERT(DATETIME2,x.adj_proc_date) AS procedure_datetime,
	44786630 AS procedure_type_concept_id,
	ISNULL(mm.modifier_concept_id,0) AS modifier_concept_id,
	1 as quantity,
	NULL as provider_id, -- TODO: is present is trinetx extract but needs cleaning and policy about responsible clinician vs all staff
	x.visit_occurrence_id,
	NULL as visit_detail_id, -- TODO: where a procedure is done by multiple providers you could implement this with a nested set of visit_details
	x.procedure_source_value,
	pm.procedure_source_concept_id,
	x.modifier_source_value --,
	--p.*
FROM (
	SELECT
		t1.procedure_occurrence_id,
		t1.person_id,
		t1.visit_occurrence_id,
		t1.procedure_source_value,
		t1.adj_proc_date,
		t2.procedure_source_value AS modifier_source_value
	FROM 
		#tmpProcedure2 t1
		LEFT JOIN #tmpProcedure2 t2 ON (t1.person_id = t2.person_id
		AND t1.visit_occurrence_id = t2.visit_occurrence_id
		AND t1.adj_proc_date = t1.adj_proc_date
		AND t2.isQualifier = 1
		AND t1.procedure_occurrence_id-1 <= t2.procedure_occurrence_id
		AND t1.procedure_occurrence_id+4 >= t2.procedure_occurrence_id
		)
	WHERE
		t1.isProcCode = 1 AND t1.isAnatCode = 0 and t1.isQualifier = 0
	) x 
	INNER JOIN #tmpProcedureMap pm ON pm.procedure_source_value = x.procedure_source_value
	LEFT JOIN #tmpModifierMap mm ON mm.modifier_source_value = x.modifier_source_value
ORDER BY x.procedure_occurrence_id;

-- ----------------------------------------------------
-- LIMITATIONS:
-- Not all OPCS4 qualifier values are represented in the final data set.
-- An operation which specified a site and a lateratlity for example.
-- OPCS4 heavy use of qualifiers
-- Multiple providers tricky to model.

-- ----------------------------------------------------
--Procedure Type codes
--concept_id	concept_name
--44786630	Primary Procedure
--44786631	Secondary Procedure

DROP TABLE IF EXISTS #tmpProcedureMap;
DROP TABLE IF EXISTS #tmpModifierMap;
DROP TABLE IF EXISTS  #tmpProcedure2;
