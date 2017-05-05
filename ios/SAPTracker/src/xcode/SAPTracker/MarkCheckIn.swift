//
//  MarkCheckIn.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class MarkCheckIn: CheckIn {

    override func updateWithRegattaData(regattaData: RegattaData) {
        super.updateWithRegattaData(regattaData)
        markID = regattaData.markID
        name = regattaData.markData.name
    }

}
