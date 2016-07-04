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
        setupGPSAccuracyLabel()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupGPSAccuracyLabel() {
        gpsAccuracyLabel.text = NSLocalizedString("No GPS", comment: "")
    }
    
    private func setupGPSAccuracyLabel(horizontalAccuracy: Double!) {
        gpsAccuracyLabel.text = "~ " + String(format: "%.0f", horizontalAccuracy) + " m"
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.LocationManagerUpdated,
                                                         object: nil)
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed(_:)),
                                                         name:LocationManager.NotificationType.LocationManagerFailed,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupGPSAccuracyLabel(notification.userInfo![LocationManager.UserInfo.HorizontalAccuracy] as! Double)
        })
    }
    
    func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupGPSAccuracyLabel()
        })
    }
    
}
