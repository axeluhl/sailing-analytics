//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class RegattaViewController : UIViewController, UIActionSheetDelegate {
    
    // var regatta: Regatta
    
    @IBAction func showActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Checkout", "Settings", "Edit Photo", "About", "Cancel")
        actionSheet.cancelButtonIndex = 4
        actionSheet.showInView(self.view)
    }
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        switch buttonIndex{
        case 1:
            performSegueWithIdentifier("Settings", sender: actionSheet)
            break
        case 3:
            performSegueWithIdentifier("About", sender: actionSheet)
            break
        default:
            break
        }
    }
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        LocationManager.sharedManager.startTracking()
    }
    
}