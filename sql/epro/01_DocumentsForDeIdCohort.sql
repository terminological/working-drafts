/****** Script for SelectTopNRows command from SSMS  ******/
-- Local identifiers in ePRO = RBA mrn
SELECT c.*, e.id, e.versionId, e.surname, e.forename, l.*
  FROM [RobsDatabase].[dbo].[deidMatchedCohort] c 
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_patients e ON c.nhsno = e.nhsNumber COLLATE DATABASE_DEFAULT
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_patient_local_identifiers l ON e.id = l.patientId

  /****** Script for SelectTopNRows command from SSMS  ******/
SELECT TOP (1000) c.*, e.id, e.versionId, e.surname, e.forename, l.*, h.*, html.*
  FROM [RobsDatabase].[dbo].[deidMatchedCohort] c 
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_patients e ON c.nhsno = e.nhsNumber COLLATE DATABASE_DEFAULT
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_episodes l ON e.pGuid = l.pGuid
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_documents h ON l.versionId = h.episodeVersionId
  LEFT OUTER JOIN [EproLive-Copy].dbo.t_document_html html ON h.versionId = html.versionId

USE [EproLive-Copy];

SELECT COUNT(*)
  FROM [RobsDatabase].[dbo].[deidMatchedCohort] c 
  LEFT JOIN t_patients P ON c.nhsno = P.nhsNumber COLLATE DATABASE_DEFAULT
  LEFT JOIN t_clinical_records CR ON CR.patientId = P.id
  LEFT JOIN t_documents D ON CR.id = D.clinicalRecordId
  LEFT JOIN t_document_html N ON D.[versionId] = N.[versionId]
  WHERE notesActive <> ''
  
-- Drug JSON files - not many in here
  /****** Script for SelectTopNRows command from SSMS  ******/
SELECT TOP (1000) [patientId]
      ,[versionId]
      ,[currentDrugsJson]
      ,[tagsString]
      ,[previousDrugsJson]
      ,[dischargeSummaryDate]
  FROM [EproLive-Copy].[dbo].[t_patient_current_drugs]
  where dischargeSummaryDate IS NOT NULL
  and previousDrugsJson <> '[]'
