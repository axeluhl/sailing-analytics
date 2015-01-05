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
public class LeaderBoard: NSManagedObject {

    @NSManaged public var name: String
    @NSManaged public var competitor: Competitor?
    @NSManaged public var event: Event?
    
    public func initWithDictionary(dictionary: Dictionary<NSObject, AnyObject>) {
        name = dictionary["name"] as String
    }

}
