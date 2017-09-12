//
//  RegattaCompetitorCreateAndAddData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 25.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaCompetitorCreateAndAddData: BaseData {
    
    fileprivate struct Keys {
        static let ID = "id"
    }
    
    var competitorID: String { get { return stringValue(forKey: Keys.ID) } }
    
}
