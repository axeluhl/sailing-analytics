//
//  Competitor.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Competitor)
class Competitor: NSManagedObject {

    func updateWithCompetitorData(competitorData: CompetitorData) {
        boatClassName = competitorData.boatClassName
        competitorID = competitorData.competitorID
        countryCode = competitorData.countryCode
        name = competitorData.name
        nationality = competitorData.nationality
        sailID = competitorData.sailID
    }
    
}
