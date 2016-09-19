//
//  GPSAccuracyTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TrackingViewGPSAccuracyCell: UITableViewCell {

    @IBOutlet weak var gpsAccuracyTitleLabel: UILabel!
    @IBOutlet weak var gpsAccuracyLabel: UILabel!
    @IBOutlet weak var gpsAccuracyView: GPSAccuracyView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        setup()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupGPSAccuracyLabel()
        setupLocalization()
    }
    
    private func setupGPSAccuracyLabel() {
        gpsAccuracyLabel.text = Translation.TrackingView.TableView.GPSAccuracyCell.GPSAccuracyLabel.Text.NoGPS.String
    }
    
    private func setupGPSAccuracyLabel(horizontalAccuracy: Double!) {
        gpsAccuracyLabel.text = "~ " + String(format: "%.0f", horizontalAccuracy) + " m"
    }
    
    private func setupLocalization() {
        gpsAccuracyTitleLabel.text = Translation.TrackingView.TableView.GPSAccuracyCell.GPSAccuracyTitleLabel.Text.String
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
                                                         object: nil
        )
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerFailed(_:)),
                                                         name:LocationManager.NotificationType.Failed,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    @objc private func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupGPSAccuracyLabel(locationData.location.horizontalAccuracy)
        })
    }
    
    @objc private func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupGPSAccuracyLabel()
        })
    }
    
}
