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
        static let About = "About"
        static let Scan = "Scan"
        static let Settings = "Settings"
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
        scanCodeButton.setBackgroundImage(Images.BlueHighlighted, for: .highlighted)
        noCodeButton.setBackgroundImage(Images.GrayHighlighted, for: .highlighted)
    }
    
    fileprivate func setupLocalization() {
        navigationItem.title = "REGATTAS TITLE"
        headerTitleLabel.text = Translation.RegattaCheckInListView.HeaderTitleLabel.Text.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, for: .normal)
        noCodeButton.setTitle(Translation.RegattaCheckInListView.NoCodeAlert.Title.String, for: .normal)
        infoCodeLabel.text = Translation.RegattaCheckInListView.InfoCodeLabel.Text.String
        footerTextView.text = Translation.RegattaCheckInListView.FooterTextView.Text.String
    }
    
    // MARK: - Review
    
    fileprivate func review() {
        self.reviewTerms(completion: {
            logInfo(name: "\(#function)", info: "Review terms done.")
            self.reviewCodeConvention(completion: {
                logInfo(name: "\(#function)", info: "Review code convention done.")
                self.reviewGPSFixes(completion: {
                    logInfo(name: "\(#function)", info: "Review GPS fixes done.")
                    self.reviewNewCheckIn(completion: { checkIn in
                        logInfo(name: "\(#function)", info: "Review new check-in done.")
                        self.performSegue(forCheckIn: checkIn)
                    })
                })
            })
        })
    }
    
    // MARK: 1. Review Terms
    
    fileprivate func reviewTerms(completion: @escaping () -> Void) {
        guard Preferences.termsAccepted == false else { completion(); return }
        let alertController = UIAlertController(
            title: Translation.RegattaCheckInListView.TermsAlert.Title.String,
            message: Translation.RegattaCheckInListView.TermsAlert.Message.String,
            preferredStyle: .alert
        )
        let showTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.ShowTermsAction.Title.String, style: .default) { [weak self] action in
            UIApplication.shared.openURL(URLs.Terms)
            self?.reviewTerms(completion: completion) // Review terms until user accepted terms
        }
        let acceptTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.AcceptTermsAction.Title.String, style: .default) { action in
            Preferences.termsAccepted = true
            completion() // Terms accepted
        }
        alertController.addAction(showTermsAction)
        alertController.addAction(acceptTermsAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: 2. Review Code Convention
    
    fileprivate func reviewCodeConvention(completion: @escaping () -> Void) {
        #if DEBUG
            guard Preferences.codeConventionRead == false else { completion(); return }
            let alertController = UIAlertController(
                title: "Code Convention",
                message: "Please try to respect the code convention which is used for this project.",
                preferredStyle: .alert
            )
            let showCodeConventionAction = UIAlertAction(title: "Code Convention", style: .default) { [weak self] action in
                UIApplication.shared.openURL(URLs.CodeConvention)
                self?.reviewCodeConvention(completion: completion)
            }
            let okAction = UIAlertAction(title: "OK", style: .default) { action in
                Preferences.codeConventionRead = true
                completion()
            }
            alertController.addAction(showCodeConventionAction)
            alertController.addAction(okAction)
            present(alertController, animated: true, completion: nil)
        #else
            completion()
        #endif
    }
    
    // MARK: 3. Review GPS Fixes
    
    fileprivate func reviewGPSFixes(completion: @escaping () -> Void) {
        SVProgressHUD.show()
        fetchedResultsController.delegate = nil
        let checkIns = RegattaCoreDataManager.shared.fetchCheckIns() ?? []
        reviewGPSFixes(checkIns: checkIns) {
            self.reviewGPSFixesCompleted(completion: completion)
        }
    }
    
    fileprivate func reviewGPSFixes(checkIns: [CheckIn], completion: @escaping () -> Void) {
        guard checkIns.count > 0 else { completion(); return }
        let gpsFixController = GPSFixController.init(checkIn: checkIns[0], coreDataManager: RegattaCoreDataManager.shared)
        gpsFixController.sendAll(completion: { (withSuccess) in
            self.reviewGPSFixes(checkIns: Array(checkIns[1..<checkIns.count]), completion: completion)
        })
    }
    
    fileprivate func reviewGPSFixesCompleted(completion: () -> Void) {
        SVProgressHUD.popActivity()
        fetchedResultsController.delegate = self
        completion()
    }
    
    // MARK: 4. Review New Check-In
    
    fileprivate func reviewNewCheckIn(completion: @escaping (_ checkIn: CheckIn?) -> Void) {
        guard let urlString = Preferences.newCheckInURL else { completion(nil); return }
        guard let checkInData = CheckInData(urlString: urlString) else { completion(nil); return }
        checkInController.checkInWithViewController(self, checkInData: checkInData, success: { checkIn in
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
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.Settings, sender: alertController)
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { [weak self] action in
            self?.performSegue(withIdentifier: Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    @IBAction func scanButtonTapped(_ sender: AnyObject) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera) {
            performSegue(withIdentifier: Segue.Scan, sender: sender)
        } else {
            showNoCameraAlert()
        }
    }
    
    @IBAction func noCodeButtonTapped(_ sender: AnyObject) {
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
    
    lazy var regattaCheckInController: CheckInController = {
        let checkInController = CheckInController(coreDataManager: self.regattaCoreDataManager)
        return checkInController
    }()
    
    lazy var regattaCoreDataManager: RegattaCoreDataManager = {
        return RegattaCoreDataManager.shared
    }()
    
}

// MARK: - CheckInTableViewControllerDelegate

extension RegattaCheckInTableViewController: CheckInTableViewControllerDelegate {
    
    var checkInController: CheckInController {
        get {
            return regattaCheckInController
        }
    }
    
    var coreDataManager: CoreDataManager {
        get {
            return regattaCoreDataManager
        }
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn) {
        guard let regattaCheckInTableViewCell = cell as? RegattaCheckInTableViewCell else { return }
        regattaCheckInTableViewCell.eventLabel.text = checkIn.event.name
        regattaCheckInTableViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        regattaCheckInTableViewCell.competitorLabel.text = checkIn.name
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andCompetitorCheckIn checkIn: CompetitorCheckIn) {
        guard let competitorVC = segue.destination as? CompetitorViewController else { return }
        guard let competitorCheckIn = segueCheckIn as? CompetitorCheckIn else { return }
        competitorVC.competitorCheckIn = competitorCheckIn
        competitorVC.coreDataManager = coreDataManager
    }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, prepareForSegue segue: UIStoryboardSegue, andMarkCheckIn checkIn: MarkCheckIn) {
        guard let markVC = segue.destination as? MarkViewController else { return }
        guard let markCheckIn = segueCheckIn as? MarkCheckIn else { return }
        markVC.markCheckIn = markCheckIn
        markVC.coreDataManager = coreDataManager
    }
    
}

// MARK: - ScanViewControllerDelegate

extension RegattaCheckInTableViewController: ScanViewControllerDelegate {
    
    func scanViewController(_ controller: ScanViewController, didCheckIn checkIn: CheckIn) {
        performSegue(forCheckIn: checkIn)
    }
    
}
