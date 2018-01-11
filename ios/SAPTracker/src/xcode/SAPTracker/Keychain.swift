//
//  Keychain.swift
//  SAPTracker
//
//  Created by Raimund Wege on 08.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class Keychain: NSObject {

    fileprivate enum Keychain {
        static let UserNameAccount = "user.name"
        static let UserPasswordAccount = "user.password"
        static let UserAccessTokenAccount = "user.access_token"
    }

    static func userName(forService service: String) -> KeychainPasswordItem {
        return KeychainPasswordItem.init(service: service, account: Keychain.UserNameAccount)
    }

    static func userPassword(forService service: String) -> KeychainPasswordItem {
        return KeychainPasswordItem.init(service: service, account: Keychain.UserPasswordAccount)
    }

    static func userAccessToken(forService service: String) -> KeychainPasswordItem {
        return KeychainPasswordItem.init(service: service, account: Keychain.UserAccessTokenAccount)
    }

}
