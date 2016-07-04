//
//  RequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
// http://wiki.sapsailing.com/wiki/tracking-app/api-v1#Competitor-Information-%28in-general%29

import UIKit

class RequestManager: NSObject {

    private let basePathString = "/sailingserver/api/v1"
    
    private enum BodyKeys {
        static let CompetitorID = "competitorId"
        static let DeviceType = "deviceType"
        static let DeviceUUID = "deviceUuid"
        static let Fixes = "fixes"
        static let FixesCourse = "course"
        static let FixesLatitude = "latitude"
        static let FixesLongitude = "longitude"
        static let FixesSpeed = "speed"
        static let FixesTimestamp = "timestamp"
        static let FromMillis = "fromMillis"
        static let PushDeviceID = "pushDeviceId"
        static let ToMillis = "toMillis"
    }
    
    private enum TeamKeys {
        static let ImageURL = "imageUri"
    }
    
    private enum DeviceType {
        static let IOS = "iOS"
    }
    
    let baseURLString: String!
    let manager: AFHTTPRequestOperationManager!
    let sessionManager: AFURLSessionManager!
    
    init(baseURLString: String!) {
        self.baseURLString = baseURLString
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: baseURLString))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        sessionManager = AFURLSessionManager(sessionConfiguration: NSURLSessionConfiguration.defaultSessionConfiguration())
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        super.init()
    }
    
    // MARK: - Event
    
    func getEvent(eventID: String!,
                  success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                  failure: (AFHTTPRequestOperation!, AnyObject!) -> Void) {
        let encodedEventID = eventID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/events/\(encodedEventID)"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Leaderboard
    
    func getLeaderboard(leaderboardName: String!,
                        success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                        failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Competitor
    
    func getCompetitor(competitorID: String!,
                       success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                       failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    // MARK: - Team
    
    func getTeam(competitorID: String!,
                 success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                 failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)/team"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    func getTeamImageURL(competitorId: String!, result: (imageURL: String?) -> Void) {
        getTeam(competitorId,
                success: { (operation, responseObject) -> Void in self.getTeamImageURLSucceed(responseObject, result: result) },
                failure: { (operation, error) -> Void in self.getTeamImageURLFailed(error, result: result) }
        )
    }
    
    private func getTeamImageURLSucceed(responseObject: AnyObject, result: (imageURL: String?) -> Void) {
        if let team = responseObject as? [String: AnyObject], let imageURL = team[TeamKeys.ImageURL] as? String {
            result(imageURL: imageURL)
        } else {
            result(imageURL: nil)
        }
    }

    private func getTeamImageURLFailed(error: AnyObject, result: (imageURL: String?) -> Void) {
        result(imageURL: nil) // No image but that's ok
    }
    
    // MARK: - Post
    
    func postCheckIn(leaderboardName: String!,
                     competitorID: String!,
                     success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                     failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.CompetitorID] = competitorID
        body[BodyKeys.DeviceType] = DeviceType.IOS
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.PushDeviceID] = ""
        body[BodyKeys.FromMillis] = millisSince1970()
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/start"
        manager.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    func postGPSFixes(gpsFixes: [GPSFix]!,
                      success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                      failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        // Setup fixes
        let fixes = gpsFixes.map { (gpsFix) -> [String: AnyObject] in [
            BodyKeys.FixesCourse: gpsFix.course,
            BodyKeys.FixesLatitude: gpsFix.latitude,
            BodyKeys.FixesLongitude: gpsFix.longitude,
            BodyKeys.FixesSpeed: gpsFix.speed,
            BodyKeys.FixesTimestamp: gpsFix.timestamp]
        }
        
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.Fixes] = fixes
        
        // Post body
        let urlString = "\(basePathString)/gps_fixes"
        manager.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    func postCheckOut(leaderboardName: String!,
                      competitorId: String!,
                      success:(AFHTTPRequestOperation!, AnyObject!) -> Void,
                      failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.CompetitorID] = competitorId
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.ToMillis] = millisSince1970()
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/end"
        manager.POST(urlString, parameters: body, success: success, failure: failure)
    }
    
    func postTeamImageData(imageData: NSData!,
                           competitorId: String!,
                           success: (AnyObject!) -> Void,
                           failure: (AnyObject!) -> Void)
    {
        let urlString = "\(baseURLString)\(basePathString)/competitors/\(competitorId)/team/image"
        let url = NSURL(string: urlString)
        let request = NSMutableURLRequest(URL: url!)
        request.setValue("image/jpeg", forHTTPHeaderField: "Content-Type")
        request.HTTPBody = imageData
        request.HTTPMethod = "POST"
        let dataTask = sessionManager.dataTaskWithRequest(request, completionHandler:{ (response, responseObject, error) in
            if (error != nil) {
                failure(error)
            } else {
                success(responseObject)
            }
        })
        dataTask.resume();
    }
    
    // MARK: - Connection
    
    var suspended: Bool { get { return manager.operationQueue.suspended } }
    
    // MARK: - Helper
    
    private func millisSince1970() -> NSNumber {
        return NSNumber(longLong: Int64(NSDate().timeIntervalSince1970 * 1000))
    }
    
}
