ALTER TABLE [App].[Procedure] ALTER COLUMN [CDWIdNumber] bigint NOT NULL
GO

ALTER TABLE [App].[Procedure] ALTER COLUMN [CDWIdResourceCode] char(1) NOT NULL
GO

ALTER TABLE [App].[Procedure] DROP CONSTRAINT PK_Procedure
GO

ALTER TABLE [App].[Procedure] ADD CONSTRAINT PK_Procedure PRIMARY KEY CLUSTERED (CDWIdNumber, CDWIdResourceCode)
GO

ALTER TABLE [App].[Procedure] ALTER COLUMN [CDWId] varchar(26) NULL
GO
