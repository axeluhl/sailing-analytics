//
//  TrackingViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TrackingViewController : UIViewController, UIAlertViewDelegate {
    
    enum AlertViewTag: Int {
        case StopTracking
    }
    
    /* Stop tracking, go back to regattas view */
    @IBAction func stopTrackingButtonTapped(sender: AnyObject) {
        let alertView = UIAlertView(title: "SAP Tracker", message: "Stop tracking?", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "Stop")
        alertView.tag = AlertViewTag.StopTracking.rawValue;
        alertView.alertViewStyle = .Default
        alertView.show()
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
        case AlertViewTag.StopTracking.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break;
            default:
                LocationManager.sharedManager.stopTracking()
                self.dismissViewControllerAnimated(true, nil)
            }
            break;
        default:
            break;
        }
    }
    
}