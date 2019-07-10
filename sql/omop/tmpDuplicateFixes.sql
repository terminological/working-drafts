

-- --------------------------------------
-- DIAGNOSE PROCEDUER DUPLICATION
SELECT TOP(1000) po.* FROM omop.dbo.procedure_occurrence po INNER JOIN (
SELECT DISTINCT procedure_occurrence_id, procedure_concept_id FROM omop.dbo.procedure_occurrence GROUP BY procedure_occurrence_id, procedure_concept_id HAVING COUNT(*)>1
) sel on po.procedure_occurrence_id = sel.procedure_occurrence_id and sel.procedure_concept_id = po.procedure_concept_id
ORDER BY procedure_occurrence_id, procedure_concept_id

SELECT DISTINCT procedure_occurrence_id, procedure_concept_id, modifier_source_value
FROM omop.dbo.procedure_occurrence 
GROUP BY procedure_occurrence_id, procedure_concept_id,  modifier_source_value  HAVING COUNT(*)>1

-- ----------------------------------------
-- FIX procedures
DROP TABLE IF EXISTS procedureIdMigration ;

CREATE TABLE procedureIdMigration (
	new_procedure_occurrence_id bigint IDENTITY(1,1) PRIMARY KEY,
	old_procedure_occurrence_id bigint,
	old_procedure_concept_id int,
	old_modifier_source_value varchar(20)
)

CREATE UNIQUE INDEX X_uniqueProc ON procedureIdMigration (old_procedure_occurrence_id, old_procedure_concept_id, old_modifier_source_value);

INSERT INTO procedureIdMigration (old_procedure_occurrence_id, old_procedure_concept_id, old_modifier_source_value)
SELECT procedure_occurrence_id, procedure_concept_id, modifier_source_value FROM omop.dbo.procedure_occurrence

UPDATE po
SET po.procedure_occurrence_id = pim.new_procedure_occurrence_id 
FROM omop.dbo.procedure_occurrence po, procedureIdMigration pim
WHERE po.procedure_occurrence_id = pim.old_procedure_occurrence_id
AND po.procedure_concept_id = pim.old_procedure_concept_id
AND ( po.modifier_source_value IS NULL or po.modifier_source_value COLLATE Latin1_General_CI_AS = pim.old_modifier_source_value  COLLATE Latin1_General_CI_AS)

-- N.B. this shoudl be fixed by sorting out ids being assigned properly
SELECT TOP(100) * FROM  omop.dbo.procedure_occurrence ORDER BY procedure_occurrence_id DESC

-- ---------------------------------------------
-- DIAGNOSE drug exposure duplicates
SELECT COUNT(*) FROM omop.dbo.drug_exposure po

SELECT * FROM (
SELECT 
	DENSE_RANK() OVER(PARTITION BY po.drug_exposure_id, po.drug_source_concept_id, po.drug_exposure_start_datetime ORDER BY IIF(po.drug_exposure_end_date IS NULL,1,0)) as selector,
	ROW_NUMBER() OVER(PARTITION BY po.drug_exposure_id, po.drug_source_concept_id, po.drug_exposure_start_datetime,po.drug_exposure_end_datetime ORDER BY po.drug_exposure_id) as selector2,
	po.*
FROM omop.dbo.drug_exposure po INNER JOIN (
SELECT DISTINCT 
	drug_exposure_id, drug_source_concept_id, drug_exposure_start_datetime 
	FROM omop.dbo.drug_exposure 
	GROUP BY drug_exposure_id, drug_source_concept_id, drug_exposure_start_datetime 
	HAVING COUNT(*)>1
) sel on po.drug_exposure_id = sel.drug_exposure_id
and sel.drug_source_concept_id = po.drug_source_concept_id
and sel.drug_exposure_start_datetime = po.drug_exposure_start_datetime
) x
WHERE x.selector != 1 OR x.selector2 != 1
ORDER BY drug_exposure_id, drug_source_concept_id, drug_exposure_start_datetime 
-- N.B. Fixed in source extraction
-- -------------------------------------------

-- DIAGNOSE observation DUPLICATES
SELECT DISTINCT observation_occurrence_id, observation_concept_id --, observation_source_value
FROM omop.dbo.observation_occurrence 
GROUP BY observation_occurrence_id, observation_concept_id
-- , observation_source_value  
HAVING COUNT(*)>1

-- ----------------------------------------
-- FIX observation
DROP TABLE IF EXISTS observationIdMigration ;

CREATE TABLE observationIdMigration (
	new_observation_occurrence_id bigint IDENTITY(1,1) PRIMARY KEY,
	old_observation_occurrence_id bigint,
	old_observation_concept_id int
)

CREATE UNIQUE INDEX X_uniqueCond ON observationIdMigration (old_observation_occurrence_id, old_observation_concept_id);

INSERT INTO observationIdMigration (old_observation_occurrence_id, old_observation_concept_id)
SELECT observation_occurrence_id, observation_concept_id FROM omop.dbo.observation_occurrence

UPDATE po
SET po.observation_occurrence_id = pim.new_observation_occurrence_id 
FROM omop.dbo.observation_occurrence po, observationIdMigration pim
WHERE po.observation_occurrence_id = pim.old_observation_occurrence_id
AND po.observation_concept_id = pim.old_observation_concept_id

-- N.B. this shoudl be fixed by sorting out ids being assigned properly
SELECT TOP(100) * FROM  omop.dbo.observation_occurrence ORDER BY observation_occurrence_id DESC

-- -------------------------------------------

-- DIAGNOSE observation DUPLICATES
-- Many to many mapping for source_concept ids generate additional entries
SELECT TOP(1000) po.* FROM omop.dbo.observation po INNER JOIN (
SELECT DISTINCT observation_id, observation_concept_id, observation_source_concept_id --, observation_source_value
FROM omop.dbo.observation 
GROUP BY observation_id, observation_concept_id, observation_source_concept_id
-- , observation_source_value  
HAVING COUNT(*)>1
) x ON x.observation_id=po.observation_id and x.observation_source_concept_id=po.observation_source_concept_id and x.observation_date = po.observation_date
ORDER BY  observation_id, observation_concept_id, observation_source_concept_id

-- FIX
DELETE FROM  omop.dbo.observation where observation_id < 8377901750000000000

-- FIX observation
DROP TABLE IF EXISTS observationIdMigration ;

CREATE TABLE observationIdMigration (
	new_observation_id bigint IDENTITY(1,1) PRIMARY KEY,
	old_observation_id bigint,
	old_observation_concept_id int,
	old_observation_source_concept_id int
)

CREATE UNIQUE INDEX X_uniqueCond ON observationIdMigration (old_observation_id, old_observation_concept_id, old_observation_source_concept_id);

INSERT INTO observationIdMigration (old_observation_id, old_observation_concept_id,old_observation_source_concept_id)
SELECT observation_id, observation_concept_id, observation_source_concept_id FROM omop.dbo.observation

UPDATE po
SET po.observation_id = pim.new_observation_id 
FROM omop.dbo.observation po, observationIdMigration pim
WHERE po.observation_id = pim.old_observation_id
AND po.observation_concept_id = pim.old_observation_concept_id
AND po.observation_source_concept_id = pim.old_observation_source_concept_id

-- N.B. this shoudl be fixed by sorting out ids being assigned properly
SELECT TOP(100) * FROM  omop.dbo.observation ORDER BY observation_id DESC