/****** Script for SelectTopNRows command from SSMS  ******/
SELECT event_type, COUNT(*)
  FROM [omopBuild].[dbo].[NlpAudit]
  GROUP BY event_type
GO





CREATE VIEW NlpProcessingTime AS
SELECT 
	DATEDIFF(ms,n1.event_time,n2.event_time) as millisecondsTaken, 
n1.*
  FROM 
	[omopBuild].[dbo].[NlpAudit] n1,
	[omopBuild].[dbo].[NlpAudit] n2
  WHERE n1.note_id = n2.note_id
  AND n1.nlp_system_instance = n2.nlp_system_instance
  AND n1.event_type = 'PROCESSING'
  AND n2.event_type = 'COMPLETE'
GO 

SELECT avg(docsPerMin), nlp_system_instance from (
SELECT 
	(60*1000.0)/millisecondsTaken as docsPerMin, n1.*
  FROM NlpProcessingTime n1
) x
GROUP BY nlp_system_instance

SELECT avg(millisecondsTaken)*6000000.0/1000/60/60/24  FROM NlpProcessingTime n1

SELECT COUNT(DISTINCT note_id) from omop.dbo.note_nlp

SELECT COUNT(DISTINCT note_id) from NlpAudit WHERE event_type = 'COMPLETE'