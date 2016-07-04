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
    
    private struct EventKeys {
        static let EndDate = "endDate"
        static let ID = "id"
        static let ImageURLs = "imageURLs"
        static let Name = "name"
        static let StartDate = "startDate"
    }
    
    private struct CompetitorKeys {
        static let BoatClassName = "boatClassName"
        static let CountryCode = "countryCode"
        static let ID = "id"
        static let Name = "name"
        static let Nationality = "nationality"
        static let SailID = "sailID"
    }
    
    private struct LeaderboardKeys {
        static let Name = "name"
    }
    
    private let checkInData: CheckInData!
    private let delegate: CheckInControllerDelegate!
    private let requestManager: RequestManager!
    
    // Responses
    private var eventDictionary: [String: AnyObject]?
    private var leaderboardDictionary: [String: AnyObject]?
    private var competitorDictionary: [String: AnyObject]?
    private var teamImageURL: String?
    
    init(checkInData: CheckInData, delegate: CheckInControllerDelegate) {
        self.checkInData = checkInData
        self.delegate = delegate
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
        super.init()
    }
    
    // MARK: - Start check-in
    
    func startCheckIn() {
        checkInDidStart()
        requestManager.getEvent(checkInData.eventID,
                                success: { (operation, responseObject) -> Void in self.eventSucceed(responseObject) },
                                failure: { (operation, error) -> Void in self.eventFailed(error) })
    }
    
    // MARK: - Event
    
    private func eventSucceed(eventResponseObject: AnyObject) {
        eventDictionary = eventResponseObject as? [String: AnyObject]
        requestManager.getLeaderboard(checkInData.leaderboardName,
                                      success: { (operation, responseObject) -> Void in self.leaderboardSucceed(responseObject) },
                                      failure: { (operation, error) -> Void in self.leaderboardFailed(error) })
    }
    
    private func eventFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get event %@", comment: ""), checkInData.eventID)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - Leaderboard
    
    private func leaderboardSucceed(leaderboardResponseObject: AnyObject) {
        leaderboardDictionary = leaderboardResponseObject as? [String: AnyObject]
        requestManager.getCompetitor(checkInData.competitorID,
                                     success: { (operation, responseObject) in self.competitorSucceed(responseObject) },
                                     failure: { (operation, error) in self.competitorFailed(error) })
    }
    
    private func leaderboardFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get leader board %@", comment: ""), checkInData.leaderboardName)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - Competitor
    
    private func competitorSucceed(competitorResponseObject: AnyObject) {
        competitorDictionary = competitorResponseObject as? [String: AnyObject]
        requestManager.getTeamImageURL(competitorID(), result: { (imageURL) in self.teamImageURLResult(imageURL) })
    }
    
    private func competitorFailed(error: AnyObject) {
        let alertTitle = String(format: NSLocalizedString("Couldn't get competitor %@", comment: ""), checkInData.competitorID)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        showCheckInAlert(alertController)
    }
    
    // MARK: - Team
    
    private func teamImageURLResult(imageURL: String?) {
        teamImageURL = imageURL
        let alertTitle = String(format:NSLocalizedString("Hello %@. Welcome to %@. You are registered as %@.", comment: ""),
                                competitorName(),
                                leaderboardName(),
                                competitorSailID())
        let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .Alert)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default) { (action) in
            self.checkInOnServer()
        }
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Check-in on server
    
    private func checkInOnServer() {
        SVProgressHUD.show()
        requestManager.postCheckIn(leaderboardName(),
                                   competitorID: competitorID(),
                                   success: { (operation, responseObject) -> Void in self.checkInOnServerSucceed() },
                                   failure: { (operation, error) -> Void in self.checkInOnServerFailed(error) })
    }
    
    private func checkInOnServerSucceed() {
        
        // Check database if check-in already exist
        if let checkIn = DataManager.sharedManager.fetchCheckIn(checkInData.eventID,
                                                                leaderboardName: checkInData.leaderboardName,
                                                                competitorID: checkInData.competitorID) {
            // Delete old check-in
            DataManager.sharedManager.deleteCheckIn(checkIn)
            DataManager.sharedManager.saveContext()
        }
        
        // Create new check-in
        let checkIn = DataManager.sharedManager.newCheckIn()
        checkIn.competitorID = checkInData!.competitorID
        checkIn.eventID = checkInData.eventID
        checkIn.lastSyncDate = NSDate().timeIntervalSince1970
        checkIn.leaderboardName = checkInData!.leaderboardName
        checkIn.serverURL = checkInData!.serverURL
        checkIn.teamImageURL = teamImageURL
        checkIn.teamImageRetry = false
        
        // Create new event
        let event = DataManager.sharedManager.newEvent(checkIn)
        event.endDate = (eventEndDate() / 1000)
        event.eventID = eventID()
        event.name = eventName()
        event.startDate = (eventStartDate() / 1000)

        // Create new leaderboard
        let leaderboard = DataManager.sharedManager.newLeaderBoard(checkIn)
        leaderboard.name = leaderboardName()
        
        // Create new competitor
        let competitor = DataManager.sharedManager.newCompetitor(checkIn)
        competitor.boatClassName = competitorBoatClassName()
        competitor.competitorID = competitorID()
        competitor.countryCode = competitorCountryCode()
        competitor.name = competitorName()
        competitor.nationality = competitorNationality()
        competitor.sailID = competitorSailID()
        
        // Save objects
        DataManager.sharedManager.saveContext()
        
        // Check-in completed
        self.checkInDidEnd(withSuccess: true)
    }
    
    private func checkInOnServerFailed(error: AnyObject) {
        let alertTitle = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), checkInData!.leaderboardName)
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel) { (action) in
            self.checkInDidEnd(withSuccess: false)
        }
        alertController.addAction(cancelAction)
        self.showCheckInAlert(alertController)
    }
    
    // MARK: - Controller
    
    private func checkInDidStart() {
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.show()
            self.delegate.checkInDidStart?(self)
        })
    }
    
    private func checkInDidEnd(withSuccess succeed: Bool) {
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.popActivity()
            self.delegate.checkInDidEnd?(self, withSuccess: succeed)
        })
    }
    
    private func showCheckInAlert(alertController: UIAlertController) {
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.popActivity()
            self.delegate.showCheckInAlert(self, alertController: alertController)
        })
    }
    
    // MARK: - Event
    
    private func eventID() -> String! { return stringFromEvent(forKey: EventKeys.ID) }
    private func eventName() -> String! { return stringFromEvent(forKey: EventKeys.Name) }
    private func eventStartDate() -> Double! { return doubleFromEvent(forKey: EventKeys.StartDate) }
    private func eventEndDate() -> Double! { return doubleFromEvent(forKey: EventKeys.EndDate) }
    
    private func doubleFromEvent(forKey key: String) -> Double { return doubleFromDictionary(eventDictionary, forKey: key) }
    private func stringFromEvent(forKey key: String) -> String { return stringFromDictionary(eventDictionary, forKey: key) }
    
    // MARK: - Leaderboard
    
    private func leaderboardName() -> String! { return stringFromLeaderboard(forKey: LeaderboardKeys.Name) }
    
    private func stringFromLeaderboard(forKey key: String) -> String { return stringFromDictionary(leaderboardDictionary, forKey: key) }
    
    // MARK: - Competitor
    
    private func competitorBoatClassName() -> String! { return stringFromCompetitor(forKey: CompetitorKeys.BoatClassName) }
    private func competitorCountryCode() -> String! { return stringFromCompetitor(forKey: CompetitorKeys.CountryCode) }
    private func competitorID() -> String { return stringFromCompetitor(forKey: CompetitorKeys.ID) }
    private func competitorName() -> String! { return stringFromCompetitor(forKey: CompetitorKeys.Name) }
    private func competitorNationality() -> String! { return stringFromCompetitor(forKey: CompetitorKeys.Nationality) }
    private func competitorSailID() -> String! { return stringFromCompetitor(forKey: CompetitorKeys.SailID) }

    private func stringFromCompetitor(forKey key: String) -> String { return stringFromDictionary(competitorDictionary, forKey: key) }
    
    // MARK: - Helper
    
    private func doubleFromDictionary(dictionary: [String: AnyObject]?, forKey key: String!) -> Double {
        if let value = dictionary?[key] as? Double {
            return value
        } else {
            return 0.0
        }
    }
    
    private func stringFromDictionary(dictionary: [String: AnyObject]?, forKey key: String!) -> String {
        if let value = dictionary?[key] as? String {
            return value
        } else {
            return ""
        }
    }

}