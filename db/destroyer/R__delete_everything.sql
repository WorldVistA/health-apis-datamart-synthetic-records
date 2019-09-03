DROP TABLE IF EXISTS [dbo].[flyway_schema_history];

IF EXISTS(select * from sys.databases where name='${db_name}') ALTER DATABASE dq SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
DROP DATABASE IF EXISTS ${db_name} ;

DROP USER IF EXISTS ${db_user};
IF EXISTS(select * from sys.syslogins where name='${db_user}') DROP LOGIN ${db_user};