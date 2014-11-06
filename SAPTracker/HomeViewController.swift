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
    enum AlertViewTag: Int {
        case StopTracking
    }
   
    override func viewDidLoad() {
        super.viewDidLoad()
        addObservers()
    }

    deinit {
        removeObservers()
    }
    
    func addObservers() {
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStarted:", name: LocationManager.NotificationType.trackingStarted, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStopped:", name: LocationManager.NotificationType.trackingStopped, object: nil)
    }
    
    func removeObservers() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    func trackingStarted(notification: NSNotification) {
        println("trackingStarted")
        trackingButton.setTitle("Stop Tracking", forState: UIControlState.Normal)
    }
    
    func trackingStopped(notification: NSNotification) {
        println("trackingStopped")
        trackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
    }

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
}

