//
//  CheckInDataCollector.swift
//  SAPTracker
//
//  Created by Raimund Wege on 08.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum CheckInDataCollectorError: Error {
    case checkInDataIsIncomplete
}

extension CheckInDataCollectorError: LocalizedError {
    var errorDescription: String? {
        switch self {
        case .checkInDataIsIncomplete:
            return Translation.CheckInDataCollectorError.CheckInDataIsIncomplete.String
        }
    }
}

class CheckInDataCollector: NSObject {
    
    fileprivate let requestManager: RequestManager
    
    init(checkInData: CheckInData) {
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
        super.init()
    }
    
    func collect(
        checkInData: CheckInData,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        getEventData(collector: checkInData, success: success, failure: failure)
    }
    
    // MARK: - Event
    
    fileprivate func getEventData(
        collector: CheckInData,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.getEvent(eventID: collector.eventID, success: { (eventData) in
            collector.eventData = eventData
            self.getLeaderboardData(collector: collector, success: success, failure: failure)
        }) { (error) in
            failure(error)
        }
    }
    
    // MARK: - Leaderboard
    
    fileprivate func getLeaderboardData(
        collector: CheckInData,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.getLeaderboard(leaderboardName: collector.leaderboardName, success: { (leaderboardData) in
            collector.leaderboardData = leaderboardData
            self.getLeaderboardDataSuccess(collector: collector, success: success, failure: failure)
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func getLeaderboardDataSuccess(
        collector: CheckInData,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        if let competitorID = collector.competitorID {
            getCompetitorData(collector: collector, competitorID: competitorID, success: success, failure: failure)
        } else if let markID = collector.markID {
            getMarkData(collector: collector, markID: markID, success: success, failure: failure)
        } else {
            failure(CheckInDataCollectorError.checkInDataIsIncomplete)
        }
    }
    
    fileprivate func getCompetitorData(
        collector: CheckInData,
        competitorID: String,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.getCompetitor(competitorID: competitorID, success: { (competitorData) in
            collector.competitorData = competitorData
            self.getCompetitorDataSuccess(
                collector: collector,
                competitorID: competitorID,
                success: success,
                failure: failure
            )
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func getCompetitorDataSuccess(
        collector: CheckInData,
        competitorID: String,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.getTeamImageURL(competitorID: competitorID, result: { (imageURL) in
            collector.teamImageURL = imageURL
            success(collector)
        })
    }
    
    // MARK: - Mark
    
    fileprivate func getMarkData(
        collector: CheckInData,
        markID: String,
        success: @escaping (_ collector: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.getMark(leaderboardName: collector.leaderboardName, markID: markID, success: { (markData) in
            collector.markData = markData
            success(collector)
        }) { error in
            failure(error)
        }
    }
    
}
