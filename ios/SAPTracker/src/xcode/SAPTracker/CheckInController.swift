//
//  QRCodeManager.swift
//  SAPTracker
//
//  Created by computing on 17/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import UIKit
import AVFoundation

@objc protocol CheckInControllerDelegate {
    optional var activityIndicatorView: UIActivityIndicatorView { get }
    optional func qrCodeOK()
    optional func qrCodeCancel()
}

class CheckInController : NSObject, UIAlertViewDelegate {
    
    enum AlertViewTag: Int {
        case ConfirmCompetitor, ConfirmFailure
    }
    
    private var delegate: CheckInControllerDelegate?
    private var checkInData: CheckInData?
    
    init(delegate: CheckInControllerDelegate) {
        self.delegate = delegate
    }
    
    // MARK: - Start check-in
    
    func startCheckIn(urlString: String) {
        self.delegate?.activityIndicatorView?.startAnimating()
        if let checkInData = CheckInData(urlString: urlString) {
            self.checkInData = checkInData
            APIManager.sharedManager.initManager(checkInData.serverURL)
            APIManager.sharedManager.getEvent(checkInData.eventId,
                                              success: { (operation, responseObject) -> Void in self.eventSucceed(responseObject) },
                                              failure: { (operation, error) -> Void in self.eventFailed(error) })
        } else {
            let alertView = UIAlertView(title: NSLocalizedString("Incorrect QR Code", comment: ""),
                                        message: "",
                                        delegate: self,
                                        cancelButtonTitle: nil,
                                        otherButtonTitles: NSLocalizedString("Cancel", comment: ""))
            alertView.tag = AlertViewTag.ConfirmFailure.rawValue
            alertView.show()
        }
    }
    
    // MARK: - Event
    
    private func eventSucceed(eventResponseObject: AnyObject) {
        checkInData!.eventDictionary = eventResponseObject as? [String: AnyObject]
        APIManager.sharedManager.getLeaderBoard(checkInData!.leaderboardName,
                                                success: { (operation, responseObject) -> Void in self.leaderboardSucceed(responseObject) },
                                                failure: { (operation, error) -> Void in self.leaderboardFailed(error) })
    }
    
    private func eventFailed(error: AnyObject) {
        let title = String(format: NSLocalizedString("Couldn't get event %@", comment: ""), checkInData!.eventId)
        let cancelButtonTitle = NSLocalizedString("Cancel", comment: "")
        let alertView = UIAlertView(title: title,
                                    message: error.localizedDescription,
                                    delegate: self,
                                    cancelButtonTitle: cancelButtonTitle)
        alertView.tag = AlertViewTag.ConfirmFailure.rawValue
        alertView.show()
    }
    
    // MARK: - Leaderboard
    
    private func leaderboardSucceed(leaderboardResponseObject: AnyObject) {
        checkInData!.leaderboardDictionary = leaderboardResponseObject as? [String: AnyObject]
        APIManager.sharedManager.getCompetitor(checkInData!.competitorId, success: { (operation, responseObject) in self.competitorSucceed(responseObject) },
                                               failure: { (operation, error) in self.competitorFailed(error) })
    }
    
    private func leaderboardFailed(error: AnyObject) {
        let title = String(format: NSLocalizedString("Couldn't get leader board %@", comment: ""), checkInData!.leaderboardName)
        let cancelButtonTitle = NSLocalizedString("Cancel", comment: "")
        let alertView = UIAlertView(title: title,
                                    message: error.localizedDescription,
                                    delegate: self,
                                    cancelButtonTitle: cancelButtonTitle)
        alertView.tag = AlertViewTag.ConfirmFailure.rawValue
        alertView.show()
    }
    
    // MARK: - Competitor
    
    private func competitorSucceed(competitorResponseObject: AnyObject) {
        checkInData!.competitorDictionary = competitorResponseObject as? [String: AnyObject]
        APIManager.sharedManager.getTeamImageURI(checkInData?.dictionaryCompetitorId(),
                                                 result: { (imageURI) in self.teamImageURIResult(imageURI) })
    }
    
    private func competitorFailed(error: AnyObject) {
        let title = String(format: NSLocalizedString("Couldn't get competitor %@", comment: ""), checkInData!.competitorId)
        let cancelButtonTitle = NSLocalizedString("Cancel", comment: "")
        let alertView = UIAlertView(title: title,
                                    message: error.localizedDescription,
                                    delegate: self,
                                    cancelButtonTitle: cancelButtonTitle)
        alertView.tag = AlertViewTag.ConfirmFailure.rawValue
        alertView.show()
    }
    
    // MARK: - Team image URI
    
    private func teamImageURIResult(teamImageURI: String?) {
        checkInData!.teamImageURI = teamImageURI
        let title = String(format:NSLocalizedString("Hello %@. Welcome to %@. You are registered as %@.", comment: ""),
                           checkInData!.dictionaryCompetitorName(),
                           checkInData!.dictionaryLeaderboardName(),
                           checkInData!.dictionaryCompetitorSailId())
        let alertView = UIAlertView(title: title,
                                    message: "",
                                    delegate: self,
                                    cancelButtonTitle: NSLocalizedString("Cancel", comment: ""),
                                    otherButtonTitles: NSLocalizedString("OK", comment: ""))
        alertView.tag = AlertViewTag.ConfirmCompetitor.rawValue
        alertView.show()
    }
    
    // MARK: - AlertViewDelegate
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
        case AlertViewTag.ConfirmCompetitor.rawValue:
            confirmCompetitorAlertView(alertView, clickedButtonAtIndex: buttonIndex)
            break
        default:
            confirmFailureAlertView(alertView, clickedButtonAtIndex: buttonIndex)
            break
        }
    }
    
    private func confirmCompetitorAlertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch buttonIndex {
        case alertView.cancelButtonIndex:
            checkInFinishedWithFailure()
            break
        default:
            checkInOnServer()
            break
        }
    }
    
    private func confirmFailureAlertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        checkInFinishedWithFailure()
    }
    
    // MARK: - Check-in on server
    
    private func checkInOnServer() {
        APIManager.sharedManager.checkIn(checkInData!.dictionaryLeaderboardName(),
                                         competitorId: checkInData!.dictionaryCompetitorId(),
                                         deviceUuid: DeviceUDIDManager.UDID,
                                         pushDeviceId: "",
                                         fromMillis: millisSince1970(),
                                         success: { (operation, responseObject) -> Void in self.checkInOnServerSucceed() },
                                         failure: { (operation, error) -> Void in self.checkInOnServerFailed(error) })
    }
    
    private func checkInOnServerSucceed() {
        
        // Check database if check-in already exist
        if let checkIn = DataManager.sharedManager.getCheckIn(checkInData!.eventId,
                                                              leaderBoardName: checkInData!.leaderboardName,
                                                              competitorId: checkInData!.competitorId) {
            // Delete old check-in
            DataManager.sharedManager.deleteCheckIn(checkIn)
            DataManager.sharedManager.saveContext()
        }
        
        // Create new check-in
        let checkIn = DataManager.sharedManager.newCheckIn()
        checkIn.serverUrl = checkInData!.serverURL
        checkIn.eventId = checkInData!.eventId
        checkIn.leaderBoardName = checkInData!.leaderboardName
        checkIn.competitorId = checkInData!.competitorId
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
        checkInFinishedWithSuccess()
    }
    
    private func checkInOnServerFailed(error: AnyObject) {
        let title = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), checkInData!.leaderboardName)
        let cancelButtonTitle = NSLocalizedString("Cancel", comment: "")
        let alertView = UIAlertView(title: title,
                                    message: error.localizedDescription,
                                    delegate: self,
                                    cancelButtonTitle: cancelButtonTitle)
        alertView.tag = AlertViewTag.ConfirmFailure.rawValue
        alertView.show()
    }
    
    // MARK: - Finish
    
    private func checkInFinishedWithSuccess() {
        delegate?.qrCodeOK?()
        checkInFinished()
    }
    
    private func checkInFinishedWithFailure() {
        delegate?.qrCodeCancel?()
        checkInFinished()
    }
    
    private func checkInFinished() {
        checkInData = nil
        delegate?.activityIndicatorView?.stopAnimating()
    }
    
    // MARK: - Helper
    
    private func millisSince1970() -> Int64 {
        return Int64(NSDate().timeIntervalSince1970 * 1000)
    }

}