//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController {
    
    @IBOutlet weak var gpsAccuracy: UILabel!
    @IBOutlet weak var trackingStatusLabel: UILabel!
    @IBOutlet weak var onlineModeLabel: UILabel!
    
    struct Color {
        static let GpsActive = UIColor(hex: 0x8AB54D)
        static let GpsInactive = UIColor(hex: 0x445A2F)
        static let Green = UIColor(hex: 0x408000)
        static let Red = UIColor(hex: 0xFF0000)
        static let Orange = UIColor(hex: 0xFF8000)
    }
    
    /* Register for notifications. Set up timer */
    override func viewDidLoad() {
        super.viewDidLoad()
  
        // set values
        navigationItem.title = DataManager.sharedManager.selectedEvent!.leaderBoard!.name
        
        // set tracking event, data for this event is sent in higher priority
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
            if !BatteryManager.sharedManager.batterySaving {
                onlineModeLabel.text = "Online"
                onlineModeLabel.textColor = Color.Green
            } else {
                onlineModeLabel.text = "Battery Saving"
                onlineModeLabel.textColor = Color.Orange
            }
        } else {
            onlineModeLabel.text = "Offline"
            onlineModeLabel.textColor = Color.Red
        }
    }
    
    func newLocation(notification: NSNotification) {
        let horizontalAccuracy = notification.userInfo!["horizontalAccuracy"] as Double
        gpsAccuracy.text = "~ " + String(format: "%.0f", horizontalAccuracy) + " m"
        trackingStatusLabel.text = "Tracking"
        trackingStatusLabel.textColor = Color.Green
    }
    
    func locationManagerFailed(notification: NSNotification) {
        gpsAccuracy.text = "no GPS"
        trackingStatusLabel.text = "Not Tracking"
        trackingStatusLabel.textColor = Color.Red
    }

}