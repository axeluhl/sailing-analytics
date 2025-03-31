//
//  LeaderboardData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class LeaderboardData: BaseData {
    
    fileprivate struct Keys {
        static let Name = "name"
    }
    
    var name: String { get { return stringValue(forKey: Keys.Name) } }

}
