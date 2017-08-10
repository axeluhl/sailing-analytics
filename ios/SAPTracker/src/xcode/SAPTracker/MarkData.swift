//
//  MarkData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation

class MarkData: BaseData {
    
    fileprivate enum Keys {
        static let ID = "id"
        static let Name = "name"
    }
    
    var markID: String { get { return stringValue(forKey: Keys.ID) } }

    var name: String { get { return stringValue(forKey: Keys.Name) } }

}
