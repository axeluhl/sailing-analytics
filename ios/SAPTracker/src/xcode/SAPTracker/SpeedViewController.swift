//
//  SpeedViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SpeedViewController: UIViewController {
    
    fileprivate let mpsToKn = 1.94552529182879
    
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
    
    fileprivate func setup() {
        setupLocalization()
        setupSpeedLabel(mps: -1.0)
    }
    
    fileprivate func setupLocalization() {
        titleLabel.text = Translation.SpeedView.TitleLabel.Text.String
    }
    
    fileprivate func setupSpeedLabel(mps: Double) {
        if mps >= 0 {
            let kn = mps * mpsToKn
            speedLabel.text = String(format: "%0.1f kn", kn)
        } else {
            speedLabel.text = "– kn"
        }
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
            name:NSNotification.Name(rawValue: LocationManager.NotificationType.Failed),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func locationManagerUpdated(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let locationData = notification.userInfo?[LocationManager.UserInfo.LocationData] as? LocationData else { return }
            self.setupSpeedLabel(mps: locationData.location.speed)
        })
    }
    
    @objc fileprivate func locationManagerFailed(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.setupSpeedLabel(mps: -1.0)
        })
    }
    
}
