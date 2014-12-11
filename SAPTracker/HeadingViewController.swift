//
//  HeadingViewController.swift
//  SAPTracker
//
//  Created by computing on 09/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class HeadingViewController: UIViewController {
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var headingLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        NSNotificationCenter.defaultCenter().addObserver(self, selector:"newHeading:", name:LocationManager.NotificationType.newHeading, object: nil)
        titleLabel.text = LocationManager.sharedManager.headingPreference == LocationManager.Heading.True.rawValue ? "True Heading" : "Magnetic Heading"
    }
    
    func newHeading(notification: NSNotification) {
        titleLabel.text = LocationManager.sharedManager.headingPreference == LocationManager.Heading.True.rawValue ? "True Heading" : "Magnetic Heading"
        
        var heading: Double
        if LocationManager.sharedManager.headingPreference == LocationManager.Heading.True.rawValue {
            heading = notification.userInfo!["trueHeading"] as Double
        } else {
            heading = notification.userInfo!["magneticHeading"] as Double
        }
        headingLabel.text = String(format: "%.0f °", heading)
    }
    
}