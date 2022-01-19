DROP TABLE [App].[MedicationStatement]
GO

CREATE TABLE [App].[MedicationStatement]
(
    [CDWId] varchar(26) not null,
    [CDWIdNumber] bigint null,
    [CDWIdResourceCode] char(1) null,
    [PatientFullICN] varchar(50) not null,
    [DateUTC] datetime null,
    [MedicationStatement] varchar(max) null,
    [LastUpdated] date null,
    constraint PK_MedicationStatement primary key clustered (CDWId)
)
