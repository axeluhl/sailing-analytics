//
//  MarkSessionViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class MarkSessionViewController: SessionViewController {
    
    struct MarkSessionSegue {
        static let EmbedMark = "EmbedMark"
    }
    
    weak var markCheckIn: MarkCheckIn!
    weak var markCoreDataManager: CoreDataManager!
    weak var markViewController: MarkViewController?

    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == MarkSessionSegue.EmbedMark) {
            if let markViewController = segue.destination as? MarkViewController {
                markViewController.markCheckIn = markCheckIn
                markViewController.markSessionController = markSessionController
                markViewController.markCoreDataManager = markCoreDataManager
                self.markViewController = markViewController
            }
        }
    }
    
    // MARK: - Properties
    
    lazy var markSessionController: MarkSessionController = {
        return MarkSessionController(checkIn: self.markCheckIn, coreDataManager: self.markCoreDataManager)
    }()
    
}
