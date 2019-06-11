USE omopBuild;

-- N.B. https://www.nlm.nih.gov/research/umls/implementation_resources/community/dbloadscripts/metathesaurus_sql.txt

DROP TABLE IF EXISTS dbo.UmlsMRCONSO;
CREATE TABLE dbo.UmlsMRCONSO (
    CUI	char(8) NOT NULL,
    LAT	char(3) NOT NULL,
    TS	char(1) NOT NULL,
    LUI	varchar(10) NOT NULL,
    STT	varchar(3) NOT NULL,
    SUI	varchar(10) NOT NULL,
    ISPREF	char(1) NOT NULL,
    AUI	varchar(9) NOT NULL,
    SAUI	varchar(50) DEFAULT null,
    SCUI	varchar(100) DEFAULT null,
    SDUI	varchar(100) DEFAULT null,
    SAB	varchar(40) NOT NULL,
    TTY	varchar(40) NOT NULL,
    CODE	varchar(100) NOT NULL,
    STR	text NOT NULL,
    SRL	int NOT NULL,
    SUPPRESS	char(1) NOT NULL,
    CVF	int DEFAULT null
);

bulk insert
dbo.UmlsMRCONSO  from
'P:/data/umls/MRCONSO.RRF'
with (
fieldquote='',fieldterminator='|', rowterminator='|\n', batchsize=10000
--codepage=65001, 
--keepnulls
)

CREATE INDEX X_MRCONSO_CUI ON dbo.UmlsMRCONSO(CUI);

CREATE INDEX X_MRCONSO_SUI ON dbo.UmlsMRCONSO(SUI);

CREATE INDEX X_MRCONSO_LUI ON dbo.UmlsMRCONSO(LUI);

CREATE INDEX X_MRCONSO_CODE ON dbo.UmlsMRCONSO(CODE);

CREATE INDEX X_MRCONSO_SAB_TTY ON dbo.UmlsMRCONSO(SAB,TTY);
GO

DROP TABLE IF EXISTS CuiOmopMap;
GO


SELECT distinct u.CUI,u.CODE,c.concept_id
INTO CuiOmopMap 
FROM 
		(
			SELECT CUI,CODE FROM dbo.UmlsMRCONSO WHERE SAB='SNOMEDCT_US'
		) u
		INNER JOIN 
		(
			SELECT concept_id,concept_code FROM omop.dbo.concept WHERE vocabulary_id='SNOMED' AND standard_concept = 'S'
		) c
ON u.CODE = c.concept_code
GO

CREATE UNIQUE CLUSTERED INDEX X_CuiOmopMap_id ON CuiOmopMap (CUI,concept_id)
CREATE INDEX X_CuiOmopMap_CUI ON CuiOmopMap (CUI)