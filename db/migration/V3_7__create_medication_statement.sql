/*
==========================================================================
Author:			Mark D Shaffer
Create date:	7/29/2019
Database:       OIT_Lighthouse
Jira Ticket:	DEV-XXXX
GIT:            XXX
Table:          MedicationStatement
Description:	Represents a MedicationStatement. This table architecture is
                intended to support a single trip to the database using an
				entity framework style approach.  Resource data is stored
				in a json (or XML) format to make it readily available to the api
				layer for consumption.

Data Definition:
Column Name      | Description
-----------------|-----------------------------------------------------
CDWId            | Primary Key.  the CDW SID value 
-----------------|-----------------------------------------------------
PatientFullICN   | The patient's VA internal control number
-----------------|-----------------------------------------------------
DateUTC          | the date and time in utc the record was first recorded
-----------------|-----------------------------------------------------
MedicationStatement | A JSON (or XML) formatted data payload intended to 
                    | support the API Layer.
-----------------|-----------------------------------------------------
ETLBacthId       | ETL process BatchId
-----------------|-----------------------------------------------------
ETLChunkNum      | ETL process chunk number  Used for managing initial 
                 | loading and updates of records
-----------------|-----------------------------------------------------
ETLCreateDate    | Date and time in UTC ETL record was first created
-----------------|-----------------------------------------------------
ETLEditDate      | Date and time in UTC ETL batch process last updated
                 | the record
-----------------|-----------------------------------------------------

Change History   
--------------------------------------------------------------------------
m/d/yyyy DEV-XXXX Somed Udesname
 YADDA YADDA YADDA YADDA YADDA YADDA YADDA YADDA YADDA YADDA YADDA YADDA

========================================================================== */



Create table [App].[MedicationStatement]
(
	[CDWId] varchar(50) not null,
	[PatientFullICN] varchar(50) not null,	
	[DateUTC] datetime null,
	[MedicationStatement] varchar(max) null,
	[ETLBatchId] int null,
	[ETLChunkNum] int null,
	[ETLCreateDate] datetime2(0) null,
	[ETLEditDate] datetime2(0) null,
	constraint PK_MedicationStatement primary key clustered (CDWId)
)

Create index [IX_MedicationStatement_PatientFullICN] on [App].[MedicationStatement]([PatientFullICN])
go

Create index [IX_MedicationStatement_DateUTC] on [App].[MedicationStatement]([DateUTC])
go