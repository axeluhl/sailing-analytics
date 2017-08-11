//
//  SAPTrackerTests.swift
//  SAPTrackerTests
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import XCTest
import CoreData
import SAPTracker

class SAPTrackerAPITests: XCTestCase {
    
    let bundle = Bundle(for: SAPTrackerAPITests.self)
    
    override func setUp() {
        // TODO: point to real server
        // APIManager.sharedManager.initManager("http://95.85.58.11:8000/")
    }
    
    // MARK: - Event
    
//    func testParseEvent() {
//        // read JSON from resources
//        let data = NSData(contentsOfURL: bundle.URLForResource("event1", withExtension: "json")!)
//        do {
//            let dictionary = try NSJSONSerialization.JSONObjectWithData(data!, options:NSJSONReadingOptions.MutableContainers) as! [String: AnyObject]
//            
//            // check values
//            checkEventDictionary(dictionary)
//        } catch {
//            print(error)
//        }
//    }
    
//    func testGetEvent() {
//        let expectation = expectationWithDescription("get event from server")
//        
//        // get JSON from server
//        APIManager.sharedManager.getEvent("35dc9389-b59d-4ebd-8f6b-432065642f40",
//            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
//                // check values
//                self.checkEventDictionary(eventResponseObject as! [String: AnyObject])
//                expectation.fulfill()
//            },
//            failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
//                XCTFail("server error \(error)")
//                expectation.fulfill()
//            }
//        );
//        
//        waitForExpectationsWithTimeout(1, handler: nil)
//    }
    
    /*
    {
    "id": "35dc9389-b59d-4ebd-8f6b-432065642f40",
    "name": "505 Worlds 2013",
    "description": "",
    "officialWebsiteURL": null,
    "logoImageURL": null,
    "startDate": 1429653600000,
    "endDate": 1430604000000,
    "venue": {
    "name": "Barbados",
    "courseAreas": [{
    "name": "Barbados",
    "id": "0b240bfb-e825-4904-8b97-220fb2cea014"
    }]
    },
    "imageURLs": ["http://static.sapsailing.com/ubilabsimages/505Worlds2013_eventteaser.jpg"],
    "videoURLs": ["https://www.youtube.com/watch?v=ddUfIRBNbnQ"],
    "sponsorImageURLs": [],
    "leaderboardGroups": [{
    "id": "444d5ce7-119e-4c9e-9f81-e914519bdc92",
    "name": "505 Worlds 2013",
    "description": "The 505 Worlds 2013 in Barbados",
    "displayName": null,
    "hasOverallLeaderboard": false
    }],
    "imageSizes": [{
    "imageURL": "http://static.sapsailing.com/ubilabsimages/505Worlds2013_eventteaser.jpg",
    "imageWidth": 370,
    "imageHeight": 240
    }]
    }
    */
    
//    func checkEventDictionary(dictionary: [String: AnyObject]) {
//        // http://stackoverflow.com/a/26444319
//        let entity = NSEntityDescription.entityForName("Event", inManagedObjectContext: DataManager.sharedManager.managedObjectContext!)
//        
//        // create (temporary) event object
//        var event = Event(entity: entity!, insertIntoManagedObjectContext:  DataManager.sharedManager.managedObjectContext!)
//        
//        // read dictionary into object
//        event.initWithDictionary(dictionary)
//        
//        // test values
//        XCTAssertEqual(event.eventId, "35dc9389-b59d-4ebd-8f6b-432065642f40")
//        XCTAssertEqual(event.name, "505 Worlds 2013")
//        XCTAssertEqual(event.startDate, NSDate(timeIntervalSince1970: 1429653600))
//        XCTAssertEqual(event.endDate, NSDate(timeIntervalSince1970: 1430604000))
//        
//        // delete test object
//        DataManager.sharedManager.managedObjectContext!.deleteObject(event)
//    }
    
    // MARK: - Leader Board
    
//    func testParseLeaderBoard() {
//        // read JSON from resources
//        let data = NSData(contentsOfURL: bundle.URLForResource("leaderboard1", withExtension: "json")!)
//        do {
//            let dictionary = try NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers) as! [String: AnyObject]
//            
//            // check values
//            checkLeaderBoardDictionary(dictionary)
//        } catch {
//            print(error)
//        }
//    }
    
//    func testGetLeaderBoard() {
//        let expectation = expectationWithDescription("get leader board from server")
//        
//        // get JSON from server
//        APIManager.sharedManager.getLeaderBoard("505%20Worlds%202013",
//            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
//                // check values
//                self.checkLeaderBoardDictionary(eventResponseObject as! [String: AnyObject])
//                expectation.fulfill()
//            },
//            failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
//                XCTFail("server error \(error)")
//                expectation.fulfill()
//            }
//        );
//        
//        waitForExpectationsWithTimeout(1, handler: nil)
//    }
    
    /*
    {
    "name": "505 Worlds 2013",
    "resultTimepoint": 1367614894000,
    "resultState": "Live",
    "maxCompetitorsCount": 1000,
    "higherScoreIsBetter": false,
    "scoringComment": "Final results after Race 9",
    "lastScoringUpdate": 1367614894000,
    "columnNames": ["R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9"],
    "competitors": [
    ...
    ]
    */
    
//    func checkLeaderBoardDictionary(dictionary: [String: AnyObject]) {
//        // http://stackoverflow.com/a/26444319
//        let entity = NSEntityDescription.entityForName("LeaderBoard", inManagedObjectContext: DataManager.sharedManager.managedObjectContext!)
//        
//        // create (temporary) event object
//        var leaderBoard = LeaderBoard(entity: entity!, insertIntoManagedObjectContext: DataManager.sharedManager.managedObjectContext!)
//        
//        // read dictionary into object
//        leaderBoard.initWithDictionary(dictionary)
//        
//        // test values
//        XCTAssertEqual(leaderBoard.name, "505 Worlds 2013")
//        
//        // delete test object
//        DataManager.sharedManager.managedObjectContext!.deleteObject(leaderBoard)
//    }
    
    // MARK: - Competitor
    
//    func testParseCompetitor() {
//        // read JSON from resources
//        let data = NSData(contentsOfURL: bundle.URLForResource("competitor1", withExtension: "json")!)
//        do {
//            let dictionary = try NSJSONSerialization.JSONObjectWithData(data!, options: NSJSONReadingOptions.MutableContainers) as! [String: AnyObject]
//        
//            // check values
//            checkCompetitorDictionary(dictionary)
//        } catch {
//            print(error)
//        }
//    }
    
//    func testGetCompetitor() {
//        let expectation = expectationWithDescription("get competitor from server")
//        
//        // get JSON from server
//        APIManager.sharedManager.getCompetitor("1d028378-e12c-a6bd-afa3-b8b21bc5a9ea",
//            success: { (AFHTTPRequestOperation operation, AnyObject eventResponseObject) -> Void in
//                // check values
//                self.checkCompetitorDictionary(eventResponseObject as! [String: AnyObject])
//                expectation.fulfill()
//            },
//            failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
//                XCTFail("server error \(error)")
//                expectation.fulfill()
//            }
//        );
//        
//        waitForExpectationsWithTimeout(1, handler: nil)
//    }
    
    /*
    {
    "name": "Lukas ZIELINSKI",
    "id": "1d028378-e12c-a6bd-afa3-b8b21bc5a9ea",
    "sailID": "LUK 1",
    "nationality": "GER",
    "countryCode": "DE",
    "boatClassName": "49er"
    }
    */
    
//    func checkCompetitorDictionary(dictionary: [String: AnyObject]) {
//        // http://stackoverflow.com/a/26444319
//        let entity = NSEntityDescription.entityForName("Competitor", inManagedObjectContext: DataManager.sharedManager.managedObjectContext!)
//        
//        // create (temporary) event object
//        let competitor = Competitor(entity: entity!, insertIntoManagedObjectContext: DataManager.sharedManager.managedObjectContext!)
//        
//        // read dictionary into object
//        competitor.initWithDictionary(dictionary)
//        
//        // test values
//        XCTAssertEqual(competitor.name, "Lukas ZIELINSKI")
//        XCTAssertEqual(competitor.competitorId, "1d028378-e12c-a6bd-afa3-b8b21bc5a9ea")
//        XCTAssertEqual(competitor.sailId, "LUK 1")
//        XCTAssertEqual(competitor.nationality, "GER")
//        XCTAssertEqual(competitor.countryCode, "DE")
//        
//        // delete test object
//        DataManager.sharedManager.managedObjectContext!.deleteObject(competitor)
//    }
    
//    func testParseUrl() {
//        let url = "comsapsailingtracker://95.85.58.11:8000?event_id=35dc9389-b59d-4ebd-8f6b-432065642f40&leaderboard_name=505%20Worlds%202013&competitor_id=92d06f15-1d61-fc2d-4e64-dae37577d3d2"
//        let qrcodeData = QRCodeData()
//        XCTAssertTrue(qrcodeData.parseString(url), "cannot parse QR code URL "+url)
//    }
    
}
