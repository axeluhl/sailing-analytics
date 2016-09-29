//
//  TrackingViewGPSFixesCell.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.06.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TrackingViewGPSFixesCell: UITableViewCell {

    var regatta: Regatta?
    
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
    
    private func setup() {
        setupLocalization()
        setupGPSFixesLabel()
    }
    
    private func setupLocalization() {
        gpsFixesTitleLabel.text = Translation.TrackingView.TableView.GPSFixesCell.GPSFixesTitleLabel.Text.String
    }
    
    private func setupGPSFixesLabel() {
        gpsFixesLabel.text = String(format: "%d", regatta?.gpsFixes?.count ?? 0)
    }
    
    // MARK: - Notifications
    
    private func subscribeForNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector:#selector(locationManagerUpdated(_:)),
                                                         name:LocationManager.NotificationType.Updated,
                                                         object: nil
        )
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    @objc private func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupGPSFixesLabel()
        })
    }
    
}
