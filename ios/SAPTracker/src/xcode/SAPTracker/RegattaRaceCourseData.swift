//
//  RegattaRaceCourseData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 20.12.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class RegattaRaceCourseData: BaseData {

    func isEmpty() -> Bool {
        guard let waypoints = dictionary["waypoints"] as? [[String: AnyObject]] else { return true }
        return waypoints.count == 0
    }

    func isAutoCourse() -> Bool {
        return dictionary["name"] as? String == "Auto-Course"
    }

}
