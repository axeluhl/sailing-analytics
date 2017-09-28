//
//  RegattaCheckInTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaCheckInTableViewController: CheckInTableViewController {
    
    fileprivate struct Segue {
        static let Scan = "Scan"
    }
    
    @IBOutlet weak var scanCodeButton: UIButton!
    @IBOutlet weak var noCodeButton: UIButton!
    @IBOutlet weak var infoCodeLabel: UILabel!
    
    override func viewDidLoad() {
        delegate = self
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        review()
        subscribeForNewCheckInURLNotifications()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        unsubscribeFromNewCheckInURLNotifications()
        super.viewWillDisappear(animated)
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
    }
    
    fileprivate func setupButtons() {
        makeBlue(button: scanCodeButton)
        makeGray(button: noCodeButton)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = Translation.RegattaCheckInListView.Title.String
        headerTitleLabel.text = Translation.RegattaCheckInListView.HeaderTitleLabel.Text.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, for: .normal)
        noCodeButton.setTitle(Translation.RegattaCheckInListView.NoCodeAlert.Title.String, for: .normal)
        infoCodeLabel.text = Translation.RegattaCheckInListView.InfoCodeLabel.Text.String
        footerTextView.text = Translation.RegattaCheckInListView.FooterTextView.Text.String
    }
    
    // MARK: - Review
    
    fileprivate func review() {
        reviewNewCheckIn(completion: { [weak self] (checkIn) in
            logInfo(name: "\(#function)", info: "Review new check-in done.")
            self?.performSegue(forCheckIn: checkIn)
        })
    }
    
    fileprivate func reviewNewCheckIn(completion: @escaping (_ checkIn: CheckIn?) -> Void) {
        guard let urlString = Preferences.newCheckInURL else { completion(nil); return }
        guard let checkInData = CheckInData(urlString: urlString) else { completion(nil); return }
        regattaCheckInController.checkInWithViewController(self, checkInData: checkInData, success: { (checkIn) in
            Preferences.newCheckInURL = nil
            completion(checkIn)
        }) { (error) in
            Preferences.newCheckInURL = nil // TODO: Ask user for retry, retry later or dismiss before deleting check-in url
            completion(nil)
        }
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNewCheckInURLNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(RegattaCheckInTableViewController.newCheckInURLNotification(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.NewCheckInURLChanged),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNewCheckInURLNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func newCheckInURLNotification(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.reviewNewCheckIn(completion: { checkIn in
                logInfo(name: "\(#function)", info: "Review new check-in done.")
                self.performSegue(forCheckIn: checkIn)
            })
        })
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: Any) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.presentSettingsViewController()
        }
        let infoAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.presentAboutViewController()
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(infoAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    @IBAction func scanButtonTapped(_ sender: Any) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera) {
            performSegue(withIdentifier: Segue.Scan, sender: sender)
        } else {
            showNoCameraAlert()
        }
    }
    
    @IBAction func noCodeButtonTapped(_ sender: Any) {
        showNoCodeAlert()
    }
    
    // MARK: - Alerts
    
    fileprivate func showNoCameraAlert() {
        let alertController = UIAlertController(
            title: Translation.Common.Error.String,
            message: Translation.RegattaCheckInListView.NoCameraAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func showNoCodeAlert() {
        let alertController = UIAlertController(
            title: Translation.RegattaCheckInListView.NoCodeAlert.Title.String,
            message: Translation.RegattaCheckInListView.NoCodeAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == Segue.Scan) {
            guard let scanVC = segue.destination as? ScanViewController else { return }
            scanVC.coreDataManager = coreDataManager
            scanVC.delegate = self
        }
    }
    
    // MARK: - Properties
    
    lazy var regattaCheckInController: RegattaCheckInController = {
        return RegattaCheckInController(coreDataManager: self.regattaCoreDataManager)
    }()
    
    lazy var regattaCoreDataManager: RegattaCoreDataManager = {
        return RegattaCoreDataManager.shared
    }()
    
}

// MARK: - CheckInTableViewControllerDelegate

extension RegattaCheckInTableViewController: CheckInTableViewControllerDelegate {
    
    var checkInController: CheckInController { get { return regattaCheckInController } }
    
    var coreDataManager: CoreDataManager { get { return regattaCoreDataManager } }

    var isFooterViewHidden: Bool { get { return false } }

    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn) {
        guard let regattaCheckInTableViewCell = cell as? RegattaCheckInTableViewCell else { return }
        regattaCheckInTableViewCell.eventLabel.text = checkIn.event.name
        regattaCheckInTableViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        regattaCheckInTableViewCell.competitorLabel.text = checkIn.name
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andCompetitorCheckIn competitorCheckIn: CompetitorCheckIn) {
        guard let regattaCompetitorVC = segue.destination as? RegattaCompetitorViewController else { return }
        regattaCompetitorVC.competitorCheckIn = competitorCheckIn
        regattaCompetitorVC.competitorCoreDataManager = coreDataManager
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andMarkCheckIn markCheckIn: MarkCheckIn) {
        guard let regattaMarkVC = segue.destination as? RegattaMarkViewController else { return }
        regattaMarkVC.markCheckIn = markCheckIn
        regattaMarkVC.markCoreDataManager = coreDataManager
    }
    
}

// MARK: - ScanViewControllerDelegate

extension RegattaCheckInTableViewController: ScanViewControllerDelegate {
    
    func scanViewController(_ controller: ScanViewController, didCheckIn checkIn: CheckIn) {
        performSegue(forCheckIn: checkIn)
    }
    
}
