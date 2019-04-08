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
        static let BoatClass = "boatClass"
        static let Color = "color"
        static let ID = "id"
        static let Name = "name"
        static let SailID = "sailId"
    }

    var boatClass: BoatClassData { get { return BoatClassData(dictionary: dictionaryValue(forKey: Keys.BoatClass)) } }
    var boatID: String { get { return stringValue(forKey: Keys.ID) } }
    var color: String { get { return stringValue(forKey: Keys.Color) } }
    var name: String { get { return stringValue(forKey: Keys.Name) } }
    var sailID: String { get { return stringValue(forKey: Keys.SailID) } }

}
