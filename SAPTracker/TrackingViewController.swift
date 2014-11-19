//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController, UIAlertViewDelegate {
    
    enum AlertViewTag: Int {
        case StopTracking
    }
    @IBOutlet weak var gpsQuality1: UIView!
    @IBOutlet weak var gpsQuality2: UIView!
    @IBOutlet weak var gpsQuality3: UIView!
    @IBOutlet weak var gpsQuality4: UIView!
    @IBOutlet weak var trackingStatusLabel: UILabel!
    @IBOutlet weak var onlineModeLabel: UILabel!
    @IBOutlet weak var trackingTimeLabel: UILabel!
    
    let gpsActiveColor = UIColor(hex: 0x8AB54D)
    let gpsInactiveColor = UIColor(hex: 0x445A2F)
    let greenColor = UIColor(hex: 0x408000)
    let redColor = UIColor(hex: 0xFF0000)
    let orangeColor = UIColor(hex: 0xFF8000)
    let startDate = NSDate()
    let dateFormatter = NSDateFormatter()
    
    /* Register for notifications. Set up timer */
    override func viewDidLoad() {
        super.viewDidLoad()
        
        APIManager.sharedManager.initManager(DataManager.sharedManager.selectedEvent!.serverUrl)
        
        // set online/buffering label
        networkAvailabilityChanged()
        
        // register for notifications
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"networkAvailabilityChanged", name:APIManager.NotificationType.networkAvailabilityChanged, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newLocation:", name:LocationManager.NotificationType.newLocation, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"locationManagerFailed:", name:LocationManager.NotificationType.locationManagerFailed, object: nil)
        
        // start tracking timer
        let timer = NSTimer(timeInterval: 0.1, target: self, selector: "timer:", userInfo: nil, repeats: true)
        NSRunLoop.currentRunLoop().addTimer(timer, forMode:NSRunLoopCommonModes)
        dateFormatter.dateFormat = "HH:mm:ss.S"
        dateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
    }
    
    // MARK:- Notifications
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func networkAvailabilityChanged() {
        if (APIManager.sharedManager.networkAvailable) {
            onlineModeLabel.text = "Online"
            onlineModeLabel.textColor = greenColor
        } else {
            onlineModeLabel.text = "Buffering"
            onlineModeLabel.textColor = orangeColor
        }
    }
    
    func newLocation(notification: NSNotification) {
        let horizontalAccuracy = notification.userInfo!["horizontalAccuracy"] as Double
        gpsQuality1.backgroundColor = gpsInactiveColor
        gpsQuality2.backgroundColor = gpsInactiveColor
        gpsQuality3.backgroundColor = gpsInactiveColor
        gpsQuality4.backgroundColor = gpsInactiveColor
        if (horizontalAccuracy < 0) {
        }
        else if (horizontalAccuracy > 163) {
            gpsQuality1.backgroundColor = gpsActiveColor
            gpsQuality2.backgroundColor = gpsActiveColor
        }
        else if (horizontalAccuracy > 48) {
            gpsQuality1.backgroundColor = gpsActiveColor
            gpsQuality2.backgroundColor = gpsActiveColor
            gpsQuality3.backgroundColor = gpsActiveColor
        }
        else {
            gpsQuality1.backgroundColor = gpsActiveColor
            gpsQuality2.backgroundColor = gpsActiveColor
            gpsQuality3.backgroundColor = gpsActiveColor
            gpsQuality4.backgroundColor = gpsActiveColor
        }
        trackingStatusLabel.text = "Tracking"
        trackingStatusLabel.textColor = greenColor
    }
    
    func locationManagerFailed(notification: NSNotification) {
        trackingStatusLabel.text = "Not Tracking"
        trackingStatusLabel.textColor = redColor
    }
    
    // MARK:- Timer
    
    func timer(timer: NSTimer) {
        let currentDate = NSDate()
        let timeInterval = currentDate.timeIntervalSinceDate(startDate)
        let timerDate = NSDate(timeIntervalSince1970: timeInterval)
        trackingTimeLabel.text = dateFormatter.stringFromDate(timerDate)
    }
    
    // MARK:- Buttons
    
    /* Stop tracking, go back to regattas view */
    @IBAction func stopTrackingButtonTapped(sender: AnyObject) {
        let alertView = UIAlertView(title: "Stop tracking?", message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "Stop")
        alertView.tag = AlertViewTag.StopTracking.rawValue;
        alertView.show()
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertViewTag.StopTracking.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break
            default:
                LocationManager.sharedManager.stopTracking()
                self.dismissViewControllerAnimated(true, nil)
            }
            break
        default:
            break
        }
    }
    
}