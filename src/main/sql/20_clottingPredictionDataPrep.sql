/****** Script for SelectTopNRows command from SSMS  ******/
USE RobsDatabase;

-- two approaches to this.
-- 1) build something that looks at known results and tests done on the same day to see if we can predict 
-- things clotting would have been normal if we had not done the test

-- 2) build something that looks at previous results, and their timing in relation to a clotting result

-- Lets look at (1) first
DROP VIEW IF EXISTS aggClottingTrainingSet;
GO

CREATE VIEW aggClottingTrainingSet AS
SELECT 
	ch1.sequence_no,
	ch1.internal_id,
	ch1.date,
	ch1.test as label_test,
	ch1.test_abnormal as label_test_abnormal,
	ch1.numeric_result as label_numeric_result,
	ch1.unit as label_unit,
	ch2.test as value_test,
	ch2.test_abnormal as value_test_abnormal,
	ch2.numeric_result as value_numeric_result,
	ch2.unit as value_unit,
	ch1.location_id,
	ch1.patient_id
  FROM 
	tsftOrderedChemHaemResult ch1,
	tsftOrderedChemHaemResult ch2
  WHERE 
	ch1.investigation = 'CS' AND
	floor(cast(ch1.date as float)) = floor(cast(ch2.date as float)) AND
	ch1.patient_id = ch2.patient_id AND
	ch2.investigation in ('LFT','B','UEC','CRP');
GO

SELECT value_test,value_unit FROM  aggClottingTrainingSet group by value_test,value_unit;

DROP TABLE IF EXISTS tmpTestIndex;
GO
SELECT 
	row_number() OVER( ORDER BY investigation,value_test) as value_id, 
	investigation, 
	value_test, 
	mean,
	sd
INTO tmpTestIndex
FROM (
	SELECT 
		investigation,
		test as value_test,
		AVG(CAST(numeric_result as FLOAT)) as mean,
		STDEV(CAST(numeric_result as FLOAT)) as sd
	 FROM 
		tsftOrderedChemHaemResult where investigation in ('LFT','B','UEC','CRP') 
	group by 
		investigation,
		test
) as tmp;
GO
-- SELECT * FROM tmpTestIndex
-- https://www.mssqltips.com/sqlservertip/2783/script-to-create-dynamic-pivot-queries-in-sql-server/
-- https://stackoverflow.com/questions/10404348/sql-server-dynamic-pivot-query

DROP TABLE IF EXISTS aggClottingTrainingLibSVM;
GO

DECLARE @columns NVARCHAR(MAX),@columns2 NVARCHAR(MAX),@missing NVARCHAR(MAX), @sql NVARCHAR(MAX);

SET @columns = N'';
SELECT @columns += N', 
ISNULL(p.'+QUOTENAME(value_test)+','''+CAST(value_id AS VARCHAR)+':0'') as '+value_test  FROM tmpTestIndex order by value_id;
SET @columns = STUFF(@columns, 1, 3, '')

SET @columns2 = N'';
SELECT @columns2 += N',' + QUOTENAME(value_test) FROM tmpTestIndex order by value_id;
SET @columns2 = STUFF(@columns2, 1, 1, '')

SET @missing = N'';
SELECT @missing += N'+IIF(p.'+QUOTENAME(value_test)+' IS NULL,1,0)' FROM tmpTestIndex order by value_id;
SET @missing = STUFF(@missing, 1, 1, '')


SET @sql = N'
SELECT 
	date,internal_id,sequence_no,location_id,patient_id, label_test, label_test_abnormal, label_numeric_result, 
	'+@missing+' as missing,
	'+@columns+'
INTO aggClottingTrainingLibSVM
FROM
(
	SELECT DISTINCT
		a.date,
		a.internal_id, 
		a.sequence_no,
		a.location_id,
		a.patient_id, 
		a.label_test, 
		a.label_test_abnormal,
		a.label_numeric_result,
		a.value_test,
		CONCAT(t.value_id,'':'',((a.value_numeric_result-t.mean)/(2*t.sd))) as feature 
		FROM aggClottingTrainingSet a, tmpTestIndex t
  WHERE a.value_test=t.value_test
) j
PIVOT
(
  MAX(feature) FOR value_test IN ('+ @columns2+')
) AS p;';
PRINT @sql;
EXEC sp_executesql @sql;
GO

--SELECT 
--	date,internal_id,location_id,patient_id, label_test, label_test_abnormal, label_numeric_result, 
--	p.[BASO], p.[EOS], p.[HCT], p.[HGB], p.[LUC], p.[LYMP], p.[MCH], p.[MCHC], p.[MCV], p.[MONO], p.[NEUT], p.[PLTS], p.[RBC], p.[RDW], p.[WBC], p.[CRP], p.[ALBU], p.[ALP], p.[ALT], p.[BILI], p.[CREA], p.[K], p.[NA], p.[UREA]
--INTO aggClottingTrainingLibSVM
--FROM
--(
--	SELECT DISTINCT
--		a.date,
--		a.internal_id, 
--		a.location_id,
--		a.patient_id, 
--		a.label_test, 
--		a.label_test_abnormal,
--		a.label_numeric_result,
--		a.value_test,
--		CONCAT(t.value_id,':',a.value_numeric_result) as feature 
--		FROM aggClottingTrainingSet a, tmpTestIndex t
--  WHERE a.value_test=t.value_test
--) j
--PIVOT
--(
--  MAX(feature) FOR value_test IN ([BASO],[EOS],[HCT],[HGB],[LUC],[LYMP],[MCH],[MCHC],[MCV],[MONO],[NEUT],[PLTS],[RBC],[RDW],[WBC],[CRP],[ALBU],[ALP],[ALT],[BILI],[CREA],[K],[NA],[UREA])
--) p;

DROP VIEW IF EXISTS aggClottingTrainingLibSVMout;
GO

CREATE VIEW aggClottingTrainingLibSVMout AS
SELECT
	a.*,
	IIF(l.inpatient=1,'30:1','30:-1') as inpatient,
	IIF(p.sex='Male','31:1','31:-1') as sex,
	CONCAT('32:',
		((YEAR(a.date)-p.year_of_birth)-tmp.meanAge)/(2*tmp.sdAge)
	) as age,
	dependency = CASE l.dependency_level
		WHEN 'TRANSITIONAL_CARE' THEN '33:-0.5'
		WHEN 'GENERAL' THEN '33:0'
		WHEN 'EMERGENCY' THEN '33:0.5'
		WHEN 'HIGH_DEPENDENCY' THEN '33:1'
		ELSE '33:-1' --outpatient
	END
from 
	aggClottingTrainingLibSVM a,
	tsftLocationTypes l,
	tsftPatientIndex p,
	(
		SELECT 
			AVG(YEAR(a2.date)-p2.year_of_birth) as meanAge,
			STDEV(YEAR(a2.date)-p2.year_of_birth) as sdAge
		FROM
			aggClottingTrainingLibSVM a2,
			tsftPatientIndex p2
		WHERE
			p2.patient_id = a2.patient_id
			) tmp
	where label_test='INR'
	AND p.patient_id = a.patient_id
	AND l.location_id = a.location_id;
GO

SELECT missing,COUNT(*) FROM aggClottingTrainingLibSVM group by missing order by missing;

SELECT label_test, sum(label_test_abnormal) as abnormal, sum(1-label_test_abnormal) from aggClottingTrainingLibSVM where missing <= 2 group by label_test

SELECT TOP 1000 * from aggClottingTrainingLibSVMout
