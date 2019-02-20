use TriNetX;

-- create an audit log of extractions
BEGIN TRY
	Create table TriNetX.dbo.tblExtractLog (
		db VARCHAR(70),
		tbl VARCHAR(70),
		pkName VARCHAR(70), -- the name of the primary key
		maxPkValue VARCHAR(70), -- the max value of the primary key extracted so far
		extractDate DATETIME,
		comment TEXT
	)
END TRY
BEGIN CATCH END CATCH

BEGIN TRY
	-- excludes radiology
	CREATE TABLE TriNetX.dbo.tblTriNetXLabResult (
		[Patient id] VARCHAR(255), 
		[Encounter id] VARCHAR(255), 
		[Provider id] VARCHAR(255), 
		[Code System] VARCHAR(70), 
		[Code] VARCHAR(70), 
		[Description] VARCHAR(70), 
		[Battery Code System] VARCHAR(70), 
		[Battery Code] VARCHAR(70), 
		[Battery Description] VARCHAR(70), 
		[Lab Section] VARCHAR(70), 
		[Normal Range] VARCHAR(51), 
		[Test date] SMALLDATETIME, 
		[Result type] VARCHAR(10), --NUMERIC or TEXT 
		[Numeric value] FLOAT, 
		[Text value] TEXT, 
		[Units of measure] VARCHAR(25)

		INDEX X_Patient_id ([Patient id]),
		INDEX X_Code ([Code]),
		INDEX X_Battery_Code ([Battery Code]),
		INDEX X_Battery_Code ([Test date])
	);
END TRY
BEGIN CATCH END CATCH
GO;

DROP TABLE TriNetX.dbo.tmpReport;
CREATE TABLE TriNetX.dbo.tmpReport (
	report_id INT PRIMARY KEY,
	location_id INT,
	result_date SMALLDATETIME,
	result_time CHAR(5),
	patient_id INT,
	requester_id INT,
	discipline_id INT,
	abnormal BIT,
	INDEX X_location_id (location_id),
	INDEX X_patient_id (patient_id),
	INDEX X_requester_id (requester_id),
	INDEX X_discipline_id (discipline_id)
);
GO

DROP PROCEDURE IF EXISTS dbo.sliceReports;
GO;

CREATE PROCEDURE dbo.sliceReports @minReportId INT, @size INT as
BEGIN
	DELETE FROM TriNetX.dbo.tmpReport;
	INSERT INTO TriNetX.dbo.tmpReport
	SELECT 
		r.report_id,
		r.location_id,
		r.result_date,
		r.result_time,
		r.patient_id,
		r.requester_id,
		r.discipline_id,
		r.abnormal
	FROM ordercomms_review.dbo.report r WITH (INDEX(PK_report_reportid))
		INNER JOIN ordercomms_review.dbo.lab_patient l 
		ON r.patient_id = l.patient_id
	WHERE 
		l.hospital_no like 'RBA%' -- previously done on request location id...
		AND r.report_id <= @minReportId+@size 
		and r.report_id > @minReportId
		and r.amended=0
		and r.result_date IS NOT NULL
		and r.result_time IS NOT NULL;
END
GO;



DECLARE @minReportId INT;
DECLARE @maxReportId INT;
-- DECLARE @date datetime;
-- DECLARE @message nvarchar(max);
DECLARE @size int;


-- TODO:

Select TOP(100) * from
	dbo.tblAnonTriNetXDemographicData an,
	dbo.tblPatlinkBaK ln,
	dbo.tblDemographicData dem
Where ln.anonpid = an.[Patient id]
and ln.pid = dem.patient_id

DROP TABLE TriNetX.dbo.tmpReport;

----------------------------------------------------
SET @size = 100000;
-- initialise the minReportId to be the largest value that appears in the tblExtract log
SET @minReportId = (SELECT MAX(CAST(maxPkValue as INT)) FROM TriNetX.dbo.tblExtractLog WHERE db='ordercomms_review' and tbl='report' and pkName='report_id');
SET @minReportId = IIF(@minReportId IS NULL, 0, @minReportId);
SET @maxReportId = (SELECT TOP 1 report_id FROM ordercomms_review.dbo.report order by report_id DESC);


WHILE @minReportId <= @maxReportId
BEGIN
	EXEC RobsDatabase.dbo.sliceReports @maxReportId,@size;

	--TODO: RENAME THIS
	INSERT INTO RobsDatabase.[dbo].[tsftResult]
	SELECT
	-- SELECT TOP(1000)
			r.report_id as internal_id,
			r.location_id,
			r.result_date+r.result_time as date,
			CONVERT(binary(32),hashbytes('SHA2_256',tmp.salt+CONVERT(nvarchar(4000),r.patient_id))) as patient_id,
			r.requester_id as clinician_id,
			 
			cast(rqs.src_comments as nvarchar(max)) as src_comments,
			d.code as discipline,
			d.name as discipline_name,
			
			-- if no battery but there is a test we use that instead
			IIF(b.battery_id IS NULL, ts.test_id, b.battery_id) as investigation_id,
			IIF(b.from_code IS NULL, ts.from_code, b.from_code) as investigation,
			IIF(b.original_display_name IS NULL, ts.original_display_name, b.original_display_name) as investigation_name,
			r.abnormal as investigation_abnormal,
			
			s.sample_id,
			s.from_code as sample,

			ts.test_id as test_id,
			ts.from_code as test,
			ts.original_display_name as test_name,
			IIF(rqt.abnormal IS NULL,0,rqt.abnormal) as test_abnormal,
			
			-- b.subtype_id as test_discipline,
			cast(rqt.textual_result as nvarchar(max)) as textual_result,
			rqt.numeric_result,
			ts.unit,
			rqt.normalcy,
			ts.low_range,
			ts.high_range

		FROM 
		(Select CONVERT(char(36),uuid) as salt from RobsDatabase.dbo.tsftSalt GROUP BY id,uuid having id=max(id)) tmp,
		 RobsDatabase.dbo.tmpReport r
			INNER JOIN ordercomms_review.dbo.rsample rqs ON r.report_id = rqs.report_id
			LEFT OUTER JOIN ordercomms_review.dbo.rbattery rqb on rqs.rsample_id = rqb.rsample_id
			INNER JOIN ordercomms_review.dbo.sampleSynonym s ON rqs.sampleSynonym_id = s.sampleSynonym_id
			INNER JOIN ordercomms_review.dbo.batterySynonym b ON rqb.batterySynonym_id = b.batterySynonym_id
			LEFT OUTER JOIN ordercomms_review.dbo.rtest rqt on rqs.rsample_id = rqt.rsample_id and rqt.rbattery_id = rqb.rbattery_id
			INNER JOIN ordercomms_review.dbo.testSynonym ts on rqt.testsynonym_id = ts.testSynonym_id
			INNER JOIN ordercomms_review.dbo.discipline d on r.discipline_id = d.discipline_id
		-- ORDER BY patient_id, date, investigation
		;

	-- TODO: 

	SET @minReportId = (SELECT MAX(CAST(maxPkValue as INT)) FROM TriNetX.dbo.tblExtractLog WHERE db='ordercomms_review' and tbl='report' and pkName='report_id');
	SET @minReportId = IIF(@minReportId IS NULL, 0, @minReportId);
END;
GO

-----------------------------------------------
--Generate file for TrinetX code system mapping
SELECT
	'TSFT' as [Source Id],
	[Code],
	[Code System],
	[Description],
	[Units of measure],
	[Lab section], --i.e. specialty
	NULL as [LOINC Code],
	COUNT(Distinct([Patient id])) as [Patient count], --patients with this type of result
	COUNT(*) as [Observation count], --number of observations for this type of result
	[Normal Range], -- as a range ##-##
	[Battery Code],
	[Battery Description],
	[Battery Code System],
	NULL as [Related Code],
	NULL as [Related Code System],
	NULL as [Related Code Description],
	MIN([Test date]) as [Earliest Observation timestamp],
	MAX([Test date]) as [Most Recent Observation timestamp],
	MIN([Numeric value]) as [Minimum value],
	MAX([Numeric value]) as [Maximum value],
	AVG([Numeric value]) as [Average value]
from TriNetX.dbo.tblTriNetXLabResult GROUP BY
	[Code System], 
	[Code], 
	[Description], 
	[Battery Code System], 
	[Battery Code], 
	[Battery Description], 
	[Lab Section], 
	[Normal Range], 
	[Units of measure];
