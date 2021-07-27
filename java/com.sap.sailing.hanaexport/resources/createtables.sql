CREATE COLUMN TABLE SAILING."Event" (
        "id"                    NVARCHAR(36) PRIMARY KEY,
        "name"                  NVARCHAR(255) NOT NULL,
        "startDate"             TIMESTAMP,
        "endDate"               TIMESTAMP,
        "venue"                 NVARCHAR(255),
        "isListed"              BOOLEAN,
        "description"           NVARCHAR(5000),
        "location"              ST_POINT
);
CREATE ROW TABLE SAILING."ScoringScheme" (
	"id" NVARCHAR(255) PRIMARY KEY,
	"higherIsBetter" BOOLEAN
);
CREATE ROW TABLE SAILING."BoatClass" (
	"id" NVARCHAR(255) PRIMARY KEY,
	"description" NVARCHAR(255),
	"hullLengthInMeters" DOUBLE,
	"hullBeamInMeters" DOUBLE,
	"hullType" NVARCHAR(20)
);
CREATE ROW TABLE SAILING."IRM" (
        "name" NVARCHAR(4) PRIMARY KEY,
        "discardable" BOOLEAN,
        "advanceCompetitorsTrackedWorse" BOOLEAN,
        "appliesAtStartOfRace" BOOLEAN
);
CREATE COLUMN TABLE SAILING."Regatta" (
	"name"          NVARCHAR(255) PRIMARY KEY,
	"boatClass"     NVARCHAR(255) NOT NULL,
	"scoringScheme" NVARCHAR(255) NOT NULL,
	"rankingMetric" NVARCHAR(255) NOT NULL,
	"eventId"       NVARCHAR(36),
	FOREIGN KEY ("boatClass") REFERENCES SAILING."BoatClass" ("id"),
	FOREIGN KEY ("eventId") REFERENCES SAILING."Event" ("id"),
	FOREIGN KEY ("scoringScheme") REFERENCES SAILING."ScoringScheme" ("id")
);
CREATE COLUMN TABLE SAILING."Race" (
	"name" NVARCHAR(255) NOT NULL,
	"regatta" NVARCHAR(255) NOT NULL,
	"raceColumn" NVARCHAR(255) NOT NULL,
	"fleet" NVARCHAR(255) NOT NULL,
	"startOfTracking" TIMESTAMP,
	"startOfRace" TIMESTAMP,
	"endOfTracking" TIMESTAMP,
	"endOfRace" TIMESTAMP,
	"avgWindSpeedInKnots" DOUBLE,
	PRIMARY KEY ("name", "regatta"),
	FOREIGN KEY ("regatta") REFERENCES SAILING."Regatta" ("name")
);
CREATE TABLE SAILING."Competitor" (
        "id"            NVARCHAR(36)    PRIMARY KEY,
        "name"          NVARCHAR(255)   NOT NULL,
        "shortName"     NVARCHAR(20),
        "nationality"   NVARCHAR(3)     NOT NULL,
        "sailNumber"    NVARCHAR(255)
);
CREATE TABLE SAILING."CompetitorRace" (
        "competitorId"  NVARCHAR(36),
        "race"          NVARCHAR(255),
        "regatta"	NVARCHAR(255),
        PRIMARY KEY ("competitorId", "race"),
        FOREIGN KEY ("competitorId")    REFERENCES SAILING."Competitor" ("id"),
        FOREIGN KEY ("race", "regatta") REFERENCES SAILING."Race" ("name", "regatta")
);
CREATE TABLE SAILING."RaceResult" (
        "regatta"       NVARCHAR(255)    NOT NULL,
        "raceColumn"	NVARCHAR(255)    NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "points"        DOUBLE,
        "discarded"	BOOLEAN,
        "irm"           NVARCHAR(4),
        PRIMARY KEY ("regatta", "raceColumn", "competitorId"),
        FOREIGN KEY ("competitorId")            REFERENCES SAILING."Competitor" ("id")
);
CREATE TABLE SAILING."RaceStats" (
        "race"          NVARCHAR(255)    NOT NULL,
        "regatta"       NVARCHAR(255)    NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "rankOneBased"  INTEGER,
        "distanceSailedInMeters"                DOUBLE,
        "elapsedTimeInSeconds"                  DOUBLE,
        "avgCrossTrackErrorInMeters"            DOUBLE,
        "absoluteAvgCrossTrackErrorInMeters"    DOUBLE,
        "numberOfTacks"                         INTEGER,
        "numberOfGybes"                         INTEGER,
        "numberOfPenaltyCircles"                INTEGER,
        "startDelayInSeconds"                   DOUBLE,
        "distanceFromStartLineInMetersAtStart"  DOUBLE,
        "speedWhenCrossingStartLineInKnots"     DOUBLE,
        "startTack"     NVARCHAR(20),
        PRIMARY KEY ("race", "regatta", "competitorId"),
        FOREIGN KEY ("competitorId")            REFERENCES SAILING."Competitor" ("id"),
        FOREIGN KEY ("race", "regatta")         REFERENCES SAILING."Race" ("name", "regatta")
);
CREATE TABLE SAILING."Leg" (
        "race"          NVARCHAR(255)    NOT NULL,
        "regatta"       NVARCHAR(255)    NOT NULL,
        "number"        INTEGER          NOT NULL,
        "type"          NVARCHAR(20),
        PRIMARY KEY ("race", "regatta", "number"),
        FOREIGN KEY ("race", "regatta")         REFERENCES SAILING."Race" ("name", "regatta")
);
CREATE TABLE SAILING."LegStats" (
        "race"          NVARCHAR(255)    NOT NULL,
        "regatta"       NVARCHAR(255)    NOT NULL,
        "number"        INTEGER          NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "rankOneBased"  INTEGER,
        "distanceSailedInMeters"                DOUBLE,
        "elapsedTimeInSeconds"                  DOUBLE,
        "avgCrossTrackErrorInMeters"            DOUBLE,
        "absoluteAvgCrossTrackErrorInMeters"    DOUBLE,
        "numberOfTacks"                         INTEGER,
        "numberOfGybes"                         INTEGER,
        "numberOfPenaltyCircles"                INTEGER,
        "avgVelocityMadeGoodInKnots"            DOUBLE,
        "gapToLeaderInSeconds"                  DOUBLE,
        PRIMARY KEY ("race", "regatta", "number", "competitorId"),
        FOREIGN KEY ("competitorId")            REFERENCES SAILING."Competitor" ("id"),
        FOREIGN KEY ("race", "regatta")         REFERENCES SAILING."Race" ("name", "regatta")
);
CREATE TABLE SAILING."Maneuver" (
        "race"          NVARCHAR(255)    NOT NULL,
        "regatta"       NVARCHAR(255)    NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "timepoint"     TIMESTAMP        NOT NULL,
        "type"          NVARCHAR(20)     NOT NULL,
        "newTack"       NVARCHAR(20),
        "lossInMeters"  DOUBLE,
        "speedBeforeInKnots"                    DOUBLE,
        "speedAfterInKnots"                     DOUBLE,
        "courseBeforeInTrueDegrees"             DOUBLE,
	"courseAfterInTrueDegrees"              DOUBLE,
        "directionChangeInDegrees"              DOUBLE,
        "maximumTurningRateInDegreesPerSecond"  DOUBLE,
        "lowestSpeedInKnots"                    DOUBLE,
        "toSide"        NVARCHAR(20),
        PRIMARY KEY ("race", "regatta", "competitorId", "timepoint"),
        FOREIGN KEY ("competitorId")            REFERENCES SAILING."Competitor" ("id"),
        FOREIGN KEY ("race", "regatta")         REFERENCES SAILING."Race" ("name", "regatta")
);