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
    
    private struct TeamImageKeys {
        static let TeamImageURL = "teamImageUri"
    }
    
    private enum DeviceType {
        static let IOS = "iOS"
    }
    
    let baseURLString: String
    let manager: AFHTTPRequestOperationManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPRequestOperationManager(baseURL: NSURL(string: baseURLString))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        sessionManager = AFURLSessionManager(sessionConfiguration: NSURLSessionConfiguration.defaultSessionConfiguration())
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        super.init()
    }
    
    // MARK: - Regatta
    
    func getRegattaData(regattaData: RegattaData,
                        success: (RegattaData) -> Void,
                        failure: (String, NSError) -> Void)
    {
        getEventData(regattaData, success: success, failure: failure)
    }
    
    private func getEventData(regattaData: RegattaData,
                              success: (RegattaData) -> Void,
                              failure: (String, NSError) -> Void)
    {
        getEvent(regattaData.eventID,
                 success: { (data) in self.getEventDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                 failure: { (title, error) in failure(title, error) }
        )
    }
    
    private func getEventDataSuccess(eventData: EventData,
                                     regattaData: RegattaData,
                                     success: (RegattaData) -> Void,
                                     failure: (String, NSError) -> Void)
    {
        regattaData.eventData = eventData
        getLeaderboardData(regattaData, success: success, failure: failure)
    }
    
    private func getLeaderboardData(regattaData: RegattaData,
                                    success: (RegattaData) -> Void,
                                    failure: (String, NSError) -> Void)
    {
        getLeaderboard(regattaData.leaderboardName,
                       success: { (data) in self.getLeaderboardDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                       failure: { (title, error) in failure(title, error) }
        )
    }
    
    private func getLeaderboardDataSuccess(leaderboardData: LeaderboardData,
                                           regattaData: RegattaData,
                                           success: (RegattaData) -> Void,
                                           failure: (String, NSError) -> Void)
    {
        regattaData.leaderboardData = leaderboardData
        getCompetitorData(regattaData, success: success, failure: failure)
    }
    
    private func getCompetitorData(regattaData: RegattaData,
                                   success: (RegattaData) -> Void,
                                   failure: (String, NSError) -> Void)
    {
        getCompetitor(regattaData.competitorID,
                      success: { (data) in self.getCompetitorDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                      failure: { (title, error) in failure(title, error) }
        )
    }
    
    private func getCompetitorDataSuccess(competitorData: CompetitorData,
                                          regattaData: RegattaData,
                                          success: (RegattaData) -> Void,
                                          failure: (String, NSError) -> Void)
    {
        regattaData.competitorData = competitorData
        getTeamImageURL(regattaData.competitorID, result: { (imageURL) in
            regattaData.teamImageURL = imageURL
            success(regattaData)
        })
    }
    
    // MARK: - Event
    
    private func getEvent(eventID: String,
                          success: (EventData) -> Void,
                          failure: (String, NSError) -> Void)
    {
        let encodedEventID = eventID.stringByAddingPercentEncodingWithAllowedCharacters(.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/events/\(encodedEventID)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getEventSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getEventFailure(eventID, error: error, failure: failure) }
        )
    }
    
    private func getEventSuccess(responseObject: AnyObject, success: (EventData) -> Void) {
        success(EventData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getEventFailure(eventID: String, error: NSError, failure: (String, NSError) -> Void) -> Void {
        failure(String(format: Translation.RequestManager.EventLoadingFailure.Message.String, eventID), error)
    }
    
    // MARK: - Leaderboard
    
    private func getLeaderboard(leaderboardName: String,
                                success: (LeaderboardData) -> Void,
                                failure: (String, NSError) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getLeaderboardSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getLeaderboardFailure(leaderboardName, error: error, failure: failure) }
        )
    }
    
    private func getLeaderboardSuccess(responseObject: AnyObject, success: (LeaderboardData) -> Void) {
        success(LeaderboardData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getLeaderboardFailure(leaderboardName: String, error: NSError, failure: (String, NSError) -> Void) {
        failure(String(format: Translation.RequestManager.LeaderboardLoadingFailure.Message.String, leaderboardName), error)
    }
    
    // MARK: - Competitor
    
    private func getCompetitor(competitorID: String,
                               success: (CompetitorData) -> Void,
                               failure: (String, NSError) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getCompetitorSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getCompetitorFailure(competitorID, error: error, failure: failure) }
        )
    }
    
    private func getCompetitorSuccess(responseObject: AnyObject, success: (CompetitorData) -> Void) {
        success(CompetitorData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getCompetitorFailure(competitorID: String, error: NSError, failure: (String, NSError) -> Void) {
        failure(String(format: Translation.RequestManager.CompetitorLoadingFailure.Message.String, competitorID), error)
    }
    
    // MARK: - Team
    
    private func getTeam(competitorID: String!,
                         success: (AFHTTPRequestOperation!, AnyObject!) -> Void,
                         failure: (AFHTTPRequestOperation!, AnyObject!) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)/team"
        manager.GET(urlString, parameters: nil, success: success, failure: failure)
    }
    
    private func getTeamImageURL(competitorId: String!, result: (imageURL: String?) -> Void) {
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
    
    // MARK: CheckIn
    
    func postCheckIn(leaderboardName: String!,
                     competitorID: String!,
                     success: () -> Void,
                     failure: (title: String, message: String) -> Void)
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
        manager.POST(urlString,
                     parameters: body,
                     success: { (requestOperation, responseObject) in self.postCheckInSuccess(success) },
                     failure: { (requestOperation, error) in self.postCheckInFailure(error, failure: failure) }
        )
    }
    
    private func postCheckInSuccess(success: () -> Void) {
        success()
    }
    
    private func postCheckInFailure(error: NSError, failure: (title: String, message: String) -> Void) {
        failure(title: Translation.Common.Error.String, message: error.localizedDescription)
    }
    
    // MARK: - CheckOut
    
    func postCheckOut(leaderboardName: String!,
                      competitorId: String!,
                      success:() -> Void,
                      failure: (title: String, message: String) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.CompetitorID] = competitorId
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.ToMillis] = millisSince1970()
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/end"
        manager.POST(urlString,
                     parameters: body,
                     success: { (requestOperation, responseObject) in self.postCheckOutSuccess(success) },
                     failure: { (requestOperation, error) in self.postCheckOutFailure(error, failure: failure) }
        )
    }
    
    private func postCheckOutSuccess(success: () -> Void) {
        success()
    }
    
    private func postCheckOutFailure(error: NSError, failure: (title: String, message: String) -> Void) {
        failure(title: Translation.Common.Error.String, message: error.localizedDescription)
    }
    
    // MARK: - GPSFixes
    
    func postGPSFixes(gpsFixes: Array<GPSFix>,
                      success: () -> Void,
                      failure: (title: String, message: String) -> Void)
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
        manager.POST(urlString,
                     parameters: body,
                     success: { (requestOperation, responseObject) in self.postGPSFixesSuccess(success) },
                     failure: { (requestOperation, error) in self.postGPSFixesFailure(error, failure: failure) }
        )
    }
    
    private func postGPSFixesSuccess(success: () -> Void) {
        success()
    }
    
    private func postGPSFixesFailure(error: NSError, failure: (title: String, message: String) -> Void) {
        failure(title: Translation.Common.Error.String, message: error.localizedDescription)
    }
    
    // MARK: - TeamImage
    
    func postTeamImageData(imageData: NSData,
                           competitorID: String,
                           success: (teamImageURL: String) -> Void,
                           failure: (title: String, message: String) -> Void)
    {
        let urlString = "\(baseURLString)\(basePathString)/competitors/\(competitorID)/team/image"
        let url = NSURL(string: urlString) ?? NSURL()
        let request = NSMutableURLRequest(URL: url)
        request.setValue("image/jpeg", forHTTPHeaderField: "Content-Type")
        request.HTTPBody = imageData
        request.HTTPMethod = "POST"
        let dataTask = sessionManager.dataTaskWithRequest(request, completionHandler:{ (response, responseObject, error) in
            if (error != nil) {
                self.postTeamImageDataFailure(error.localizedDescription, failure: failure)
            } else {
                self.postTeamImageDataSuccess(responseObject, success: success, failure: failure)
            }
        })
        dataTask.resume();
    }
    
    private func postTeamImageDataSuccess(responseObject: AnyObject,
                                          success: (teamImageURL: String) -> Void,
                                          failure: (title: String, message: String) -> Void)
    {
        guard let teamImageDictionary = responseObject as? [String: AnyObject] else {
            postTeamImageDataFailure(Translation.RequestManager.Failure.Message.String, failure: failure)
            return
        }
        guard let teamImageURL = teamImageDictionary[TeamImageKeys.TeamImageURL] as? String else {
            postTeamImageDataFailure(Translation.RequestManager.Failure.Message.String, failure: failure)
            return
        }
        success(teamImageURL: teamImageURL)
    }
    
    private func postTeamImageDataFailure(message: String, failure: (title: String, message: String) -> Void) {
        failure(title: Translation.RequestManager.TeamImageUploadFailure.Title.String, message: message)
    }
    
    // MARK: - Helper
    
    private func millisSince1970() -> NSNumber {
        return NSNumber(longLong: Int64(NSDate().timeIntervalSince1970 * 1000))
    }
    
}
