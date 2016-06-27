//
//  ModeTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class ModeTrackingTableViewCell: UITableViewCell {

    @IBOutlet weak var modeLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.setupModeLabel()
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupModeLabel() {
        if (APIManager.sharedManager.networkAvailable) {
            if !BatteryManager.sharedManager.batterySaving {
                modeLabel.text = NSLocalizedString("Online", comment: "")
            } else {
                modeLabel.text = NSLocalizedString("Battery Saving", comment: "")
            }
        } else {
            modeLabel.text = NSLocalizedString("Offline", comment: "")
        }
        
        // FIXME: - Show "API Error" in red in case of response errors
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(networkAvailabilityChanged),
                                                         name:APIManager.NotificationType.networkAvailabilityChanged,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func networkAvailabilityChanged(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupModeLabel()
        })
    }
    
}
