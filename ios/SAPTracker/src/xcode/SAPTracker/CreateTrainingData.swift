//
//  CreateTrainingData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CreateTrainingData: NSObject {
    
    let serverURL: String
    let boatClassName: String
    let sailID: String
    let nationality: String
    
    var createEventData: CreateEventData?
    var competitorID: String?
    
    init(serverURL: String, boatClassName: String, sailID: String, nationality: String) {
        self.serverURL = serverURL
        self.boatClassName = boatClassName
        self.sailID = sailID
        self.nationality = nationality
        super.init()
    }
    
}
