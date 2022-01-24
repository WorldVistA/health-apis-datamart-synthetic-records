DROP TABLE [App].[MedicationOrder]
GO

CREATE TABLE [App].[MedicationOrder]
(
    [CDWId] varchar(26) not null,
    [CDWIdNumber] bigint null,
    [CDWIdResourceCode] char(1) null,
    [PatientFullICN] varchar(50) not null,
    [DateUTC] datetime null,
    [MedicationOrder] varchar(max) null,
    [LastUpdated] date null,
    constraint PK_MedicationOrder primary key clustered (CDWId)
)
