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
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var timeLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupLocalization()
        setupTimer()
    }
    
    private func setupLocalization() {
        titleLabel.text = Translation.TimerView.TitleLabel.Text.String
    }
    
    private func setupTimer() {
        let timer = NSTimer(timeInterval: 0.1,
                            target: self,
                            selector: #selector(tick),
                            userInfo: nil,
                            repeats: true
        )
        NSRunLoop.currentRunLoop().addTimer(timer, forMode:NSRunLoopCommonModes)    
    }
    
    // MARK: - Timer
    
    @objc private func tick(timer: NSTimer) {
        let currentDate = NSDate()
        let timeInterval = currentDate.timeIntervalSinceDate(startDate)
        let timerDate = NSDate(timeIntervalSince1970: timeInterval)
        timeLabel.text = dateFormatter.stringFromDate(timerDate)
    }
    
    // MARK: - Properties
    
    private lazy var dateFormatter: NSDateFormatter = {
        let dateFormatter = NSDateFormatter()
        dateFormatter.dateFormat = "HH:mm:ss"
        dateFormatter.timeZone = NSTimeZone(forSecondsFromGMT: 0)
        return dateFormatter
    }()
    
}