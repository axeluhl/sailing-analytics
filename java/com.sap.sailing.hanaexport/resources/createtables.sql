CREATE COLUMN TABLE "Event" (
        "id"                    NVARCHAR(36) PRIMARY KEY,
        "name"                  NVARCHAR(255) NOT NULL,
        "startDate"             TIMESTAMP NOT NULL,
        "endDate"               TIMESTAMP NOT NULL,
        "venue"                 NVARCHAR(255),
        "isListed"              BOOLEAN,
        "description"           NVARCHAR(5000)
);
CREATE ROW TABLE "ScoringScheme" (
	"id" NVARCHAR(255) PRIMARY KEY,
	"higherIsBetter" BOOLEAN
);
CREATE ROW TABLE "BoatClass" (
	"id" NVARCHAR(20) PRIMARY KEY,
	"description" NVARCHAR(255),
	"hullLengthInMeters" DECIMAL(10, 2),
	"hullBeamInMeters" DECIMAL(10, 2),
	"hullType" NVARCHAR(20)
);
CREATE ROW TABLE "IRM" (
        "name" NVARCHAR(4) PRIMARY KEY,
        "discardable" BOOLEAN,
        "advanceCompetitorsTrackedWorse" BOOLEAN,
        "appliesAtStartOfRace" BOOLEAN
);
CREATE COLUMN TABLE "Regatta" (
	"name" NVARCHAR(255) PRIMARY KEY,
	"boatClass" NVARCHAR(20) NOT NULL,
	"scoringScheme" NVARCHAR(255) NOT NULL,
	"rankingMetric" NVARCHAR(255) NOT NULL,
	FOREIGN KEY ("boatClass") REFERENCES "BoatClass" ("id"),
	FOREIGN KEY ("scoringScheme") REFERENCES "ScoringScheme" ("id")
);
CREATE COLUMN TABLE "Race" (
	"name" NVARCHAR(255) NOT NULL,
	"regatta" NVARCHAR(255) NOT NULL,
	"raceColumn" NVARCHAR(255) NOT NULL,
	"fleet" NVARCHAR(255) NOT NULL,
	"startOfTracking" TIMESTAMP NOT NULL,
	"startOfRace" TIMESTAMP NOT NULL,
	"endOfTracking" TIMESTAMP NOT NULL,
	"endOfRace" TIMESTAMP NOT NULL,
	"avgWindSpeedInKnots" DECIMAL(5, 3),
	PRIMARY KEY ("name", "regatta"),
	FOREIGN KEY ("regatta") REFERENCES "Regatta" ("name")
);
CREATE TABLE "Competitor" (
        "id"            NVARCHAR(36)    PRIMARY KEY,
        "name"          NVARCHAR(255)   NOT NULL,
        "shortName"     NVARCHAR(20),
        "nationality"   NVARCHAR(3)     NOT NULL,
        "sailNumber"    NVARCHAR(255)
);
CREATE TABLE "CompetitorRace" (
        "competitorId"  NVARCHAR(36),
        "race"          NVARCHAR(255),
        "regatta"	NVARCHAR(255),
        PRIMARY KEY ("competitorId", "race"),
        FOREIGN KEY ("competitorId")    REFERENCES "Competitor" ("id"),
        FOREIGN KEY ("race", "regatta") REFERENCES "Race" ("name", "regatta")
);
CREATE TABLE "RaceResult" (
        "regatta"       NVARCHAR(255)    NOT NULL,
        "raceColumn"	NVARCHAR(255)    NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "points"        DECIMAL(10, 2),
        "discarded"	BOOLEAN,
        "irm"           NVARCHAR(4),
        PRIMARY KEY ("regatta", "raceColumn", "competitorId"),
        FOREIGN KEY ("competitorId")            REFERENCES "Competitor" ("id")
);
CREATE TABLE "RaceStats" (
        "race"          NVARCHAR(255)    NOT NULL,
        "regatta"       NVARCHAR(255)    NOT NULL,
        "competitorId"  NVARCHAR(36)     NOT NULL,
        "rankOneBased"  INTEGER,
        "distanceSailedInMeters"                DECIMAL(10, 2),
        "elapsedTimeInSeconds"                  DECIMAL(10, 2),
        "avgCrossTrackErrorInMeters"            DECIMAL(10, 2),
        "absoluteAvGCrossTrackErrorInMeters"    DECIMAL(10, 2),
        "numberOfTacks"                         INTEGER,
        "numberOfGybes"                         INTEGER,
        "numberOfPenaltyCircles"                INTEGER,
        "startDelayInSeconds"                   DECIMAL(10, 2),
        "distanceFromStartLineInMetersAtStart"  DECIMAL(10, 2),
        PRIMARY KEY ("race", "regatta", "competitorId"),
        FOREIGN KEY ("competitorId")            REFERENCES "Competitor" ("id"),
        FOREIGN KEY ("race", "regatta")         REFERENCES "Race" ("name", "regatta")
);