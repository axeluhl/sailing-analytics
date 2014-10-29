//
//  DeviceCompetitorMappingEvent.swift
//  SAPTracker
//
//  Created by computing on 29/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//


/* This is the format of device mappings.

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
class DeviceCompetitorMappingEvent : RaceLogEvent {
    
    var fromMillis = 0.0
    var toMillis = 0.0
    
    struct Item {
        var id: String
        let idType = "java.util.UUID"
        
        init(competitor: String) {
            id = competitor
        }
        
        /*
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
        */
        func dictionary() -> [String: AnyObject] {
            var dictionary = [String: AnyObject]()
            dictionary["id"] = id
            dictionary["nationality"] = ""
            dictionary["displayColor"] = NSNull()
            dictionary["idtype"] = idType
            dictionary["name"] = NSNull()
            dictionary["nationalityISO3"] = ""
            dictionary["sailID"] = ""
            dictionary["nationalityISO2"] = ""
            return dictionary
        }
        
    }
    var item: Item
    
    struct Device {
        var stringRepresentation: String
        var id: String
        let type  = "smartphoneUUID"
        
        init(deviceId: String) {
            stringRepresentation = deviceId
            id = deviceId
        }
        
        /*
        "device": {
        "stringRepresentation": "147d8230-4db0-11e4-916c-0800200c9a66",
        "id": "147d8230-4db0-11e4-916c-0800200c9a66",
        "type": "smartphoneUUID"
        },
        */
        func dictionary() -> [String: AnyObject] {
            var dictionary = [String: AnyObject]()
            dictionary["stringRepresentation"] = stringRepresentation;
            dictionary["id"] = id
            dictionary["type"] = type
            return dictionary
        }
        
    }
    var device : Device
    
    init(deviceId: String, competitor: String, from: Double, to: Double) {
        device = Device(deviceId: deviceId)
        item = Item(competitor: competitor)
        fromMillis = from
        toMillis = to
        super.init()
    }
    
    override func aclass() -> String {
        return "DeviceCompetitorMappingEvent"
    }
    
    override func dictionary() -> [String: AnyObject] {
        var dictionary = super.dictionary()
        dictionary["fromMillis"] = fromMillis
        dictionary["toMillis"] = toMillis
        dictionary["device"] = device.dictionary()
        dictionary["item"] = item.dictionary()
        return dictionary
    }
}