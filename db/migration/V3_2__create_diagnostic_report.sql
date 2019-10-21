/*
==========================================================================
Author:			Mark D Shaffer
Create date:	7/29/2019
Database:       OIT_Lighthouse
Jira Ticket:	DEV-XXXX
GIT:            XXX
Table:          DiagnosticReport
Description:	Represents a Diagnostic Report. This table architecture is
                intended to support a single trip to the database using an
				entity framework style approach.  Resource data is stored
				in a json (or XML) format to make it readily available to the api
				layer for consumption.

Data Definition:
Column Name      | Description
-----------------|-----------------------------------------------------
PatientFullICN   | Primary Key. The patient's VA internal control number
-----------------|-----------------------------------------------------
DiagnosticReport | A JSON (or XML) formatted data payload intended to 
                 | support the API Layer.
-----------------|-----------------------------------------------------
ETLBacthId       | ETL process BatchId
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



Create table [app].[DiagnosticReport]
(
	PatientFullICN varchar(50) not null,
	DiagnosticReport varchar(max) null,
	ETLBatchId int null,
	ETLCreateDate datetime2(0) null,
	ETLEditDate datetime2(0) null,
	constraint PK_DiagnosticReport primary key clustered (PatientFullICN)
);




