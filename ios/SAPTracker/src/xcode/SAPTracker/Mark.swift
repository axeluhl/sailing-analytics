//
//  Mark.swift
//  SAPTracker
//
//  Created by Raimund Wege on 03.05.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

@objc(Mark)
class Mark: NSManagedObject {
 
    func updateWithMarkData(markData: MarkData) {
        markID = markData.markID
        name = markData.name
    }

}
