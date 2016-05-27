//
//  CheckInRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class CheckInRequestManager: NSObject {

    private let baseURLString = "/sailingserver/api/v1"
    
    private enum BodyKeys {
        static let competitorID = "competitorId"
        static let deviceType = "deviceType"
        static let deviceUUID = "deviceUuid"
        static let pushDeviceID = "pushDeviceId"
        static let fromMillis = "fromMillis"
    }
    
    private enum DeviceType {
        static let iOS = "iOS"
    }
    
    let checkInData: CheckInData!
    let manager: AFHTTPRequestOperationManager!
    
    init(checkInData: CheckInData!) {
        self.checkInData = checkInData
        
        // Initialize request manager and json request and response serializer
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: self.checkInData.serverURL))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        
        // Call super
        super.init()
    }
    
    // MARK: - Event
    
    func getEvent(eventID: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let encodedEventID = eventID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(baseURLString)/events/\(encodedEventID)"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Leaderboard
    
    func getLeaderboard(leaderboardName: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let encodedLeaderboardName = leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(baseURLString)/leaderboards/\(encodedLeaderboardName)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Competitor
    
    func getCompetitor(competitorID: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(baseURLString)/competitors/\(encodedCompetitorID)"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Team
    
    func getTeam(competitorID: String!, success: (AFHTTPRequestOperation!, AnyObject!) -> Void, failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(baseURLString)/competitors/\(encodedCompetitorID)/team"
        manager!.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    func getTeamImageURI(competitorId: String!, result: (imageURI: String?) -> Void) {
        getTeam(competitorId,
                success: { (operation, responseObject) -> Void in self.getTeamImageURISucceed(responseObject, result: result) },
                failure: { (operation, error) -> Void in self.getTeamImageURIFailed(error, result: result) }
        )
    }
    
    private func getTeamImageURISucceed(responseObject: AnyObject, result: (imageURI: String?) -> Void) {
        if let team = responseObject as? [String: AnyObject], let imageURI = team["imageUri"] as? String {
            result(imageURI: imageURI)
        } else {
            result(imageURI: nil)
        }
    }

    private func getTeamImageURIFailed(error: AnyObject, result: (imageURI: String?) -> Void) {
        result(imageURI: nil) // No image but that's ok
    }
    
    // MARK: - CheckIn
    
    func checkIn(leaderboardName: String!,
                 competitorID: String!,
                 deviceUUID: String!,
                 pushDeviceID: String!,
                 fromMillis: Int64!,
                 success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                 failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.competitorID] = competitorID
        body[BodyKeys.deviceType] = DeviceType.iOS
        body[BodyKeys.deviceUUID] = deviceUUID
        body[BodyKeys.pushDeviceID] = pushDeviceID
        body[BodyKeys.fromMillis] = NSNumber(longLong: fromMillis)
        
        // Post body
        // TODO: - Check the leaderbord name encoding
        let urlString = "\(baseURLString)/leaderboards/\(leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/start"
        manager!.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
}
