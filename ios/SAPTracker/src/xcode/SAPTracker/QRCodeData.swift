//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

public class QRCodeData {
    
    struct Keys {
        static let eventId = "event_id"
        static let leaderBoardName = "leaderboard_name"
        static let competitorId = "competitor_id"
    }
    
    public var serverUrl: String?
    public var eventId: String?
    public var leaderBoardName: String?
    public var competitorId: String?

    public init() {
    }
    
    public func parseString(urlString: String) -> Bool {
        let url = NSURL(string: urlString)
        if (url == nil || url!.host == nil || url!.query == nil) {
            return false
        }
        
        // Set server url
        serverUrl = url!.scheme + "://" + url!.host!
        if ((url!.port) != nil) {
            serverUrl! += ":" + url!.port!.stringValue
        }
        
        let comps = NSURLComponents(string: (url?.absoluteString)!);
        comps!.percentEncodedQuery = comps!.percentEncodedQuery!.stringByReplacingOccurrencesOfString("+", withString: "%20");
     
        // Get query parameter
        eventId = getQueryStringParameter(comps!, param: QRCodeData.Keys.eventId) // queryStringDictionary[QRCodeData.Keys.eventId]
        leaderBoardName = getQueryStringParameter(comps!, param: QRCodeData.Keys.leaderBoardName)
        competitorId = getQueryStringParameter(comps!, param: QRCodeData.Keys.competitorId) // queryStringDictionary[QRCodeData.Keys.competitorId]
        
        return eventId != nil && leaderBoardName != nil && competitorId != nil
    }
    
    func getQueryStringParameter(comps: NSURLComponents, param: String) -> String? {
        return comps.queryItems?.filter({(item) -> Bool in item.name == param}).first?.value;
    }
    
}
