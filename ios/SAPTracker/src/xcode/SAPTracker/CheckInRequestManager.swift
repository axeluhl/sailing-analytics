//
//  CheckInRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
// See: http://wiki.sapsailing.com/wiki/tracking-app/api-v1#Competitor-Information-%28in-general%29

import UIKit

enum CheckInRequestManagerError: Error {
    case communicationFailed
    case getBoatFailed
    case getCompetitorFailed
    case getEventFailed
    case getLeaderboardFailed
    case getMarkFailed
    case getTeamFailed
    case postCheckInFailed
    case postCheckOutFailed
    case postGPSFixFailed
    case postTeamImageFailed
    case teamImageURLIsInvalid
}

extension CheckInRequestManagerError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .communicationFailed:
            return Translation.CheckInRequestManagerError.CommunicationFailed.String
        case .getBoatFailed:
            return Translation.CheckInRequestManagerError.GetBoatFailed.String
        case .getCompetitorFailed:
            return Translation.CheckInRequestManagerError.GetCompetitorFailed.String
        case .getEventFailed:
            return Translation.CheckInRequestManagerError.GetEventFailed.String
        case .getLeaderboardFailed:
            return Translation.CheckInRequestManagerError.GetLeaderboardFailed.String
        case .getMarkFailed:
            return Translation.CheckInRequestManagerError.GetMarkFailed.String
        case .getTeamFailed:
            return Translation.CheckInRequestManagerError.GetTeamFailed.String
        case .postCheckInFailed:
            return Translation.CheckInRequestManagerError.PostCheckInFailed.String
        case .postCheckOutFailed:
            return Translation.CheckInRequestManagerError.PostCheckOutFailed.String
        case .postGPSFixFailed:
            return Translation.CheckInRequestManagerError.PostGPSFixFailed.String
        case .postTeamImageFailed:
            return Translation.CheckInRequestManagerError.PostTeamImageFailed.String
        case .teamImageURLIsInvalid:
            return Translation.CheckInRequestManagerError.TeamImageURLIsInvalid.String
        }
    }
}

class CheckInRequestManager: NSObject {
    
    fileprivate let basePathString = "/sailingserver/api/v1"
    
    fileprivate enum BodyKeys {
        static let BoatID = "boatId"
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
    
    fileprivate struct TeamImageKeys {
        static let TeamImageURL = "teamImageUri"
    }
    
    fileprivate enum DeviceType {
        static let IOS = "iOS"
    }
    
    let baseURLString: String
    let manager: AFHTTPSessionManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPSessionManager(baseURL: URL(string: baseURLString))
        manager.requestSerializer = AFJSONRequestSerializer() as AFHTTPRequestSerializer
        manager.requestSerializer.timeoutInterval = Application.RequestTimeout
        manager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        sessionManager = AFURLSessionManager(sessionConfiguration: URLSessionConfiguration.default)
        sessionManager.responseSerializer = AFJSONResponseSerializer() as AFHTTPResponseSerializer
        super.init()
    }
    
    // MARK: - Event
    
    func getEvent(
        eventID: String,
        success: @escaping (_ eventData: EventData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedEventID = eventID.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/events/\(encodedEventID)"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getEventSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getEventFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func getEventSuccess(responseObject: Any, success: (_ eventData: EventData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(EventData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    fileprivate func getEventFailure(error: Error, failure: (_ error: Error) -> Void) -> Void {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getEventFailed)
    }
    
    // MARK: - Leaderboard
    
    func getLeaderboard(
        leaderboardName: String,
        success: @escaping (_ leaderboardData: LeaderboardData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getLeaderboardSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getLeaderboardFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func getLeaderboardSuccess(responseObject: Any, success: (_ leaderboardData: LeaderboardData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(LeaderboardData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    fileprivate func getLeaderboardFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getLeaderboardFailed)
    }

    // MARK: - Boat

    func getBoat(
        boatID: String,
        success: @escaping (_ boatData: BoatData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedBoatID = boatID.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/boats/\(encodedBoatID)"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getBoatSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getBoatFailure(error: error, failure: failure) }
        )
    }

    fileprivate func getBoatSuccess(responseObject: Any, success: (_ boatData: BoatData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(BoatData(dictionary: responseObject as? [String: AnyObject]))
    }

    fileprivate func getBoatFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getBoatFailed)
    }

    // MARK: - Competitor
    
    func getCompetitor(
        competitorID: String,
        success: @escaping (_ competitorData: CompetitorData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedCompetitorID = competitorID.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getCompetitorSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getCompetitorFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func getCompetitorSuccess(responseObject: Any, success: (_ competitorData: CompetitorData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(CompetitorData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    fileprivate func getCompetitorFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getCompetitorFailed)
    }
    
    // MARK: - Mark

    func getMark(
        leaderboardName: String,
        markID: String,
        success: @escaping (_ markData: MarkData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedMarkID = markID.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/marks/\(encodedMarkID)"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getMarkSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getMarkFailure(error: error, failure: failure) }
        )
    }

    fileprivate func getMarkSuccess(responseObject: Any, success: (_ markData: MarkData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(MarkData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    fileprivate func getMarkFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getMarkFailed)
    }

    // MARK: - Team
    
    func getTeam(
        competitorID: String,
        success: @escaping (_ teamData: TeamData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let encodedCompetitorID = competitorID.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/competitors/\(encodedCompetitorID)/team"
        manager.get(
            urlString,
            parameters: nil,
            progress: nil,
            success: { (requestOperation, responseObject) in self.getTeamSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.getTeamFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func getTeamSuccess(responseObject: Any, success: (_ teamData: TeamData) -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(TeamData(dictionary: responseObject as? [String: AnyObject]))
    }
    
    fileprivate func getTeamFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.getTeamFailed)
    }
    
    func getTeamImageURL(competitorID: String, result: @escaping (_ imageURL: String?) -> Void) {
        getTeam(
            competitorID: competitorID,
            success: { (teamData) in result(teamData.imageURL) },
            failure: { (error) in result(nil) } // No image but that's ok
        )
    }
    
    // MARK: - CheckIn
    
    func postCheckIn(
        checkInData: CheckInData!,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceType] = DeviceType.IOS as AnyObject?
        body[BodyKeys.DeviceUUID] = Preferences.uuid as AnyObject?
        body[BodyKeys.PushDeviceID] = "" as AnyObject?
        body[BodyKeys.FromMillis] = millisSince1970()
        switch checkInData.type {
        case .boat:
            body[BodyKeys.BoatID] = checkInData.boatData.boatID as AnyObject?
            break
        case .competitor:
            body[BodyKeys.CompetitorID] = checkInData.competitorData.competitorID as AnyObject?
            break
        case .mark:
            body[BodyKeys.MarkID] = checkInData.markData.markID as AnyObject?
            break
        }
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(checkInData.leaderboardData.name.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!)/device_mappings/start"
        manager.post(
            urlString,
            parameters: body,
            progress: nil,
            success: { (requestOperation, responseObject) in self.postCheckInSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.postCheckInFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func postCheckInSuccess(responseObject: Any, success: () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postCheckInFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.postCheckInFailed)
    }
    
    // MARK: - CheckOut
    
    func postCheckOut(
        _ checkIn: CheckIn,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceUUID] = Preferences.uuid as AnyObject?
        body[BodyKeys.ToMillis] = millisSince1970()
        if let competitorCheckIn = checkIn as? CompetitorCheckIn {
            body[BodyKeys.CompetitorID] = competitorCheckIn.competitorID as AnyObject?
        } else if let markCheckIn = checkIn as? MarkCheckIn {
            body[BodyKeys.MarkID] = markCheckIn.markID as AnyObject?
        } else {
            logError(name: "\(#function)", error: "unknown check-in type")
        }
        
        // Post body
        let urlString = "\(basePathString)/leaderboards/\(checkIn.leaderboard.name.addingPercentEncoding(withAllowedCharacters: .urlHostAllowed)!)/device_mappings/end"
        manager.post(
            urlString,
            parameters: body,
            progress: nil,
            success: { (requestOperation, responseObject) in self.postCheckOutSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.postCheckOutFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func postCheckOutSuccess(responseObject: Any, success: () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postCheckOutFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.postCheckOutFailed)
    }
    
    // MARK: - GPSFixes
    
    func postGPSFixes(
        gpsFixes: Array<GPSFix>,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        // Setup fixes
        let fixes = gpsFixes.map { (gpsFix) -> [String: AnyObject] in [
            BodyKeys.FixesCourse: gpsFix.course as AnyObject,
            BodyKeys.FixesLatitude: gpsFix.latitude as AnyObject,
            BodyKeys.FixesLongitude: gpsFix.longitude as AnyObject,
            BodyKeys.FixesSpeed: gpsFix.speed as AnyObject,
            BodyKeys.FixesTimestamp: gpsFix.timestamp as AnyObject]
        }
        
        // Setup body
        var body = [String: AnyObject]()
        body[BodyKeys.DeviceUUID] = Preferences.uuid as AnyObject?
        body[BodyKeys.Fixes] = fixes as AnyObject?
        
        // Post body
        let urlString = "\(basePathString)/gps_fixes"
        manager.post(
            urlString,
            parameters: body,
            progress: nil,
            success: { (requestOperation, responseObject) in self.postGPSFixesSuccess(responseObject: responseObject, success: success) },
            failure: { (requestOperation, error) in self.postGPSFixesFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func postGPSFixesSuccess(responseObject: Any, success: () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postGPSFixesFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(CheckInRequestManagerError.postGPSFixFailed)
    }
    
    // MARK: - TeamImage
    
    func postTeamImageData(
        imageData: Data,
        competitorID: String,
        success: @escaping (_ teamImageURL: String) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        guard let teamImageURL = URL(string: "\(baseURLString)\(basePathString)/competitors/\(competitorID)/team/image") else {
            failure(CheckInRequestManagerError.teamImageURLIsInvalid)
            return
        }
        
        postTeamImageData(
            imageData: imageData,
            teamImageURL: teamImageURL,
            success: success,
            failure: failure
        )
    }
    
    fileprivate func postTeamImageData(
        imageData: Data,
        teamImageURL: URL,
        success: @escaping (_ teamImageURL: String) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let request = NSMutableURLRequest(url: teamImageURL)
        request.setValue("image/jpeg", forHTTPHeaderField: "Content-Type")
        request.httpBody = imageData
        request.httpMethod = "POST"
        let dataTask = sessionManager.dataTask(with: request as URLRequest, completionHandler:{ (response, responseObject, error) in
            if (error != nil) {
                self.postTeamImageDataFailure(error: error!, failure: failure)
            } else {
                self.postTeamImageDataSuccess(responseObject: responseObject, success: success, failure: failure)
            }
        })
        dataTask.resume();
    }
    
    fileprivate func postTeamImageDataSuccess(
        responseObject: Any?,
        success: (_ teamImageURL: String) -> Void,
        failure: (_ error: Error) -> Void)
    {
        guard let teamImageDictionary = responseObject as? [String: AnyObject] else {
            postTeamImageDataFailure(error: CheckInRequestManagerError.communicationFailed, failure: failure)
            return
        }
        guard let teamImageURL = teamImageDictionary[TeamImageKeys.TeamImageURL] as? String else {
            postTeamImageDataFailure(error: CheckInRequestManagerError.communicationFailed, failure: failure)
            return
        }
        
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success(teamImageURL)
    }
    
    fileprivate func postTeamImageDataFailure(error: Error, failure: (_ error: Error) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error)
    }
    
    // MARK: - Helper
    
    fileprivate func millisSince1970() -> NSNumber {
        return NSNumber(value: Int64(Date().timeIntervalSince1970 * 1000) as Int64)
    }

    fileprivate func responseObjectToString(responseObject: Any?) -> String {
        if (responseObject == nil) {
            return "response is empty"
        } else if let responseString = responseObject as? String {
            return responseString
        } else if let responseDictionary = responseObject as? Dictionary<String, Any> {
            return responseDictionary.description
        }
        return "response cannot be casted"
    }

}
