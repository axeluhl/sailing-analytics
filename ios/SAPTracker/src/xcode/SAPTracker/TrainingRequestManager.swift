//
//  TrainingRequestManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum TrainingRequestManagerError: Error {
    case invalidResponse
}

class TrainingRequestManager: NSObject {
    
    static let OneWeekInMillis: Int64 = 7 * 24 * 60 * 60 * 1000
    
    fileprivate let basePathString = "/sailingserver/api/v1"
    
    let baseURLString: String
    let manager: AFHTTPSessionManager
    let sessionManager: AFURLSessionManager
    
    init(baseURLString: String = "") {
        self.baseURLString = baseURLString
        manager = AFHTTPSessionManager(baseURL: URL(string: baseURLString))
        manager.requestSerializer = AFHTTPRequestSerializer()
        manager.requestSerializer.timeoutInterval = Application.RequestTimeout
        manager.responseSerializer = AFHTTPResponseSerializer()
        sessionManager = AFURLSessionManager(sessionConfiguration: URLSessionConfiguration.default)
        sessionManager.responseSerializer = AFHTTPResponseSerializer()
        super.init()
    }
    
    // MARK: - CreateEvent
    
    func postCreateEvent(
        boatClassName: String,
        success: @escaping (_ createEventData: CreateEventData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let urlString = "\(basePathString)/events/createEvent"
        var body = [String: AnyObject]()
        //        body["eventname"] = "raimund" as AnyObject
        //        body["eventdescription"] = "raimund" as AnyObject
        //        body["startdate"] = "" as AnyObject
        //        body["startdateasmillis"] = "" as AnyObject
        //        body["enddate"] = "" as AnyObject
        //        body["enddateasmillis"] = "" as AnyObject
        body["venuename"] = "hamburg" as AnyObject
        //        body["venuelat"] = "" as AnyObject
        //        body["venuelng"] = "" as AnyObject
        //        body["ispublic"] = "" as AnyObject
        //        body["officialwebsiteurl"] = "" as AnyObject
        //        body["baseurl"] = "" as AnyObject
        //        body["leaderboardgroupids"] = "" as AnyObject
        //        body["createleaderboardgroup"] = "" as AnyObject
        body["createregatta"] = "true" as AnyObject
        body["boatclassname"] = boatClassName as AnyObject
        body["numberofraces"] = "0" as AnyObject
        manager.post(urlString, parameters: body, progress: nil, success: { (requestOperation, responseObject) in
            self.postCreateEventSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postCreateEventFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postCreateEventSuccess(responseObject: Any?, success: (_ createEventData: CreateEventData) -> Void, failure: (_ error: Error, _ message: String?) -> Void) {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(CreateEventData(dictionary: jsonObject as? [String: AnyObject]))
            } catch {
                logError(name: "\(#function)", error: error)
                postCreateEventFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
            }
        } else {
            postCreateEventFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
        }
    }
    
    fileprivate func postCreateEventFailure(error: Error, failure: (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - EventUpdate
    
    func putFinishEventUpdate(
        eventID: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let endDateAsMillis = millisSince1970()
        putEventUpdate(eventID: eventID, endDateAsMillis: endDateAsMillis, success: success, failure: failure)
    }
    
    func putReactivateEventUpdate(
        eventID: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let endDateAsMillis = NSNumber(value: millisSince1970().int64Value + TrainingRequestManager.OneWeekInMillis)
        putEventUpdate(eventID: eventID, endDateAsMillis: endDateAsMillis, success: success, failure: failure)
    }
    
    fileprivate func putEventUpdate(
        eventID: String,
        endDateAsMillis: NSNumber,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedEventID = eventID.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/events/\(encodedEventID)/update"
        var body = [String: AnyObject]()
        body["enddateasmillis"] = endDateAsMillis as AnyObject
        manager.put(urlString, parameters: body, success: { (requestOperation, responseObject) in
            success()
        }) { (requestOperation, error) in
            failure(error, self.stringForError(error))
        }
    }
    
    // MARK: - RegattaCompetitorAdd
    
    func postRegattaCompetitorAdd(
        regattaName: String,
        competitorID: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedRegattaName = regattaName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedCompetitorID = competitorID.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/regattas/\(encodedRegattaName)/competitors/\(encodedCompetitorID)/add"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postRegattaCompetitorAddSuccess(responseObject: responseObject, success: success)
        }) { (requestOperation, error) in
            self.postRegattaCompetitorAddFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postRegattaCompetitorAddSuccess(responseObject: Any?, success: @escaping () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postRegattaCompetitorAddFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - RegattaCompetitorCreateAndAdd
    
    func postRegattaCompetitorCreateAndAdd(
        regattaName: String,
        boatClassName: String,
        sailID: String,
        nationality: String,
        success: @escaping (_ regattaCompetitorCreateAndAddData: RegattaCompetitorCreateAndAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedRegattaName = regattaName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedBoatClassName = boatClassName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedSailID = sailID.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedNationality = nationality.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/regattas/\(encodedRegattaName)/competitors/createandadd?boatclass=\(encodedBoatClassName)&sailid=\(encodedSailID)&nationalityIOC=\(encodedNationality)"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postRegattaCompetitorCreateAndAddSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postRegattaCompetitorCreateAndAddFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postRegattaCompetitorCreateAndAddSuccess(
        responseObject: Any?,
        success: @escaping (_ regattaCompetitorCreateAndAddData: RegattaCompetitorCreateAndAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(RegattaCompetitorCreateAndAddData(dictionary: jsonObject as? [String: AnyObject]))
            } catch {
                self.postRegattaCompetitorCreateAndAddFailure(error: error, failure: failure)
            }
        } else {
            self.postRegattaCompetitorCreateAndAddFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
        }
    }
    
    fileprivate func postRegattaCompetitorCreateAndAddFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - RegattaRaceColumnAdd
    
    func postRegattaRaceColumnAdd(
        regattaName: String,
        success: @escaping (_ regattaRaceColummAddData: RegattaRaceColumnAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedRegattaName = regattaName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/regattas/\(encodedRegattaName)/addracecolumns"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postRegattaRaceColumnAddSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postRegattaRaceColumnAddAddFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postRegattaRaceColumnAddSuccess(
        responseObject: Any?,
        success: @escaping (_ regattaRaceColumnAddData: RegattaRaceColumnAddData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(RegattaRaceColumnAddData(dictionary: (jsonObject as? [AnyObject])?.first as? [String: AnyObject]))
            } catch {
                self.postRegattaRaceColumnAddAddFailure(error: error, failure: failure)
            }
        } else {
            self.postRegattaRaceColumnAddAddFailure(error: TrainingRequestManagerError.invalidResponse, failure: failure)
        }
    }
    
    fileprivate func postRegattaRaceColumnAddAddFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - LeaderboardRaceSetStartTrackingTime
    
    func postLeaderboardRaceSetStartTrackingTime(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        postLeaderboardRaceSetTrackingTimeMillis(
            forMillisParameter: "startoftrackingasmillis",
            leaderboardName: leaderboardName,
            raceColumnName: raceColumnName,
            fleetName: fleetName,
            success: success,
            failure: failure
        )
    }
    
    func postLeaderboardRaceSetStopTrackingTime(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        postLeaderboardRaceSetTrackingTimeMillis(
            forMillisParameter: "endoftrackingasmillis",
            leaderboardName: leaderboardName,
            raceColumnName: raceColumnName,
            fleetName: fleetName,
            success: success,
            failure: failure
        )
    }
    
    fileprivate func postLeaderboardRaceSetTrackingTimeMillis(
        forMillisParameter millisParameter: String,
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedRaceColumnName = raceColumnName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedFleetName = fleetName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/settrackingtimes?race_column=\(encodedRaceColumnName)&fleet=\(encodedFleetName)&\(millisParameter)=\(millisSince1970())"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postLeaderboardRaceSetTrackingTimeMillisSuccess(responseObject: responseObject, success: success)
        }) { (requestOperation, error) in
            self.postLeaderboardRaceSetTrackingTimeMillisFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postLeaderboardRaceSetTrackingTimeMillisSuccess(responseObject: Any?, success: @escaping () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postLeaderboardRaceSetTrackingTimeMillisFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - LeaderboardStartTracking
    
    func postLeaderboardStartTracking(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedRaceColumnName = raceColumnName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedFleetName = fleetName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/starttracking?race_column=\(encodedRaceColumnName)&fleet=\(encodedFleetName)"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postLeaderboardStartTrackingSuccess(responseObject: responseObject, success: success)
        }) { (requestOperation, error) in
            self.postLeaderboardStartTrackingFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postLeaderboardStartTrackingSuccess(responseObject: Any?, success: @escaping () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postLeaderboardStartTrackingFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - LeaderboardStopTracking
    
    func postLeaderboardStopTracking(
        leaderboardName: String,
        raceColumnName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        postLeaderboardStopTracking(leaderboardName: leaderboardName, query: "race_column=\(raceColumnName)", success: success, failure: failure)
    }
    
    func postLeaderboardStopTracking(
        leaderboardName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        postLeaderboardStopTracking(leaderboardName: leaderboardName, query: "fleet=\(fleetName)", success: success, failure: failure)
    }
    
    func postLeaderboardStopTracking(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        postLeaderboardStopTracking(leaderboardName: leaderboardName, query: "race_column=\(raceColumnName)&fleet=\(fleetName)", success: success, failure: failure)
    }
    
    fileprivate func postLeaderboardStopTracking(
        leaderboardName: String,
        query: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedQuery = query.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/stoptracking?\(encodedQuery)"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postLeaderboardStopTrackingSuccess(responseObject: responseObject, success: success)
        }) { (requestOperation, error) in
            self.postLeaderboardStopTrackingFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postLeaderboardStopTrackingSuccess(responseObject: Any?, success: @escaping () -> Void) {
        logInfo(name: "\(#function)", info: responseObjectToString(responseObject: responseObject))
        success()
    }
    
    fileprivate func postLeaderboardStopTrackingFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }

    // MARK: - LeaderboardGroup

    func getLeaderboardGroup(
        leaderboardName: String,
        success: @escaping (_ leaderboardGroupData: LeaderboardGroupData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboardgroups/\(encodedLeaderboardName)"
        manager.get(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.getLeaderboardGroupSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.getLeaderboardGroupFailure(error: error, failure: failure)
        }
    }

    fileprivate func getLeaderboardGroupSuccess(
        responseObject: Any?,
        success: @escaping (_ leaderboardGroupData: LeaderboardGroupData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(LeaderboardGroupData(dictionary: jsonObject as? [String: AnyObject]))
            } catch {
                failure(error, nil)
            }
        } else {
            failure(TrainingRequestManagerError.invalidResponse, nil)
        }
    }

    fileprivate func getLeaderboardGroupFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }

    // MARK: - RegattaRaceCourse

    func getRegattaRaceCourse(
        regattaName: String,
        raceName: String,
        success: @escaping (_ regattaRaceCourseData: RegattaRaceCourseData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedRegattaName = regattaName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedRaceName = raceName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/regattas/\(encodedRegattaName)/races/\(encodedRaceName)/course"
        manager.get(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.getRegattaRaceCourseSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.getRegattaRaceCourseFailure(error: error, failure: failure)
        }
    }

    fileprivate func getRegattaRaceCourseSuccess(
        responseObject: Any?,
        success: @escaping (_ regattaRaceCourseData: RegattaRaceCourseData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                success(RegattaRaceCourseData(dictionary: (jsonObject as? [String: AnyObject])))
            } catch {
                failure(error, nil)
            }
        } else {
            failure(TrainingRequestManagerError.invalidResponse, nil)
        }
    }

    fileprivate func getRegattaRaceCourseFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }

    // MARK: - LeaderboardAutoCourse
    
    func postLeaderboardAutoCourse(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping (_ leaderboardAutoCourseData: LeaderboardAutoCourseData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        let encodedLeaderboardName = leaderboardName.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? ""
        let encodedRaceColumnName = raceColumnName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let encodedFleetName = fleetName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        let urlString = "\(basePathString)/leaderboards/\(encodedLeaderboardName)/autocourse?race_column=\(encodedRaceColumnName)&fleet=\(encodedFleetName)"
        manager.post(urlString, parameters: nil, progress: nil, success: { (requestOperation, responseObject) in
            self.postLeaderboardAutoCourseSuccess(responseObject: responseObject, success: success, failure: failure)
        }) { (requestOperation, error) in
            self.postLeaderboardAutoCourseFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func postLeaderboardAutoCourseSuccess(
        responseObject: Any?,
        success: @escaping (_ leaderboardAutoCourseData: LeaderboardAutoCourseData) -> Void,
        failure: @escaping (_ error: Error, _ message: String?) -> Void)
    {
        if let data = responseObject as? Data {
            do {
                let jsonObject = try JSONSerialization.jsonObject(with: data)
                logInfo(name: "\(#function)", info: jsonObject as? String ?? "")
                success(LeaderboardAutoCourseData(dictionary: (jsonObject as? [AnyObject])?.first as? [String: AnyObject]))
            } catch {
                failure(error, nil)
            }
        } else {
            failure(TrainingRequestManagerError.invalidResponse, nil)
        }
    }

    fileprivate func postLeaderboardAutoCourseFailure(error: Error, failure: @escaping (_ error: Error, _ message: String?) -> Void) {
        logError(name: "\(#function)", error: error)
        failure(error, stringForError(error))
    }
    
    // MARK: - Helper
    
    fileprivate func millisSince1970() -> NSNumber {
        return NSNumber(value: Int64(Date().timeIntervalSince1970 * 1000) as Int64)
    }
    
    fileprivate func responseObjectToString(responseObject: Any?) -> String {
        return (responseObject as? String) ?? "response object is empty or cannot be casted"
    }
    
    fileprivate func stringForError(_ error: Error) -> String? {
        guard let data = ((error as NSError).userInfo[AFNetworkingOperationFailingURLResponseDataErrorKey] as? NSData) else { return nil }
        return String(data: data as Data, encoding: String.Encoding.utf8)
    }
    
    func fixJSON(data: Data) -> Data {
        guard let json = String(data: data, encoding: String.Encoding.utf8) else { return data }
        guard let newData = fixJSON(string: json).data(using: String.Encoding.utf8) else { return data }
        return newData
    }
    
    func fixJSON(string: String) -> String {
        let pattern = "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}"
        let regex = try! NSRegularExpression(pattern: pattern, options: [])
        let matches = regex.matches(in: string, options: [], range: NSRange(location: 0, length: string.characters.count))
        var newString = string
        matches.forEach { (match) in
            let uuid = (string as NSString).substring(with: match.range)
            newString = newString.replacingOccurrences(of: uuid, with: "\"\(uuid)\"")
        }
        return newString
    }

}
