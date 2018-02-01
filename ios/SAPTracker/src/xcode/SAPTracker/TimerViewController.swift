//
//  TimerViewController.swift
//  SAPTracker
//
//  Created by computing on 11/12/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class TimerViewController: UIViewController {

    fileprivate let startDate = Date()
    
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var timeLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocalization()
        setupTimer()
    }
    
    fileprivate func setupLocalization() {
        titleLabel.text = Translation.TimerView.TitleLabel.Text.String
    }
    
    fileprivate func setupTimer() {
        let timer = Timer(
            timeInterval: 0.1,
            target: self,
            selector: #selector(tick),
            userInfo: nil,
            repeats: true
        )
        RunLoop.current.add(timer, forMode: RunLoopMode.commonModes)
    }
    
    // MARK: - Timer
    
    @objc fileprivate func tick(_ timer: Timer) {
        let currentDate = Date()
        let timeInterval = currentDate.timeIntervalSince(startDate)
        let timerDate = Date(timeIntervalSince1970: timeInterval)
        timeLabel.text = dateFormatter.string(from: timerDate)
    }
    
    // MARK: - Properties
    
    fileprivate lazy var dateFormatter: DateFormatter = {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "HH:mm:ss"
        dateFormatter.timeZone = TimeZone(secondsFromGMT: 0)
        return dateFormatter
    }()
    
}
