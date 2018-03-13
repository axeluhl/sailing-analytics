//
//  BoatData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.03.18.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation

class BoatData: BaseData {

    fileprivate enum Keys {
        static let ID = "id"
        static let Name = "name"
    }

    var boatID: String { get { return stringValue(forKey: Keys.ID) } }

    var name: String { get { return stringValue(forKey: Keys.Name) } }

}
