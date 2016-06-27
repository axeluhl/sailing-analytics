//
//  GPSAccuracyTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class GPSAccuracyTrackingTableViewCell: UITableViewCell {

    @IBOutlet weak var gpsAccuracyLabel: UILabel!
    @IBOutlet weak var gpsAccuracyView: GPSAccuracyView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.setupGPSAccuracyLabel()
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupGPSAccuracyLabel() {
        self.gpsAccuracyLabel.text = NSLocalizedString("No GPS", comment: "")
    }
    
    private func setupGPSAccuracyLabel(horizontalAccuracy: Double!) {
        self.gpsAccuracyLabel.text = "~ " + String(format: "%.0f", horizontalAccuracy) + " m"
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(newLocation),
                                                         name:LocationManager.NotificationType.newLocation,
                                                         object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed),
                                                         name:LocationManager.NotificationType.locationManagerFailed,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func newLocation(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            let horizontalAccuracy = notification.userInfo!["horizontalAccuracy"] as! Double
            self.setupGPSAccuracyLabel(horizontalAccuracy)
        })
    }
    
    func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupGPSAccuracyLabel()
        })
    }
    
}
