//
//  SessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol SessionViewControllerDelegate {

    func performCheckOut()

    func startTracking() throws

}

class SessionViewController: UIViewController {

    struct Segue {
        static let About = "About"
        static let Leaderboard = "Leaderboard"
        static let Settings = "Settings"
        static let Tracking = "Tracking"
    }

    @IBOutlet weak var startTrackingButton: UIButton!

    var delegate: SessionViewControllerDelegate!
    
    // MARK: - Actions

    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        // TODO: Add or add not WiFi Alert?
        //if SMTWiFiStatus.wifiStatus() == WiFiStatus.On && !AFNetworkReachabilityManager.sharedManager().reachableViaWiFi {
        //    showStartTrackingWiFiAlert(sender)
        //} else {
        startTracking(sender)
        //}
    }

    // MARK: - Tracking

    private func startTracking(sender: AnyObject) {
        do {
            try delegate.startTracking()
            performSegueWithIdentifier(Segue.Tracking, sender: sender)
        } catch let error as LocationManager.LocationManagerError {
            showStartTrackingFailureAlert(error.description)
        } catch {
            logError("\(#function)", error: error)
        }
    }
    
    // MARK: - CheckOut
    
    func checkOut() {
        showCheckOutAlert()
    }
    
    // MARK: - Alerts
    
    private func showCheckOutAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Warning.String,
            message: Translation.CompetitorView.CheckOutAlert.Message.String,
            preferredStyle: .Alert
        )
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .Default) { (action) in
            self.delegate.performCheckOut()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .Cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        presentViewController(alertController, animated: true, completion: nil)
    }

    private func showStartTrackingWiFiAlert(sender: AnyObject) {
        let alertController = UIAlertController(title: "INFO",
                                                message: "WIFI IS ON BUT NOT CONNECTED",
                                                preferredStyle: .Alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .Default) { (action) in
            UIApplication.sharedApplication().openURL(NSURL(string: "prefs:root=WIFI") ?? NSURL())
        }
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.startTracking(sender)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func showStartTrackingFailureAlert(message: String) {
        let alertController = UIAlertController(title: Translation.Common.Warning.String,
                                                message: message,
                                                preferredStyle: .Alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .Default) { (action) in
            UIApplication.sharedApplication().openURL(NSURL(string: "prefs:root=LOCATION_SERVICES") ?? NSURL())
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Default, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }

}