//
//  CheckInData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class CheckInData: NSObject {
    
    fileprivate struct ItemNames {
        static let BoatID = "boat_id"
        static let EventID = "event_id"
        static let LeaderboardName = "leaderboard_name"
        static let CompetitorID = "competitor_id"
        static let MarkID = "mark_id"
    }
    
    public enum CheckInDataType {
        case boat
        case competitor
        case mark
    }
    
    let serverURL: String
    let eventID: String
    let leaderboardName: String
    let boatID: String?
    let competitorID: String?
    let markID: String?
    let isTraining: Bool
    let type: CheckInDataType

    var eventData = EventData()
    var leaderboardData = LeaderboardData()
    var boatData = BoatData()
    var competitorData = CompetitorData()
    var markData = MarkData()
    var teamImageURL: String?
    
    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         competitorID: String,
         isTraining: Bool)
    {
        self.boatID = nil
        self.competitorID = competitorID
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = nil
        self.serverURL = serverURL
        self.isTraining = isTraining
        self.type = CheckInDataType.competitor
        super.init()
    }

    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         markID: String,
         isTraining: Bool)
    {
        self.boatID = nil
        self.competitorID = nil
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = markID
        self.serverURL = serverURL
        self.isTraining = isTraining
        type = CheckInDataType.mark
        super.init()
    }

    init(serverURL: String,
         eventID: String,
         leaderboardName: String,
         boatID: String,
         isTraining: Bool)
    {
        self.boatID = boatID
        self.competitorID = nil
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.markID = nil
        self.serverURL = serverURL
        self.isTraining = isTraining
        type = CheckInDataType.boat
        super.init()
    }

    convenience init(boatCheckIn: BoatCheckIn) {
        self.init(
            serverURL: boatCheckIn.serverURL,
            eventID: boatCheckIn.event.eventID,
            leaderboardName: boatCheckIn.leaderboard.name,
            boatID: boatCheckIn.boatID,
            isTraining: boatCheckIn.isTraining.boolValue
        )
    }

    convenience init(competitorCheckIn: CompetitorCheckIn) {
        self.init(
            serverURL: competitorCheckIn.serverURL,
            eventID: competitorCheckIn.event.eventID,
            leaderboardName: competitorCheckIn.leaderboard.name,
            competitorID: competitorCheckIn.competitorID,
            isTraining: competitorCheckIn.isTraining.boolValue
        )
    }

    convenience init(markCheckIn: MarkCheckIn) {        
        self.init(
            serverURL: markCheckIn.serverURL,
            eventID: markCheckIn.event.eventID,
            leaderboardName: markCheckIn.leaderboard.name,
            markID: markCheckIn.markID,
            isTraining: markCheckIn.isTraining.boolValue
        )
    }

    convenience init?(url: URL) {

        // handle the case where a user uses the app to scan a QR code containing a branch.io deeplink
        let branchQueryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems
        let checkinUrl = branchQueryItems?.first(where: {$0.name == "checkinUrl"}).map { URL(string: $0.value!)! }

        let unwrappedUrl = checkinUrl ?? url

        guard let host = unwrappedUrl.host else { return nil }
        
        // Set server URL
        let serverURL = unwrappedUrl.scheme! + "://" + host + (unwrappedUrl.port != nil ? ":\(unwrappedUrl.port!)" : "")
        
        // In query component replace '+' occurrences with '%20' as a workaround for bug 3664
        var components = URLComponents(string: unwrappedUrl.absoluteString)
        components?.percentEncodedQuery = components?.percentEncodedQuery?.replacingOccurrences(of: "+", with: "%20")
        guard let queryItems = components?.queryItems else { return nil }
        
        // Get check-in items
        guard let eventID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.EventID) else { return nil }
        guard let leaderboardName = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.LeaderboardName) else { return nil }
        if let competitorID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.CompetitorID) {
            self.init(
                serverURL: serverURL,
                eventID: eventID,
                leaderboardName: leaderboardName,
                competitorID: competitorID,
                isTraining: false
            )
        } else if let markID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.MarkID) {
            self.init(
                serverURL: serverURL,
                eventID: eventID,
                leaderboardName: leaderboardName,
                markID: markID,
                isTraining: false
            )
        } else if let boatID = CheckInData.queryItemValue(queryItems: queryItems, itemName: ItemNames.BoatID) {
            self.init(
                serverURL: serverURL,
                eventID: eventID,
                leaderboardName: leaderboardName,
                boatID: boatID,
                isTraining: false
            )
        } else {
            logError(name: "\(#function)", error: "unknown check-in type")
            return nil
        }
    }
    
    convenience init?(urlString: String) {
        guard let url = URL(string: urlString) else { return nil }
        
        // Init with URL
        self.init(url: url)
    }

    convenience init?(checkIn: CheckIn) {
        if let boatCheckIn = checkIn as? BoatCheckIn {
            self.init(boatCheckIn: boatCheckIn)
        } else if let competitorCheckIn = checkIn as? CompetitorCheckIn {
            self.init(competitorCheckIn: competitorCheckIn)
        } else if let markCheckIn = checkIn as? MarkCheckIn {
            self.init(markCheckIn: markCheckIn)
        } else {
            logError(name: "\(#function)", error: "unknown check-in type")
            return nil
        }
    }
    
    convenience init?(createTrainingData: CreateTrainingData) {
        guard let eventID = createTrainingData.createEventData?.eventID else { return nil }
        guard let leaderboardName = createTrainingData.createEventData?.leaderboardName else { return nil }
        guard let competitorID = createTrainingData.competitorID else { return nil }
        self.init(
            serverURL: createTrainingData.serverURL,
            eventID: eventID,
            leaderboardName: leaderboardName,
            competitorID: competitorID,
            isTraining: true
        )
    }
    
    // MARK: - Helper
    
    class fileprivate func queryItemValue(queryItems: [URLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
}
