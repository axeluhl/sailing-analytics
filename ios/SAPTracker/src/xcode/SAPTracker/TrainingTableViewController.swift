//
//  TrainingTableViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 22.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingTableViewController: UIViewController {

    @IBOutlet weak var headerTitleLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var footerTextView: UITextView!
    @IBOutlet weak var addButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()

        // Uncomment the following line to preserve selection between presentations
        // self.clearsSelectionOnViewWillAppear = false

        // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
        // self.navigationItem.rightBarButtonItem = self.editButtonItem()
        setup()
    }

    fileprivate func setup() {
        setupAddButton()
    }

    fileprivate func setupAddButton() {
        makeViewRoundWithShadow(addButton)
    }

}

// MARK: - UITableViewDataSource

extension TrainingTableViewController: UITableViewDataSource {
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 0
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell") ?? UITableViewCell()
        self.configureCell(cell: cell, atIndexPath: indexPath)
        return cell
    }
    
    func configureCell(cell: UITableViewCell, atIndexPath indexPath: IndexPath) {
//        guard let homeViewCell = cell as? HomeViewCell else { return }
//        let checkIn = fetchedResultsController.object(at: indexPath)
//        homeViewCell.eventLabel.text = checkIn.event.name
//        homeViewCell.leaderboardLabel.text = checkIn.leaderboard.name
//        homeViewCell.competitorLabel.text = checkIn.name
    }
    
}

// MARK: - UITableViewDelegate

extension TrainingTableViewController: UITableViewDelegate {
    
}
