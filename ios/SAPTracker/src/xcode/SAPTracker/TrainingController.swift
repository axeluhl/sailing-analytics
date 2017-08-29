//
//  TrainingController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

enum TrainingControllerError: Error {
    case regattaIsMissing
    case checkInDataIsIncomplete
}

class TrainingController: NSObject {
    
    let requestManager: TrainingRequestManager

    init(baseURLString: String) {
        requestManager = TrainingRequestManager(baseURLString: baseURLString)
        super.init()
    }
    
    // MARK: - CreateTraining
    
    func createTraining(
        forBoatClassName boatClassName: String,
        success: @escaping (_ checkInData: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        let collector = CreateTrainingData.init(serverURL: requestManager.baseURLString, boatClassName: boatClassName)
        createTraining(collector: collector, success: success, failure: failure)
    }
    
    fileprivate func createTraining(
        collector: CreateTrainingData,
        success: @escaping (_ checkInData: CheckInData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        createEvent(collector: collector, success: { createTrainingData in
            self.createTrainingSuccess(collector: createTrainingData, success: success, failure: failure)
        }) { error in
            self.createTrainingFailure(error: error, failure: failure)
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
    
    fileprivate func createTrainingFailure(error: Error, failure: (_ error: Error) -> Void) {
        failure(error)
    }
    
    // MARK: - CreateEvent
    
    fileprivate func createEvent(
        collector: CreateTrainingData,
        success: @escaping (_ createTrainingData: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        requestManager.postCreateEvent(boatClassName: collector.boatClassName, success: { createEventData in
            collector.createEventData = createEventData
            self.createEventSuccess(collector: collector, success: success, failure: failure)
        }) { (error, message) in
            self.createEventFailure(error: error, message: message, failure: failure)
        }
    }
    
    fileprivate func createEventSuccess(
        collector: CreateTrainingData,
        success: @escaping (_ createTrainingData: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        do {
            try competitorCreateAndAdd(collector: collector, success: success, failure: failure)
        } catch {
            failure(error)
        }
    }
    
    fileprivate func createEventFailure(error: Error, message: String?, failure: (_ error: Error) -> Void) {
        failure(error)
    }
    
    // MARK: - CompetitorCreateAndAdd
    
    fileprivate func competitorCreateAndAdd(
        collector: CreateTrainingData,
        success: @escaping (_ createTrainingData: CreateTrainingData) -> Void,
        failure: @escaping (_ error: Error) -> Void) throws
    {
        guard let regatta = collector.createEventData?.regatta else {
            throw TrainingControllerError.regattaIsMissing
        }
        
        let boatClassName = collector.boatClassName
        let sailID = collector.sailID
        let nationality = collector.nationality
        
        requestManager.postCompetitorCreateAndAdd(regatta: regatta, boatClassName: boatClassName, sailID: sailID, nationality: nationality, success: { competitorCreateAndAddData in
            collector.competitorCreateAndAddData = competitorCreateAndAddData
            self.competitorCreateAndAddSuccess(collector: collector, success: success)
        }) { (error, message) in
            self.competitorCreateAndAddFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func competitorCreateAndAddSuccess(
        collector: CreateTrainingData,
        success: (_ createTrainingData: CreateTrainingData) -> Void)
    {
        success(collector)
    }
    
    fileprivate func competitorCreateAndAddFailure(error: Error, failure: (_ error: Error) -> Void) {
        failure(error)
    }
    
}
