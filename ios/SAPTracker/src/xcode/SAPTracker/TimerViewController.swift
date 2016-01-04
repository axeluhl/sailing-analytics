//
//  TimerViewController.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TimerViewController: UIViewController, UIAlertViewDelegate {
    enum AlertView: Int {
        case StopTracking
    }
    
    @IBOutlet weak var trackingTimeLabel: UILabel!
    
    let startDate = NSDate()
    let dateFormatter = NSDateFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // start tracking timer
        let timer = NSTimer(timeInterval: 0.1, target: self, selector: "timer:", userInfo: nil, repeats: true)
        NSRunLoop.currentRunLoop().addTimer(timer, forMode:NSRunLoopCommonModes)
        dateFormatter.dateFormat = "HH:mm:ss"
        dateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
        
    }
    // MARK:- Timer
    
    func timer(timer: NSTimer) {
        let currentDate = NSDate()
        let timeInterval = currentDate.timeIntervalSinceDate(startDate)
        let timerDate = NSDate(timeIntervalSince1970: timeInterval)
        trackingTimeLabel.text = dateFormatter.stringFromDate(timerDate)
    }

    // MARK:- Buttons
    
    /* Stop tracking, go back to regattas view */
    @IBAction func stopTrackingButtonTapped(sender: AnyObject) {
        let alertView = UIAlertView(title: NSLocalizedString("Stop tracking?", comment: ""), message: "", delegate: self, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""), otherButtonTitles: NSLocalizedString("Stop", comment: ""))
        alertView.tag = AlertView.StopTracking.rawValue;
        alertView.show()
    }
    
    /* Alert view delegate */
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Stop tracking?
        case AlertView.StopTracking.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break
            default:
                LocationManager.sharedManager.stopTracking()
                SendGPSFixController.sharedManager.checkIn = nil
                self.dismissViewControllerAnimated(true, completion: nil)
            }
            break
        default:
            break
        }
    }

}