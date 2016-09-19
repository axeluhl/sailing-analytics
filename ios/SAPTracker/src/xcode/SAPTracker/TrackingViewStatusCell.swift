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
    
    private func setup() {
        setupLocalization()
        setupStatusLabel(LocationManager.Status.NotTracking)
    }
    
    private func setupLocalization() {
        statusTitleLabel.text = Translation.LocationManager.Status.String
    }
    
    private func setupStatusLabel(status: LocationManager.Status) {
        statusLabel.text = status.description
        statusLabel.textColor = (status == .NotTracking ? UIColor.redColor() : UIColor.blackColor())
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
            self.setupStatusLabel(LocationManager.Status.Tracking)
        })
    }
    
    @objc private func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupStatusLabel(LocationManager.Status.NotTracking)
        })
    }
    
}
