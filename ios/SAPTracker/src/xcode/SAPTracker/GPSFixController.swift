//
//  GPSFixController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class GPSFixController: NSObject {

    let regatta: Regatta
    
    init(regatta: Regatta) {
        self.regatta = regatta
    }
    
    // MARK: - Methods
    
    func sendGPSFixes(completion: () -> Void) {
        guard let gpsFixes = regatta.gpsFixes as? Set<GPSFix> else {
            log("Can't get GPS fixes")
            completion()
            return
        }
        guard gpsFixes.count > 0 else {
            log("No GPS fixes available")
            completion()
            return
        }
        sendGPSFixes(gpsFixes, completion: completion)
    }

    func sendGPSFixes(gpsFixes: Set<GPSFix>, completion: () -> Void) {
        log("\(gpsFixes.count) GPS fixes will be sent")
        requestManager.postGPSFixes(gpsFixes,
                                    success: { (operation, responseObject) in self.sendGPSFixesSuccess(gpsFixes, completion: completion) },
                                    failure: { (operation, error) in self.sendGPSFixesFailure(error, completion: completion) }
        )
    }
    
    private func sendGPSFixesSuccess(gpsFixes: Set<GPSFix>, completion: () -> Void) {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending \(gpsFixes.count) GPS fixes was successful")
            CoreDataManager.sharedManager.deleteObjects(gpsFixes)
            CoreDataManager.sharedManager.saveContext()
            self.log("\(gpsFixes.count) GPS fixes deleted")
            completion()
        })
    }
    
    private func sendGPSFixesFailure(error: AnyObject, completion: () -> Void) {
        dispatch_async(dispatch_get_main_queue(), {
            self.log("Sending GPS fixes failed for reason: \(error)")
            completion()
        })
    }
    
    // MARK: - Properties
    
    lazy var requestManager: RequestManager = {
        let requestManager = RequestManager(baseURLString: self.regatta.serverURL)
        return requestManager
    }()
    
    // MARK: - Helper
    
    private func log(message: String) {
        print("[GPSFixController] \(message)")
    }
    
}
