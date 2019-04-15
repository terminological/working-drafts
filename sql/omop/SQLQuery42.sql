SELECT TOP(100) *
	-- m.instanceId as sourceInstanceId,
	-- m2.instanceId as targetInstanceId
FROM
	[EproLive-Copy].[dbo].[t_patient_local_identifiers] l,
	[EproLive-Copy].[dbo].[t_patients] p,
	omopBuild.dbo.MasterIndex m,
	omopBuild.dbo.MasterIndex m2
WHERE 
	l.patientId = p.pGuid
	AND l.value = m.rawId
	AND m.source = 'epro'
	AND m.idType = 'R'
	AND p.nhsNumber = m2.rawId
	AND m2.idType = 'N'
;