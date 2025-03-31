//
//  TrainingCheckInController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 07.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingCheckInController: CheckInController {
    
    override init(coreDataManager: CoreDataManager) {
        super.init(coreDataManager: coreDataManager)
        delegate = self
    }
    
}

extension TrainingCheckInController: CheckInControllerDelegate {
    
}
