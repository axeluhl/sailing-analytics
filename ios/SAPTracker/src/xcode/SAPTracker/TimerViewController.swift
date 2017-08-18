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
    
    weak var timer: Timer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        validateTimer()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        invalidateTimer()
    }
    
    // MARK: - Timer
    
    fileprivate func validateTimer() {
        timer = Timer.scheduledTimer(
            timeInterval: 0.1,
            target: self,
            selector: #selector(tickTimer),
            userInfo: nil,
            repeats: true
        )
        tickTimer()
    }

    fileprivate func invalidateTimer() {
        timer?.invalidate()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocalization()
    }
    
    fileprivate func setupLocalization() {
        titleLabel.text = Translation.TimerView.TitleLabel.Text.String
    }
    
    // MARK: - Timer
    
    @objc fileprivate func tickTimer() {
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
