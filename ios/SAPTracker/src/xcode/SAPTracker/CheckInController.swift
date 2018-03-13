//
//  CheckInController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.05.16.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import UIKit
import AVFoundation

// MARK: CheckInControllerDelegate

protocol CheckInControllerDelegate: class {

    func checkInController(
        _ controller: CheckInController, postBoatCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)

    func checkInController(
        _ controller: CheckInController, postCompetitorCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    
    func checkInController(
        _ controller: CheckInController, postMarkCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    
}

// MARK: CheckInControllerDelegate (default implementation)

extension CheckInControllerDelegate {

    func checkInController(
        _ controller: CheckInController, postBoatCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        controller.postCheckIn(checkInData: checkInData, success: success, failure: failure)
    }

    func checkInController(
        _ controller: CheckInController, postCompetitorCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        controller.postCheckIn(checkInData: checkInData, success: success, failure: failure)
    }
    
    func checkInController(
        _ controller: CheckInController, postMarkCheckIn
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        controller.postCheckIn(checkInData: checkInData, success: success, failure: failure)
    }
    
}

class CheckInController : NSObject {
    
    enum CheckInControllerError: Error {
        case cancelled
    }
    
    fileprivate unowned let coreDataManager: CoreDataManager
    
    weak var viewController: UIViewController?
    weak var delegate: CheckInControllerDelegate?
    
    init(coreDataManager: CoreDataManager) {
        self.coreDataManager = coreDataManager
        super.init()
    }
    
    // MARK: - CheckIn
    
    func checkInWithViewController(
        _ viewController: UIViewController?,
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        self.viewController = viewController
        collectCheckInData(checkInData: checkInData, success: success, failure: failure)
    }
    
    // MARK: - CollectCheckInData
    
    fileprivate func collectCheckInData(
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        SVProgressHUD.show()
        let checkInDataCollector = CheckInDataCollector(checkInData: checkInData)
        checkInDataCollector.collect(checkInData: checkInData, success: { [weak self] checkInData in
            SVProgressHUD.popActivity()
            self?.collectCheckInDataSuccess(checkInData: checkInData, success: success, failure: failure)
        }) { [weak self] error in
            SVProgressHUD.popActivity()
            self?.didFailCheckIn(withError: error, failure: failure)
        }
    }
    
    fileprivate func collectCheckInDataSuccess(
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        switch checkInData.type {
        case .boat:
            delegate?.checkInController(self, postBoatCheckIn: checkInData, success: success, failure: failure)
            break
        case .competitor:
            delegate?.checkInController(self, postCompetitorCheckIn: checkInData, success: success, failure: failure)
            break
        case .mark:
            delegate?.checkInController(self, postMarkCheckIn: checkInData, success: success, failure: failure)
            break
        }
    }
    
    // MARK: - PostCheckIn
    
    func postCheckIn(
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        SVProgressHUD.show()
        let checkInRequestManager = CheckInRequestManager(baseURLString: checkInData.serverURL)
        checkInRequestManager.postCheckIn(checkInData: checkInData, success: { [weak self] () in
            SVProgressHUD.popActivity()
            self?.postCheckInSuccess(checkInData: checkInData, success: success)
        }) { [weak self] error in
            SVProgressHUD.popActivity()
            self?.didFailCheckIn(withError: error, failure: failure)
        }
    }
    
    fileprivate func postCheckInSuccess(
        checkInData: CheckInData,
        success: (_ checkIn: CheckIn) -> Void)
    {
        switch checkInData.type {
        case .boat:
            let boatCheckIn = coreDataManager.fetchBoatCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                boatID: checkInData.boatID!
            ) ?? coreDataManager.newBoatCheckIn()
            boatCheckIn.updateWithCheckInData(checkInData: checkInData)
            coreDataManager.saveContext()
            didFinishCheckIn(boatCheckIn, success: success)
            break
        case .competitor:
            let competitorCheckIn = coreDataManager.fetchCompetitorCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                competitorID: checkInData.competitorID!
            ) ?? coreDataManager.newCompetitorCheckIn()
            competitorCheckIn.updateWithCheckInData(checkInData: checkInData)
            coreDataManager.saveContext()
            didFinishCheckIn(competitorCheckIn, success: success)
            break
        case .mark:
            let markCheckIn = coreDataManager.fetchMarkCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                markID: checkInData.markID!
            ) ?? coreDataManager.newMarkCheckIn()
            markCheckIn.updateWithCheckInData(checkInData: checkInData)
            coreDataManager.saveContext()
            didFinishCheckIn(markCheckIn, success: success)
            break
        }
    }
    
    // MARK: - Finish, Cancel, Fail
    
    func didFinishCheckIn(_ checkIn: CheckIn, success:(_ checkIn: CheckIn) -> Void) {
        success(checkIn)
    }
    
    func didCancelCheckIn(failure:(_ error: Error) -> Void) {
        failure(CheckInControllerError.cancelled)
    }
    
    func didFailCheckIn(withError error: Error, failure: @escaping (_ error: Error) -> Void) {
        let alertController = UIAlertController(title: Translation.Common.Error.String, message: error.localizedDescription, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { action in
            failure(error)
        }
        alertController.addAction(okAction)
        viewController?.present(alertController, animated: true)
    }
    
}
