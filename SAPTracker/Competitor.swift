//
//  Competitor.swift
//  SAPTracker
//
//  Created by computing on 11/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class Competitor: NSManagedObject {

    @NSManaged var competitorId: String
    @NSManaged var displayName: String
    @NSManaged var name: String
    @NSManaged var sailID: String
    @NSManaged var countryCode: String
    @NSManaged var nationality: String

}
