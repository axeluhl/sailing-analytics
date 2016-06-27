//
//  CachedFixesTrackingTableViewCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class CachedFixesTrackingTableViewCell: UITableViewCell {

    @IBOutlet weak var cachedFixesLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        self.setupCachedFixesLabel()
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupCachedFixesLabel() {
        self.cachedFixesLabel.text = String(format: "%d", DataManager.sharedManager.countCachedFixes())
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(newLocation),
                                                         name:LocationManager.NotificationType.newLocation,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func newLocation(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupCachedFixesLabel()
        })
    }
    
}
