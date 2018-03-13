/****** Script for SelectTopNRows command from SSMS  ******/
USE RobsDatabase;

-- two approaches to this.
-- 1) build something that looks at known results and tests done on the same day to see if we can predict 
-- things clotting would have been normal if we had not done the test

-- 2) build something that looks at previous results, and their timing in relation to a clotting result

-- Lets look at (1) first
SELECT TOP (100) *
  FROM 
	tsftOrderedChemHaemResult ch1,
	tsftOrderedChemHaemResult ch2,
	tsftPatientIndex pi,
	tsftLocationTypes lt
  WHERE 
	ch1.investigation = 'CS' AND
	floor(cast(ch1.date as float)) = floor(cast(ch2.date as float)) AND
	ch1.patient_id = ch2.patient_id AND
	ch2.investigation in ('LFT','B','UEC','CRP')
	and pi.patient_id = ch1.patient_id
	and lt.location_id = ch1.location_id;

