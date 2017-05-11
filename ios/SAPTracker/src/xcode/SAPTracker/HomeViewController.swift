//
//  ViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class HomeViewController: UIViewController {
    
    fileprivate struct Segue {
        static let About = "About"
        static let Competitor = "Competitor"
        static let Mark = "Mark"
        static let Scan = "Scan"
        static let Settings = "Settings"
    }
    
    var selectedCheckIn: CheckIn?
    
    @IBOutlet var headerView: UIView! // Strong reference needed to avoid deallocation when not attached to table view
    
    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var scanCodeButton: UIButton!
    @IBOutlet weak var noCodeButton: UIButton!
    @IBOutlet weak var infoCodeLabel: UILabel!
    @IBOutlet weak var footerView: UIView!
    @IBOutlet weak var footerTextView: UITextView!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        review()
        subscribeForNewCheckInURLNotifications()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        unsubscribeFromNewCheckInURLNotifications()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
        setupTableView()
        setupTableViewDataSource()
        setupTableViewHeader()
    }
    
    fileprivate func setupButtons() {
        scanCodeButton.setBackgroundImage(Images.BlueHighlighted, for: .highlighted)
        noCodeButton.setBackgroundImage(Images.GrayHighlighted, for: .highlighted)
    }
    
    fileprivate func setupLocalization() {
        headerTitleLabel.text = Translation.HomeView.HeaderTitleLabel.Text.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, for: .normal)
        noCodeButton.setTitle(Translation.HomeView.NoCodeAlert.Title.String, for: .normal)
        infoCodeLabel.text = Translation.HomeView.InfoCodeLabel.Text.String
        footerTextView.text = Translation.HomeView.FooterTextView.Text.String
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.title = Application.Title
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    fileprivate func setupTableViewDataSource() {
        do {
            try fetchedResultsController.performFetch()
        } catch {
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func setupTableViewHeader() {
        if fetchedResultsController.sections?[0].numberOfObjects ?? 0 == 0 {
            tableView.tableHeaderView = nil
        } else {
            tableView.tableHeaderView = headerView
        }
    }
    
    // MARK: - Layout
    
    fileprivate func layout() {
        self.layoutFooterView()
    }
    
    fileprivate func layoutFooterView() {
        let height = footerTextView.sizeThatFits(CGSize(width: footerView.frame.width, height: CGFloat.greatestFiniteMagnitude)).height
        footerView.frame = CGRect(
            x: footerView.frame.origin.x,
            y: footerView.frame.origin.y,
            width: footerView.frame.width,
            height: height
        )
    }
    
    // MARK: - Review
    
    fileprivate func review() {
        self.reviewTerms(completion: {
            logInfo(name: "\(#function)", info: "Review terms done.")
            self.reviewCodeConvention(completion: {
                logInfo(name: "\(#function)", info: "Review code convention done.")
                self.reviewGPSFixes(completion: {
                    logInfo(name: "\(#function)", info: "Review GPS fixes done.")
                    self.reviewNewCheckIn(completion: {
                        logInfo(name: "\(#function)", info: "Review new check-in done.")
                        self.reviewSelectedCheckIn(completion: {
                            logInfo(name: "\(#function)", info: "Review selected check-in done.")
                        })
                    })
                })
            })
        })
    }
    
    // MARK: 1. Review Terms
    
    fileprivate func reviewTerms(completion: @escaping () -> Void) {
        guard Preferences.termsAccepted == false else { completion(); return }
        let alertController = UIAlertController(
            title: Translation.HomeView.TermsAlert.Title.String,
            message: Translation.HomeView.TermsAlert.Message.String,
            preferredStyle: .alert
        )
        let showTermsAction = UIAlertAction(title: Translation.HomeView.TermsAlert.ShowTermsAction.Title.String, style: .default) { action in
            UIApplication.shared.openURL(URLs.Terms)
            self.reviewTerms(completion: completion) // Review terms until user accepted terms
        }
        let acceptTermsAction = UIAlertAction(title: Translation.HomeView.TermsAlert.AcceptTermsAction.Title.String, style: .default) { action in
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
            let showCodeConventionAction = UIAlertAction(title: "Code Convention", style: .default) { action in
                UIApplication.shared.openURL(URLs.CodeConvention)
                self.reviewCodeConvention(completion: completion)
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
        let checkIns = CoreDataManager.sharedManager.fetchCheckIns() ?? []
        reviewGPSFixes(checkIns: checkIns) {
            self.reviewGPSFixesCompleted(completion: completion)
        }
    }
    
    fileprivate func reviewGPSFixes(checkIns: [CheckIn], completion: @escaping () -> Void) {
        guard checkIns.count > 0 else { completion(); return }
        let gpsFixController = GPSFixController.init(checkIn: checkIns[0])
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
    
    fileprivate func reviewNewCheckIn(completion: @escaping () -> Void) {
        guard let urlString = Preferences.newCheckInURL else { completion(); return }
        guard let checkInData = CheckInData(urlString: urlString) else { completion(); return }
        checkInController.checkIn(checkInData: checkInData, completion: { (withSuccess) in
            Preferences.newCheckInURL = nil
            self.selectedCheckIn = CoreDataManager.sharedManager.fetchCheckIn(checkInData: checkInData)
            completion()
        })
    }
    
    // MARK: 5. Review Selected Check-In
    
    fileprivate func reviewSelectedCheckIn(completion: () -> Void) {
        if shouldPerformCompetitorSegue() {
            performSegue(withIdentifier: Segue.Competitor, sender: self)
        } else if shouldPerformMarkSegue() {
            performSegue(withIdentifier: Segue.Mark, sender: self)
        }
        completion()
    }
    
    // MARK: - Notifications
    
    fileprivate func subscribeForNewCheckInURLNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(HomeViewController.newCheckInURLNotification(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.NewCheckInURLChanged),
            object: nil
        )
    }
    
    fileprivate func unsubscribeFromNewCheckInURLNotifications() {
        NotificationCenter.default.removeObserver(self)
    }
    
    @objc fileprivate func newCheckInURLNotification(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.reviewNewCheckIn(completion: {
                logInfo(name: "\(#function)", info: "Review new check-in done.")
                self.reviewSelectedCheckIn(completion: {
                    logInfo(name: "\(#function)", info: "Review selected check-in done.")
                })
            })
        })
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { (action) -> Void in
            self.performSegue(withIdentifier: Segue.Settings, sender: alertController)
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { (action) -> Void in
            self.performSegue(withIdentifier: Segue.About, sender: alertController)
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
            message: Translation.HomeView.NoCameraAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    fileprivate func showNoCodeAlert() {
        let alertController = UIAlertController(
            title: Translation.HomeView.NoCodeAlert.Title.String,
            message: Translation.HomeView.NoCodeAlert.Message.String,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Segues
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if (identifier == Segue.Competitor) {
            return shouldPerformCompetitorSegue()
        } else if (identifier == Segue.Mark) {
            return shouldPerformMarkSegue()
        } else {
            return true
        }
    }
    
    fileprivate func shouldPerformCompetitorSegue() -> Bool {
        return selectedCheckIn != nil && selectedCheckIn is CompetitorCheckIn
    }

    fileprivate func shouldPerformMarkSegue() -> Bool {
        return selectedCheckIn != nil && selectedCheckIn is MarkCheckIn
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.Competitor) {
            guard let competitorVC = segue.destination as? CompetitorViewController else { return }
            guard let competitorCheckIn = selectedCheckIn as? CompetitorCheckIn else { return }
            competitorVC.competitorCheckIn = competitorCheckIn
            selectedCheckIn = nil
        } else if (segue.identifier == Segue.Mark) {
            guard let markVC = segue.destination as? MarkViewController else { return }
            guard let markCheckIn = selectedCheckIn as? MarkCheckIn else { return }
            markVC.markCheckIn = markCheckIn
            selectedCheckIn = nil
        } else if (segue.identifier == Segue.Scan) {
            guard let scanVC = segue.destination as? ScanViewController else { return }
            scanVC.homeViewController = self
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var checkInController: CheckInController = {
        let checkInController = CheckInController()
        checkInController.delegate = self
        return checkInController
    }()
    
    fileprivate lazy var fetchedResultsController: NSFetchedResultsController<CheckIn> = {
        let fetchedResultsController = CoreDataManager.sharedManager.checkInFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension HomeViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        self.configureCell(cell: cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: IndexPath) {
        guard let homeViewCell = cell as? HomeViewCell else { return }
        let checkIn = fetchedResultsController.object(at: indexPath)
        homeViewCell.eventLabel.text = checkIn.event.name
        homeViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        homeViewCell.competitorLabel.text = checkIn.name
    }
    
}

// MARK: - UITableViewDelegate

extension HomeViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, willSelectRowAt indexPath: IndexPath) -> IndexPath? {
        selectedCheckIn = fetchedResultsController.object(at: indexPath)
        return indexPath
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        reviewSelectedCheckIn { 
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.removeSeparatorInset()
    }
    
}

// MARK: - NSFetchedResultsControllerDelegate

extension HomeViewController: NSFetchedResultsControllerDelegate {
    
    func controllerWillChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.beginUpdates()
    }
    
    func controller(
        _ controller: NSFetchedResultsController<NSFetchRequestResult>,
        didChange object: Any,
        at indexPath: IndexPath?,
        for type: NSFetchedResultsChangeType,
        newIndexPath: IndexPath?)
    {
        switch type {
        case .insert:
            tableView.insertRows(at: [newIndexPath!], with: UITableViewRowAnimation.automatic)
        case .update:
            let cell = tableView.cellForRow(at: indexPath!)
            if cell != nil {
                configureCell(cell: cell!, atIndexPath: indexPath!)
                tableView.reloadRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
            }
        case .move:
            tableView.deleteRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
            tableView.insertRows(at: [newIndexPath!], with: .automatic)
        case .delete:
            tableView.deleteRows(at: [indexPath!], with: UITableViewRowAnimation.automatic)
        }
    }
    
    func controllerDidChangeContent(_ controller: NSFetchedResultsController<NSFetchRequestResult>) {
        tableView.endUpdates()
        setupTableViewHeader()
    }
    
}

// MARK: - CheckInControllerDelegate

extension HomeViewController: CheckInControllerDelegate {
    
    func checkInController(_ sender: CheckInController, show alertController: UIAlertController) {
        present(alertController, animated: true, completion: nil)
    }
    
}
