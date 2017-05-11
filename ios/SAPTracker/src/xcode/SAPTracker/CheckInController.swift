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

@objc protocol CheckInControllerDelegate {
    
    func checkInController(_ sender: CheckInController, show alertController: UIAlertController)

}

class CheckInController : NSObject {
    
    var delegate: CheckInControllerDelegate?
    
    fileprivate var requestManager = RequestManager()
    
    // MARK: - CheckIn
    
    func checkIn(checkInData: CheckInData, completion: @escaping (_ withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
        requestManager.getCheckInData(checkInData: checkInData, success:
            { (checkInData) in
                SVProgressHUD.popActivity()
                self.checkInSuccess(checkInData: checkInData, completion: completion)
            }, failure: { (error) in
                SVProgressHUD.popActivity()
                self.checkInFailure(error: error, completion: completion)
            }
        )
    }
    
    fileprivate func checkInSuccess(checkInData: CheckInData, completion: @escaping (_ withSuccess: Bool) -> Void) {
        switch checkInData.type {
        case .competitor:
            let alertController = UIAlertController(
                title: String(format: Translation.CheckInController.WelcomeAlert.Title.String, checkInData.competitorData.name),
                message: String(format: Translation.CheckInController.WelcomeAlert.Message.String, checkInData.competitorData.sailID),
                preferredStyle: .alert
            )
            let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { (action) in
                self.postCheckIn(checkInData: checkInData, completion: completion)
            }
            let cancelAction = UIAlertAction(title: Translation.CheckInController.WelcomeAlert.CancelAction.Title.String, style: .cancel) { (action) in
                self.checkInDidFinish(withSuccess: false, completion: completion)
            }
            alertController.addAction(okAction)
            alertController.addAction(cancelAction)
            showCheckInAlert(alertController: alertController)
            break
        case .mark:
            self.postCheckIn(checkInData: checkInData, completion: completion)
            break
        }
    }
    
    fileprivate func checkInFailure(error: Error, completion: @escaping (_ withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: error.localizedDescription,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        showCheckInAlert(alertController: alertController)
    }
    
    // MARK: - PostCheckIn
    
    fileprivate func postCheckIn(checkInData: CheckInData, completion: @escaping (_ withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager.postCheckIn(checkInData: checkInData, success: { () -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInSuccess(checkInData: checkInData, completion: completion)
            }, failure: { (error) -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInFailure(error: error, completion: completion)
            }
        )
    }
    
    fileprivate func postCheckInSuccess(checkInData: CheckInData, completion: (_ withSuccess: Bool) -> Void) {
        switch checkInData.type {
        case .competitor:
            let competitorCheckIn = CoreDataManager.sharedManager.fetchCompetitorCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                competitorID: checkInData.competitorID!
            ) ?? CoreDataManager.sharedManager.newCompetitorCheckIn()
            competitorCheckIn.updateWithCheckInData(checkInData: checkInData)
            CoreDataManager.sharedManager.saveContext()
            checkInDidFinish(withSuccess: true, completion: completion)
            break
        case .mark:
            let markCheckIn = CoreDataManager.sharedManager.fetchMarkCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                markID: checkInData.markID!
            ) ?? CoreDataManager.sharedManager.newMarkCheckIn()
            markCheckIn.updateWithCheckInData(checkInData: checkInData)
            CoreDataManager.sharedManager.saveContext()
            checkInDidFinish(withSuccess: true, completion: completion)
            break
        }
    }
    
    fileprivate func postCheckInFailure(error: Error, completion: @escaping (_ withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: error.localizedDescription,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        showCheckInAlert(alertController: alertController)
    }
    
    fileprivate func checkInDidFinish(withSuccess success: Bool, completion: (_ withSuccess: Bool) -> Void) {
        completion(success)
    }
    
    // MARK: - Controller
    
    fileprivate func showCheckInAlert(alertController: UIAlertController) {
        self.delegate?.checkInController(self, show: alertController)
    }
    
}
