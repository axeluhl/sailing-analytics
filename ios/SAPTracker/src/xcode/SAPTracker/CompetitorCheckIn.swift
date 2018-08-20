//
//  CompetitorCheckIn.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class CompetitorCheckIn: CheckIn {

    override func updateWithCheckInData(checkInData: CheckInData) {
        super.updateWithCheckInData(checkInData: checkInData)
        teamImageURL = checkInData.teamImageURL
        boatClassName = checkInData.competitorData.boatClassName
        competitorID = checkInData.competitorData.competitorID
        countryCode = checkInData.competitorData.countryCode
        name = checkInData.competitorData.name
        nationality = checkInData.competitorData.nationality
        sailID = checkInData.competitorData.sailID
    }

}
