//
//  TrainingController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 24.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class TrainingController: NSObject {

    let requestManager: TrainingRequestManager

    init(baseURLString: String) {
        requestManager = TrainingRequestManager(baseURLString: baseURLString)
        super.init()
    }
    
    // MARK: - CreateEvent
    
    func createEvent(boatClassName: String) {
        requestManager.postCreateEvent(boatClassName: boatClassName, success: {
            
        }, failure: { (error, message) in
            
        })
    }
    
}
