//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 22/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class QRCodeData {
    
    struct Keys {
        static let eventId = "event_id"
        static let leaderBoardName = "leaderboard_name"
        static let competitorId = "competitor_id"        
    }
    
    var server: String?
    var eventId: String?
    var leaderBoardName: String?
    var competitorId: String?
 
    init() {
    }
    
    func parseString(urlString: String) -> Bool {
        let url = NSURL(string: urlString)
        
        if (url == nil || url!.scheme == nil || url!.host == nil) {
            return false
        }
        
        server = url!.scheme! + "://" + url!.host!
        if ((url!.port) != nil) {
            server! += ":" + url!.port!.stringValue
        }
        
        let queryString = url!.query
        if (queryString == nil) {
            return false
        }
        let urlComponents = queryString!.componentsSeparatedByString("&")
        var queryStringDictionary = [String:String]()
        for keyValuePair in urlComponents
        {
            let pairComponents = keyValuePair.componentsSeparatedByString("=")
            let key = pairComponents[0]
            let value = pairComponents[1]
            queryStringDictionary[key] = value
        }
        eventId = queryStringDictionary[QRCodeData.Keys.eventId]
        leaderBoardName = queryStringDictionary[QRCodeData.Keys.leaderBoardName]
        competitorId = queryStringDictionary[QRCodeData.Keys.competitorId]
        return eventId != nil && leaderBoardName != nil && competitorId != nil
     }
}
