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
        self.subscribeForNotifications()
    }
    
    deinit {
        self.unsubscribeFromNotifications()
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
        let mps = notification.userInfo!["speed"] as! Double
        if mps >= 0 {
            let kn = mps * mpsToKn
            speedLabel.text = String(format: "%0.1f kn", kn)
        } else {
            speedLabel.text = "– kn"
        }
    }

}