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
    
    fileprivate func setup() {
        setupGPSAccuracyLabel()
        setupLocalization()
    }
    
    fileprivate func setupGPSAccuracyLabel() {
        gpsAccuracyLabel.text = Translation.TrackingView.TableView.GPSAccuracyCell.GPSAccuracyLabel.Text.NoGPS.String
    }
    
    fileprivate func setupGPSAccuracyLabel(horizontalAccuracy: Double!) {
        gpsAccuracyLabel.text = "~ " + String(format: "%.0f", horizontalAccuracy) + " m"
    }
    
    fileprivate func setupLocalization() {
        gpsAccuracyTitleLabel.text = Translation.TrackingView.TableView.GPSAccuracyCell.GPSAccuracyTitleLabel.Text.String
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerUpdated(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Updated),
            object: nil
        )
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerFailed(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Failed),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func locationManagerUpdated(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupGPSAccuracyLabel(horizontalAccuracy: locationData.location.horizontalAccuracy)
        })
    }
    
    @objc fileprivate func locationManagerFailed(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.setupGPSAccuracyLabel()
        })
    }
    
}
