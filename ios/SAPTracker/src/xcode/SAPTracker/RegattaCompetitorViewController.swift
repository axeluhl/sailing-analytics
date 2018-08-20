//
//  RegattaCompetitorViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class RegattaCompetitorViewController : CompetitorSessionViewController {
    
    @IBOutlet weak var regattaStartLabel: UILabel!
    @IBOutlet weak var countdownView: UIView!
    @IBOutlet weak var countdownViewHeight: NSLayoutConstraint!
    @IBOutlet weak var countdownDaysLabel: UILabel!
    @IBOutlet weak var countdownDaysTitleLabel: UILabel!
    @IBOutlet weak var countdownHoursLabel: UILabel!
    @IBOutlet weak var countdownHoursTitleLabel: UILabel!
    @IBOutlet weak var countdownMinutesLabel: UILabel!
    @IBOutlet weak var countdownMinutesTitleLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var eventButton: UIButton!
    @IBOutlet weak var announcementLabel: UILabel!
    
    weak var countdownTimer: Timer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        updateOptimistic()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        validateTimer()
        refresh(false)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        invalidateTimer()
    }
    
    // MARK: - Timer
    
    fileprivate func validateTimer() {
        tickTimer()
        countdownTimer = Timer.scheduledTimer(
            timeInterval: 1,
            target: self,
            selector: #selector(tickTimer),
            userInfo: nil,
            repeats: true
        )
    }
    
    fileprivate func invalidateTimer() {
        countdownTimer?.invalidate()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: eventButton)
        makeBlue(button: leaderboardButton)
        makeGreen(button: startTrackingButton)
    }
    
    fileprivate func setupLocalization() {
        announcementLabel.text = Translation.RegattaView.AnnouncementLabel.Text.String
        countdownDaysTitleLabel.text = Translation.RegattaView.CountdownDaysTitleLabel.Text.String
        countdownHoursTitleLabel.text = Translation.RegattaView.CountdownHoursTitleLabel.Text.String
        countdownMinutesTitleLabel.text = Translation.RegattaView.CountdownMinutesTitleLabel.Text.String
        eventButton.setTitle(Translation.RegattaView.EventButton.Title.String, for: .normal)
        leaderboardButton.setTitle(Translation.LeaderboardView.Title.String, for: .normal)
        startTrackingButton.setTitle(Translation.RegattaView.StartTrackingButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: competitorCheckIn.event.name, subtitle: competitorCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Timer
    
    @objc fileprivate func tickTimer() {
        guard competitorCheckIn != nil else { return }
        if competitorCheckIn.event.startDate - Date().timeIntervalSince1970 > 0 {
            regattaStartLabel.text = Translation.RegattaView.StartLabel.Text.BeforeRegattaDidStart.String
            let duration = competitorCheckIn.event.startDate - Date().timeIntervalSince1970
            let days = Int(duration / (60 * 60 * 24))
            let hours = Int(duration / (60 * 60)) - (days * 24)
            let minutes = Int(duration / 60) - (days * 24 * 60) - (hours * 60)
            countdownDaysLabel.text = String(format: "%02d", days)
            countdownHoursLabel.text = String(format: "%02d", hours)
            countdownMinutesLabel.text = String(format: "%02d", minutes)
            countdownView.isHidden = false
            countdownViewHeight.constant = 60
        } else {
            regattaStartLabel.text = Translation.RegattaView.StartLabel.Text.AfterRegattaDidStart.String
            countdownView.isHidden = true
            countdownViewHeight.constant = 0
        }
    }
    
}

// MARK: SessionViewControllerDelegate

extension RegattaCompetitorViewController: SessionViewControllerDelegate {

    var checkIn: CheckIn { get { return competitorCheckIn } }

    var checkOutActionTitle: String { get { return Translation.RegattaView.OptionSheet.CheckOutAction.Title.String } }

    var checkOutAlertMessage: String { get { return Translation.RegattaView.CheckOutAlert.Message.String } }

    var coreDataManager: CoreDataManager { get { return competitorCoreDataManager } }
    
    var sessionController: SessionController { get { return competitorSessionController } }
    
    func makeOptionSheet() -> UIAlertController {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = self.optionButton
        }
        alertController.addAction(self.makeActionCheckOut())
        alertController.addAction(self.makeActionSettings())
        alertController.addAction(self.makeActionInfo())
        alertController.addAction(self.makeActionCancel())
        return alertController
    }
    
    func refresh(_ animated: Bool) {
        competitorViewController?.refresh(animated)
    }
    
}
