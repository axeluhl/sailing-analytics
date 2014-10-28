# Tracking App API
This document describes the API used by the tracking smartphone apps.

## Device Mapping
Following POST call maps smartphones to competitors.

### Verb

    POST

### Path

    /sailingserver/rc/racelog
        ?leaderboard=Guy%27s+Regatta
        &raceColumn=M1
        &fleet=Default
        &clientuuid=92669a6c-72c9-46d2-a815-f51ff15a4369

_Are query parameters really necessary?_

### Body

    {
        "timestamp": 1413460233257,
        "id": "c72eabea-f456-4b29-9ba0-dadd06f5a179",
        "passId": 0, ***IS THIS NEEDED?***
        "fromMillis": 1413459900000,
        "@class": "DeviceCompetitorMappingEvent",
        "createdAt": 1413460233258,
        "item": {
            "id": "da815c7f-c935-44f5-b01d-20b750ce75ab",
            "nationality": "", ***IS THIS NEEDED?***
            "displayColor": null, ***IS THIS NEEDED?***
            "idtype": "java.util.UUID",
            "name": null, ***IS THIS NEEDED?***
            "nationalityISO3": "", ***IS THIS NEEDED?***
            "sailID": "", ***IS THIS NEEDED?***
            "nationalityISO2": "" ***IS THIS NEEDED?***
        },
        "authorName": "Tracking App",
        "competitors": [ ], ***IS THIS NEEDED?***
        "device": {
            "stringRepresentation": "147d8230-4db0-11e4-916c-0800200c9a66",
            "id": "147d8230-4db0-11e4-916c-0800200c9a66",
            "type": "smartphoneUUID"
        },
        "authorPriority": 0, ***IS THIS NEEDED?***
        "toMillis": 1413460500000
    }

#### Response

The response is a JSON array containing all events pertaining to leaderboard.
_Is that correct?_

**@class**

* `RaceLogStartProcedureChangedEvent` ???

* `RaceLogStartTimeEvent` ???

* `DenoteForTrackingEvent` ???

* `RegisterCompetitorEvent` Add new competitor.

* `DefineMarkEvent` Add new mark (e.g. buoy).

* `RaceLogCourseDesignChangedEvent` Called when defining course with marks.

* `DeviceCompetitorMappingEvent` Triggered when device and competitor are mapped.

* `RevokeEvent` Called e.g. when deleting a device / competitor mapping.


    [
        {
            "@class": "RaceLogStartProcedureChangedEvent",
            "id": "1e76925c-6518-434c-8d43-f5abf97154bf",
            "createdAt": 1413794205930,
            "timestamp": 1413794205905,
            "passId": 0,
            "competitors": [],
            "authorName": "Shore",
            "authorPriority": 4,
            "startProcedureType": "RRS26"
        },
        {
            "@class": "RaceLogStartTimeEvent",
            "id": "978701be-d087-4ac1-a263-db2d7059e5d8",
            "createdAt": 1413794205931,
            "timestamp": 1413794205905,
            "passId": 0,
            "competitors": [],
            "authorName": "Shore",
            "authorPriority": 4,
            "nextStatus": "SCHEDULED",
            "startTime": 1413794160000
        },
        {
            "@class": "DenoteForTrackingEvent",
            "id": "f332a53b-db8c-4e22-b250-042400e08c9a",
            "createdAt": 1413460016871,
            "timestamp": 1413460016869,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1,
            "raceName": "Guy's Regatta M1 Default",
            "boatClass": "Small",
            "raceId": "dee82eb7-4d0f-4d1b-b834-9d1faa6b2517"
        },
        {
            "@class": "RegisterCompetitorEvent",
            "id": "f381d118-3344-41d3-bd47-4a2beb206d30",
            "createdAt": 1413460067296,
            "timestamp": 1413460067294,
            "passId": 0,
            "competitors": [
                {
                    "idtype": "java.util.UUID",
                    "id": "da815c7f-c935-44f5-b01d-20b750ce75ab",
                    "name": "Competitor A",
                    "displayColor": null,
                    "sailID": "A",
                    "nationality": "",
                    "nationalityISO2": "",
                    "nationalityISO3": ""
                }
            ],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1
        },
        {
            "@class": "RegisterCompetitorEvent",
            "id": "f7e6da48-d245-4615-8cbb-f7b4a96c7566",
            "createdAt": 1413460067298,
            "timestamp": 1413460067297,
            "passId": 0,
            "competitors": [
                {
                    "idtype": "java.util.UUID",
                    "id": "9c09ad1a-e05c-45d2-8b08-b8a6f4b03004",
                    "name": "Competitor B",
                    "displayColor": null,
                    "sailID": "B",
                    "nationality": "",
                    "nationalityISO2": "",
                    "nationalityISO3": ""
                }
            ],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1
        },
        {
            "@class": "DefineMarkEvent",
            "id": "87b347c8-ad57-47ca-9b67-2fe01ac72e3f",
            "createdAt": 1413461050887,
            "timestamp": 1413461050886,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1,
            "mark": {
                "@class": "Mark",
                "name": "Buoy 1",
                "id": "a8f20289-0246-438f-a4de-9bc1c30b9fc3",
                "color": "",
                "pattern": "",
                "shape": "CONICAL",
                "type": "BUOY"
            }
        },
        {
            "@class": "DefineMarkEvent",
            "id": "d48d2e37-496b-48b8-8fb6-5233638c018b",
            "createdAt": 1413461060946,
            "timestamp": 1413461060946,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1,
            "mark": {
                "@class": "Mark",
                "name": "Buoy 2",
                "id": "a8a84cf8-ad78-4db1-a593-91f43ced523a",
                "color": "",
                "pattern": "",
                "shape": "CONICAL",
                "type": "BUOY"
            }
        },
        {
            "@class": "RaceLogCourseDesignChangedEvent",
            "id": "f4e0d5c6-494c-4492-a09d-1665baf385ca",
            "createdAt": 1413461086484,
            "timestamp": 1413461086483,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 1,
            "courseDesign": {
                "name": "Course for Guy's Regatta - M1 - Default",
                "waypoints": [
                    {
                        "name": "Buoy 1",
                        "passingInstruction": "None",
                        "controlPoint": {
                            "@class": "Mark",
                            "name": "Buoy 1",
                            "id": "a8f20289-0246-438f-a4de-9bc1c30b9fc3",
                            "color": "",
                            "pattern": "",
                            "shape": "CONICAL",
                            "type": "BUOY"
                        }
                    },
                    {
                        "name": "Buoy 2",
                        "passingInstruction": "None",
                        "controlPoint": {
                            "@class": "Mark",
                            "name": "Buoy 2",
                            "id": "a8a84cf8-ad78-4db1-a593-91f43ced523a",
                            "color": "",
                            "pattern": "",
                            "shape": "CONICAL",
                            "type": "BUOY"
                        }
                    }
                ]
            }
        },
        {
            "@class": "DeviceCompetitorMappingEvent",
            "id": "cf5995a4-6cca-4cca-a6cc-2357a32bb436",
            "createdAt": 1413463043674,
            "timestamp": 1413463043674,
            "passId": 0,
            "competitors": [],
            "authorName": "Tracking App",
            "authorPriority": 0,
            "fromMillis": 1413461400000,
            "toMillis": 1413464100000,
            "item": {
                "idtype": "java.util.UUID",
                "id": "da815c7f-c935-44f5-b01d-20b750ce75ab",
                "name": "Competitor A",
                "displayColor": null,
                "sailID": "A",
                "nationality": "",
                "nationalityISO2": "",
                "nationalityISO3": ""
            },
            "device": {
                "type": "smartphoneUUID",
                "id": "147d8230-4db0-11e4-916c-0800200c9a66",
                "stringRepresentation": "147d8230-4db0-11e4-916c-0800200c9a66"
            }
        },
        {
            "@class": "RevokeEvent",
            "id": "481e710e-9f8d-4222-a4ae-ec4b15b7a8bd",
            "createdAt": 1413793852767,
            "timestamp": 1413793852767,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 0,
            "revokedEventId": "f332a53b-db8c-4e22-b250-042400e08c9a"
        },
        {
            "@class": "DenoteForTrackingEvent",
            "id": "740701f2-639b-4bfb-8f72-06093b0ac1e8",
            "createdAt": 1413793857143,
            "timestamp": 1413793857143,
            "passId": 0,
            "competitors": [],
            "authorName": "com.sap.sailing.server.RacingEventService",
            "authorPriority": 0,
            "raceName": "Guy's Regatta M1 Default",
            "boatClass": "Small",
            "raceId": "8fd69b14-f9bf-46bc-8bda-def5814d8758"
        },
        ...
    ]

## GPSFix

### Verb

    POST

### Path

    ???

### Body

    ???

### Response

    ???