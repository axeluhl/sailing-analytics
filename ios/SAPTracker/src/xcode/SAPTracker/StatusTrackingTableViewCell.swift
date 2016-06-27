//
//  StatusTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class StatusTrackingTableViewCell: UITableViewCell {

    @IBOutlet weak var statusLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.setupStatusLabel(false)
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupStatusLabel(isTracking: Bool) {
        statusLabel.text = isTracking ?
            NSLocalizedString("Tracking", comment: "") :
            NSLocalizedString("Not Tracking", comment: "")
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
            self.setupStatusLabel(true)
        })
    }
    
    func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupStatusLabel(false)
        })
    }
    
}
