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
        setupModeLabel(GPSFixController.Mode.Offline)
        subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
    private func setupModeLabel(mode: GPSFixController.Mode) {
        modeLabel.text = mode.description
    }
    
    // MARK: - Notifications

    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(regattaControllerModeChanged),
                                                         name:GPSFixController.NotificationType.ModeChanged,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func regattaControllerModeChanged(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let rawValue = notification.userInfo?[GPSFixController.UserInfo.Mode] as? String else { return }
            guard let mode = GPSFixController.Mode(rawValue: rawValue) else { return }
            self.setupModeLabel(mode)
        })
    }
    
}
