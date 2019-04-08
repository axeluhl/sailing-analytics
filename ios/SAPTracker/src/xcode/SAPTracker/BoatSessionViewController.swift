//
//  BoatSessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.03.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

class BoatSessionViewController: SessionViewController {

    struct BoatSessionSegue {
        static let EmbedBoat = "EmbedBoat"
    }

    weak var boatCheckIn: BoatCheckIn!
    weak var boatCoreDataManager: CoreDataManager!
    weak var boatViewController: BoatViewController?

    // MARK: - Segues

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == BoatSessionSegue.EmbedBoat) {
            if let boatViewController = segue.destination as? BoatViewController {
                boatViewController.boatCheckIn = boatCheckIn
                boatViewController.boatSessionController = boatSessionController
                boatViewController.boatCoreDataManager = boatCoreDataManager
                self.boatViewController = boatViewController
            }
        }
    }

    // MARK: - Properties

    lazy var boatSessionController: BoatSessionController = {
        return BoatSessionController(checkIn: self.boatCheckIn, coreDataManager: self.boatCoreDataManager)
    }()

}
