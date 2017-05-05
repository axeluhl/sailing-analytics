//
//  RequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
// See: http://wiki.sapsailing.com/wiki/tracking-app/api-v1#Competitor-Information-%28in-general%29

import UIKit

class RequestManager: NSObject {
    
    struct Error {
        
        let title, message: String
        
        init(title: String, message: String) {
            self.title = title
            self.message = message
        }
        
        init(message: String) {
            self.init(title: Translation.Common.Error.String, message: message)
        }
        
        init() {
            self.init(message: Translation.RequestManager.Failure.Message.String)
        }
        
    }
    
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
        static let MarkID = "markId"
        static let PushDeviceID = "pushDeviceId"
        static let ToMillis = "toMillis"
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
                        success: (regattaData: RegattaData) -> Void,
                        failure: (error: Error) -> Void)
    {
        getEventData(regattaData, success: success, failure: failure)
    }
    
    private func getEventData(regattaData: RegattaData,
                              success: (regattaData: RegattaData) -> Void,
                              failure: (error: Error) -> Void)
    {
        getEvent(regattaData.eventID,
                 success: { (data) in self.getEventDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                 failure: { (error) in failure(error: error) }
        )
    }
    
    private func getEventDataSuccess(eventData: EventData,
                                     regattaData: RegattaData,
                                     success: (regattaData: RegattaData) -> Void,
                                     failure: (error: Error) -> Void)
    {
        regattaData.eventData = eventData
        getLeaderboardData(regattaData, success: success, failure: failure)
    }
    
    private func getLeaderboardData(regattaData: RegattaData,
                                    success: (regattaData: RegattaData) -> Void,
                                    failure: (error: Error) -> Void)
    {
        getLeaderboard(regattaData.leaderboardName,
                       success: { (data) in self.getLeaderboardDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                       failure: { (error) in failure(error: error) }
        )
    }
    
    private func getLeaderboardDataSuccess(leaderboardData: LeaderboardData,
                                           regattaData: RegattaData,
                                           success: (regattaData: RegattaData) -> Void,
                                           failure: (error: Error) -> Void)
    {
        regattaData.leaderboardData = leaderboardData
        if (regattaData.competitorID != nil) {
            getCompetitorData(regattaData, competitorID: regattaData.competitorID!, success: success, failure: failure)
        } else if (regattaData.markID != nil) {
            getMarkData(regattaData, markID: regattaData.markID!, success: success, failure: failure)
        } else {
            failure(error: Error(message: Translation.RequestManager.NoDataFailure.Message.String))
        }
    }
    
    private func getCompetitorData(regattaData: RegattaData,
                                   competitorID: String,
                                   success: (regattaData: RegattaData) -> Void,
                                   failure: (error: Error) -> Void)
    {
        getCompetitor(competitorID,
                      success: { (data) in self.getCompetitorDataSuccess(data, regattaData: regattaData, competitorID: competitorID, success: success, failure: failure) },
                      failure: { (error) in failure(error: error) }
        )
    }
    
    private func getCompetitorDataSuccess(competitorData: CompetitorData,
                                          regattaData: RegattaData,
                                          competitorID: String,
                                          success: (regattaData: RegattaData) -> Void,
                                          failure: (error: Error) -> Void)
    {
        regattaData.competitorData = competitorData
        getTeamImageURL(competitorID, result: { (imageURL) in
            regattaData.teamImageURL = imageURL
            success(regattaData: regattaData)
        })
    }

    private func getMarkData(regattaData: RegattaData,
                             markID: String,
                             success: (regattaData: RegattaData) -> Void,
                             failure: (error: Error) -> Void)
    {
        getMark(regattaData.leaderboardName,
                markID: markID,
                success: { (data) in self.getMarkDataSuccess(data, regattaData: regattaData, success: success, failure: failure) },
                failure: { (error) in failure(error: error) }
        )
    }

    private func getMarkDataSuccess(markData: MarkData,
                                    regattaData: RegattaData,
                                    success: (regattaData: RegattaData) -> Void,
                                    failure: (error: Error) -> Void)
    {
        regattaData.markData = markData
        success(regattaData: regattaData)
    }

    // MARK: - Event
    
    private func getEvent(eventID: String,
                          success: (eventData: EventData) -> Void,
                          failure: (error: Error) -> Void)
    {
        let encodedEventID = eventID.stringByAddingPercentEncodingWithAllowedCharacters(.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/events/\(encodedEventID)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getEventSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getEventFailure(error, eventID: eventID, failure: failure) }
        )
    }
    
    private func getEventSuccess(responseObject: AnyObject, success: (eventData: EventData) -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(eventData: EventData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getEventFailure(error: NSError, eventID: String, failure: (error: Error) -> Void) -> Void {
        logError("\(#function)", error: error)
        let message = String(format: Translation.RequestManager.EventLoadingFailure.Message.String, eventID)
        failure(error: Error(message: message))
    }
    
    // MARK: - Leaderboard
    
    private func getLeaderboard(leaderboardName: String,
                                success: (leaderboardData: LeaderboardData) -> Void,
                                failure: (error: Error) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getLeaderboardSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getLeaderboardFailure(error, leaderboardName: leaderboardName, failure: failure) }
        )
    }
    
    private func getLeaderboardSuccess(responseObject: AnyObject, success: (leaderboardData: LeaderboardData) -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(leaderboardData: LeaderboardData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getLeaderboardFailure(error: NSError, leaderboardName: String, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        let message = String(format: Translation.RequestManager.LeaderboardLoadingFailure.Message.String, leaderboardName)
        failure(error: Error(message: message))
    }
    
    // MARK: - Competitor
    
    private func getCompetitor(competitorID: String,
                               success: (competitorData: CompetitorData) -> Void,
                               failure: (error: Error) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getCompetitorSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getCompetitorFailure(error, competitorID: competitorID, failure: failure) }
        )
    }
    
    private func getCompetitorSuccess(responseObject: AnyObject, success: (competitorData: CompetitorData) -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(competitorData: CompetitorData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getCompetitorFailure(error: NSError, competitorID: String, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        let message = String(format: Translation.RequestManager.CompetitorLoadingFailure.Message.String, competitorID)
        failure(error: Error(message: message))
    }
    
    // MARK: - Mark

    private func getMark(leaderboardName: String,
                         markID: String,
                         success: (markData: MarkData) -> Void,
                         failure: (error: Error) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.stringByAddingPercentEncodingWithAllowedCharacters(.URLPathAllowedCharacterSet()) ?? ""
        let encodedMarkID = markID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/marks/\(encodedMarkID)"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getMarkSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getMarkFailure(error, markID: markID, failure: failure) }
        )
    }

    private func getMarkSuccess(responseObject: AnyObject, success: (markData: MarkData) -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(markData: MarkData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getMarkFailure(error: NSError, markID: String, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        let message = String(format: Translation.RequestManager.MarkLoadingFailure.Message.String, markID)
        failure(error: Error(message: message))
    }

    // MARK: - Team
    
    private func getTeam(competitorID: String,
                         success: (teamData: TeamData) -> Void,
                         failure: (error: Error) -> Void)
    {
        let encodedCompetitorID = competitorID.stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)/team"
        manager.GET(urlString,
                    parameters: nil,
                    success: { (requestOperation, responseObject) in self.getTeamSuccess(responseObject, success: success) },
                    failure: { (requestOperation, error) in self.getTeamFailure(error, failure: failure) }
        )
    }
    
    private func getTeamSuccess(responseObject: AnyObject, success: (teamData: TeamData) -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(teamData: TeamData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    private func getTeamFailure(error: NSError, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        failure(error: Error())
    }
    
    private func getTeamImageURL(competitorID: String, result: (imageURL: String?) -> Void) {
        getTeam(competitorID,
                success: { (teamData) in result(imageURL: teamData.imageURL) },
                failure: { (error) in result(imageURL: nil) } // No image but that's ok
        )
    }
    
    // MARK: CheckIn
    
    func postCheckIn(regattaData: RegattaData!,
                     success: () -> Void,
                     failure: (error: Error) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceType] = DeviceType.IOS
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.PushDeviceID] = ""
        body[BodyKeys.FromMillis] = millisSince1970()
        switch regattaData.type() {
        case .Competitor:
            body[BodyKeys.CompetitorID] = regattaData.competitorData.competitorID
            break
        case .Mark:
            body[BodyKeys.MarkID] = regattaData.markData.markID
            break
        default:
            break
        }
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(regattaData.leaderboardData.name.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/start"
        manager.POST(urlString,
                     parameters: body,
                     success: { (requestOperation, responseObject) in self.postCheckInSuccess(responseObject, success: success) },
                     failure: { (requestOperation, error) in self.postCheckInFailure(error, failure: failure) }
        )
    }
    
    private func postCheckInSuccess(responseObject: AnyObject, success: () -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success()
    }
    
    private func postCheckInFailure(error: NSError, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        failure(error: Error())
    }
    
    // MARK: - CheckOut
    
    func postCheckOut(checkIn: CheckIn,
                      success:() -> Void,
                      failure: (error: Error) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceUUID] = Preferences.uuid
        body[BodyKeys.ToMillis] = millisSince1970()
        if let competitorCheckIn = checkIn as? CompetitorCheckIn {
            body[BodyKeys.CompetitorID] = competitorCheckIn.competitorID
        } else if let markCheckIn = checkIn as? MarkCheckIn {
            body[BodyKeys.MarkID] = markCheckIn.markID
        } else {
            logError("\(#function)", error: "unknown check-in type")
        }
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(checkIn.leaderboard.name.stringByAddingPercentEncodingWithAllowedCharacters(.URLHostAllowedCharacterSet())!)/device_mappings/end"
        manager.POST(urlString,
                     parameters: body,
                     success: { (requestOperation, responseObject) in self.postCheckOutSuccess(responseObject, success: success) },
                     failure: { (requestOperation, error) in self.postCheckOutFailure(error, failure: failure) }
        )
    }
    
    private func postCheckOutSuccess(responseObject: AnyObject, success: () -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success()
    }
    
    private func postCheckOutFailure(error: NSError, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        failure(error: Error(message: error.localizedDescription))
    }
    
    // MARK: - GPSFixes
    
    func postGPSFixes(gpsFixes: Array<GPSFix>,
                      success: () -> Void,
                      failure: (error: Error) -> Void)
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
                     success: { (requestOperation, responseObject) in self.postGPSFixesSuccess(responseObject, success: success) },
                     failure: { (requestOperation, error) in self.postGPSFixesFailure(error, failure: failure) }
        )
    }
    
    private func postGPSFixesSuccess(responseObject: AnyObject, success: () -> Void) {
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success()
    }
    
    private func postGPSFixesFailure(error: NSError, failure: (error: Error) -> Void) {
        logError("\(#function)", error: error)
        failure(error: Error(message: error.localizedDescription))
    }
    
    // MARK: - TeamImage
    
    func postTeamImageData(imageData: NSData,
                           competitorID: String,
                           success: (teamImageURL: String) -> Void,
                           failure: (error: Error) -> Void)
    {
        let urlString = "\(baseURLString)\(basePathString)/competitors/\(competitorID)/team/image"
        let url = NSURL(string: urlString) ?? NSURL()
        let request = NSMutableURLRequest(URL: url)
        request.setValue("image/jpeg", forHTTPHeaderField: "Content-Type")
        request.HTTPBody = imageData
        request.HTTPMethod = "POST"
        let dataTask = sessionManager.dataTaskWithRequest(request, completionHandler:{ (response, responseObject, error) in
            if (error != nil) {
                self.postTeamImageDataFailure(error!.localizedDescription, failure: failure)
            } else {
                self.postTeamImageDataSuccess(responseObject, success: success, failure: failure)
            }
        })
        dataTask.resume();
    }
    
    private func postTeamImageDataSuccess(responseObject: AnyObject?,
                                          success: (teamImageURL: String) -> Void,
                                          failure: (error: Error) -> Void)
    {
        guard let teamImageDictionary = responseObject as? [String: AnyObject] else {
            postTeamImageDataFailure(Translation.RequestManager.Failure.Message.String, failure: failure)
            return
        }
        guard let teamImageURL = teamImageDictionary[TeamImageKeys.TeamImageURL] as? String else {
            postTeamImageDataFailure(Translation.RequestManager.Failure.Message.String, failure: failure)
            return
        }
        logInfo("\(#function)", info: responseObjectToString(responseObject))
        success(teamImageURL: teamImageURL)
    }
    
    private func postTeamImageDataFailure(message: String, failure: (error: Error) -> Void) {
        logError("\(#function)", error: message)
        let title = Translation.RequestManager.TeamImageUploadFailure.Title.String
        failure(error: Error(title: title, message: message))
    }
    
    // MARK: - Helper
    
    private func millisSince1970() -> NSNumber {
        return NSNumber(longLong: Int64(NSDate().timeIntervalSince1970 * 1000))
    }

    private func responseObjectToString(responseObject: AnyObject?) -> String {
        return (responseObject as? String) ?? "response object is empty or cannot be casted"
    }

}
