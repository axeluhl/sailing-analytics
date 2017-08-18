//
//  TrackingViewGPSFixesCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TrackingViewGPSFixesCell: UITableViewCell {

    weak var checkIn: CheckIn?
    
    @IBOutlet weak var gpsFixesTitleLabel: UILabel!
    @IBOutlet weak var gpsFixesLabel: UILabel!
    
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
        setupLocalization()
        setupGPSFixesLabel()
    }
    
    fileprivate func setupLocalization() {
        gpsFixesTitleLabel.text = Translation.TrackingView.TableView.GPSFixesCell.GPSFixesTitleLabel.Text.String
    }
    
    fileprivate func setupGPSFixesLabel() {
        gpsFixesLabel.text = String(format: "%d", checkIn?.gpsFixes?.count ?? 0)
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(locationManagerUpdated(_:)),
            name: NSNotification.Name(rawValue: LocationManager.NotificationType.Updated),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func locationManagerUpdated(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.setupGPSFixesLabel()
        })
    }
    
}
