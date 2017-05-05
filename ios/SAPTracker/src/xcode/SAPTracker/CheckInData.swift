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
        case None
        case Competitor
        case Mark
    }
    
    let serverURL: String
    let eventID: String
    let leaderboardName: String
    let competitorID: String?
    let markID: String?

    var eventData = EventData()
    var leaderboardData = LeaderboardData()
    var competitorData = CompetitorData()
    var markData = MarkData()
    var teamImageURL: String?
    
    override init() {
        serverURL = ""
        eventID = ""
        leaderboardName = ""
        competitorID = nil
        markID = nil
        super.init()
    }
    
    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         competitorID: String?,
         markID: String?)
    {
        self.serverURL = serverURL
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.competitorID = competitorID
        self.markID = markID
        super.init()
    }
    
    init(competitorCheckIn: CompetitorCheckIn) {
        serverURL = competitorCheckIn.serverURL
        eventID = competitorCheckIn.event.eventID
        competitorID = competitorCheckIn.competitorID
        leaderboardName = competitorCheckIn.leaderboard.name
        markID = ""
        super.init()
    }

    init(markCheckIn: MarkCheckIn) {
        serverURL = markCheckIn.serverURL
        eventID = markCheckIn.event.eventID
        competitorID = ""
        leaderboardName = markCheckIn.leaderboard.name
        markID = markCheckIn.markID
        super.init()
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
        let competitorID = CheckInData.queryItemValue(queryItems, itemName: ItemNames.CompetitorID)
        let markID = CheckInData.queryItemValue(queryItems, itemName: ItemNames.MarkID)
        guard competitorID != nil || markID != nil else { return nil }
        
        // Init with values
        self.init(serverURL: serverURL,
                  eventID: eventID,
                  leaderboardName: leaderboardName,
                  competitorID: competitorID,
                  markID: markID)
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

    // MARK: - Getter

    func type() -> Type {
        return competitorID != nil ? .Competitor : markID != nil ? .Mark : .None
    }

    // MARK: - Helper
    
    class private func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
