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
    
    func showCheckInAlert(checkInController: CheckInController, alertController: UIAlertController)

    optional func checkInDidStart(checkInController: CheckInController)
    optional func checkInDidEnd(checkInController: CheckInController, withSuccess succeed: Bool)

}

class CheckInController : NSObject {
    
    var delegate: CheckInControllerDelegate?
    
    private var requestManager = RequestManager()
    
    // MARK: - CheckIn
    
    func checkIn(regattaData: RegattaData) {
        checkInDidStart()
        requestManager = RequestManager(baseURLString: regattaData.serverURL)
        requestManager.getRegattaData(regattaData,
                                      success: { (regattaData) in self.checkInSuccess(regattaData) },
                                      failure: { (title, error) in self.checkInFailure(title, error: error) }
        )
    }
    
    private func checkInSuccess(regattaData: RegattaData) {
        let alertController = UIAlertController(title: regattaData.welcomeString, message: nil, preferredStyle: .Alert)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default) { (action) in
            self.postCheckIn(regattaData)
        }
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    private func checkInFailure(title: String, error: NSError) {
        let alertController = UIAlertController(title: title, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    private func postCheckIn(regattaData: RegattaData) {
        SVProgressHUD.show()
        requestManager.postCheckIn(regattaData.leaderboardData.name,
                                   competitorID: regattaData.competitorData.competitorID,
                                   success: { (operation, responseObject) -> Void in self.postCheckInSuccess(regattaData) },
                                   failure: { (operation, error) -> Void in self.postCheckInFailure(regattaData, error: error) }
        )
    }
    
    private func postCheckInSuccess(regattaData: RegattaData) {
        
        // Regatta
        let regatta = CoreDataManager.sharedManager.fetchRegatta(regattaData.eventID,
                                                                 leaderboardName: regattaData.leaderboardName,
                                                                 competitorID: regattaData.competitorID
        ) ?? CoreDataManager.sharedManager.newRegatta()
        regatta.updateWirhRegattaData(regattaData)
        
        // Event
        let event = regatta.event ?? CoreDataManager.sharedManager.newEvent(regatta)
        event.updateWithEventData(regattaData.eventData)
        
        // Leaderboard
        let leaderboard = regatta.leaderboard ?? CoreDataManager.sharedManager.newLeaderboard(regatta)
        leaderboard.updateWithLeaderboardData(regattaData.leaderboardData)
        
        // Competitor
        let competitor = regatta.competitor ?? CoreDataManager.sharedManager.newCompetitor(regatta)
        competitor.updateWithCompetitorData(regattaData.competitorData)
        
        // Save
        CoreDataManager.sharedManager.saveContext()
        
        // Check-in completed
        checkInDidEnd(withSuccess: true)
    }
    
    private func postCheckInFailure(regattaData: RegattaData, error: AnyObject) {
        let alertTitle = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), regattaData.leaderboardName)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - Controller
    
    private func checkInDidStart() {
        SVProgressHUD.show()
        self.delegate?.checkInDidStart?(self)
    }
    
    private func checkInDidEnd(withSuccess succeed: Bool) {
        SVProgressHUD.popActivity()
        self.delegate?.checkInDidEnd?(self, withSuccess: succeed)
    }
    
    private func showCheckInAlert(alertController: UIAlertController) {
        SVProgressHUD.popActivity()
        self.delegate?.showCheckInAlert(self, alertController: alertController)
    }

}