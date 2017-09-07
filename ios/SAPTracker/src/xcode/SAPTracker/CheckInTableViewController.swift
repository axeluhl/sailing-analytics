//
//  BaseCheckInTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 01.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

protocol CheckInTableViewControllerDelegate {
    
    var coreDataManager: CoreDataManager { get }
    
    func checkInTableViewController(_ controller: CheckInTableViewController, configureCell cell: UITableViewCell, forCheckIn checkIn: CheckIn)
    
    func checkInTableViewController(_ controller: CheckInTableViewController, didSelectCheckIn checkIn: CheckIn)
    
}

class CheckInTableViewController: UIViewController {
    
    var delegate: CheckInTableViewControllerDelegate!
    
    @IBOutlet var headerView: UIView! // Strong reference needed to avoid deallocation when not attached to table view
    
    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var footerView: UIView!
    @IBOutlet weak var footerTextView: UITextView!
    
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
    
    // MARK: - TableView
    
    fileprivate func tableViewDeselectRow() {
        if let indexPath = tableView.indexPathForSelectedRow {
            tableView.deselectRow(at: indexPath, animated: true)
        }
    }
    
    // MARK: - Properties
    
    lazy var checkInController: CheckInController = {
        let checkInController = CheckInController(coreDataManager: self.delegate.coreDataManager)
        return checkInController
    }()
    
    lazy var fetchedResultsController: NSFetchedResultsController<CheckIn> = {
        let fetchedResultsController = self.delegate.coreDataManager.checkInFetchedResultsController()
        fetchedResultsController.delegate = self
        return fetchedResultsController
    }()
    
}

// MARK: - UITableViewDataSource

extension CheckInTableViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return fetchedResultsController.sections?[section].numberOfObjects ?? 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        delegate.checkInTableViewController(self, configureCell: cell, forCheckIn: fetchedResultsController.object(at: indexPath))
        return cell
    }
    
}

// MARK: - UITableViewDelegate

extension CheckInTableViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        delegate.checkInTableViewController(self, didSelectCheckIn: fetchedResultsController.object(at: indexPath))
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
            tableView.insertRows(at: [newIndexPath!], with: UITableViewRowAnimation.automatic)
        case .update:
            if let cell = tableView.cellForRow(at: indexPath!) {
                let checkIn = fetchedResultsController.object(at: indexPath!)
                delegate.checkInTableViewController(self, configureCell: cell, forCheckIn: checkIn)
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
