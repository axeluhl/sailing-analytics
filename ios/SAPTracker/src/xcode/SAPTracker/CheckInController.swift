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
    
    func checkIn(regattaData: RegattaData, completion: (withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager = RequestManager(baseURLString: regattaData.serverURL)
        requestManager.getRegattaData(regattaData,
                                      success:
            { (regattaData) in
                SVProgressHUD.popActivity()
                self.checkInSuccess(regattaData, completion: completion)
            }, failure: { (title, error) in
                SVProgressHUD.popActivity()
                self.checkInFailure(title, error: error, completion: completion)
            }
        )
    }
    
    private func checkInSuccess(regattaData: RegattaData, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(title: String(format: Translation.CheckInController.WelcomeAlert.Title.String, regattaData.competitorData.name),
                                                message: String(format: Translation.CheckInController.WelcomeAlert.Message.String, regattaData.competitorData.sailID),
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.postCheckIn(regattaData, completion: completion)
        }
        let cancelAction = UIAlertAction(title: Translation.CheckInController.WelcomeAlert.CancelAction.Title.String, style: .Cancel) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    private func checkInFailure(title: String, error: NSError, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(title: title, message: error.localizedDescription, preferredStyle: .Alert)
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - PostCheckIn
    
    private func postCheckIn(regattaData: RegattaData, completion: (withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager.postCheckIn(regattaData.leaderboardData.name,
                                   competitorID: regattaData.competitorData.competitorID,
                                   success:
            { () -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInSuccess(regattaData, completion: completion)
            }, failure: { (title, message) -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInFailure(title, message: message, completion: completion)
            }
        )
    }
    
    private func postCheckInSuccess(regattaData: RegattaData, completion: (withSuccess: Bool) -> Void) {
        let regatta = CoreDataManager.sharedManager.fetchRegatta(regattaData) ?? CoreDataManager.sharedManager.newRegatta()
        regatta.updateWirhRegattaData(regattaData)
        let event = regatta.event ?? CoreDataManager.sharedManager.newEvent(regatta)
        event.updateWithEventData(regattaData.eventData)
        let leaderboard = regatta.leaderboard ?? CoreDataManager.sharedManager.newLeaderboard(regatta)
        leaderboard.updateWithLeaderboardData(regattaData.leaderboardData)
        let competitor = regatta.competitor ?? CoreDataManager.sharedManager.newCompetitor(regatta)
        competitor.updateWithCompetitorData(regattaData.competitorData)
        CoreDataManager.sharedManager.saveContext()
        checkInDidFinish(withSuccess: true, completion: completion)
    }
    
    private func postCheckInFailure(title: String, message: String, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(title: title,
                                                message: message,
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