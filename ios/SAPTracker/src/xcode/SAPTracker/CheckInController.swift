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
    
    var delegate: CheckInControllerDelegate?
    
    private var checkInData = CheckInData()
    private var requestManager = RequestManager()

    // Responses
    private var eventDictionary: [String: AnyObject]?
    private var leaderboardDictionary: [String: AnyObject]?
    private var competitorDictionary: [String: AnyObject]?
    private var teamImageURL: String?
    
    // MARK: - Initialize
    
    private func initialize(checkInData: CheckInData) {
        self.checkInData = checkInData
        setupRequestManager()
    }
    
    // MARK: - Setups
    
    private func setupRequestManager() {
        requestManager = RequestManager(baseURLString: checkInData.serverURL)
    }
    
    // MARK: - Start check-in
    
    func startCheckIn(checkInData: CheckInData) {
        initialize(checkInData)
        checkInDidStart()
        requestManager.getEvent(checkInData.eventID,
                                success: { (operation, responseObject) -> Void in self.eventSucceed(responseObject) },
                                failure: { (operation, error) -> Void in self.eventFailed(error) }
        )
    }
    
    // MARK: - Event
    
    private func eventSucceed(eventResponseObject: AnyObject) {
        eventDictionary = eventResponseObject as? [String: AnyObject]
        requestManager.getLeaderboard(checkInData.leaderboardName,
                                      success: { (operation, responseObject) -> Void in self.leaderboardSucceed(responseObject) },
                                      failure: { (operation, error) -> Void in self.leaderboardFailed(error) }
        )
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
                                     failure: { (operation, error) in self.competitorFailed(error) }
        )
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
        requestManager.getTeamImageURL(competitorID, result: { (imageURL) in self.teamImageURLResult(imageURL) })
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
                                competitorName,
                                leaderboardName,
                                competitorSailID)
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
        showCheckInAlert(alertController)
    }
    
    // MARK: - Check-in on server
    
    private func checkInOnServer() {
        SVProgressHUD.show()
        requestManager.postCheckIn(leaderboardName,
                                   competitorID: competitorID,
                                   success: { (operation, responseObject) -> Void in self.checkInOnServerSucceed() },
                                   failure: { (operation, error) -> Void in self.checkInOnServerFailed(error) }
        )
    }
    
    private func checkInOnServerSucceed() {
        
        // Regatta
        let regatta = CoreDataManager.sharedManager.fetchRegatta(checkInData.eventID,
                                                                 leaderboardName: checkInData.leaderboardName,
                                                                 competitorID: checkInData.competitorID
        ) ?? CoreDataManager.sharedManager.newRegatta()
        regatta.serverURL = checkInData.serverURL
        regatta.teamImageURL = teamImageURL
        regatta.teamImageRetry = false        
        
        // Event
        let event = regatta.event ?? CoreDataManager.sharedManager.newEvent(regatta)
        event.endDate = (eventEndDate / 1000)
        event.eventID = eventID
        event.name = eventName
        event.startDate = (eventStartDate / 1000)
        
        // Leaderboard
        let leaderboard = regatta.leaderboard ?? CoreDataManager.sharedManager.newLeaderboard(regatta)
        leaderboard.name = leaderboardName
        
        // Competitor
        let competitor = regatta.competitor ?? CoreDataManager.sharedManager.newCompetitor(regatta)
        competitor.boatClassName = competitorBoatClassName
        competitor.competitorID = competitorID
        competitor.countryCode = competitorCountryCode
        competitor.name = competitorName
        competitor.nationality = competitorNationality
        competitor.sailID = competitorSailID
        
        // Save
        CoreDataManager.sharedManager.saveContext()
        
        // Check-in completed
        checkInDidEnd(withSuccess: true)
    }
    
    private func checkInOnServerFailed(error: AnyObject) {
        let alertTitle = String(format:NSLocalizedString("Couldn't check-in to %@", comment: ""), checkInData.leaderboardName)
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
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.show()
            self.delegate?.checkInDidStart?(self)
        })
    }
    
    private func checkInDidEnd(withSuccess succeed: Bool) {
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.popActivity()
            self.delegate?.checkInDidEnd?(self, withSuccess: succeed)
        })
    }
    
    private func showCheckInAlert(alertController: UIAlertController) {
        dispatch_async(dispatch_get_main_queue(), {
            SVProgressHUD.popActivity()
            self.delegate?.showCheckInAlert(self, alertController: alertController)
        })
    }
    
    // MARK: - Event
    
    var eventID: String { get { return stringFromEvent(forKey: EventKeys.ID) } }
    var eventName: String { get { return stringFromEvent(forKey: EventKeys.Name) } }
    var eventStartDate: Double { get { return doubleFromEvent(forKey: EventKeys.StartDate) } }
    var eventEndDate: Double { get { return doubleFromEvent(forKey: EventKeys.EndDate) } }
    
    // MARK: - Leaderboard
    
    var leaderboardName: String { get { return stringFromLeaderboard(forKey: LeaderboardKeys.Name) } }
    
    // MARK: - Competitor
    
    var competitorBoatClassName: String { get { return stringFromCompetitor(forKey: CompetitorKeys.BoatClassName) } }
    var competitorCountryCode: String { get { return stringFromCompetitor(forKey: CompetitorKeys.CountryCode) } }
    var competitorID: String { get { return stringFromCompetitor(forKey: CompetitorKeys.ID) } }
    var competitorName: String { get { return stringFromCompetitor(forKey: CompetitorKeys.Name) } }
    var competitorNationality: String { get { return stringFromCompetitor(forKey: CompetitorKeys.Nationality) } }
    var competitorSailID: String { get { return stringFromCompetitor(forKey: CompetitorKeys.SailID) } }
    
    // MARK: - Helper

    private func doubleFromEvent(forKey key: String) -> Double { return doubleFromDictionary(eventDictionary, forKey: key) }
    private func stringFromEvent(forKey key: String) -> String { return stringFromDictionary(eventDictionary, forKey: key) }
    private func stringFromLeaderboard(forKey key: String) -> String { return stringFromDictionary(leaderboardDictionary, forKey: key) }
    private func stringFromCompetitor(forKey key: String) -> String { return stringFromDictionary(competitorDictionary, forKey: key) }
    
    private func doubleFromDictionary(dictionary: [String: AnyObject]?, forKey key: String) -> Double {
        if let value = dictionary?[key] as? Double {
            return value
        } else {
            return 0.0
        }
    }
    
    private func stringFromDictionary(dictionary: [String: AnyObject]?, forKey key: String) -> String {
        if let value = dictionary?[key] as? String {
            return value
        } else {
            return ""
        }
    }

}