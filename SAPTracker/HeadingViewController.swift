//
//  HeadingViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class HeadingViewController: UIViewController {
    
    @IBOutlet weak var headingLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newHeading:", name:LocationManager.NotificationType.newHeading, object: nil)
    }
    
    func newHeading(notification: NSNotification) {
        let heading = notification.userInfo!["trueHeading"] as Double
        headingLabel.text = String(format: "%.0f °", heading)
    }
    
}