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
    
    func trainingViewControllerDidStopTraining(_ controller: TrainingViewController)
    
    func trainingViewControllerDidReactivateTraining(_ controller: TrainingViewController)
    
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
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        stopActiveRace()
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
    
    // MARK: - StartTracking
    
    fileprivate func startTracking(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        SVProgressHUD.show()
        self.stopActiveRace {
            self.startNewRace(success: {
                SVProgressHUD.dismiss()
                success()
            }) { (error) in
                SVProgressHUD.dismiss()
                failure(error)
            }
        }
    }
    
    fileprivate func stopActiveRace(completion: (() -> Void)? = nil) {
        if let trainingRaceData = Preferences.activeTrainingRaceData {
            trainingController.stopActiveRace(forTrainingRaceData: trainingRaceData, success: {
                Preferences.activeTrainingRaceData = nil
                completion?()
            }) { (error) in
                completion?()
            }
        } else {
            completion?()
        }
    }
    
    fileprivate func startNewRace(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        trainingController.startNewRace(forCheckIn: self.trainingCheckIn, success: { (trainingRaceData) in
            Preferences.activeTrainingRaceData = trainingRaceData
            success()
        }) { (error) in
            failure(error)
        }
    }
    
    fileprivate func finishTraining() {
        SVProgressHUD.show()
        trainingController.finishTraining(forCheckIn: trainingCheckIn, success: { [weak self] in
            SVProgressHUD.dismiss()
            if let strongSelf = self {
                strongSelf.delegate?.trainingViewControllerDidStopTraining(strongSelf)
            }
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.showAlert(forError: error)
        }
    }
    
    fileprivate func reactivateTraining() {
        SVProgressHUD.show()
        trainingController.reactivateTraining(forCheckIn: trainingCheckIn, success: { [weak self] in
            SVProgressHUD.dismiss()
            if let strongSelf = self {
                strongSelf.delegate?.trainingViewControllerDidReactivateTraining(strongSelf)
            }
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.showAlert(forError: error)
        }
    }
    
    // MARK: - Actions
    
    @IBAction func stopTrainingButtonTapped(_ sender: Any) {
        finishTraining()
    }
    
    @IBAction func leaderboardButtonTapped(_ sender: Any) {
        delegate?.trainingViewController(self, leaderboardButtonTapped: sender)
    }
    
    @IBAction func startTrackingButtonTapped(_ sender: Any) {
        if isTrainingActive {
            startTracking(success: {
                self.delegate?.trainingViewController(self, startTrackingButtonTapped: sender)
            }) { (error) in
                self.showAlert(forError: error)
            }
        } else {
            showReactivateAlert()
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
            self?.reactivateTraining()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Properties
    
    fileprivate var isTrainingInactive: Bool {
        get {
            return !isTrainingActive
        }
    }
    
    fileprivate var isTrainingActive: Bool {
        get {
            print("\(trainingCheckIn.event.endDate) - \(Date().timeIntervalSince1970)")
            return trainingCheckIn.event.endDate - Date().timeIntervalSince1970 > 0
        }
    }
    
    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: self.trainingCheckIn.serverURL)
    }()
    
}
