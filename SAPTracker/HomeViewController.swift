//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class HomeViewController: UIViewController, UIAlertViewDelegate {
    
    @IBOutlet weak var trackingButton: UIButton!
    @IBOutlet weak var batterySavingSwitch: UISwitch!
    enum AlertViewTag: Int {
        case StopTracking
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStarted:", name: LocationManager.NotificationType.trackingStarted, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStopped:", name: LocationManager.NotificationType.trackingStopped, object: nil)
        
        batterySavingSwitch.setOn(BatteryManager.sharedManager.batterySavingPreference, animated: true)
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    // MARK: - Notification Listeners
    func trackingStarted(notification: NSNotification) {
        trackingButton.setTitle("Stop Tracking", forState: UIControlState.Normal)
    }
    
    func trackingStopped(notification: NSNotification) {
        trackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
    }
    
    // MARK: - Button actions
    @IBAction func trackingButtonTap(sender: AnyObject) {
        if LocationManager.sharedManager.isTracking {
            let alertView = UIAlertView(title: "SAP Tracker", message: "Stop tracking?", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "Stop")
            alertView.tag = AlertViewTag.StopTracking.rawValue;
            alertView.alertViewStyle = .Default
            alertView.show()
        } else {
            LocationManager.sharedManager.startTracking()
        }
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
        case AlertViewTag.StopTracking.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break;
            default:
                LocationManager.sharedManager.stopTracking()
            }
            break;
        default:
            break;
        }
    }
    
    @IBAction func batterySavingChanged(sender: UISwitch) {
        BatteryManager.sharedManager.batterySavingPreference = sender.on
    }
    
}

