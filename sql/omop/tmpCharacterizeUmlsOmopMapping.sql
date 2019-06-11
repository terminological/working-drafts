SELECT DISTINCT SAB FROM UmlsMRCONSO

SELECT count(DISTINCT u.CODE) FROM UmlsMRCONSO u, omop.dbo.concept c where u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code AND standard_concept = 'S'
SELECT count(DISTINCT c.concept_id) FROM UmlsMRCONSO u, omop.dbo.concept c where u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code AND standard_concept = 'S'
-- 432033
-- 295449 standard omop concepts

SELECT count(DISTINCT CODE) FROM  UmlsMRCONSO where SAB='SNOMEDCT_US'
-- 432811

SELECT count(DISTINCT concept_code) FROM  omop.dbo.concept where vocabulary_id='SNOMED' AND standard_concept = 'S'
-- 861732
-- 529381 standard concept

SELECT TOP(100) c.* FROM omop.dbo.concept c LEFT OUTER JOIN  UmlsMRCONSO u ON u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code WHERE u.CODE IS NULL AND c.vocabulary_id='SNOMED'

SELECT u.* FROM UmlsMRCONSO u LEFT OUTER JOIN omop.dbo.concept c ON u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code WHERE c.concept_code IS NULL AND u.SAB='SNOMEDCT_US'

select count(*) from (
SELECT distinct u.CUI --, STRING_AGG(c.concept_name,'|')
--,c.*,c.concept_id,c.concept_name 
FROM UmlsMRCONSO u, omop.dbo.concept c 
where u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code AND standard_concept = 'S'
GROUP BY u.CUI HAVING COUNT(*)>2
) tmp
-- 143138 concepts with multiple matches (CUI to OMOP standard concept)


select count(*) from (
SELECT distinct c.concept_id --, STRING_AGG(c.concept_name,'|')
--,c.*,c.concept_id,c.concept_name 
FROM UmlsMRCONSO u, omop.dbo.concept c 
where u.SAB='SNOMEDCT_US' AND c.vocabulary_id='SNOMED' AND u.CODE = c.concept_code AND standard_concept = 'S'
GROUP BY c.concept_id HAVING COUNT(*)>2
) tmp
-- 144860 concepts with multiple matches (OMOP standard concept to CUI)

-- mapping stats
select
*,
total_omop-unmapped_omop as mapped_omop,
total_umls-unmapped_umls as mapped_umls
FROM (
	select 

	count(distinct concept_id) as total_omop,
	count(distinct CUI) as total_umls,
	sum(iif(CUI IS NULL,1,0)) as unmapped_omop,
	sum(iif(concept_id IS NULL,1,0)) as unmapped_umls,
	sum(iif(concept_id is not null and CUI is not null,1,0)) as mappings

	from (

		SELECT distinct c.concept_id, u.CUI
		FROM 
			(
			SELECT * FROM UmlsMRCONSO WHERE SAB='SNOMEDCT_US'
			) u
			FULL OUTER JOIN 
			(
			SELECT * FROM omop.dbo.concept WHERE vocabulary_id='SNOMED' AND standard_concept = 'S'
			) c
		ON u.CODE = c.concept_code

	) tmp
) tmp2

-- mapping breakdown
SELECT
	mapping_count,
	count(*) as cuis_count
FROM 
	(
		SELECT sum(iif(concept_id IS NOT NULL,1,0)) as mapping_count FROM 
		(
			SELECT distinct c.concept_id, u.CUI
			FROM 
				(
				SELECT * FROM UmlsMRCONSO WHERE SAB='SNOMEDCT_US'
				) u
				FULL OUTER JOIN 
				(
				SELECT * FROM omop.dbo.concept WHERE vocabulary_id='SNOMED' AND standard_concept = 'S'
				) c
			ON u.CODE = c.concept_code
		) tmp
		WHERE CUI IS NOT NULL
		GROUP BY CUI
	) tmp2
GROUP BY mapping_count

-- mapping breakdown
SELECT
	mapping_count,
	count(*) as omop_concept_count
FROM 
	(
		SELECT sum(iif(CUI IS NOT NULL,1,0)) as mapping_count FROM 
		(
			SELECT distinct c.concept_id, u.CUI
			FROM 
				(
				SELECT * FROM UmlsMRCONSO WHERE SAB='SNOMEDCT_US'
				) u
				FULL OUTER JOIN 
				(
				SELECT * FROM omop.dbo.concept WHERE vocabulary_id='SNOMED' AND standard_concept = 'S'
				) c
			ON u.CODE = c.concept_code
		) tmp
		WHERE concept_id IS NOT NULL
		GROUP BY concept_id
	) tmp2
GROUP BY mapping_count

