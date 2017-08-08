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
        static let Service = "com.sap.sailing.ios.saptracker"
        static let UserNameAccount = "user.name"
        static let UserPasswordAccount = "user.password"
        static let UserAccessTokenAccount = "user.access_token"
    }
    
    static let userName = KeychainPasswordItem.init(service: Keychain.Service, account: Keychain.UserNameAccount)
    static let userPassword = KeychainPasswordItem.init(service: Keychain.Service, account: Keychain.UserPasswordAccount)
    static let userAccessToken = KeychainPasswordItem.init(service: Keychain.Service, account: Keychain.UserAccessTokenAccount)

}
