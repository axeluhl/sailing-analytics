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
    
    func showCheckInAlert(sender: CheckInController, alertController: UIAlertController)

}

class CheckInController : NSObject {
    
    var delegate: CheckInControllerDelegate?
    
    private var requestManager = RequestManager()
    
    // MARK: - CheckIn
    
    func checkIn(checkInData: CheckInData, completion: (withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
        requestManager.getCheckInData(checkInData, success:
            { (checkInData) in
                SVProgressHUD.popActivity()
                self.checkInSuccess(checkInData, completion: completion)
            }, failure: { (error) in
                SVProgressHUD.popActivity()
                self.checkInFailure(error, completion: completion)
            }
        )
    }
    
    private func checkInSuccess(checkInData: CheckInData, completion: (withSuccess: Bool) -> Void) {
        switch checkInData.type {
        case .Competitor:
            let alertController = UIAlertController(
                title: String(format: Translation.CheckInController.WelcomeAlert.Title.String, checkInData.competitorData.name),
                message: String(format: Translation.CheckInController.WelcomeAlert.Message.String, checkInData.competitorData.sailID),
                preferredStyle: .Alert
            )
            let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
                self.postCheckIn(checkInData, completion: completion)
            }
            let cancelAction = UIAlertAction(title: Translation.CheckInController.WelcomeAlert.CancelAction.Title.String, style: .Cancel) { (action) in
                self.checkInDidFinish(withSuccess: false, completion: completion)
            }
            alertController.addAction(okAction)
            alertController.addAction(cancelAction)
            showCheckInAlert(alertController)
            break
        case .Mark:
            self.postCheckIn(checkInData, completion: completion)
            break
        }
    }
    
    private func checkInFailure(error: RequestManager.Error, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(title: error.title,
                                                message: error.message,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - PostCheckIn
    
    private func postCheckIn(checkInData: CheckInData, completion: (withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager.postCheckIn(checkInData, success: { () -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInSuccess(checkInData, completion: completion)
            }, failure: { (error) -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInFailure(error, completion: completion)
            }
        )
    }
    
    private func postCheckInSuccess(checkInData: CheckInData, completion: (withSuccess: Bool) -> Void) {
        switch checkInData.type {
        case .Competitor:
            let competitorCheckIn = CoreDataManager.sharedManager.fetchCompetitorCheckIn(
                checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                competitorID: checkInData.competitorID!
            ) ?? CoreDataManager.sharedManager.newCompetitorCheckIn()
            competitorCheckIn.updateWithCheckInData(checkInData)
            CoreDataManager.sharedManager.saveContext()
            checkInDidFinish(withSuccess: true, completion: completion)
            break
        case .Mark:
            let markCheckIn = CoreDataManager.sharedManager.fetchMarkCheckIn(
                checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                markID: checkInData.markID!
            ) ?? CoreDataManager.sharedManager.newMarkCheckIn()
            markCheckIn.updateWithCheckInData(checkInData)
            CoreDataManager.sharedManager.saveContext()
            checkInDidFinish(withSuccess: true, completion: completion)
            break
        }
    }
    
    private func postCheckInFailure(error: RequestManager.Error, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(
            title: error.title,
            message: error.message,
            preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        showCheckInAlert(alertController)
    }
    
    private func checkInDidFinish(withSuccess success: Bool, completion: (withSuccess: Bool) -> Void) {
        completion(withSuccess: success)
    }
    
    // MARK: - Controller
    
    private func showCheckInAlert(alertController: UIAlertController) {
        self.delegate?.showCheckInAlert(self, alertController: alertController)
    }
    
}