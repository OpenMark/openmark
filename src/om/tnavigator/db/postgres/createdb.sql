-- Script used to create all database tables in Postgres 8.

-- Notes:
-- 1) SQL format comments (only) are permitted in this file. 
-- 2) Each statement must end in semicolon.

CREATE TABLE prefix_tests (
  ti SERIAL PRIMARY KEY,
  oucu VARCHAR(8) NOT NULL,
  deploy VARCHAR(64) NOT NULL,
  rseed BIGINT NOT NULL,
  attempt INTEGER NOT NULL,
  finished SMALLINT NOT NULL,
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  admin SMALLINT NOT NULL,
  pi CHAR(8) NOT NULL,
  variant INTEGER,
  testposition SMALLINT NOT NULL,
  finishedclock TIMESTAMP WITH TIME ZONE,
  navigatorversion CHAR(16) NOT NULL,
  dateWarningEmailSent TIMESTAMP WITH TIME ZONE,
  authorshipConfirmation INTEGER NOT NULL DEFAULT 0
); 

CREATE INDEX prefix_tests_deploy ON prefix_tests (deploy);
CREATE INDEX prefix_tests_oucu ON prefix_tests (oucu);

CREATE TABLE prefix_infopages (
  ti INTEGER NOT NULL REFERENCES prefix_tests,
  testposition SMALLINT NOT NULL
);

CREATE TABLE prefix_testquestions (
  ti INTEGER NOT NULL REFERENCES prefix_tests,
  questionnumber SMALLINT NOT NULL,
  question VARCHAR(64) NOT NULL,
  requiredversion SMALLINT,
  sectionname VARCHAR(255),
  PRIMARY KEY(ti,questionnumber)  
);

CREATE TABLE prefix_questions (
  qi SERIAL PRIMARY KEY,
  ti INTEGER NOT NULL REFERENCES prefix_tests,
  question VARCHAR(64) NOT NULL,
  attempt INTEGER NOT NULL,
  finished SMALLINT NOT NULL,
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  majorversion SMALLINT NOT NULL,
  minorversion SMALLINT NOT NULL
);

CREATE INDEX prefix_questions_ti on prefix_questions (ti);

CREATE TABLE prefix_scores (
  qi INTEGER NOT NULL REFERENCES prefix_questions,
  axis VARCHAR(64) NOT NULL,
  score SMALLINT NOT NULL,
  PRIMARY KEY(qi,axis)
);

CREATE TABLE prefix_results (
  qi INTEGER NOT NULL PRIMARY KEY REFERENCES prefix_questions,
  questionline VARCHAR(4096) not  NULL,
  answerline VARCHAR(4096) not  NULL,
  actions TEXT NOT NULL,
  attempts INTEGER NOT NULL
);

CREATE TABLE prefix_customresults (
  qi INTEGER NOT NULL PRIMARY KEY REFERENCES prefix_questions,
  name VARCHAR(64) NOT NULL,
  customresult TEXT NOT NULL
);
  
CREATE TABLE prefix_actions (
  qi INTEGER NOT NULL REFERENCES prefix_questions,
  seq SMALLINT NOT NULL,
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(qi,seq)
);

CREATE TABLE prefix_navconfig (
  name VARCHAR(64) NOT NULL PRIMARY KEY,
  value VARCHAR(64)
  
);

CREATE TABLE prefix_params (
  qi INTEGER NOT NULL REFERENCES prefix_questions,
  seq SMALLINT NOT NULL,
  paramname VARCHAR(255),
  paramvalue VARCHAR(4000),
  PRIMARY KEY(qi,seq,paramname)  
);

CREATE TABLE prefix_sessioninfo (
  ti INTEGER NOT NULL REFERENCES prefix_tests,
  ip VARCHAR(255),
  useragent VARCHAR(255),
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE precoursediag (
  ti INTEGER NOT NULL REFERENCES prefix_tests,
  precoursediagcode VARCHAR(64) NOT NULL,
  timecodeupdated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  trafficlights VARCHAR(1024) NOT NULL

);
CREATE INDEX prefix_sessioninfo_ti on prefix_sessioninfo (ti);
