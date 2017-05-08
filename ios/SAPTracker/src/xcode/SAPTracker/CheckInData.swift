//
//  CheckInData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class CheckInData: NSObject {
    
    private struct ItemNames {
        static let EventID = "event_id"
        static let LeaderboardName = "leaderboard_name"
        static let CompetitorID = "competitor_id"
        static let MarkID = "mark_id"
    }
    
    public enum Type {
        case Competitor
        case Mark
    }
    
    let serverURL: String
    let eventID: String
    let leaderboardName: String
    let competitorID: String?
    let markID: String?
    let type: Type

    var eventData = EventData()
    var leaderboardData = LeaderboardData()
    var competitorData = CompetitorData()
    var markData = MarkData()
    var teamImageURL: String?
    
    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         competitorID: String)
    {
        self.competitorID = competitorID
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = nil
        self.serverURL = serverURL
        self.type = Type.Competitor
        super.init()
    }

    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         markID: String)
    {
        self.competitorID = nil
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = markID
        self.serverURL = serverURL
        type = Type.Mark
        super.init()
    }

    convenience init(competitorCheckIn: CompetitorCheckIn) {
        self.init(
            serverURL: competitorCheckIn.serverURL,
            eventID: competitorCheckIn.event.eventID,
            leaderboardName: competitorCheckIn.leaderboard.name,
            competitorID: competitorCheckIn.competitorID
        )
    }

    convenience init(markCheckIn: MarkCheckIn) {        
        self.init(
            serverURL: markCheckIn.serverURL,
            eventID: markCheckIn.event.eventID,
            leaderboardName: markCheckIn.leaderboard.name,
            markID: markCheckIn.markID
        )
    }

    convenience init?(url: NSURL) {
        guard let host = url.host else { return nil }
        guard let components = NSURLComponents(string: url.absoluteString) else { return nil }
        
        // Set server URL
        let serverURL = url.scheme + "://" + host + (url.port != nil ? ":" + url.port!.stringValue : "")
        
        // In query component replace '+' occurrences with '%20' as a workaround for bug 3664
        components.percentEncodedQuery = components.percentEncodedQuery?.stringByReplacingOccurrencesOfString("+", withString: "%20")
        let queryItems = components.queryItems
        
        // Get check-in items
        guard let eventID = CheckInData.queryItemValue(queryItems, itemName: ItemNames.EventID) else { return nil }
        guard let leaderboardName = CheckInData.queryItemValue(queryItems, itemName: ItemNames.LeaderboardName) else { return nil }
        if let competitorID = CheckInData.queryItemValue(queryItems, itemName: ItemNames.CompetitorID) {
            self.init(serverURL: serverURL,
                      eventID: eventID,
                      leaderboardName: leaderboardName,
                      competitorID: competitorID
            )
        } else if let markID = CheckInData.queryItemValue(queryItems, itemName: ItemNames.MarkID) {
            self.init(serverURL: serverURL,
                      eventID: eventID,
                      leaderboardName: leaderboardName,
                      markID: markID
            )
        } else {
            logError("\(#function)", error: "unknown check-in type")
            return nil
        }
    }
    
    convenience init?(urlString: String) {
        guard let url = NSURL(string: urlString) else { return nil }
        
        // Init with URL
        self.init(url: url)
    }

    convenience init?(checkIn: CheckIn) {
        if let competitorCheckIn = checkIn as? CompetitorCheckIn {
            self.init(competitorCheckIn: competitorCheckIn)
        } else if let markCheckIn = checkIn as? MarkCheckIn {
            self.init(markCheckIn: markCheckIn)
        } else {
            logError("\(#function)", error: "unknown check-in type")
            return nil
        }
    }

    // MARK: - Helper
    
    class private func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
