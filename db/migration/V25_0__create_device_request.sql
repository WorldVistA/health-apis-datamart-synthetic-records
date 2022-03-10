CREATE TABLE [App].[DeviceRequest]
(
  [CDWIdNumber] [bigint] NOT NULL,
  [CDWIdResourceCode] [char](1) NOT NULL,
  [PatientFullICN] [varchar](50) NOT NULL,
  [DateUTC] [datetime2](0) NULL,
  [LastUpdated] date NULL,
  [DeviceRequest] varchar(max) NOT NULL,
  CONSTRAINT PK_DeviceRequest PRIMARY KEY CLUSTERED (CDWIdNumber, CDWIdResourceCode)
)
GO

CREATE INDEX [IX_DeviceRequest_PatientFullICN] ON [App].[DeviceRequest]([PatientFullICN])
GO
