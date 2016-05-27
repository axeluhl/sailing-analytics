//
//  CheckInData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class CheckInData: NSObject {
    
    struct PropertyKey {
        static let serverURL = "serverURL"
        static let eventID = "eventID"
        static let leaderboardName = "leaderboardName"
        static let competitorID = "competitorID"
    }
    
    private struct ItemNames {
        static let eventID = "event_id"
        static let leaderboardName = "leaderboard_name"
        static let competitorID = "competitor_id"
    }
    
    private struct CompetitorKeys {
        static let id = "id"
        static let name = "name"
        static let sailID = "sailID"
    }
    
    private struct LeaderboardKeys {
        static let name = "name"
    }
    
    public let serverURL: String!
    public let eventID: String!
    public let leaderboardName: String!
    public let competitorID: String!

    public var eventDictionary: [String: AnyObject]?
    public var leaderboardDictionary: [String: AnyObject]?
    public var competitorDictionary: [String: AnyObject]?
    public var teamImageURI: String?
    
    public init?(serverURL: String!,
                 eventID: String!,
                 leaderboardName: String!,
                 competitorID: String!)
    {
        // Fails if any value is nil
        guard serverURL != nil &&
            eventID != nil &&
            leaderboardName != nil &&
            competitorID != nil
            else { return nil }
        
        self.serverURL = serverURL
        self.eventID = eventID
        self.leaderboardName = leaderboardName
        self.competitorID = competitorID
        
        super.init()
    }
    
    public convenience init?(urlString: String!) {
        
        // Fails if url string is invalid
        guard let url = NSURL(string: urlString)
            else { return nil }
        
        // Init with url
        self.init(url: url)
    }
    
    public convenience init?(url: NSURL!) {
        
        // Fails if url is invalid
        guard url != nil &&
            url.host != nil &&
            url.query != nil
            else { return nil }
        
        // Fails if components are invalid
        guard let components = NSURLComponents(string: url.absoluteString)
            else { return nil }
        
        // In query component replace '+' occurrences with '%20' as a workaround for bug 3664
        components.percentEncodedQuery = components.percentEncodedQuery?.stringByReplacingOccurrencesOfString("+", withString: "%20")
        let queryItems = components.queryItems
        
        // Set server URL and check-in items
        let serverURL = url.scheme + "://" + url.host! + (url.port == nil ? "" : ":" + url.port!.stringValue)
        let eventID = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.eventID)
        let leaderboardName = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.leaderboardName)
        let competitorID = CheckInData.queryItemValue(queryItems, itemName: CheckInData.ItemNames.competitorID)
        
        // Init with values
        self.init(serverURL: serverURL,
                  eventID: eventID,
                  leaderboardName: leaderboardName,
                  competitorID: competitorID)
    }
    
    // MARK: - Leaderboard
    
    func dictionaryLeaderboardName() -> String! {
        return valueFromLeaderboard(forKey: LeaderboardKeys.name)
    }
    
    private func valueFromLeaderboard(forKey key: String) -> String {
        return valueFromDictionary(leaderboardDictionary, forKey: key)
    }
    
    // MARK: - Competitor
    
    func dictionaryCompetitorId() -> String {
        return valueFromCompetitor(forKey: CompetitorKeys.id)
    }
    
    func dictionaryCompetitorName() -> String! {
        return valueFromCompetitor(forKey: CompetitorKeys.name)
    }
    
    func dictionaryCompetitorSailId() -> String! {
        return valueFromCompetitor(forKey: CompetitorKeys.sailID)
    }
    
    private func valueFromCompetitor(forKey key: String) -> String {
        return valueFromDictionary(competitorDictionary, forKey: key)
    }
    
    // MARK: - Helper
    
    private class func queryItemValue(queryItems: [NSURLQueryItem]?, itemName: String) -> String? {
        return queryItems?.filter({(item) -> Bool in item.name == itemName}).first?.value
    }
    
    private func valueFromDictionary(dictionary: [String: AnyObject]?, forKey key: String!) -> String {
        if let value = dictionary?[key] {
            return value as! String
        }
        return ""
    }
    
}
