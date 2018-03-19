//
//  BaseCheckInTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 01.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

class CheckInTableViewController: UIViewController {

    struct CheckInSegue {
        static let NewCheckIn = "NewCheckIn"
        static let RegattaBoat = "RegattaBoat"
        static let RegattaCompetitor = "RegattaCompetitor"
        static let RegattaMark = "RegattaMark"
        static let TrainingBoat = "TrainingBoat"
        static let TrainingCompetitor = "TrainingCompetitor"
        static let TrainingMark = "TrainingMark"
    }

    struct CheckInSegues {
        static let boat = [CheckInSegue.RegattaBoat, CheckInSegue.TrainingBoat]
        static let competitor = [CheckInSegue.RegattaCompetitor, CheckInSegue.TrainingCompetitor]
        static let mark = [CheckInSegue.RegattaMark, CheckInSegue.TrainingMark]
    }

    var segueCheckIn: CheckIn?

    // Strong reference needed to avoid deallocation when not attached to table view
    @IBOutlet var headerView: UIView!
    @IBOutlet var footerTextView: UITextView!

    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var footerView: UIView!

    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        subscribeForTrainingEndpointChangedNotifications()
    }

    deinit {
        unsubscribeFromTrainingEndpointChangedNotifications()
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        tableViewDeselectRow()
        titleView.setSubtitle(subtitle: signUpController.userName ?? "")
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

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupLocalization()
        setupNavigationBar()
        setupTableView()
        setupTableViewDataSource()
        setupTableViewHeader()
        setupTableViewFooter()
    }

    fileprivate func setupLocalization() {
        navigationItem.title = Translation.RegattaCheckInListView.Title.String
        headerTitleLabel.text = Translation.RegattaCheckInListView.HeaderTitleLabel.Text.String
        footerTextView.text = Translation.RegattaCheckInListView.FooterTextView.Text.String
    }

    fileprivate func setupNavigationBar() {
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
        // TODO: Activate Training Feature -> Use this custom title view:
        // navigationItem.titleView = titleView
        navigationController?.navigationBar.setNeedsLayout()
    }

    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    fileprivate func setupTableViewDataSource() {
        do {
            try fetchedResultsController?.performFetch()
            tableView.reloadData()
        } catch {
            logError(name: "\(#function)", error: error)
        }
    }
    
    fileprivate func setupTableViewHeader() {
        if fetchedResultsController?.sections?[0].numberOfObjects ?? 0 == 0 {
            tableView.tableHeaderView = nil
        } else {
            tableView.tableHeaderView = headerView
        }
    }

    fileprivate func setupTableViewFooter() {
        tableView.tableFooterView?.isHidden = false
    }

    // MARK: - Review

    fileprivate func review() {
        reviewTerms(completion: { [weak self] in
            logInfo(name: "\(#function)", info: "Review terms done.")
            self?.reviewCodeConvention(completion: { [weak self] in
                logInfo(name: "\(#function)", info: "Review code convention done.")
                self?.reviewGPSFixes(completion: { [weak self] in
                    logInfo(name: "\(#function)", info: "Review GPS fixes done.")
                    self?.reviewNewCheckIn(completion: { [weak self] (checkIn) in
                        logInfo(name: "\(#function)", info: "Review new check-in done.")
                        self?.performSegue(forCheckIn: checkIn)
                    })
                })
            })
        })
    }

    // MARK: 1. Review Terms

    fileprivate func reviewTerms(completion: @escaping () -> Void) {
        guard Preferences.termsAccepted == false else {
            completion()
            return
        }
        let alertController = UIAlertController(
            title: Translation.RegattaCheckInListView.TermsAlert.Title.String,
            message: Translation.RegattaCheckInListView.TermsAlert.Message.String,
            preferredStyle: .alert
        )
        let showTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.ShowTermsAction.Title.String, style: .default) { [weak self] (action) in
            UIApplication.shared.openURL(URLs.Terms)
            self?.reviewTerms(completion: completion) // Review terms until user accepted terms
        }
        let acceptTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.AcceptTermsAction.Title.String, style: .default) { (action) in
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
            let showCodeConventionAction = UIAlertAction(title: "Code Convention", style: .default) { [weak self] (action) in
                UIApplication.shared.openURL(URLs.CodeConvention)
                self?.reviewCodeConvention(completion: completion)
            }
            let okAction = UIAlertAction(title: "OK", style: .default) { (action) in
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
        let checkIns = coreDataManager.fetchCheckIns() ?? []
        reviewGPSFixes(checkIns: checkIns) { [weak self] in
            self?.reviewGPSFixesCompleted(completion: completion)
        }
    }

    fileprivate func reviewGPSFixes(checkIns: [CheckIn], completion: @escaping () -> Void) {
        guard checkIns.count > 0 else { completion(); return }
        let gpsFixController = GPSFixController.init(checkIn: checkIns[0], coreDataManager: coreDataManager)
        gpsFixController.sendAll(completion: { [weak self] (withSuccess) in
            self?.reviewGPSFixes(checkIns: Array(checkIns[1..<checkIns.count]), completion: completion)
        })
    }

    fileprivate func reviewGPSFixesCompleted(completion: () -> Void) {
        SVProgressHUD.popActivity()
        completion()
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
            selector: #selector(CheckInTableViewController.newCheckInURLNotification(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.NewCheckInURLChanged),
            object: nil
        )
    }

    fileprivate func unsubscribeFromNewCheckInURLNotifications() {
        NotificationCenter.default.removeObserver(
            self,
            name: NSNotification.Name(rawValue: Preferences.NotificationType.NewCheckInURLChanged),
            object: nil
        )
    }

    @objc fileprivate func newCheckInURLNotification(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            self.reviewNewCheckIn(completion: { checkIn in
                logInfo(name: "\(#function)", info: "Review new check-in done.")
                self.performSegue(forCheckIn: checkIn)
            })
        })
    }

    fileprivate func subscribeForTrainingEndpointChangedNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(trainingEndpointChanged(_:)),
            name: NSNotification.Name(rawValue: Preferences.NotificationType.TrainingEndpointChanged),
            object: nil
        )
    }

    fileprivate func unsubscribeFromTrainingEndpointChangedNotifications() {
        NotificationCenter.default.removeObserver(
            self,
            name: NSNotification.Name(rawValue: Preferences.NotificationType.TrainingEndpointChanged),
            object: nil
        )
    }

    @objc fileprivate func trainingEndpointChanged(_ notification: Notification) {
        DispatchQueue.main.async(execute: {
            guard let trainingEndpoint = notification.userInfo?[Preferences.UserInfo.TrainingEndpoint] as? String else { return }
            self.signUpController = SignUpController(baseURLString: trainingEndpoint)
        })
    }

    // MARK: - Layout
    
    fileprivate func layout() {
        layoutFooterView()
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
    
    // MARK: - TableView
    
    fileprivate func tableViewDeselectRow() {
        if let indexPath = tableView.indexPathForSelectedRow {
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    // MARK: - Segues
    
    func performSegue(forCheckIn checkIn: CheckIn?) {
        segueCheckIn = checkIn
        guard let checkIn = checkIn else {
            logInfo(name: "\(#function)", info: "check-in is nil")
            return
        }
        if checkIn is BoatCheckIn {
            if checkIn.isTraining.boolValue {
                performSegue(withIdentifier: CheckInSegue.TrainingBoat, sender: self)
            } else {
                performSegue(withIdentifier: CheckInSegue.RegattaBoat, sender: self)
            }
        } else if checkIn is CompetitorCheckIn {
            if checkIn.isTraining.boolValue {
                performSegue(withIdentifier: CheckInSegue.TrainingCompetitor, sender: self)
            } else {
                performSegue(withIdentifier: CheckInSegue.RegattaCompetitor, sender: self)
            }
        } else if (checkIn is MarkCheckIn) {
            if checkIn.isTraining.boolValue {
                performSegue(withIdentifier: CheckInSegue.TrainingMark, sender: self)
            } else {
                performSegue(withIdentifier: CheckInSegue.RegattaMark, sender: self)
            }
        } else {
            logInfo(name: "\(#function)", info: "unknown check-in type")
        }
    }
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if CheckInSegues.boat.contains(identifier) {
            return segueCheckIn != nil && segueCheckIn is BoatCheckIn
        } else if CheckInSegues.competitor.contains(identifier) {
            return segueCheckIn != nil && segueCheckIn is CompetitorCheckIn
        } else if CheckInSegues.mark.contains(identifier) {
            return segueCheckIn != nil && segueCheckIn is MarkCheckIn
        }
        return true
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if let identifier = segue.identifier {
            if CheckInSegues.boat.contains(identifier) {
                if let boatCheckIn = segueCheckIn as? BoatCheckIn {
                    if let regattaBoatVC = segue.destination as? RegattaBoatViewController {
                        regattaBoatVC.boatCheckIn = boatCheckIn
                        regattaBoatVC.boatCoreDataManager = coreDataManager
                    } else if let trainingBoatVC = segue.destination as? TrainingBoatViewController {
                        trainingBoatVC.boatCheckIn = boatCheckIn
                        trainingBoatVC.boatCoreDataManager = coreDataManager
                    }
                }
            } else if CheckInSegues.competitor.contains(identifier) {
                if let competitorCheckIn = segueCheckIn as? CompetitorCheckIn {
                    if let regattaCompetitorVC = segue.destination as? RegattaCompetitorViewController {
                        regattaCompetitorVC.competitorCheckIn = competitorCheckIn
                        regattaCompetitorVC.competitorCoreDataManager = coreDataManager
                    } else if let trainingCompetitorVC = segue.destination as? TrainingCompetitorViewController {
                        trainingCompetitorVC.competitorCheckIn = competitorCheckIn
                        trainingCompetitorVC.competitorCoreDataManager = coreDataManager
                    }                    
                }
            } else if CheckInSegues.mark.contains(identifier) {
                if let markCheckIn = segueCheckIn as? MarkCheckIn {
                    if let regattaMarkVC = segue.destination as? RegattaMarkViewController {
                        regattaMarkVC.markCheckIn = markCheckIn
                        regattaMarkVC.markCoreDataManager = coreDataManager
                    } else if let trainingMarkVC = segue.destination as? TrainingMarkViewController {
                        trainingMarkVC.markCheckIn = markCheckIn
                        trainingMarkVC.markCoreDataManager = coreDataManager
                    }
                }
            } else if (identifier == CheckInSegue.NewCheckIn) {
                guard let newCheckInNC = segue.destination as? UINavigationController else { return }
                guard let newCheckInVC = newCheckInNC.viewControllers[0] as? NewCheckInViewController else { return }
                newCheckInVC.coreDataManager = coreDataManager
                newCheckInVC.delegate = self
            }
        }
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

    // TODO: Activate Training Feature -> Set sort button visible in IB
    @IBAction func sortButtonTapped(_ sender: Any) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let dateAction = UIAlertAction(title: "DATE", style: .default) { [weak self] action in
            self?.sortByDate()
        }
        let leaderboardAction = UIAlertAction(title: "LEADERBOARD", style: .default) { [weak self] action in
            self?.sortByLeaderboard()
        }
        let competitorAction = UIAlertAction(title: "COMPETITOR", style: .default) { [weak self] action in
            self?.sortByCompetitor()
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(dateAction)
        alertController.addAction(leaderboardAction)
        alertController.addAction(competitorAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }

    func sortByDate() {
        fetchedResultsController?.fetchRequest.sortDescriptors = [NSSortDescriptor(key: "event.startDate", ascending: true)]
        setupTableViewDataSource()
    }

    func sortByLeaderboard() {
        fetchedResultsController?.fetchRequest.sortDescriptors = [NSSortDescriptor(key: "leaderboard.name", ascending: true)]
        setupTableViewDataSource()
    }

    func sortByCompetitor() {
        fetchedResultsController?.fetchRequest.sortDescriptors = [NSSortDescriptor(key: "name", ascending: true)]
        setupTableViewDataSource()
    }

    // MARK: - Properties

    fileprivate lazy var titleView: TitleView = {
        return TitleView(
            title: Translation.TrainingCheckInListView.Title.String,
            subtitle: self.signUpController.userName ?? ""
        )
    }()

    lazy var fetchedResultsController: NSFetchedResultsController<CheckIn>? = {
        let fetchedResultsController = self.coreDataManager.checkInFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()

    lazy var regattaCheckInController: RegattaCheckInController = {
        return RegattaCheckInController(coreDataManager: self.coreDataManager)
    }()

    lazy var signUpController: SignUpController = {
        return SignUpController(baseURLString: Preferences.trainingEndpoint)
    }()

    lazy var coreDataManager: CoreDataManager = {
        return CoreDataManager(name: "CoreData")
    }()

}

// MARK: - UITableViewDataSource

extension CheckInTableViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController?.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        if let checkIn = fetchedResultsController?.object(at: indexPath) {
            configureCell(cell, forCheckIn: checkIn)
        }
        return cell
    }

    func configureCell(_ cell: UITableViewCell, forCheckIn checkIn: CheckIn) {
        guard let regattaCheckInTableViewCell = cell as? CheckInTableViewCell else { return }
        regattaCheckInTableViewCell.eventLabel.text = checkIn.event.name
        regattaCheckInTableViewCell.leaderboardLabel.text = checkIn.leaderboard.name
        regattaCheckInTableViewCell.competitorLabel.textAlignment = .right
        regattaCheckInTableViewCell.competitorLabel.backgroundColor = nil
        if let boatCheckIn = checkIn as? BoatCheckIn {
            regattaCheckInTableViewCell.competitorLabel.text = boatCheckIn.displayName()
            regattaCheckInTableViewCell.competitorLabel.textAlignment = .center
            if let color = UIColor.init(hexString: boatCheckIn.color) {
                regattaCheckInTableViewCell.competitorLabel.backgroundColor = color
                regattaCheckInTableViewCell.competitorLabel.textColor = UIColor.init(contrastColorFor: color)
            }
        } else {
            regattaCheckInTableViewCell.competitorLabel.text = checkIn.name
        }
    }

}

// MARK: - UITableViewDelegate

extension CheckInTableViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        performSegue(forCheckIn: fetchedResultsController?.object(at: indexPath))
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell, forRowAt indexPath: IndexPath) {
        cell.removeSeparatorInset()
    }
    
}

// MARK: - NSFetchedResultsControllerDelegate

extension CheckInTableViewController: NSFetchedResultsControllerDelegate {
    
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
            if indexPath == nil { // Workaround for an iOS 8.4 bug https://forums.developer.apple.com/thread/12184
                tableView.insertRows(at: [newIndexPath!], with: UITableViewRowAnimation.automatic)
            }
        case .update:
            if let cell = tableView.cellForRow(at: indexPath!) {
                if let checkIn = fetchedResultsController?.object(at: indexPath!) {
                    configureCell(cell, forCheckIn: checkIn)
                }
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
        setupTableViewFooter()
    }
    
}

// MARK: - NewCheckInViewControllerDelegate

extension CheckInTableViewController: NewCheckInViewControllerDelegate {

    func newCheckInViewController(_ controller: NewCheckInViewController, didCheckIn checkIn: CheckIn) {
        performSegue(forCheckIn: checkIn)
    }

}
