//
//  TrackingViewModeCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TrackingViewModeCell: UITableViewCell {

    @IBOutlet weak var modeTitleLabel: UILabel!
    @IBOutlet weak var modeLabel: UILabel!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        setup()
        subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupModeLabel(GPSFixController.Mode.Offline)
    }
    
    private func setupLocalization() {
        modeTitleLabel.text = Translation.GPSFixController.Mode.String
    }
    
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
    
    @objc private func regattaControllerModeChanged(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            guard let rawValue = notification.userInfo?[GPSFixController.UserInfo.Mode] as? String else { return }
            guard let mode = GPSFixController.Mode(rawValue: rawValue) else { return }
            self.setupModeLabel(mode)
        })
    }
    
}
