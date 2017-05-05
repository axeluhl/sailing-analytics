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

    override func updateWithRegattaData(regattaData: RegattaData) {
        super.updateWithRegattaData(regattaData)
        teamImageURL = regattaData.teamImageURL
        teamImageRetry = false
        boatClassName = regattaData.competitorData.boatClassName
        competitorID = regattaData.competitorData.competitorID
        countryCode = regattaData.competitorData.countryCode
        name = regattaData.competitorData.name
        nationality = regattaData.competitorData.nationality
        sailID = regattaData.competitorData.sailID
    }

}
