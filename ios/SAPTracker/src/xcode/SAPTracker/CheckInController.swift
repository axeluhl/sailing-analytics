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

protocol CheckInControllerDelegate {
    func checkInDidStart(checkInController: CheckInController)
    func checkInDidEnd(checkInController: CheckInController, withSuccess succeed: Bool)
    func showCheckInAlert(checkInController: CheckInController, alertController: UIAlertController)
}

class CheckInController : NSObject {
    
    private let checkInData: CheckInData!
    private let delegate: CheckInControllerDelegate!
    private let checkInRequestManager: CheckInRequestManager!
    
    // TODO: - own core data context
    
    init(checkInData: CheckInData, delegate: CheckInControllerDelegate) {
        self.checkInData = checkInData
        self.delegate = delegate
        self.checkInRequestManager = CheckInRequestManager(checkInData: checkInData)
        super.init()
    }
    
    // MARK: - Start check-in
    
    func startCheckIn() {
        self.delegate.checkInDidStart(self)
        self.checkInRequestManager.getEvent(checkInData.eventID,
                                            success: { (operation, responseObject) -> Void in self.eventSucceed(responseObject) },
                                            failure: { (operation, error) -> Void in self.eventFailed(error) })
    }
    
    // MARK: - Event
    
    private func eventSucceed(eventResponseObject: AnyObject) {
        self.checkInData.eventDictionary = eventResponseObject as? [String: AnyObject]
        self.checkInRequestManager.getLeaderboard(checkInData.leaderboardName,
                                                  success: { (operation, responseObject) -> Void in self.leaderboardSucceed(responseObject) },
                                                  failure: { (operation, error) -> Void in self.leaderboardFailed(error) })
    }
    
    private func eventFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get event %@", comment: ""), self.checkInData.eventID)
        let alertController = UIAlertController(title: alertTitle,
                                                message: error.localizedDescription,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Leaderboard
    
    private func leaderboardSucceed(leaderboardResponseObject: AnyObject) {
        self.checkInData.leaderboardDictionary = leaderboardResponseObject as? [String: AnyObject]
        self.checkInRequestManager.getCompetitor(checkInData.competitorID,
                                                 success: { (operation, responseObject) in self.competitorSucceed(responseObject) },
                                                 failure: { (operation, error) in self.competitorFailed(error) })
    }
    
    private func leaderboardFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get leader board %@", comment: ""), checkInData.leaderboardName)
        let alertController = UIAlertController(title: alertTitle,
                                                message: error.localizedDescription,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Competitor
    
    private func competitorSucceed(competitorResponseObject: AnyObject) {
        self.checkInData.competitorDictionary = competitorResponseObject as? [String: AnyObject]
        self.checkInRequestManager.getTeamImageURI(checkInData.dictionaryCompetitorId(),
                                                   result: { (imageURI) in self.teamImageURIResult(imageURI) })
    }
    
    private func competitorFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get competitor %@", comment: ""), checkInData.competitorID)
        let alertController = UIAlertController(title: alertTitle,
                                                message: error.localizedDescription,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Team
    
    private func teamImageURIResult(teamImageURI: String?) {
        self.checkInData.teamImageURI = teamImageURI
        let alertTitle = String(format:NSLocalizedString("Hello %@. Welcome to %@. You are registered as %@.", comment: ""),
                                self.checkInData.dictionaryCompetitorName(),
                                self.checkInData.dictionaryLeaderboardName(),
                                self.checkInData.dictionaryCompetitorSailId())
        let alertController = UIAlertController(title: alertTitle,
                                                message: nil,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default) { (action) in
            self.checkInOnServer()
        }
        alertController.addAction(okAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Check-in on server
    
    private func checkInOnServer() {
        SVProgressHUD.show()
        checkInRequestManager.checkIn(checkInData.dictionaryLeaderboardName(),
                                      competitorID: checkInData!.dictionaryCompetitorId(),
                                      deviceUUID: DeviceUDIDManager.UDID,
                                      pushDeviceID: "",
                                      fromMillis: millisSince1970(),
                                      success: { (operation, responseObject) -> Void in self.checkInOnServerSucceed() },
                                      failure: { (operation, error) -> Void in self.checkInOnServerFailed(error) })
    }
    
    private func checkInOnServerSucceed() {
        
        // Check database if check-in already exist
        if let checkIn = DataManager.sharedManager.getCheckIn(checkInData.eventID,
                                                              leaderBoardName: checkInData.leaderboardName,
                                                              competitorId: checkInData.competitorID) {
            // Delete old check-in
            DataManager.sharedManager.deleteCheckIn(checkIn)
            DataManager.sharedManager.saveContext()
        }
        
        // Create new check-in
        let checkIn = DataManager.sharedManager.newCheckIn()
        checkIn.serverUrl = checkInData!.serverURL
        checkIn.eventId = checkInData.eventID
        checkIn.leaderBoardName = checkInData!.leaderboardName
        checkIn.competitorId = checkInData!.competitorID
        checkIn.lastSyncDate = NSDate()
        checkIn.imageUrl = checkInData!.teamImageURI
        
        // Create core data objects for event, leaderboard and competitor
        let event = DataManager.sharedManager.newEvent(checkIn)
        event.initWithDictionary(checkInData!.eventDictionary!)
        let leaderBoard = DataManager.sharedManager.newLeaderBoard(checkIn)
        leaderBoard.initWithDictionary(checkInData!.leaderboardDictionary!)
        let competitor = DataManager.sharedManager.newCompetitor(checkIn)
        competitor.initWithDictionary(checkInData!.competitorDictionary!)
        DataManager.sharedManager.saveContext()
        
        // Check-in complete
        self.checkInDidEnd(withSuccess: true)
    }
    
    private func checkInOnServerFailed(error: AnyObject) {
        let alertTitle = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), checkInData!.leaderboardName)
        let alertController = UIAlertController(title: alertTitle,
                                                message: error.localizedDescription,
                                                preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Controller
    
    private func checkInDidStart() {
        SVProgressHUD.show()
        self.delegate.checkInDidStart(self)
    }
    
    private func checkInDidEnd(withSuccess succeed: Bool) {
        SVProgressHUD.popActivity()
        self.delegate.checkInDidEnd(self, withSuccess: succeed)
    }
    
    private func showCheckInAlert(alertController: UIAlertController) {
        SVProgressHUD.popActivity()
        self.delegate.showCheckInAlert(self, alertController: alertController)
    }
    
    private func checkInAlertDismissed() {
        SVProgressHUD.show()
    }
    
    // MARK: - Helper
    
    private func millisSince1970() -> Int64 {
        return Int64(NSDate().timeIntervalSince1970 * 1000)
    }

}