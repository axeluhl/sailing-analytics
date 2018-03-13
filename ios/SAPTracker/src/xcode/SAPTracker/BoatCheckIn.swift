//
//  BoatCheckIn+CoreDataClass.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.03.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//
//

import Foundation
import CoreData

class BoatCheckIn: CheckIn {

    override func updateWithCheckInData(checkInData: CheckInData) {
        super.updateWithCheckInData(checkInData: checkInData)
        boatID = checkInData.boatData.boatID
        color = checkInData.boatData.color
        name = checkInData.boatData.name
        sailID = checkInData.boatData.sailID
    }

    func displayName() -> String {
        return name.count > 0 ? name : sailID
    }

}
