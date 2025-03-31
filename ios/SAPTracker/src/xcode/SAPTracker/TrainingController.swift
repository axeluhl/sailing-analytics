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

    fileprivate var trainingRaceData: TrainingRaceData?
    fileprivate var sentGPSFixesCount = 0

    init(coreDataManager: CoreDataManager, baseURLString: String) {
        self.coreDataManager = coreDataManager
        trainingRequestManager = TrainingRequestManager(baseURLString: baseURLString)
        super.init()
    }

    // MARK: - Notifications

    fileprivate func subscribeForSentGPSFixesNotifications() {
        sentGPSFixesCount = 0
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(gpsFixControllerSentGPSFixes),
            name: NSNotification.Name(rawValue: GPSFixController.NotificationType.SentGPSFixes),
            object: nil
        )
    }

    fileprivate func unsubscribeFromSentGPSFixesNotifications() {
        NotificationCenter.default.removeObserver(self)
    }

    @objc fileprivate func gpsFixControllerSentGPSFixes(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let sentDict = notification.userInfo?[GPSFixController.UserInfo.Sent] as? [String: Any] else { return }
            guard let count = sentDict[GPSFixController.UserInfo.SentKey.Count] as? NSNumber else { return }
            self.sentGPSFixesCount += count.intValue
            if (self.sentGPSFixesCount >= AutoCourse.MinGPSFixesNeeded) {
                if let trainingRaceData = self.trainingRaceData {
                    self.autoCourseRace(forTrainingRaceData: trainingRaceData, completion: {
                        self.unsubscribeFromSentGPSFixesNotifications()
                    })
                }
            }
        })
    }

    // MARK: - CreateTraining
    
    func createTraining(
        forBoatClassName boatClassName: String,
        sailID: String,
        nationality: String,
        success: @escaping (_ checkInData: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let collector = CreateTrainingData.init(serverURL: trainingRequestManager.baseURLString, boatClassName: boatClassName, sailID: sailID, nationality: nationality)
        createTraining_CreateEvent(collector: collector, success: { (createTrainingData) in
            if let checkInData = CheckInData.init(createTrainingData: collector) {
                success(checkInData)
            } else {
                failure(TrainingControllerError.checkInDataIsIncomplete)
            }
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func createTraining_CreateEvent(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingRequestManager.postCreateEvent(boatClassName: collector.boatClassName, success: { (createEventData) in
            collector.createEventData = createEventData
            self.createTraining_CreateEventSuccess(collector: collector, success: success, failure: failure)
        }) { (error, message) in
            failure(error)
        }
    }
    
    fileprivate func createTraining_CreateEventSuccess(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        do {
            try createTraining_AddCompetitor(collector: collector, success: success, failure: failure)
        } catch {
            do {
                try createTraining_CreateAndAddCompetitor(collector: collector, success: success, failure: failure)
            } catch {
                failure(error)
            }
        }
    }
    
    fileprivate func createTraining_AddCompetitor(
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
                self.createTraining_AddCompetitorFailure(collector: collector, success: success, failure: failure)
            }
        } else {
            self.createTraining_AddCompetitorFailure(collector: collector, success: success, failure: failure)
        }
    }
    
    fileprivate func createTraining_AddCompetitorFailure(
        collector: CreateTrainingData,
        success: @escaping (_ collector: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        do {
            try self.createTraining_CreateAndAddCompetitor(collector: collector, success: success, failure: failure)
        } catch {
            failure(error)
        }
    }
    
    fileprivate func createTraining_CreateAndAddCompetitor(
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
    
    // MARK: - FinishTraining
    
    func finishTraining(
        forCheckIn checkIn: CheckIn,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        self.trainingRequestManager.putFinishEventUpdate(eventID: checkIn.event.eventID, success: {
            self.trainingRequestManager.postLeaderboardStopTracking(leaderboardName: checkIn.leaderboard.name, fleetName: "Default", success: {
                success()
            }) { (error, message) in
                failure(error)
            }
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - ReactivateTraining
    
    func reactivateTraining(
        forCheckIn checkIn: CheckIn,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingRequestManager.putReactivateEventUpdate(eventID: checkIn.event.eventID, success: {
            success()
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - StopActiveRace
    
    func stopActiveRace(
        forTrainingRaceData trainingRaceData: TrainingRaceData,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        self.unsubscribeFromSentGPSFixesNotifications()
        self.stopActiveRace_SetStopTrackingTime(forTrainingRaceData: trainingRaceData, success: {
            self.autoCourseRace(forTrainingRaceData: trainingRaceData, completion: {
                success()
            })
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func stopActiveRace_SetStopTrackingTime(
        forTrainingRaceData data: TrainingRaceData,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        stopActiveRace_SetStopTrackingTime(
            leaderboardName: data.leaderboardName,
            regattaName: data.regattaName,
            raceColumnName: data.raceColumnName,
            fleetName: data.fleetName,
            success: success,
            failure: failure
        )
    }
    
    fileprivate func stopActiveRace_SetStopTrackingTime(
        leaderboardName: String,
        regattaName: String,
        raceColumnName: String,
        fleetName: String,
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingRequestManager.postLeaderboardRaceSetStopTrackingTime(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: {
            success()
        }) { (error, message) in
            failure(error)
        }
    }
    
    // MARK: - StartNewRace
    
    func startNewRace(
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
                    let trainingRaceData = TrainingRaceData(
                        leaderboardName: leaderboardName,
                        regattaName: regattaName,
                        raceColumnName: raceColumnName,
                        fleetName: fleetName
                    )
                    self.trainingRaceData = trainingRaceData
                    self.subscribeForSentGPSFixesNotifications()
                    success(trainingRaceData)
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

    // MARK: - AutoCourseRace
    
    func autoCourseRace(
        forTrainingRaceData trainingRaceData: TrainingRaceData,
        completion: @escaping () -> Void)
    {
        let leaderboardName = trainingRaceData.leaderboardName
        let regattaName = trainingRaceData.regattaName
        let fleetName = trainingRaceData.fleetName
        let raceColumnName = trainingRaceData.raceColumnName

        // 1. Get tracked race name for race column
        self.trainingRequestManager.getLeaderboardGroup(leaderboardName: leaderboardName, success: { (leaderboardGroupData) in
            guard let raceName = leaderboardGroupData.raceName(trainingRaceData: trainingRaceData) else {
                completion()
                return
            }

            // 2. Get course for tracked race name
            self.trainingRequestManager.getRegattaRaceCourse(regattaName: regattaName, raceName: raceName, success: { (course) in
                guard course.isEmpty() || course.isAutoCourse() else {
                    completion()
                    return
                }

                // 3. Set auto course
                self.trainingRequestManager.postLeaderboardAutoCourse(leaderboardName: leaderboardName, raceColumnName: raceColumnName, fleetName: fleetName, success: { (leaderboardAutoCourseData) in
                    completion()
                }) { (error, message) in
                    completion()
                }
            }, failure: { (error, message) in
                completion()
            })
        }) { (error, message) in
            completion()
        }
    }

    // MARK: - Properties

    var baseURLString: String {
        get {
            return trainingRequestManager.baseURLString
        }
    }

}
