//
//  SessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol SessionViewControllerDelegate: class {
    
    var checkIn: CheckIn { get }
    
    var coreDataManager: CoreDataManager { get }
    
    var sessionController: SessionController { get }
    
    func makeOptionSheet() -> UIAlertController
    
    func refresh()
    
}

class SessionViewController: UIViewController {
    
    struct SessionSegue {
        static let About = "About"
        static let Leaderboard = "Leaderboard"
        static let Settings = "Settings"
        static let Tracking = "Tracking"
    }
    
    @IBOutlet weak var optionButton: UIBarButtonItem!
    @IBOutlet weak var startTrackingButton: UIButton!
    
    weak var delegate: SessionViewControllerDelegate!
    
    // MARK: - Tracking
    
    fileprivate func startTracking() {
        do {
            try delegate.sessionController.startTracking()
            performSegue(withIdentifier: SessionSegue.Tracking, sender: self)
        } catch let error as LocationManager.LocationManagerError {
            showStartTrackingFailureAlert(message: error.description)
        } catch {
            logError(name: "\(#function)", error: error)
        }
    }
    
    // MARK: - Update
    
    func update() {
        delegate.sessionController.update { [weak self] in
            self?.delegate.refresh()
        }
    }
    
    // MARK: - CheckOut
    
    func checkOut() {
        showCheckOutAlert()
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == SessionSegue.Tracking) {
            let trackingNC = segue.destination as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.checkIn = delegate.checkIn
            trackingVC.sessionController = delegate.sessionController
        } else if (segue.identifier == SessionSegue.Leaderboard) {
            let leaderboardNC = segue.destination as! UINavigationController
            let leaderboardVC = leaderboardNC.viewControllers[0] as! LeaderboardViewController
            leaderboardVC.checkIn = delegate.checkIn
        }
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        present(delegate.makeOptionSheet(), animated: true, completion: nil)
    }
    
    func makeActionCancel() -> UIAlertAction {
        return UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
    }
    
    func makeActionCheckOut() -> UIAlertAction {
        return UIAlertAction(title: Translation.CompetitorView.OptionSheet.CheckOutAction.Title.String, style: .default) { [weak self] action in
            self?.checkOut()
        }
    }
    
    func makeActionInfo() -> UIAlertAction {
        return UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: SessionSegue.About, sender: action)
        }
    }
    
    func makeActionSettings() -> UIAlertAction {
        return UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: SessionSegue.Settings, sender: self)
        }
    }
    
    func makeActionUpdate() -> UIAlertAction {
        return UIAlertAction(title: Translation.CompetitorView.OptionSheet.UpdateAction.Title.String, style: .default) { [weak self] action in
            self?.update()
        }
    }
    
    @IBAction func startTrackingButtonTapped(_ sender: AnyObject) {
        // TODO: Add or add not WiFi Alert?
        //if SMTWiFiStatus.wifiStatus() == WiFiStatus.On && !AFNetworkReachabilityManager.sharedManager().reachableViaWiFi {
        //    showStartTrackingWiFiAlert()
        //} else {
        startTracking()
        //}
    }
    
    // MARK: - Alerts
    
    fileprivate func showCheckOutAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Warning.String,
            message: Translation.CompetitorView.CheckOutAlert.Message.String,
            preferredStyle: .alert
        )
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .default) { [weak self] action in
            self?.performCheckOut()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func performCheckOut() {
        delegate.sessionController.checkOut { [weak self] (withSuccess) in
            self?.performCheckOutCompleted(withSuccess: withSuccess)
        }
    }
    
    fileprivate func performCheckOutCompleted(withSuccess: Bool) {
        delegate.coreDataManager.deleteObject(object: delegate.checkIn)
        delegate.coreDataManager.saveContext()
        navigationController?.popViewController(animated: true)
    }
    
    fileprivate func showStartTrackingWiFiAlert() {
        let alertController = UIAlertController(
            title: "INFO",
            message: "WIFI IS ON BUT NOT CONNECTED",
            preferredStyle: .alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .default) { action in
            if let settingsURL = URL(string: "prefs:root=WIFI") {
                UIApplication.shared.openURL(settingsURL)
            }
        }
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default) { [weak self] action in
            self?.startTracking()
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func showStartTrackingFailureAlert(message: String) {
        let alertController = UIAlertController(
            title: Translation.Common.Warning.String,
            message: message,
            preferredStyle: .alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .default) { action in
            if let locationServiceURL = URL(string: "prefs:root=LOCATION_SERVICES") {
                UIApplication.shared.openURL(locationServiceURL)
            }
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .default, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
}
