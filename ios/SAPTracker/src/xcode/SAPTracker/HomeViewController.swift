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
    
    private struct Segue {
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
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
        review()
        subscribeForNewCheckInURLNotifications()
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        unsubscribeFromNewCheckInURLNotifications()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
        setupTableView()
        setupTableViewDataSource()
        setupTableViewHeader()
    }
    
    private func setupButtons() {
        scanCodeButton.setBackgroundImage(Images.BlueHighlighted, forState: .Highlighted)
        noCodeButton.setBackgroundImage(Images.GrayHighlighted, forState: .Highlighted)
    }
    
    private func setupLocalization() {
        headerTitleLabel.text = Translation.HomeView.HeaderTitleLabel.Text.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, forState: .Normal)
        noCodeButton.setTitle(Translation.HomeView.NoCodeAlert.Title.String, forState: .Normal)
        infoCodeLabel.text = Translation.HomeView.InfoCodeLabel.Text.String
        footerTextView.text = Translation.HomeView.FooterTextView.Text.String
    }
    
    private func setupNavigationBar() {
        navigationItem.title = Application.Title
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    private func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    private func setupTableViewDataSource() {
        do {
            try fetchedResultsController.performFetch()
        } catch {
            logError("\(#function)", error: error)
        }
    }
    
    private func setupTableViewHeader() {
        if fetchedResultsController.sections?[0].numberOfObjects ?? 0 == 0 {
            tableView.tableHeaderView = nil
        } else {
            tableView.tableHeaderView = headerView
        }
    }
    
    // MARK: - Layout
    
    private func layout() {
        self.layoutFooterView()
    }
    
    private func layoutFooterView() {
        let height = footerTextView.sizeThatFits(CGSizeMake(footerView.frame.width, CGFloat.max)).height
        footerView.frame = CGRectMake(footerView.frame.origin.x,
                                      footerView.frame.origin.y,
                                      footerView.frame.width,
                                      height
        )
    }
    
    // MARK: - Review
    
    private func review() {
        self.reviewTerms({
            logInfo("\(#function)", info: "Review terms done.")
            self.reviewCodeConvention({
                logInfo("\(#function)", info: "Review code convention done.")
                self.reviewGPSFixes({
                    logInfo("\(#function)", info: "Review GPS fixes done.")
                    self.reviewNewCheckIn({
                        logInfo("\(#function)", info: "Review new check-in done.")
                        self.reviewSelectedCheckIn({
                            logInfo("\(#function)", info: "Review selected check-in done.")
                        })
                    })
                })
            })
        })
    }
    
    // MARK: 1. Review Terms
    
    private func reviewTerms(completion: () -> Void) {
        guard Preferences.termsAccepted == false else { completion(); return }
        let alertController = UIAlertController(title: Translation.HomeView.TermsAlert.Title.String,
                                                message: Translation.HomeView.TermsAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let showTermsAction = UIAlertAction(title: Translation.HomeView.TermsAlert.ShowTermsAction.Title.String, style: .Default) { action in
            UIApplication.sharedApplication().openURL(URLs.Terms)
            self.reviewTerms(completion) // Review terms until user accepted terms
        }
        let acceptTermsAction = UIAlertAction(title: Translation.HomeView.TermsAlert.AcceptTermsAction.Title.String, style: .Default) { action in
            Preferences.termsAccepted = true
            completion() // Terms accepted
        }
        alertController.addAction(showTermsAction)
        alertController.addAction(acceptTermsAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    // MARK: 2. Review Code Convention
    
    private func reviewCodeConvention(completion: () -> Void) {
        #if DEBUG
            guard Preferences.codeConventionRead == false else { completion(); return }
            let alertController = UIAlertController(title: "Code Convention",
                                                    message: "Please try to respect the code convention which is used for this project.",
                                                    preferredStyle: .Alert
            )
            let showCodeConventionAction = UIAlertAction(title: "Code Convention", style: .Default) { action in
                UIApplication.sharedApplication().openURL(URLs.CodeConvention)
                self.reviewCodeConvention(completion)
            }
            let okAction = UIAlertAction(title: "OK", style: .Default) { action in
                Preferences.codeConventionRead = true
                completion()
            }
            alertController.addAction(showCodeConventionAction)
            alertController.addAction(okAction)
            presentViewController(alertController, animated: true, completion: nil)
        #else
            completion()
        #endif
    }
    
    // MARK: 3. Review GPS Fixes
    
    private func reviewGPSFixes(completion: () -> Void) {
        SVProgressHUD.show()
        fetchedResultsController.delegate = nil
        var checkIns = CoreDataManager.sharedManager.fetchCheckIns() ?? []
        reviewGPSFixes(&checkIns) {
            self.reviewGPSFixesCompleted(completion)
        }
    }
    
    private func reviewGPSFixes(inout checkIns: [CheckIn], completion: () -> Void) {
        guard let checkIn = checkIns.popLast() else { completion(); return }
        let gpsFixController = GPSFixController.init(checkIn: checkIn)
        gpsFixController.sendAll({ (withSuccess) in
            self.reviewGPSFixes(&checkIns, completion: completion)
        })
    }
    
    private func reviewGPSFixesCompleted(completion: () -> Void) {
        SVProgressHUD.popActivity()
        fetchedResultsController.delegate = self
        completion()
    }
    
    // MARK: 4. Review New Check-In
    
    private func reviewNewCheckIn(completion: () -> Void) {
        guard let urlString = Preferences.newCheckInURL else { completion(); return }
        guard let checkInData = CheckInData(urlString: urlString) else { completion(); return }
        checkInController.checkIn(checkInData, completion: { (withSuccess) in
            Preferences.newCheckInURL = nil
            self.selectedCheckIn = CoreDataManager.sharedManager.fetchCheckIn(checkInData)
            completion()
        })
    }
    
    // MARK: 5. Review Selected Check-In
    
    private func reviewSelectedCheckIn(completion: () -> Void) {
        if shouldPerformCompetitorSegue() {
            performSegueWithIdentifier(Segue.Competitor, sender: self)
        } else if shouldPerformMarkSegue() {
            performSegueWithIdentifier(Segue.Mark, sender: self)
        }
        completion()
    }
    
    // MARK: - Notifications
    
    private func subscribeForNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(
            self,
            selector: #selector(HomeViewController.newCheckInURLNotification(_:)),
            name: Preferences.NotificationType.NewCheckInURLChanged,
            object: nil
        )
    }
    
    private func unsubscribeFromNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    @objc private func newCheckInURLNotification(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.reviewNewCheckIn({
                logInfo("\(#function)", info: "Review new check-in done.")
                self.reviewSelectedCheckIn({
                    logInfo("\(#function)", info: "Review selected check-in done.")
                })
            })
        })
    }
    
    // MARK: - Actions
    
    @IBAction func optionButtonTapped(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier(Segue.Settings, sender: alertController)
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier(Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func scanButtonTapped(sender: AnyObject) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            performSegueWithIdentifier(Segue.Scan, sender: sender)
        } else {
            showNoCameraAlert()
        }
    }
    
    @IBAction func noCodeButtonTapped(sender: AnyObject) {
        showNoCodeAlert()
    }
    
    // MARK: - Alerts
    
    private func showNoCameraAlert() {
        let alertController = UIAlertController(title: Translation.Common.Error.String,
                                                message: Translation.HomeView.NoCameraAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default, handler: nil)
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func showNoCodeAlert() {
        let alertController = UIAlertController(title: Translation.HomeView.NoCodeAlert.Title.String,
                                                message: Translation.HomeView.NoCodeAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default, handler: nil)
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Segues
    
    override func shouldPerformSegueWithIdentifier(identifier: String, sender: AnyObject?) -> Bool {
        if (identifier == Segue.Competitor) {
            return shouldPerformCompetitorSegue()
        } else if (identifier == Segue.Mark) {
            return shouldPerformMarkSegue()
        } else {
            return true
        }
    }
    
    private func shouldPerformCompetitorSegue() -> Bool {
        return selectedCheckIn != nil && selectedCheckIn is CompetitorCheckIn
    }

    private func shouldPerformMarkSegue() -> Bool {
        return selectedCheckIn != nil && selectedCheckIn is MarkCheckIn
    }

    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == Segue.Competitor) {
            guard let competitorVC = segue.destinationViewController as? CompetitorViewController else { return }
            guard let competitorCheckIn = selectedCheckIn as? CompetitorCheckIn else { return }
            competitorVC.competitorCheckIn = competitorCheckIn
            selectedCheckIn = nil
        } else if (segue.identifier == Segue.Mark) {
            guard let markVC = segue.destinationViewController as? MarkViewController else { return }
            guard let markCheckIn = selectedCheckIn as? MarkCheckIn else { return }
            markVC.markCheckIn = markCheckIn
            selectedCheckIn = nil
        } else if (segue.identifier == Segue.Scan) {
            guard let scanVC = segue.destinationViewController as? ScanViewController else { return }
            scanVC.homeViewController = self
        }
    }
    
    // MARK: - Properties
    
    private lazy var checkInController: CheckInController = {
        let checkInController = CheckInController()
        checkInController.delegate = self
        return checkInController
    }()
    
    private lazy var fetchedResultsController: NSFetchedResultsController = {
        let fetchedResultsController = CoreDataManager.sharedManager.checkInFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension HomeViewController: UITableViewDataSource {
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Regatta") ?? UITableViewCell()
        self.configureCell(cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        guard let regattaCell = cell as? HomeViewRegattaCell else { return }
        guard let checkIn = fetchedResultsController.objectAtIndexPath(indexPath) as? CheckIn else { return }
        regattaCell.eventLabel.text = checkIn.event.name
        regattaCell.leaderboardLabel.text = checkIn.leaderboard.name
        regattaCell.competitorLabel.text = checkIn.name
    }
    
}

// MARK: - UITableViewDelegate

extension HomeViewController: UITableViewDelegate {
    
    func tableView(tableView: UITableView, willSelectRowAtIndexPath indexPath: NSIndexPath) -> NSIndexPath? {
        selectedCheckIn = fetchedResultsController.objectAtIndexPath(indexPath) as? CheckIn
        return indexPath
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        reviewSelectedCheckIn { 
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    func tableView(tableView: UITableView, willDisplayCell cell: UITableViewCell, forRowAtIndexPath indexPath: NSIndexPath) {
        cell.removeSeparatorInset()
    }
    
}

// MARK: - NSFetchedResultsControllerDelegate

extension HomeViewController: NSFetchedResultsControllerDelegate {
    
    func controllerWillChangeContent(controller: NSFetchedResultsController) {
        tableView.beginUpdates()
    }
    
    func controller(controller: NSFetchedResultsController,
                    didChangeObject object: AnyObject,
                                    atIndexPath indexPath: NSIndexPath?,
                                                forChangeType type: NSFetchedResultsChangeType,
                                                              newIndexPath: NSIndexPath?)
    {
        switch type {
        case .Insert:
            tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
        case .Update:
            let cell = tableView.cellForRowAtIndexPath(indexPath!)
            if cell != nil {
                configureCell(cell!, atIndexPath: indexPath!)
                tableView.reloadRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
            }
        case .Move:
            tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
            tableView.insertRowsAtIndexPaths([newIndexPath!], withRowAnimation: .Automatic)
        case .Delete:
            tableView.deleteRowsAtIndexPaths([indexPath!], withRowAnimation: UITableViewRowAnimation.Automatic)
        }
    }
    
    func controllerDidChangeContent(controller: NSFetchedResultsController) {
        tableView.endUpdates()
        setupTableViewHeader()
    }
    
}

// MARK: - CheckInControllerDelegate

extension HomeViewController: CheckInControllerDelegate {
    
    func showCheckInAlert(sender: CheckInController, alertController: UIAlertController) {
        presentViewController(alertController, animated: true, completion: nil)
    }
    
}
