﻿SELECT TOP (1000) [concept_id]
      ,[concept_name]
      ,[domain_id]
      ,[vocabulary_id]
      ,[concept_class_id]
      ,[standard_concept]
      ,[concept_code]
      ,[valid_start_date]
      ,[valid_end_date]
      ,[invalid_reason]
  FROM [omop].[dbo].[concept]
  WHERE 
  vocabulary_id = 'Device Type'
  -- concept_class_id = 'ascii'
  -- AND concept_class_id = 'Qualifier Value'
  -- concept_name like '%report%'
  -- concept_name like '%test%'
 concept_name like '%performed%'

  SELECT TOP (1000) *
  FROM [omop].[dbo].[concept] c1,
  [omop].[dbo].[concept_relationship] r,
   [omop].[dbo].[concept] c2
  WHERE c1.concept_name like '%rifamp%'
  AND c1.domain_id like 'Drug'
  AND c1.vocabulary_id like 'SNOMED'
  -- AND c1.concept_class_id like 'VMP'
  AND c1.concept_id = r.concept_id_1
  AND r.concept_id_2 = c2.concept_id

  SELECT TOP(100) * FROM
	[omop].[dbo].[concept] c1,
	omop.dbo.drug_strength ds,
	omop.dbo.concept ing,
	--omop.dbo.concept nuc,
	--omop.dbo.concept duc
	WHERE c1.concept_name like '%rifamp%'
		AND c1.domain_id like 'Drug'
		AND c1.concept_id = ds.drug_concept_id
		AND ing.concept_id = ds.ingredient_concept_id
	--AND ds.amount_unit_concept_id = auc.concept_id
	--AND ds.numerator_unit_concept_id = nuc.concept_id
	--AND ds.denominator_unit_concept_id = duc.concept_id


  SELECT DISTINCT domain_id FROM [omop].[dbo].[concept];
  SELECT DISTINCT vocabulary_id FROM [omop].[dbo].[concept];
  SELECT DISTINCT concept_class_id FROM [omop].[dbo].[concept];

  SELECT * FROM 
  [omop].[dbo].[concept] c,
  [omop].[dbo].[concept_ancestor] a
  where ancestor_concept_id=36209248
  AND descendant_concept_id=concept_id
  AND standard_concept = 'S'
  AND concept_name like '%%'