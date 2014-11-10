//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class RegattaViewController : UIViewController {
    
    // var regatta: Regatta
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        LocationManager.sharedManager.startTracking()
    }
    
}