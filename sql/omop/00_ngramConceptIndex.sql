
USE omopBuild;
GO

-- NGRAM index stolen from
-- https://social.technet.microsoft.com/wiki/contents/articles/33419.sql-server-implementation-of-n-gram-search-index.aspx

-- -------------------------------------------------

DROP FUNCTION IF EXISTS dbo.fn4GramRange;
GO

CREATE FUNCTION dbo.fn4GramRange(@max INT)
	RETURNS @ids TABLE (beginning INT)
AS
BEGIN
INSERT INTO @ids
SELECT 
	1+ones.n+16*tens.n
	-- +0x100*hundreds.n+0x1000*thousands.n 
	-- TODO: Extend beyond 255 charaters
	as beginning
FROM 
	(VALUES(0x00),(0x01),(0x02),(0x03),(0x04),(0x05),(0x06),(0x07),(0x08),(0x09),(0x0A),(0x0B),(0x0C),(0x0D),(0x0E),(0x0F)) ones(n)
	,(VALUES(0x00),(0x01),(0x02),(0x03),(0x04),(0x05),(0x06),(0x07),(0x08),(0x09),(0x0A),(0x0B),(0x0C),(0x0D),(0x0E),(0x0F)) tens(n)
	-- ,(VALUES(0x00),(0x01),(0x02),(0x03),(0x04),(0x05),(0x06),(0x07),(0x08),(0x09),(0x0A),(0x0B),(0x0C),(0x0D),(0x0E),(0x0F)) hundreds(n),
	-- ,(VALUES(0x00),(0x01),(0x02),(0x03),(0x04),(0x05),(0x06),(0x07),(0x08),(0x09),(0x0A),(0x0B),(0x0C),(0x0D),(0x0E),(0x0F)) thousands(n)
WHERE 1+ones.n+16*tens.n < @max
	-- +0x100*hundreds.n+0x1000*thousands.n < @max
ORDER BY 1
RETURN;
END
GO

DROP FUNCTION IF EXISTS dbo.fn4GramStringNorm;
GO

CREATE FUNCTION dbo.fn4GramStringNorm(@search VARCHAR(255))
	RETURNS VARCHAR(255)
AS
BEGIN
	-- DECLARE @len int;
	-- SET @len=LEN(@search);
	-- SET @len=IIF(@len<4,4,@len);
	RETURN '  '+UPPER(@search)+'  '
END
GO

DROP FUNCTION IF EXISTS dbo.fn4GramCalculate;
GO

CREATE FUNCTION dbo.fn4GramCalculate(@search VARCHAR(255),@beginning int)
RETURNS BINARY(4)
AS
BEGIN
	RETURN 
	CONVERT(BINARY(4),SUBSTRING(@search,@beginning,4))
END
GO

-- SELECT dbo.fn4GramCalculate('ZZZZ',1);

-- -----------------------------------------------

DROP PROCEDURE IF EXISTS dbo.fn4GramIndex;
GO
DROP FUNCTION IF EXISTS dbo.fnSearch4Gram;
GO
DROP FUNCTION IF EXISTS dbo.fnBulkSearch4Gram;
GO

DROP TYPE IF EXISTS dbo.IndexInputType;
CREATE TYPE dbo.IndexInputType AS TABLE
	(concept_id INT, concept_name VARCHAR(255)
)

DROP TABLE IF EXISTS dbo.SearchIndex;
CREATE TABLE dbo.SearchIndex 
	(concept_id INT, ngram BINARY(4), seq smallint, ngram_length smallint, id smallint,
	INDEX X_SearchIndex_ngram NONCLUSTERED (ngram),
	INDEX X_SearchIndex_seq NONCLUSTERED (seq),
	INDEX X_SearchIndex_id NONCLUSTERED (id)
)

DROP TYPE IF EXISTS dbo.BulkSearchInputType;
CREATE TYPE dbo.BulkSearchInputType AS TABLE   
	( local_id VARCHAR(255), search_term VARCHAR(255) );  
GO 

DROP TYPE IF EXISTS dbo.SearchOutputType;
CREATE TYPE dbo.SearchOutputType AS TABLE   
	( concept_id int, strength float,
	INDEX X_concept_id (concept_id))
GO 

DROP TYPE IF EXISTS dbo.BulkSearchOutputType;
CREATE TYPE dbo.BulkSearchOutputType AS TABLE   
	( local_id VARCHAR(255), concept_id int, strength float,
	INDEX X_concept_id (concept_id),
	INDEX X_local_id (local_id));  
GO 

-- -------------------------------------------------

DROP PROCEDURE IF EXISTS dbo.fn4GramIndex;
GO

CREATE PROCEDURE dbo.fn4GramIndex(
	@id smallint, 
	@input dbo.IndexInputType READONLY) 
AS
BEGIN

-- PRINT 'loading ngram index';
DELETE FROM SearchIndex WHERE id=@id;

INSERT INTO SearchIndex
SELECT 
	concept_id,
	dbo.fn4GramCalculate(c.concept_name,beginning),
	beginning,
	LEN(concept_name)-3 as ngram_length,
	@id
	FROM
	(
		SELECT 
			i.concept_id,
			dbo.fn4GramStringNorm(i.concept_name) as concept_name
		FROM @input i
	) c,
	 dbo.fn4GramRange(253) n 
WHERE n.beginning<=LEN(concept_name)-3;

-- PRINT 'indexing ngrams';
-- PRINT 'done';

RETURN
END
GO

-- --------------------------------------------------

--DROP FUNCTION IF EXISTS dbo.fnSearch4Gram;
--GO

--CREATE FUNCTION dbo.fnSearch4Gram
--    (
--	 @id smallint,
--	 @search nvarchar(255),
--     @factor decimal(9, 4),
--	 @results int
--    )
--	RETURNS @ids TABLE -- SearchOutputType
--		(concept_id int, strength float)
--AS
--BEGIN
--    DECLARE @len int;
 
--    -- Normalize search string.
--    SET @search = (SELECT dbo.fn4GramStringNorm(@search));
--	SET @len = LEN(@search) - 3;

--	INSERT INTO @ids
	
--	SELECT TOP(@results) 
--		concept_id,
--		SQRT(
--			CONVERT(FLOAT,
--				@len+total-2*count(searchSeq)
--				+@len-sum(iif(searchSeq=matchSeq,1,0)) --as posNgramEuclidean,
--				+@len-sum(iif(searchSeqDelta=matchSeqDelta,1,0)) -- as relPosNgramEuclidean
--			)
--		) 
--		as ngramEuclidean
--	FROM
--		(SELECT 
--			*,
--			(matchSeq - LAG(matchSeq,1,0) OVER(PARTITION by i.concept_id ORDER BY s2.seq)) as matchSeqDelta,
--			(searchSeq - LAG(searchSeq,1,0) OVER(PARTITION by i.concept_id ORDER BY s2.seq)) as searchSeqDelta
--		FROM
--			(SELECT
--				i.concept_id,
--				s2.seq as searchSeq,
--				i.seq as matchSeq,
--				i.total,
--				ROW_NUMBER() OVER(PARTITION BY i.concept_id,s2.ngram,s2.seq ORDER BY ABS(s2.seq-i.seq)) as filt
--			FROM
--				(
--					SELECT
--						dbo.fn4GramCalculate(@search,beginning) as ngram,
--						beginning as seq
--					FROM
--						(SELECT @search as normTerm) s,
--						dbo.fn4GramRange(@len) n 
--				) s2 
--				INNER JOIN SearchIndex i 
--				ON s2.ngram = i.ngram
--				WHERE i.id = @id
--			) t
--			WHERE t.filt = 1
--		) u
--	GROUP BY concept_id
	
--	ORDER BY ngramEuclidean ASC;
--    RETURN;
--END;
--GO

DROP FUNCTION IF EXISTS dbo.fnBulkSearch4Gram;
GO

CREATE FUNCTION dbo.fnBulkSearch4Gram(
	@search dbo.BulkSearchInputType READONLY,
	@id smallint,
    @factor decimal(9, 4),
	@max int
) RETURNS @results TABLE --BulkSearchOutputType
	( local_id VARCHAR(255), concept_id int, similarity float )
AS
BEGIN

	DECLARE @searchTermsNgrams AS TABLE
		(local_id VARCHAR(255), ngram BINARY(4), seq smallint, ngram_length smallint,
		INDEX X_ngram NONCLUSTERED (ngram),
		INDEX X_id NONCLUSTERED (seq)
	);
	
	-- Calculate NGrams for the search terms
	INSERT INTO @searchTermsNgrams
	SELECT
		a.local_id,
		dbo.fn4GramCalculate(a.norm_term,b.beginning) as ngram,
		b.beginning as seq,
		ngram_length
	FROM
		(
			SELECT 
			local_id,
			dbo.fn4GramStringNorm(search_term) as norm_term,
			LEN(dbo.fn4GramStringNorm(search_term))-3 as ngram_length
			FROM @search
		) a,
		dbo.fn4GramRange(253) b 
	WHERE b.beginning <= ngram_length;

	
	DECLARE @searchTermMatches AS TABLE
		(local_id VARCHAR(255),concept_id INT,searchSeq SMALLINT,matchSeq SMALLINT, joint_ngram_length INT, seqDelta INT,
		INDEX X_local_id_concept_id (local_id,concept_id),
		INDEX X_searchSeq (searchSeq)
	)
	-- Join this to the main search table
	INSERT INTO @searchTermMatches
		SELECT
			c.local_id,
			d.concept_id,
			c.seq as searchSeq,
			d.seq as matchSeq,
			d.ngram_length + c.ngram_length as joint_ngram_length,
			ABS(c.seq-d.seq) as seqDelta
		FROM
			@searchTermsNgrams c 
			INNER JOIN SearchIndex d ON (c.ngram = d.ngram and d.id = @id)
			INNER JOIN SearchIndex d2 ON d.concept_id = d2.concept_id -- select out only those with more than one ngram match
			INNER JOIN @searchTermsNgrams c2 on (c2.ngram = d2.ngram and d2.id = @id and c2.local_id = c.local_id and c2.ngram <> c.ngram)

	

	DECLARE @searchTermMatchStats AS TABLE
		(local_id VARCHAR(255),
		concept_id INT,
		aligned SMALLINT,searchSeq SMALLINT,matchSeq SMALLINT,matchSeqDelta SMALLINT,searchSeqDelta SMALLINT, joint_ngram_length INT,
		index X_ensure_unique UNIQUE(local_id,concept_id,searchSeq,matchSeq)
		)
	INSERT INTO @searchTermMatchStats
	SELECT 
		e.local_id,
		e.concept_id,
		iif(e.searchSeq=e.matchSeq,1,0) as aligned,
		e.searchSeq,
		e.matchSeq,
		(e.matchSeq - LAG(e.matchSeq,1,0) OVER(PARTITION by e.local_id,e.concept_id ORDER BY e.searchSeq)) as matchSeqDelta,
		(e.searchSeq - LAG(e.searchSeq,1,0) OVER(PARTITION by e.local_id,e.concept_id ORDER BY e.searchSeq)) as searchSeqDelta,
		e.joint_ngram_length
	FROM (
		SELECT
			d.*,
			ROW_NUMBER() OVER(PARTITION BY d.local_id,d.concept_id,d.searchSeq ORDER BY d.seqDelta,d.searchSeq) as filter
			-- filter should ensure a unique match for each search-match ngram pair
		FROM @searchTermMatches d
	) e
	WHERE e.filter = 1
	
    
	INSERT INTO @results
	SELECT local_id,concept_id,similarity FROM (
		SELECT 
			local_id,concept_id,
			1 - SQRT(
					IIF(
						(ngram_euclidean_squared/max_euclidean_squared) < 0 , 0 , (ngram_euclidean_squared/max_euclidean_squared)
					)
				) as similarity,
			ROW_NUMBER() OVER(PARTITION BY local_id ORDER BY ngram_euclidean_squared) as rowNo
		FROM (
			SELECT 
				f.local_id,f.concept_id,
				CONVERT(FLOAT,
					f.joint_ngram_length-2*count(f.searchSeq)
					+f.joint_ngram_length-2*sum(iif(f.searchSeqDelta=f.matchSeqDelta,1,0)) -- as relPosNgramEuclidean
				) AS ngram_euclidean_squared,
				CONVERT(FLOAT,2*f.joint_ngram_length) as max_euclidean_squared
			FROM @searchTermMatchStats f
			GROUP BY f.local_id, f.concept_id, f.joint_ngram_length
		) x
	) w
	WHERE w.rowNo <= @max
	AND similarity > @factor
	;

RETURN
END
GO


-- ---------------------------------------------------

DROP TABLE IF EXISTS #tmpCounts
SELECT 
	[from_code] as local_id
    ,MIN([original_display_name]) as search_term
	,COUNT(*) as occurrence
INTO #tmpCounts
FROM [ordercomms_review].[dbo].[npisynonym] s INNER JOIN
	[ordercomms_review].[dbo].rnpi r ON s.[npisynonym_id]=r.[npisynonym_id]
	GROUP BY from_code
;

SELECT COUNT(*) FROM #tmpCounts

DROP TABLE IF EXISTS #tmpIndexInput
SELECT 
	concept_id, concept_name 
	-- DISTINCT concept_class_id, vocabulary_id	
	-- count(*) = 22418
INTO #tmpIndexInput
FROM [omop].[dbo].[concept]
WHERE 
standard_concept = 'S'
AND domain_id = 'Measurement'
AND concept_class_id = 'Clinical Observation'
AND vocabulary_id='LOINC'
;


SELECT Count(*) FROM #tmpIndexInput

-- DECLARE @conceptIndex as dbo.SearchIndexType
-- INSERT INTO @conceptIndex

DECLARE @indexInput AS dbo.IndexInputType;
INSERT INTO @indexInput SELECT * FROM #tmpIndexInput;
EXEC dbo.fn4gramIndex 1, @indexInput

SELECT COUNT(*) FROM SearchIndex

-- FROM @conceptIndex
-- CREATE INDEX X_concept_id ON #tmpConceptIndex (concept_id)
-- CREATE INDEX X_ngram ON #tmpConceptIndex (ngram)

DECLARE @bulkSearchInput AS dbo.BulkSearchInputType
INSERT INTO @bulkSearchInput
SELECT local_id,search_term FROM #tmpCounts

DROP TABLE IF EXISTS #tmpBulkSearchOutput
SELECT * 
INTO #tmpBulkSearchOutput
FROM dbo.fnBulkSearch4Gram(
	@bulkSearchInput,
	1,
	0.1,
	5)

SELECT b.local_id,search_term,0 as review,similarity,b.concept_id,concept_name,occurrence FROM
	#tmpCounts t 
	LEFT JOIN #tmpBulkSearchOutput b ON b.local_id = t.local_id COLLATE Latin1_General_CI_AS
	LEFT JOIN #tmpIndexInput i ON b.concept_id = i.concept_id
	ORDER BY occurrence DESC, b.local_id, similarity DESC



/****** Script for SelectTopNRows command from SSMS  ******/

DECLARE @indexInput2 AS dbo.IndexInputType;
INSERT INTO @indexInput2 
SELECT c.concept_id, c.concept_name FROM 
[omop].[dbo].[concept] c,
[omop].[dbo].[concept_ancestor] a
where ancestor_concept_id=36209248
AND descendant_concept_id=concept_id
AND standard_concept = 'S'

EXEC dbo.fn4gramIndex 3, @indexInput2

DECLARE @bulkSearchInput2 AS dbo.BulkSearchInputType
INSERT INTO @bulkSearchInput2
SELECT TOP (1000) [id],[text]
FROM [EproLive-Copy].[dbo].[tlu_specialties]

DROP TABLE IF EXISTS #tmpBulkSearchOutput2
SELECT * 
INTO #tmpBulkSearchOutput2
FROM dbo.fnBulkSearch4Gram(
	@bulkSearchInput2,
	3,
	0.1,
	5)

SELECT s.id, s.text, m.similarity, m.concept_id, c.concept_name FROM 
[EproLive-Copy].[dbo].[tlu_specialties] s LEFT OUTER JOIN
#tmpBulkSearchOutput2 m ON s.id = m.local_id
LEFT OUTER JOIN [omop].[dbo].[concept] c on c.concept_id = m.concept_id
order by id ASC, similarity DESC
