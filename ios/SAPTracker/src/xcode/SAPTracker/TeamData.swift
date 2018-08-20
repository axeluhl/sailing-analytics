//
//  TeamData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 03.08.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class TeamData: BaseData {

    fileprivate enum Keys {
        static let ImageURL = "imageUri"
    }
    
    var imageURL: String { get { return stringValue(forKey: Keys.ImageURL) } }
    
}
