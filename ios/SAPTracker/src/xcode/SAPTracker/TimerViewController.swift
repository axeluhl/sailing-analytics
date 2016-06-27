//
//  TimerViewController.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TimerViewController: UIViewController {

    private let startDate = NSDate()
    private let dateFormatter = NSDateFormatter()
    
    @IBOutlet weak var trackingTimeLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        self.setupDateFormatter()
        self.setupTimer()
    }
    
    // MARK: - Setups
    
    private func setupDateFormatter() {
        dateFormatter.dateFormat = "HH:mm:ss"
        dateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
    }
    
    private func setupTimer() {
        let timer = NSTimer(timeInterval: 0.1,
                            target: self,
                            selector: #selector(tick),
                            userInfo: nil,
                            repeats: true)
        NSRunLoop.currentRunLoop().addTimer(timer, forMode:NSRunLoopCommonModes)    
    }
    
    // MARK: - Timer
    
    @objc private func tick(timer: NSTimer) {
        let currentDate = NSDate()
        let timeInterval = currentDate.timeIntervalSinceDate(startDate)
        let timerDate = NSDate(timeIntervalSince1970: timeInterval)
        trackingTimeLabel.text = dateFormatter.stringFromDate(timerDate)
    }
    
}