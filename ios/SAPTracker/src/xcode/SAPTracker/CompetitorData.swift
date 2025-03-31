//
//  CompetitorData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class CompetitorData: BaseData {

    fileprivate struct Keys {
        static let Boat = "boat"
        static let BoatClassName = "boatClassName"
        static let CountryCode = "countryCode"
        static let ID = "id"
        static let Name = "name"
        static let Nationality = "nationality"
        static let SailID = "sailID"
    }

    var boat: BoatData { get { return BoatData(dictionary: dictionaryValue(forKey: Keys.Boat)) } }
    var countryCode: String { get { return stringValue(forKey: Keys.CountryCode) } }
    var competitorID: String { get { return stringValue(forKey: Keys.ID) } }
    var name: String { get { return stringValue(forKey: Keys.Name) } }
    var nationality: String { get { return stringValue(forKey: Keys.Nationality) } }

    var boatClassName: String {
        get {
            let value = boat.boatClass.name
            return !value.isEmpty ? value : stringValue(forKey: Keys.BoatClassName)
        }
    }

    var sailID: String {
        get {
            let value = boat.sailID
            return !value.isEmpty ? value : stringValue(forKey: Keys.SailID)
        }
    }
    
}
