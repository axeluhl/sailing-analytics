//
//  ViewController.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    
    @IBOutlet weak var trackingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()

        addObservers()
        
        //DataManager.sharedManager
        // TODO: start only when button pressed
        DataManager.sharedManager
    }

    deinit {
        removeObservers()
    }
    
    func addObservers() {
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStarted:", name: LocationManager.NotificationType.trackingStartedNotificationKey, object: nil)
   }
    
    func removeObservers() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }

    func trackingStarted(notification: NSNotification) {
        println("trackingStarted")
        trackingButton.setTitle("Stop Tracking", forState: UIControlState.Normal)
    }
    
    func trackingStopped(notification: NSNotification) {
        println("trackingStarted")
        trackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
    }

    @IBAction func trackingButtonTap(sender: AnyObject) {
        if (LocationManager.sharedManager.isTracking) {
            LocationManager.sharedManager.stopTracking()
        } else {
            LocationManager.sharedManager.startTracking()
        }
    }
}

