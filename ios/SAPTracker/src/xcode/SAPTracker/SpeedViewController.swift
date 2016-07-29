//
//  SpeedViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SpeedViewController: UIViewController {
    
    private let mpsToKn = 1.94552529182879
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var speedLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        subscribeForNotifications()
    }
    
    deinit {
        unsubscribeFromNotifications()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupSpeedLabel(-1.0)
    }
    
    private func setupLocalization() {
        titleLabel.text = Translation.SpeedView.TitleLabel.Text.String
    }
    
    private func setupSpeedLabel(mps: Double) {
        if mps >= 0 {
            let kn = mps * mpsToKn
            speedLabel.text = String(format: "%0.1f kn", kn)
        } else {
            speedLabel.text = "– kn"
        }
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
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupSpeedLabel(locationData.location.speed)
        })
    }
    
    @objc private func locationManagerFailed(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupSpeedLabel(-1.0)
        })
    }
    
}