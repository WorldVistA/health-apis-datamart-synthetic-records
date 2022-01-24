ALTER TABLE [App].[MedicationStatement] ALTER COLUMN [CDWIdNumber] bigint NOT NULL
GO

ALTER TABLE [App].[MedicationStatement] ALTER COLUMN [CDWIdResourceCode] char(1) NOT NULL
GO

ALTER TABLE [App].[MedicationStatement] DROP constraint PK_MedicationStatement
GO

ALTER TABLE [App].[MedicationStatement] ADD constraint PK_MedicationStatement primary key clustered (CDWIdNumber, CDWIdResourceCode)
GO

ALTER TABLE [App].[MedicationStatement] ALTER COLUMN [CDWId] varchar(26) NULL
GO
