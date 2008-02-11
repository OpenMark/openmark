-- Script used to create all database tables in SQL Server.

-- Notes:
-- 1) SQL format comments (only) are permitted in this file. 
-- 2) Each statement must end in semicolon.

CREATE TABLE dbo.prefix_tests (
  ti INT NOT NULL IDENTITY (1,1) PRIMARY KEY CLUSTERED,
  oucu VARCHAR(8) NOT NULL,
  deploy VARCHAR(64) NOT NULL,
  rseed BIGINT NOT NULL,
  attempt INT NOT NULL,
  finished BIT NOT NULL,
  clock DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  admin BIT NOT NULL,
  pi CHAR(8) NOT NULL,
  variant INT,
  testposition SMALLINT NOT NULL,
  finishedclock DATETIME,
  navigatorversion CHAR(16) NOT NULL
); 

CREATE NONCLUSTERED INDEX i1 ON prefix_tests (deploy);
CREATE NONCLUSTERED INDEX i2 ON prefix_tests (oucu);

CREATE TABLE dbo.prefix_infopages (
  ti INT NOT NULL FOREIGN KEY REFERENCES prefix_tests,
  testposition SMALLINT NOT NULL
);

CREATE TABLE dbo.prefix_testquestions (
  ti INT NOT NULL FOREIGN KEY REFERENCES prefix_tests,
  questionnumber SMALLINT NOT NULL,
  question VARCHAR(64) NOT NULL,
  requiredversion SMALLINT,
  sectionname NVARCHAR(255),
  PRIMARY KEY CLUSTERED(ti,questionnumber)  
);

CREATE TABLE dbo.prefix_questions (
  qi INT NOT NULL IDENTITY (1,1) PRIMARY KEY CLUSTERED,
  ti INT NOT NULL FOREIGN KEY REFERENCES prefix_tests,
  question VARCHAR(64) NOT NULL,
  attempt INT NOT NULL,
  finished TINYINT NOT NULL,
  clock DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  majorversion SMALLINT NOT NULL,
  minorversion SMALLINT NOT NULL
);

CREATE NONCLUSTERED INDEX i1 on prefix_questions (ti);

CREATE TABLE dbo.prefix_scores (
  qi INT NOT NULL FOREIGN KEY REFERENCES prefix_questions,
  axis VARCHAR(64) NOT NULL,
  score SMALLINT NOT NULL,
  PRIMARY KEY CLUSTERED(qi,axis)
);

CREATE TABLE dbo.prefix_results (
  qi INT NOT NULL PRIMARY KEY CLUSTERED FOREIGN KEY REFERENCES prefix_questions,
  questionline NATIONAL CHAR VARYING(255) NOT NULL,
  answerline NATIONAL CHAR VARYING(255) NOT NULL,
  actions NATIONAL TEXT NOT NULL,
  attempts INT NOT NULL
);

CREATE TABLE dbo.prefix_customresults (
  qi INT NOT NULL PRIMARY KEY CLUSTERED FOREIGN KEY REFERENCES prefix_questions,
  name VARCHAR(64) NOT NULL,
  customresult NATIONAL TEXT NOT NULL
);
  
CREATE TABLE dbo.prefix_actions (
  qi INT NOT NULL FOREIGN KEY REFERENCES prefix_questions,
  seq SMALLINT NOT NULL,
  clock DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY CLUSTERED(qi,seq)
);

CREATE TABLE dbo.prefix_params (
  qi INT NOT NULL FOREIGN KEY REFERENCES prefix_questions,
  seq SMALLINT NOT NULL,
  paramname VARCHAR(255),
  paramvalue NVARCHAR(2048),
  PRIMARY KEY CLUSTERED(qi,seq,paramname)  
);

CREATE TABLE dbo.prefix_sessioninfo (
  ti INT NOT NULL REFERENCES prefix_tests,
  ip VARCHAR(255),
  useragent VARCHAR(255),
  clock DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE CLUSTERED INDEX i2 on prefix_sessioninfo (ti);
 
