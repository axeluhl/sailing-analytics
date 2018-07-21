//
//  BaseData.swift
//  SAPTracker
//
//  Created by Raimund Wege on 05.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class BaseData: NSObject {

    let dictionary: [String: AnyObject]
    
    override init() {
        self.dictionary = [String: AnyObject]()
        super.init()
    }
    
    init(dictionary: [String: AnyObject]?) {
        self.dictionary = dictionary ?? [String: AnyObject]()
        super.init()
    }

    func dictionaryValue(forKey key: String) -> [String: AnyObject] {
        return dictionary[key] as? [String: AnyObject] ?? [String: AnyObject]()
    }

    func doubleValue(forKey key: String) -> Double {
        return dictionary[key] as? Double ?? 0.0
    }
    
    func stringValue(forKey key: String) -> String {
        return dictionary[key] as? String ?? ""
    }
    
}
