ALTER TABLE [App].[AllergyIntolerance] ALTER COLUMN [CDWIdNumber] bigint NOT NULL
GO

ALTER TABLE [App].[AllergyIntolerance] ALTER COLUMN [CDWIdResourceCode] char(1) NOT NULL
GO

ALTER TABLE [App].[AllergyIntolerance] DROP CONSTRAINT PK_AllergyIntolerance
GO

ALTER TABLE [App].[AllergyIntolerance] ADD CONSTRAINT PK_AllergyIntolerance PRIMARY KEY CLUSTERED (CDWIdNumber, CDWIdResourceCode)
GO

ALTER TABLE [App].[AllergyIntolerance] ALTER COLUMN [CDWId] varchar(26) NULL
GO
