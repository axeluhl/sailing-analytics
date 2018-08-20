//
//  ResponseErrorHelper.swift
//  SAPTracker
//
//  Created by Raimund Wege on 02.01.18.
//  Copyright © 2018 com.sap.sailing. All rights reserved.
//

import UIKit

class ErrorHelper: NSObject {

    static func isResponseUnauthorized(error: NSError) -> Bool {
        if let response = error.userInfo[AFNetworkingOperationFailingURLResponseErrorKey] as? HTTPURLResponse {
            return response.statusCode == 401
        }
        return false
    }

}
