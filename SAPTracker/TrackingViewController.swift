//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController {
    
    @IBOutlet weak var gpsQuality1: UIView!
    @IBOutlet weak var gpsQuality2: UIView!
    @IBOutlet weak var gpsQuality3: UIView!
    @IBOutlet weak var gpsQuality4: UIView!
    @IBOutlet weak var trackingStatusLabel: UILabel!
    @IBOutlet weak var onlineModeLabel: UILabel!
    
    let gpsActiveColor = UIColor(hex: 0x8AB54D)
    let gpsInactiveColor = UIColor(hex: 0x445A2F)
    let greenColor = UIColor(hex: 0x408000)
    let redColor = UIColor(hex: 0xFF0000)
    let orangeColor = UIColor(hex: 0xFF8000)
    
    /* Register for notifications. Set up timer */
    override func viewDidLoad() {
        super.viewDidLoad()
        
        SendGPSFixController.sharedManager.trackingEvent = DataManager.sharedManager.selectedEvent
        
        // set online/buffering label
        networkAvailabilityChanged()
        
        // register for notifications
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"networkAvailabilityChanged", name:APIManager.NotificationType.networkAvailabilityChanged, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newLocation:", name:LocationManager.NotificationType.newLocation, object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"locationManagerFailed:", name:LocationManager.NotificationType.locationManagerFailed, object: nil)
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

}