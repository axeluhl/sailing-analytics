//
//  MarkViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class MarkViewController: UIViewController {
    
    weak var markCheckIn: MarkCheckIn!
    weak var markCoreDataManager: CoreDataManager!
    weak var markSessionController: MarkSessionController!
    
    @IBOutlet weak var markNameLabel: UILabel!
    
    // MARK: - Refresh
    
    func refresh() {
        markNameLabel.text = markCheckIn.name
    }
    
}
