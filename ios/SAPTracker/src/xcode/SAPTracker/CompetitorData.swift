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
        static let BoatClassName = "boatClassName"
        static let CountryCode = "countryCode"
        static let ID = "id"
        static let Name = "name"
        static let Nationality = "nationality"
        static let SailID = "sailID"
    }
    
    var boatClassName: String { get { return stringValue(forKey: Keys.BoatClassName) } }
    var countryCode: String { get { return stringValue(forKey: Keys.CountryCode) } }
    var competitorID: String { get { return stringValue(forKey: Keys.ID) } }
    var name: String { get { return stringValue(forKey: Keys.Name) } }
    var nationality: String { get { return stringValue(forKey: Keys.Nationality) } }
    var sailID: String { get { return stringValue(forKey: Keys.SailID) } }
    
}
