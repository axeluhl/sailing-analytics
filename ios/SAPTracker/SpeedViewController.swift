//
//  SpeedViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SpeedViewController: UIViewController {
 
    let knPerMs = 1.94552529182879
    
    @IBOutlet weak var speedLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newLocation:", name:LocationManager.NotificationType.newLocation, object: nil)
    }
    
    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    func newLocation(notification: NSNotification) {
        let ms = notification.userInfo!["speed"] as Double
        let kn = ms * knPerMs
        speedLabel.text = String(format: "%0.1f kn", kn)
    }

}