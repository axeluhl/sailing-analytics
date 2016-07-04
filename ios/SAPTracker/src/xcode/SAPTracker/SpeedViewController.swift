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
    
    @IBOutlet weak var speedLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.setupSpeedLabel(-1.0)
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
    }
    
    // MARK: - Setups
    
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
                                                         name:LocationManager.NotificationType.LocationManagerUpdated,
                                                         object: nil)
    }
    
    private func unsubscribeFromNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func locationManagerUpdated(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.setupSpeedLabel(notification.userInfo![LocationManager.UserInfo.Speed] as! Double)
        })
    }

}