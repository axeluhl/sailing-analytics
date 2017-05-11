//
//  CompetitorCheckIn+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension CompetitorCheckIn {

    @NSManaged var teamImageURL: String?
    @NSManaged var teamImageRetry: Bool
    @NSManaged var teamImageData: Data?
    @NSManaged var sailID: String
    @NSManaged var nationality: String
    @NSManaged var countryCode: String
    @NSManaged var competitorID: String
    @NSManaged var boatClassName: String

}
