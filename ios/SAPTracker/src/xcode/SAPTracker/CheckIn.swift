//
//  CheckIn.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(CheckIn)
class CheckIn: NSManagedObject {

    func updateWithCheckInData(checkInData: CheckInData) {
        isTraining = NSNumber(booleanLiteral: checkInData.isTraining)
        serverURL = checkInData.serverURL
        event.updateWithEventData(eventData: checkInData.eventData)
        leaderboard.updateWithLeaderboardData(leaderboardData: checkInData.leaderboardData)
        secret = checkInData.secret
    }

    func eventURL() -> URL? {
        return URL(string: "\(serverURL)/gwt/Home.html?navigationTab=Regattas#EventPlace:eventId=\(event.eventID)")
    }
    
    func leaderboardURL() -> URL? {
        guard let name = leaderboard.nameWithQueryAllowedCharacters() else { return nil }
        return URL(string: "\(serverURL)/gwt/Leaderboard.html?name=\(name)&showRaceDetails=false&embedded=true&hideToolbar=true")
    }
    
}
