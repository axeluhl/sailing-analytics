//
//  BoatClassData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 18.04.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

class BoatClassData: BaseData {

    fileprivate enum Keys {
        static let Name = "name"
    }

    var name: String { get { return stringValue(forKey: Keys.Name) } }

}
