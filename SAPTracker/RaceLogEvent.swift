//
//  RaceLogEvent.swift
//  SAPTracker
//
//  Created by computing on 29/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//


/* This is the format of all events sent and received from server, e.g. device mappings.

Example JSON request body for mapping a device:
{
    "timestamp": 1413460233257,
    "id": "c72eabea-f456-4b29-9ba0-dadd06f5a179",
    "passId": 0,
    "fromMillis": 1413459900000,
    "@class": "DeviceCompetitorMappingEvent",
    "createdAt": 1413460233258,
    "item": {
        "id": "da815c7f-c935-44f5-b01d-20b750ce75ab",
        "nationality": "",
        "displayColor": null,
        "idtype": "java.util.UUID",
        "name": null,
        "nationalityISO3": "",
        "sailID": "",
        "nationalityISO2": ""
    },
    "authorName": "Tracking App",
    "competitors": [ ],
    "device": {
        "stringRepresentation": "147d8230-4db0-11e4-916c-0800200c9a66",
        "id": "147d8230-4db0-11e4-916c-0800200c9a66",
        "type": "smartphoneUUID"
    },
    "authorPriority": 0,
    "toMillis": 1413460500000
}
*/
class RaceLogEvent {
    
    let timestamp = round(NSDate().timeIntervalSince1970) * 1000
    let createdAt = round(NSDate().timeIntervalSince1970) * 1000
    let id = NSUUID().UUIDString.lowercaseString
    let passId = 0
    let authorName = "Tracking App"
    let authorPriority = 0
    let competitors = []
    
    func aclass() -> String {
        fatalError("This method must be overridden")
    }
    
    func dictionary() -> [String: AnyObject] {
        var dictionary = [String: AnyObject]()
        dictionary["timestamp"] = timestamp
        dictionary["createdAt"] = createdAt
        dictionary["id"] = id
        dictionary["passId"] = passId
        dictionary["authorName"] = authorName
        dictionary["authorPriority"] = authorPriority
        dictionary["competitors"] = competitors
        dictionary["@class"] = aclass()
        return dictionary
    }
}