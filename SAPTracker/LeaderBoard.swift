//
//  LeaderBoard.swift
//  SAPTracker
//
//  Created by computing on 14/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(LeaderBoard)
class LeaderBoard: NSManagedObject {

    @NSManaged var name: String
    @NSManaged var competitor: Competitor?
    @NSManaged var event: Event?
    
    func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        name = dictionary["name"] as String
    }

}
