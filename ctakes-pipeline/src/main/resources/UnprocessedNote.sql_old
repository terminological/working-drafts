SELECT n.* 
FROM omopBuild.dbo.IdentifiableNote n LEFT OUTER JOIN omop.dbo.note_nlp nlp ON n.note_id = nlp.note_id 
WHERE nlp.note_id IS NULL
ORDER BY n.note_id
OFFSET 0 ROWS
FETCH FIRST 100 ROWS ONLY;
