//
//  TrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol TrainingViewControllerDelegate: class {
    
    func trainingViewController(_ controller: TrainingViewController, startTrackingButtonTapped sender: Any)
    
    func trainingViewController(_ controller: TrainingViewController, leaderboardButtonTapped sender: Any)
    
}

class TrainingViewController: UIViewController {
    
    weak var delegate: TrainingViewControllerDelegate?
    
    weak var trainingCheckIn: CheckIn!
    weak var trainingCoreDataManager: CoreDataManager!
    
    @IBOutlet var stopTrainingButtonZeroHeight: NSLayoutConstraint! // Strong reference needed to avoid deallocation when constraint is not active
    
    @IBOutlet weak var stopTrainingButton: UIButton!
    @IBOutlet weak var trainingNameLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var startTrackingButton: UIButton!
    
    var isActive = false
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
    }
    
    fileprivate func setupButtons() {
        makeRed(button: stopTrainingButton)
        makeBlue(button: leaderboardButton)
        makeGreen(button: startTrackingButton)
    }
    
    fileprivate func setupLocalization() {
        leaderboardButton.setTitle(Translation.TrainingView.LeaderboardButton.Title.String, for: .normal)
        startTrackingButton.setTitle(Translation.TrainingView.StartTrackingButton.Title.String, for: .normal)
        stopTrainingButton.setTitle(Translation.TrainingView.StopTrainingButton.Title.String, for: .normal)
    }
    
    // MARK: - Refresh
    
    func refresh(_ animated: Bool) {
        refreshStopTrainingButton(animated)
        refreshTrainingNameLabel(animated)
        refreshStartTrainingButton(animated)
    }
    
    fileprivate func refreshStopTrainingButton(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshStopTrainingButton() }
        } else {
            refreshStopTrainingButton()
        }
    }
    
    fileprivate func refreshStopTrainingButton() {
        if (isTrainingActive) {
            stopTrainingButtonZeroHeight.isActive = false
            stopTrainingButton.alpha = 1
        } else {
            stopTrainingButtonZeroHeight.isActive = true
            stopTrainingButton.alpha = 0
        }
        view.layoutIfNeeded()
    }
    
    fileprivate func refreshTrainingNameLabel(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshTrainingNameLabel() }
        } else {
            refreshTrainingNameLabel()
        }
    }
    
    fileprivate func refreshTrainingNameLabel() {
        trainingNameLabel.text = trainingCheckIn.event.name
    }
    
    fileprivate func refreshStartTrainingButton(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshStartTrainingButton() }
        } else {
            refreshStartTrainingButton()
        }
    }
    
    fileprivate func refreshStartTrainingButton() {
        if (isTrainingActive) {
            makeGreen(button: startTrackingButton)
        } else {
            makeGray(button: startTrackingButton)
        }
    }
    
    // MARK: - Actions
    
    @IBAction func stopTrainingButtonTapped(_ sender: Any) {
        isActive = false
        refresh(true)
    }
    
    @IBAction func leaderboardButtonTapped(_ sender: Any) {
        delegate?.trainingViewController(self, leaderboardButtonTapped: sender)
    }
    
    @IBAction func startTrackingButtonTapped(_ sender: Any) {
        guard isTrainingActive else {
            showReactivateAlert()
            return
        }
        
        SVProgressHUD.show()
        self.trainingController.stopActiveRace(success: {
            self.trainingController.startNewRace(forCheckIn: self.trainingCheckIn, success: {
                SVProgressHUD.dismiss()
                self.delegate?.trainingViewController(self, startTrackingButtonTapped: sender)
            }) { [weak self] (error) in
                SVProgressHUD.dismiss()
                self?.showAlert(forError: error)
            }
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.showAlert(forError: error)
        }
    }
    
    // MARK: - Alerts
    
    fileprivate func showReactivateAlert() {
        let alertController = UIAlertController(
            title: Translation.TrainingView.ReactivateAlert.Title.String,
            message: Translation.TrainingView.ReactivateAlert.Message.String,
            preferredStyle: .alert
        )
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .default) { [weak self] action in
            self?.performReactivation()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func performReactivation() {
        isActive = true
        refresh(true)
    }
    
    // MARK: - Properties
    
    fileprivate var isTrainingInactive: Bool {
        get {
            return !isTrainingActive
        }
    }
    
    fileprivate var isTrainingActive: Bool {
        get {
            return isActive // trainingCheckIn.event.endDate - Date().timeIntervalSince1970 < 0
        }
    }
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: self.trainingCheckIn.serverURL)
    }()
    
}
