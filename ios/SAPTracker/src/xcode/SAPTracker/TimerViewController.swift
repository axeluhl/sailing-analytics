//
//  TimerViewController.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TimerViewController: UIViewController {

    @IBOutlet weak var trackingTimeLabel: UILabel!
    
    let startDate = NSDate()
    let dateFormatter = NSDateFormatter()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // start tracking timer
        let timer = NSTimer(timeInterval: 0.1, target: self, selector: #selector(TimerViewController.timer(_:)), userInfo: nil, repeats: true)
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
}