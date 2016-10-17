//
//  Competitor+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 04.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension Competitor {

    @NSManaged var boatClassName: String
    @NSManaged var competitorID: String
    @NSManaged var countryCode: String
    @NSManaged var name: String
    @NSManaged var nationality: String
    @NSManaged var sailID: String
    @NSManaged var regatta: Regatta

}
