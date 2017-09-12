//
//  TrainingController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum TrainingControllerError: Error {
    case checkInDataIsIncomplete
    case regattaIsMissing
}

class TrainingController: NSObject {
    
    fileprivate unowned let coreDataManager: CoreDataManager
    
    fileprivate let trainingRequestManager: TrainingRequestManager
    
    init(coreDataManager: CoreDataManager, baseURLString: String) {
        self.coreDataManager = coreDataManager
        trainingRequestManager = TrainingRequestManager(baseURLString: baseURLString)
        super.init()
    }
    
    // MARK: - CreateTraining
    
    func createTraining(
        forBoatClassName boatClassName: String,
        success: @escaping (_ checkInData: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let collector = CreateTrainingData.init(serverURL: trainingRequestManager.baseURLString, boatClassName: boatClassName)
        createEvent(collector: collector, success: { (createTrainingData) in
            self.createTrainingSuccess(collector: createTrainingData, success: success, failure: failure)
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func createTrainingSuccess(
        collector: CreateTrainingData,
        success:(_ checkInData: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        if let checkInData = CheckInData.init(createTrainingData: collector) {
            success(checkInData)
        } else {
            failure(TrainingControllerError.checkInDataIsIncomplete)
        }
    }
    
    // MARK: - CreateEvent
    
    fileprivate func createEvent(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingRequestManager.postCreateEvent(boatClassName: collector.boatClassName, success: { (createEventData) in
            collector.createEventData = createEventData
            self.createEventSuccess(collector: collector, success: success, failure: failure)
        }) { (error, message) in
            failure(error)
        }
    }
    
    fileprivate func createEventSuccess(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        do {
            try regattaCompetitorAdd(collector: collector, success: success, failure: failure)
        } catch {
            do {
                try regattaCompetitorCreateAndAdd(collector: collector, success: success, failure: failure)
            } catch {
                failure(error)
            }
        }
    }
    
    // MARK: - RegattaCompetitorAdd
    
    fileprivate func regattaCompetitorAdd(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void) throws
    {
        guard let regatta = collector.createEventData?.regatta else {
            throw TrainingControllerError.regattaIsMissing
        }
        
        let competitorCheckIns = coreDataManager.fetchCompetitorCheckIn(serverURL: collector.serverURL, boatClassName: collector.boatClassName) ?? []
        if let competitorID = competitorCheckIns.first?.competitorID {
            trainingRequestManager.postRegattaCompetitorAdd(regattaName: regatta, competitorID: competitorID, success: {
                collector.competitorID = competitorID
                success(collector)
            }) { (error, message) in
                self.regattaCompetitorAddFailure(collector: collector, success: success, failure: failure)
            }
        } else {
            self.regattaCompetitorAddFailure(collector: collector, success: success, failure: failure)
        }
    }
    
    fileprivate func regattaCompetitorAddFailure(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        do {
            try self.regattaCompetitorCreateAndAdd(collector: collector, success: success, failure: failure)
        } catch {
            failure(error)
        }
    }
    
    // MARK: - RegattaCompetitorCreateAndAdd
    
    fileprivate func regattaCompetitorCreateAndAdd(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void) throws
    {
        guard let regatta = collector.createEventData?.regatta else {
            throw TrainingControllerError.regattaIsMissing
        }
        
        let boatClassName = collector.boatClassName
        let sailID = collector.sailID
        let nationality = collector.nationality
        
        trainingRequestManager.postRegattaCompetitorCreateAndAdd(regattaName: regatta, boatClassName: boatClassName, sailID: sailID, nationality: nationality, success: { regattaCompetitorCreateAndAddData in
            collector.competitorID = regattaCompetitorCreateAndAddData.competitorID
            success(collector)
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - LeaderboardRaceStartTracking
    
    func leaderboardRaceStartTracking(
        forCheckIn checkIn: CheckIn,
        success: @escaping (_ trainingRaceData: TrainingRaceData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let leaderboardName = checkIn.leaderboard.name
        let regattaName = checkIn.event.name
        self.trainingRequestManager.postRegattaRaceColumnAdd(regattaName: regattaName, success: { (regattaRaceColumnAddData) in
            let raceColumnName = regattaRaceColumnAddData.raceColumnName
            let fleetName = regattaRaceColumnAddData.fleetName
            self.trainingRequestManager.postLeaderboardRaceSetStartTrackingTime(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
                self.trainingRequestManager.postLeaderboardStartTracking(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
                    success(
                        TrainingRaceData(
                            leaderboardName: leaderboardName,
                            regattaName: regattaName,
                            raceColumnName: raceColumnName,
                            fleetName: fleetName
                        )
                    )
                }, failure: { (error, message) in
                    failure(error)
                })
            }) { (error, message) in
                failure(error)
            }
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - LeaderboardRaceStopTracking
    
    func leaderboardRaceStopTracking(
        forTrainingRaceData data: TrainingRaceData,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        leaderboardRaceStopTracking(
            leaderboardName: data.leaderboardName,
            regattaName: data.regattaName,
            raceColumnName: data.raceColumnName,
            fleetName: data.fleetName,
            success: success,
            failure: failure
        )
    }
    
    fileprivate func leaderboardRaceStopTracking(
        leaderboardName: String,
        regattaName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        self.trainingRequestManager.postLeaderboardStopTracking(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
            self.trainingRequestManager.postLeaderboardRaceSetStopTrackingTime(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
                success()
            }) { (error, message) in
                failure(error)
            }
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - LeaderboardRaceAutoCourse
    
    func leaderboardRaceAutoCourse(
        leaderboardName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingRequestManager.postLeaderboardAutoCourse(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
            success()
        }) { (error, message) in
            failure(error)
        }
    }
    
}
