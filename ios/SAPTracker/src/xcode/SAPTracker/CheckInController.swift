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

class CheckInController : NSObject {
    
    enum CheckInControllerError: Error {
        case cancelled
    }
    
    fileprivate unowned let coreDataManager: CoreDataManager
    fileprivate weak var viewController: UIViewController?
    fileprivate var requestManager = RequestManager()
    
    init(coreDataManager: CoreDataManager) {
        self.coreDataManager = coreDataManager
        super.init()
    }
    
    // MARK: - CheckIn
    
    func checkInWithViewController(
        _ viewController: UIViewController,
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        self.viewController = viewController
        SVProgressHUD.show()
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
        requestManager.getCheckInData(checkInData: checkInData, success: { [weak self] checkInData in
            SVProgressHUD.popActivity()
            self?.checkInSuccess(checkInData: checkInData, success: success, failure: failure)
        }) { [weak self] error in
            SVProgressHUD.popActivity()
            self?.didFailCheckIn(withError: error, failure: failure)
        }
    }
    
    fileprivate func checkInSuccess(
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        switch checkInData.type {
        case .competitor:
            let title = String(format: Translation.CheckInController.WelcomeAlert.Title.String, checkInData.competitorData.name)
            let message = String(format: Translation.CheckInController.WelcomeAlert.Message.String, checkInData.competitorData.sailID)
            let alertController = UIAlertController(title: title, message: message, preferredStyle: .alert)
            let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { [weak self] action in
                self?.postCheckIn(checkInData: checkInData, success: success, failure: failure)
            }
            let cancelAction = UIAlertAction(title: Translation.CheckInController.WelcomeAlert.CancelAction.Title.String, style: .cancel) { [weak self] action in
                self?.didCancelCheckIn(failure: failure)
            }
            alertController.addAction(okAction)
            alertController.addAction(cancelAction)
            viewController?.present(alertController, animated: true)
            break
        case .mark:
            self.postCheckIn(checkInData: checkInData, success: success, failure: failure)
            break
        }
    }
    
    // MARK: - PostCheckIn
    
    fileprivate func postCheckIn(
        checkInData: CheckInData,
        success: @escaping (_ checkIn: CheckIn) -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        SVProgressHUD.show()
        requestManager.postCheckIn(checkInData: checkInData, success: { [weak self] () in
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
    
    fileprivate func didFinishCheckIn(_ checkIn: CheckIn, success:(_ checkIn: CheckIn) -> Void) {
        success(checkIn)
    }
    
    fileprivate func didCancelCheckIn(failure:(_ error: Error) -> Void) {
        failure(CheckInControllerError.cancelled)
    }
    
    fileprivate func didFailCheckIn(withError error: Error, failure: @escaping (_ error: Error) -> Void) {
        let alertController = UIAlertController(title: Translation.Common.Error.String, message: error.localizedDescription, preferredStyle: .alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { action in
            failure(error)
        }
        alertController.addAction(okAction)
        viewController?.present(alertController, animated: true)
    }
    
}
