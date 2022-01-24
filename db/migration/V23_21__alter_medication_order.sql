ALTER TABLE [App].[MedicationOrder] ALTER COLUMN [CDWIdNumber] bigint NOT NULL
GO

ALTER TABLE [App].[MedicationOrder] ALTER COLUMN [CDWIdResourceCode] char(1) NOT NULL
GO

ALTER TABLE [App].[MedicationOrder] DROP constraint PK_MedicationOrder
GO

ALTER TABLE [App].[MedicationOrder] ADD constraint PK_MedicationOrder primary key clustered (CDWIdNumber, CDWIdResourceCode)
GO

ALTER TABLE [App].[MedicationOrder] ALTER COLUMN [CDWId] varchar(26) NULL
GO
