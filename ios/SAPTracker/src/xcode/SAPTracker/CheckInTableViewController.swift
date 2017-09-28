//
//  BaseCheckInTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 01.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

protocol CheckInTableViewControllerDelegate: class {
    
    var coreDataManager: CoreDataManager { get }
    
    var checkInController: CheckInController { get }

    var isFooterViewHidden: Bool { get }

    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn)
    
    func checkInTableViewController(
        _ controller: CheckInTableViewController,
        prepareForSegue segue: UIStoryboardSegue,
        andCompetitorCheckIn competitorCheckIn: CompetitorCheckIn)
    
    func checkInTableViewController(
        _ controller: CheckInTableViewController,
        prepareForSegue segue: UIStoryboardSegue,
        andMarkCheckIn markCheckIn: MarkCheckIn)
    
}

class CheckInTableViewController: UIViewController {
    
    struct CheckInSegue {
        static let Competitor = "Competitor"
        static let Mark = "Mark"
    }
    
    weak var delegate: CheckInTableViewControllerDelegate?
    
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
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        tableViewDeselectRow()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        layout()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupTableView()
        setupTableViewDataSource()
        setupTableViewHeader()
        setupTableViewFooter()
    }
    
    fileprivate func setupTableView() {
        tableView.rowHeight = UITableViewAutomaticDimension
        tableView.estimatedRowHeight = 140
    }
    
    fileprivate func setupTableViewDataSource() {
        do {
            try fetchedResultsController?.performFetch()
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
        tableView.tableFooterView?.isHidden = delegate?.isFooterViewHidden ?? false
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
    
    // MARK: - TableView
    
    fileprivate func tableViewDeselectRow() {
        if let indexPath = tableView.indexPathForSelectedRow {
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    // MARK: - Segues
    
    func performSegue(forCheckIn checkIn: CheckIn?) {
        segueCheckIn = checkIn
        guard segueCheckIn != nil else {
            logInfo(name: "\(#function)", info: "check-in is nil")
            return
        }
        if (segueCheckIn is CompetitorCheckIn) {
            performSegue(withIdentifier: CheckInSegue.Competitor, sender: self)
        } else if (segueCheckIn is MarkCheckIn) {
            performSegue(withIdentifier: CheckInSegue.Mark, sender: self)
        } else {
            logInfo(name: "\(#function)", info: "unknown check-in type")
        }
    }
    
    override func shouldPerformSegue(withIdentifier identifier: String, sender: Any?) -> Bool {
        if (identifier == CheckInSegue.Competitor) {
            return segueCheckIn != nil && segueCheckIn is CompetitorCheckIn
        } else if (identifier == CheckInSegue.Mark) {
            return segueCheckIn != nil && segueCheckIn is MarkCheckIn
        }
        return true
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == CheckInSegue.Competitor) {
            if let competitorCheckIn = segueCheckIn as? CompetitorCheckIn {
                delegate?.checkInTableViewController(self, prepareForSegue: segue, andCompetitorCheckIn: competitorCheckIn)
            }
        } else if (segue.identifier == CheckInSegue.Mark) {
            if let markCheckIn = segueCheckIn as? MarkCheckIn {
                delegate?.checkInTableViewController(self, prepareForSegue: segue, andMarkCheckIn: markCheckIn)
            }
        }
    }
    
    // MARK: - Properties
        
    lazy var fetchedResultsController: NSFetchedResultsController<CheckIn>? = {
        let fetchedResultsController = self.delegate?.coreDataManager.checkInFetchedResultsController()
        fetchedResultsController?.delegate = self
        return fetchedResultsController
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
            delegate?.checkInTableViewController(self, configureCell: cell, forCheckIn: checkIn)
        }
        return cell
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
                    delegate?.checkInTableViewController(self, configureCell: cell, forCheckIn: checkIn)
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
