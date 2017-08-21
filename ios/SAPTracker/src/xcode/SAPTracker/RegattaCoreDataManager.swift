//
//  RegattaCoreDataManager.swift
//  SAPTracker
//
//  Created by Raimund Wege on 21.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaCoreDataManager: CoreDataManager {

    class var shared: CoreDataManager {
        struct Singleton {
            static let shared = RegattaCoreDataManager(name: "CoreData")
        }
        return Singleton.shared
    }

}
