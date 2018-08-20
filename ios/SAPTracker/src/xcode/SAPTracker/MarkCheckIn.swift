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

    override func updateWithCheckInData(checkInData: CheckInData) {
        super.updateWithCheckInData(checkInData: checkInData)
        markID = checkInData.markData.markID
        name = checkInData.markData.name
    }

}
