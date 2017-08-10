//
//  TrackingViewStatusCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TrackingViewStatusCell: UITableViewCell {

    @IBOutlet weak var statusTitleLabel: UILabel!
    @IBOutlet weak var statusLabel: UILabel!
    
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
        setupStatusLabel(status: LocationManager.Status.NotTracking)
    }
    
    fileprivate func setupLocalization() {
        statusTitleLabel.text = Translation.LocationManager.Status.String
    }
    
    fileprivate func setupStatusLabel(status: LocationManager.Status) {
        statusLabel.text = status.description
        statusLabel.textColor = (status == .NotTracking ? .red : .black)
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
            self.setupStatusLabel(status: LocationManager.Status.Tracking)
        })
    }
    
    @objc fileprivate func locationManagerFailed(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.setupStatusLabel(status: LocationManager.Status.NotTracking)
        })
    }
    
}
