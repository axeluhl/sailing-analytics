//
//  Mark+CoreDataProperties.swift
//  SAPTracker
//
//  Created by Raimund Wege on 03.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//
//  Choose "Create NSManagedObject Subclass…" from the Core Data editor menu
//  to delete and recreate this implementation file for your updated model.
//

import Foundation
import CoreData

extension Mark {

    @NSManaged var markID: String
    @NSManaged var name: String
    @NSManaged var regatta: Regatta

}
