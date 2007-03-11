-- Script used to create all database tables in Postgres 8.

-- Notes:
-- 1) SQL format comments (only) are permitted in this file. 
-- 2) Each statement must end in semicolon.

CREATE TABLE nav_tests (
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
  finishedclock TIMESTAMP WITH TIME ZONE
); 

CREATE INDEX nav_tests_deploy ON nav_tests (deploy);
CREATE INDEX nav_tests_oucu ON nav_tests (oucu);

CREATE TABLE nav_infopages (
  ti INTEGER NOT NULL REFERENCES nav_tests,
  testposition SMALLINT NOT NULL
);

CREATE TABLE nav_testquestions (
  ti INTEGER NOT NULL REFERENCES nav_tests,
  questionnumber SMALLINT NOT NULL,
  question VARCHAR(64) NOT NULL,
  requiredversion SMALLINT,
  sectionname VARCHAR(255),
  PRIMARY KEY(ti,questionnumber)  
);

CREATE TABLE nav_questions (
  qi SERIAL PRIMARY KEY,
  ti INTEGER NOT NULL REFERENCES nav_tests,
  question VARCHAR(64) NOT NULL,
  attempt INTEGER NOT NULL,
  finished SMALLINT NOT NULL,
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  majorversion SMALLINT NOT NULL,
  minorversion SMALLINT NOT NULL
);

CREATE INDEX nav_questions_ti on nav_questions (ti);

CREATE TABLE nav_scores (
  qi INTEGER NOT NULL REFERENCES nav_questions,
  axis VARCHAR(64) NOT NULL,
  score SMALLINT NOT NULL,
  PRIMARY KEY(qi,axis)
);

CREATE TABLE nav_results (
  qi INTEGER NOT NULL PRIMARY KEY REFERENCES nav_questions,
  questionline VARCHAR(255) NOT NULL,
  answerline VARCHAR(255) NOT NULL,
  actions TEXT NOT NULL,
  attempts INTEGER NOT NULL
);

CREATE TABLE nav_customresults (
  qi INTEGER NOT NULL PRIMARY KEY REFERENCES nav_questions,
  name VARCHAR(64) NOT NULL,
  customresult TEXT NOT NULL
);
  
CREATE TABLE nav_actions (
  qi INTEGER NOT NULL REFERENCES nav_questions,
  seq SMALLINT NOT NULL,
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(qi,seq)
);

CREATE TABLE nav_params (
  qi INTEGER NOT NULL REFERENCES nav_questions,
  seq SMALLINT NOT NULL,
  paramname VARCHAR(255),
  paramvalue VARCHAR(2048),
  PRIMARY KEY(qi,seq,paramname)  
);

CREATE TABLE nav_sessioninfo (
  ti INTEGER NOT NULL REFERENCES nav_tests,
  ip VARCHAR(255),
  useragent VARCHAR(255),
  clock TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX nav_sessioninfo_ti on nav_sessioninfo (ti);
 
