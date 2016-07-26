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
        let alertController = UIAlertController(title: regattaData.welcomeString, message: nil, preferredStyle: .Alert)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default) { (action) in
            self.postCheckIn(regattaData, completion: completion)
        }
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    private func checkInFailure(title: String, error: NSError, completion: (withSuccess: Bool) -> Void) {
        let alertController = UIAlertController(title: title, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - PostCheckIn
    
    private func postCheckIn(regattaData: RegattaData, completion: (withSuccess: Bool) -> Void) {
        SVProgressHUD.show()
        requestManager.postCheckIn(regattaData.leaderboardData.name,
                                   competitorID: regattaData.competitorData.competitorID,
                                   success:
            { (operation, responseObject) -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInSuccess(regattaData, completion: completion)
            }, failure: { (operation, error) -> Void in
                SVProgressHUD.popActivity()
                self.postCheckInFailure(regattaData, error: error, completion: completion)
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
    
    private func postCheckInFailure(regattaData: RegattaData, error: AnyObject, completion: (withSuccess: Bool) -> Void) {
        let alertTitle = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), regattaData.leaderboardName)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidFinish(withSuccess: false, completion: completion)
        }
        alertController.addAction(cancelAction)
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