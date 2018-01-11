//
//  TrainingViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

protocol TrainingViewControllerDelegate: class {

    func trainingViewController(_ controller: TrainingViewController, refreshButtonTapped sender: Any)

    func trainingViewController(_ controller: TrainingViewController, startTrackingButtonTapped sender: Any)
    
    func trainingViewController(_ controller: TrainingViewController, leaderboardButtonTapped sender: Any)
    
    func trainingViewControllerDidFinishTraining(_ controller: TrainingViewController)
    
    func trainingViewControllerDidReactivateTraining(_ controller: TrainingViewController)
    
}

class TrainingViewController: UIViewController {
    
    weak var delegate: TrainingViewControllerDelegate?
    
    weak var trainingCheckIn: CheckIn!
    weak var trainingCoreDataManager: CoreDataManager!

    @IBOutlet var finishButtonZeroWidth: NSLayoutConstraint! // Strong reference needed to avoid deallocation when constraint is not active

    @IBOutlet weak var finishButton: UIButton!
    @IBOutlet weak var trainingNameLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var spaceBetweenLeaderboardButtonAndFinishButton: NSLayoutConstraint!
    @IBOutlet weak var startTrackingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        login()
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
        makeGray(button: finishButton)
        makeGray(button: leaderboardButton)
        makeGreen(button: startTrackingButton)
    }
    
    fileprivate func setupLocalization() {
        leaderboardButton.setTitle(Translation.TrainingView.LeaderboardButton.Title.String, for: .normal)
        startTrackingButton.setTitle(isTrainingActive ? Translation.TrainingView.StartTrackingButton.Title.StringWhenTrainingIsActive : Translation.TrainingView.StartTrackingButton.Title.StringWhenTrainingIsInActive, for: .normal)
        finishButton.setTitle(Translation.TrainingView.FinishButton.Title.String, for: .normal)
    }

    // MARK: - Login

    fileprivate func login() {
        SVProgressHUD.show()
        signUpController.login(success: { (userName) in
            SVProgressHUD.dismiss()
        }) { (error, message) in
            SVProgressHUD.dismiss()
        }
    }

    // MARK: - Refresh
    
    func refresh(_ animated: Bool) {
        refreshFinishTrainingButton(animated)
        refreshTrainingNameLabel(animated)
        refreshStartTrainingButton(animated)
    }
    
    fileprivate func refreshFinishTrainingButton(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshFinishTrainingButton() }
        } else {
            refreshFinishTrainingButton()
        }
    }
    
    fileprivate func refreshFinishTrainingButton() {
        if (isTrainingActive) {
            spaceBetweenLeaderboardButtonAndFinishButton.constant = 8
            finishButtonZeroWidth.isActive = false
            finishButton.alpha = 1
        } else {
            spaceBetweenLeaderboardButtonAndFinishButton.constant = 0
            finishButtonZeroWidth.isActive = true
            finishButton.alpha = 0
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
            startTrackingButton.setTitle(Translation.TrainingView.StartTrackingButton.Title.StringWhenTrainingIsActive, for: .normal)
            makeGreen(button: startTrackingButton)
        } else {
            startTrackingButton.setTitle(Translation.TrainingView.StartTrackingButton.Title.StringWhenTrainingIsInActive, for: .normal)
            makeGray(button: startTrackingButton)
        }
    }
    
    // MARK: - StartTracking
    
    fileprivate func startTracking(
        success: @escaping () -> Void,
        failure: @escaping (_ error: Error) -> Void)
    {
        SVProgressHUD.show()
        self.stopActiveRace(completion: {
            self.startNewRace(success: {
                SVProgressHUD.dismiss()
                success()
            }) { (error) in
                SVProgressHUD.dismiss()
                failure(error)
            }
        })
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
            self?.finishTrainingSuccess()
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.handle(error: error)
        }
    }

    fileprivate func finishTrainingSuccess() {
        let alertController = UIAlertController.init(title: Translation.TrainingView.FinishedAlert.Title.String, message: nil, preferredStyle: .alert)
        let okAction = UIAlertAction.init(title: Translation.Common.OK.String, style: .default) { [weak self] (action) in
            if let strongSelf = self {
                strongSelf.delegate?.trainingViewControllerDidFinishTraining(strongSelf)
            }
        }
        alertController.addAction(okAction)
        present(alertController, animated: true)
    }

    fileprivate func reactivateTraining() {
        SVProgressHUD.show()
        trainingController.reactivateTraining(forCheckIn: trainingCheckIn, success: { [weak self] in
            SVProgressHUD.dismiss()
            self?.reactivateTrainingSuccess()
        }) { [weak self] (error) in
            SVProgressHUD.dismiss()
            self?.handle(error: error)
        }
    }

    fileprivate func reactivateTrainingSuccess() {
        let alertController = UIAlertController.init(title: Translation.TrainingView.ReactivatedAlert.Title.String, message: nil, preferredStyle: .alert)
        let okAction = UIAlertAction.init(title: Translation.Common.OK.String, style: .default) { [weak self] (action) in
            if let strongSelf = self {
                strongSelf.delegate?.trainingViewControllerDidReactivateTraining(strongSelf)
            }
        }
        alertController.addAction(okAction)
        present(alertController, animated: true)
    }

    // MARK: - Actions

    @IBAction func refreshButtonTapped(_ sender: Any) {
        delegate?.trainingViewController(self, refreshButtonTapped: sender)
    }

    @IBAction func finishTrainingButtonTapped(_ sender: Any) {
        showFinishAlert()
    }
    
    @IBAction func leaderboardButtonTapped(_ sender: Any) {
        delegate?.trainingViewController(self, leaderboardButtonTapped: sender)
    }

    @IBAction func startTrackingButtonTapped(_ sender: Any) {
        if isTrainingActive {
            startTracking(success: { [weak self] in
                if let strongSelf = self {
                    strongSelf.delegate?.trainingViewController(strongSelf, startTrackingButtonTapped: sender)
                }
            }) { [weak self] (error) in
                self?.handle(error: error)
            }
        } else {
            showReactivateAlert()
        }
    }
    
    // MARK: - Alerts

    fileprivate func showFinishAlert() {
        let alertController = UIAlertController(
            title: Translation.TrainingView.FinishAlert.Title.String,
            message: Translation.TrainingView.FinishAlert.Message.String,
            preferredStyle: .alert
        )
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .default) { [weak self] action in
            self?.finishTraining()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        present(alertController, animated: true, completion: nil)
    }

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

    fileprivate lazy var signUpController: SignUpController = {
        return SignUpController(baseURLString: self.trainingController.baseURLString)
    }()

    fileprivate lazy var trainingController: TrainingController = {
        return TrainingController(coreDataManager: self.trainingCoreDataManager, baseURLString: self.trainingCheckIn.serverURL)
    }()

    // MARK: - Helper

    fileprivate func handle(error: Error) {
        if ErrorHelper.isResponseUnauthorized(error: error as NSError) {
            self.signUpController.loginWithViewController(self)
        } else {
            self.showAlert(forError: error)
        }
    }

}
