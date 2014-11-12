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
    @IBOutlet weak var gpsQualityImageView: UIImageView!
    @IBOutlet weak var trackingStatusLabel: UILabel!
    @IBOutlet weak var onlineModeLabel: UILabel!
    @IBOutlet weak var trackingTimeLabel: UILabel!
    
    let startDate = NSDate()
    let dateFormatter = NSDateFormatter()
    
    /* Register for notifications. Set up timer */
    override func viewDidLoad() {
        super.viewDidLoad()
        
        networkAvailabilityChanged()
        
        // register for notifications
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"networkAvailabilityChanged", name:APIManager.NotificationType.networkAvailabilityChanged, object: nil)
        
        // timer
        let timer = NSTimer(timeInterval: 0.1, target: self, selector: "timerTick:", userInfo: nil, repeats: true)
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
            onlineModeLabel.textColor = UIColor(netHex: 0x408000)
        } else {
            onlineModeLabel.text = "Offline"
            onlineModeLabel.textColor = UIColor(netHex: 0xFF0000)
        }
    }
    
    // MARK:- Timer
    
    func timerTick(timer: NSTimer) {
        let currentDate = NSDate()
        let timeInterval = currentDate.timeIntervalSinceDate(startDate)
        let timerDate = NSDate(timeIntervalSince1970: timeInterval)
        trackingTimeLabel.text = dateFormatter.stringFromDate(timerDate)
    }
    
    // MARK:- Buttons
    
    /* Stop tracking, go back to regattas view */
    @IBAction func stopTrackingButtonTapped(sender: AnyObject) {
        let alertView = UIAlertView(title: "", message: "Stop tracking?", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "Stop")
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