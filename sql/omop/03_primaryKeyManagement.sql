USE omopBuild;
GO

-----------------------------------------
-- create a mapping table for identifiers
--BEGIN TRY
--	Create table omopBuild.dbo.UniqueIdentifiers (
--		id BIGINT IDENTITY PRIMARY KEY,
--		sourceId BIGINT,
--		sourceField VARCHAR(70)
--		INDEX X_source UNIQUE (sourceField,sourceId)
--	)
--END TRY
--BEGIN CATCH END CATCH
--GO

CREATE FUNCTION dbo.getId(@sourceId INT, @source CHAR(4)) RETURNS BIGINT
AS
BEGIN
	RETURN CONVERT(BIGINT,(CONVERT(BINARY(4),@source)))*256*256*256*256+@sourceId
END
GO

-- SELECT dbo.getId(1235,'a')
-- SELECT dbo.getId(1236,'ZZZZ')