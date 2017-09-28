//
//  CompetitorSessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CompetitorSessionViewController: SessionViewController {
    
    struct CompetitorSessionSegue {
        static let EmbedCompetitor = "EmbedCompetitor"
    }
    
    weak var competitorCheckIn: CompetitorCheckIn!
    weak var competitorCoreDataManager: CoreDataManager!
    weak var competitorViewController: CompetitorViewController?

    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == CompetitorSessionSegue.EmbedCompetitor) {
            if let competitorViewController = segue.destination as? CompetitorViewController {
                competitorViewController.competitorCheckIn = competitorCheckIn
                competitorViewController.competitorSessionController = competitorSessionController
                competitorViewController.competitorCoreDataManager = competitorCoreDataManager
                self.competitorViewController = competitorViewController
            }
        }
    }
    
    // MARK: - Properties
    
    lazy var competitorSessionController: CompetitorSessionController = {
        return CompetitorSessionController(checkIn: self.competitorCheckIn, coreDataManager: self.competitorCoreDataManager)
    }()
    
}
