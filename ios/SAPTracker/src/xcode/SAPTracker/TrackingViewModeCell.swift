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
    
    fileprivate func setup() {
        setupLocalization()
        setupModeLabel(mode: GPSFixController.Mode.None)
    }
    
    fileprivate func setupLocalization() {
        modeTitleLabel.text = Translation.GPSFixController.Mode.String
    }
    
    fileprivate func setupModeLabel(mode: GPSFixController.Mode) {
        modeLabel.text = mode.description
    }
    
    // MARK: - Notifications

    fileprivate func subscribeForNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(gpsFixControllerModeChanged),
            name: NSNotification.Name(rawValue: GPSFixController.NotificationType.ModeChanged),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func gpsFixControllerModeChanged(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let rawValue = notification.userInfo?[GPSFixController.UserInfo.Mode] as? String else { return }
            guard let mode = GPSFixController.Mode(rawValue: rawValue) else { return }
            self.setupModeLabel(mode: mode)
        })
    }
    
}
