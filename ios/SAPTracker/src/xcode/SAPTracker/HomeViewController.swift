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
        static let Scan = "Scan"
        static let Settings = "Settings"
    }
    
    @IBOutlet weak var headerView: UIView!
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
        reviews()
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
    }
    
    private func setupButtons() {
        scanCodeButton.setBackgroundImage(Images.BlueHighlighted, forState: .Highlighted)
        noCodeButton.setBackgroundImage(Images.GrayHighlighted, forState: .Highlighted)
    }
    
    private func setupLocalization() {
        navigationItem.title = Application.Title
        headerTitleLabel.text = Translation.HomeView.HeaderTitleLabel.Text.String
        scanCodeButton.setTitle(Translation.ScanView.Title.String, forState: .Normal)
        noCodeButton.setTitle(Translation.HomeView.NoCodeAlert.Title.String, forState: .Normal)
        infoCodeLabel.text = Translation.HomeView.InfoCodeLabel.Text.String
        footerTextView.text = Translation.HomeView.FooterTextView.Text.String
    }
    
    private func setupNavigationBar() {
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
            print(error)
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
    
    private func reviews() {
        self.reviewTerms({
            print("Review terms done.")
            self.reviewGPSFixes({
                print("Review GPS fixes done.")
                self.reviewNewCheckIn({
                    print("Review new check-in done.")
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
        let showTermsAction = UIAlertAction(title: Translation.HomeView.TermsAlert.ShowTermsAction.Title.String, style: .Cancel) { action in
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
    
    // MARK: 2. Review GPS Fixes
    
    private func reviewGPSFixes(completion: () -> Void) {
        SVProgressHUD.show()
        fetchedResultsController.delegate = nil
        var regattas = CoreDataManager.sharedManager.fetchRegattas() ?? []
        reviewGPSFixes(&regattas) {
            self.reviewGPSFixesCompleted(completion)
        }
    }
    
    private func reviewGPSFixes(inout regattas: [Regatta], completion: () -> Void) {
        guard let regatta = regattas.popLast() else { completion(); return }
        let gpsFixController = GPSFixController.init(regatta: regatta)
        gpsFixController.sendAll({ (withSuccess) in
            self.reviewGPSFixes(&regattas, completion: completion)
        })
    }
    
    private func reviewGPSFixesCompleted(completion: () -> Void) {
        SVProgressHUD.popActivity()
        fetchedResultsController.delegate = self
        completion()
    }
    
    // MARK: 3. Review New Check-In
    
    private func reviewNewCheckIn(completion: () -> Void) {
        guard Preferences.termsAccepted else { completion(); return }
        guard let urlString = Preferences.newCheckInURL else { completion(); return }
        guard let regattaData = RegattaData(urlString: urlString) else { completion(); return }
        checkInController.checkIn(regattaData, completion: { (withSuccess) in
            Preferences.newCheckInURL = nil
            completion()
        })
    }
    
    // MARK: - Notifications
    
    private func subscribeForNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().addObserver(self,
                                                         selector: #selector(HomeViewController.newCheckInURLNotification(_:)),
                                                         name: Preferences.NotificationType.NewCheckInURLChanged,
                                                         object: nil)
    }
    
    private func unsubscribeFromNewCheckInURLNotifications() {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    @objc private func newCheckInURLNotification(notification: NSNotification) {
        dispatch_async(dispatch_get_main_queue(), {
            self.reviewNewCheckIn({
                print("Review new check-in done.")
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
        let aboutAction = UIAlertAction(title: Translation.AboutView.Title.String, style: .Default) { (action) -> Void in
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
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func showNoCodeAlert() {
        let alertController = UIAlertController(title: Translation.HomeView.NoCodeAlert.Title.String,
                                                message: Translation.HomeView.NoCodeAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let cancelAction = UIAlertAction(title: Translation.Common.OK.String, style: .Cancel, handler: nil)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Segues
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.destinationViewController.isKindOfClass(RegattaViewController)) {
            guard let regattaVC = segue.destinationViewController as? RegattaViewController else { return }
            guard let indexPath = tableView.indexPathForSelectedRow else { return }
            guard let regatta = fetchedResultsController.objectAtIndexPath(indexPath) as? Regatta else { return }
            regattaVC.regatta = regatta
            tableView.deselectRowAtIndexPath(indexPath, animated: true)
        }
    }
    
    // MARK: - Properties
    
    private lazy var checkInController: CheckInController = {
        let checkInController = CheckInController()
        checkInController.delegate = self
        return checkInController
    }()
    
    private lazy var fetchedResultsController: NSFetchedResultsController = {
        let fetchedResultsController = CoreDataManager.sharedManager.regattaFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension HomeViewController: UITableViewDataSource {
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        let numberOfRows = fetchedResultsController.sections?[section].numberOfObjects ?? 0
        if numberOfRows == 0 {
            if tableView.tableHeaderView != nil {
                tableView.tableHeaderView = nil
            }
        } else {
            if tableView.tableHeaderView == nil {
                tableView.tableHeaderView = headerView
            }
        }
        return numberOfRows
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCellWithIdentifier("Regatta") ?? UITableViewCell()
        self.configureCell(cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: NSIndexPath) {
        guard let regattaCell = cell as? HomeViewRegattaTableViewCell else { return }
        guard let regatta = fetchedResultsController.objectAtIndexPath(indexPath) as? Regatta else { return }
        regattaCell.eventLabel.text = regatta.event.name
        regattaCell.leaderboardLabel.text = regatta.leaderboard.name
        regattaCell.competitorLabel.text = regatta.competitor.name
    }
    
}

// MARK: - UITableViewDelegate

extension HomeViewController: UITableViewDelegate {
    
    
    
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
    }
    
}

// MARK: - CheckInControllerDelegate

extension HomeViewController: CheckInControllerDelegate {
    
    func showCheckInAlert(sender: CheckInController, alertController: UIAlertController) {
        presentViewController(alertController, animated: true, completion: nil)
    }
    
}
