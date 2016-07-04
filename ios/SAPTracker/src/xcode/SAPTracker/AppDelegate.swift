//
//  AppDelegate.swift
//  SAPTracker
//
//  Created by computing on 17/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import UIKit
import CoreData

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {

        // Set up connection logging for debug builds
        #if DEBUG
        //AFNetworkActivityLogger.sharedLogger().startLogging()
        //AFNetworkActivityLogger.sharedLogger().level = AFHTTPRequestLoggerLevel.AFLoggerLevelDebug
        #endif
        
        // Initialize core data, migrate database if needed, or delete if migration needed but not possible
        CoreDataManager.sharedManager
        
        // Start timer in case GPS fixes need to be sent
//        SendGPSFixController.sharedManager.timer()
        
        // Setup
        self.setupNavigationBarApperance()
        self.setupPageControlApperance()
        
        return true
    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String?, annotation: AnyObject) -> Bool {
        Preferences.lastCheckInURLString = urlStringForDeeplink(url)
        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        CoreDataManager.sharedManager.saveContext()
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        CoreDataManager.sharedManager.saveContext()
    }

    // MARK: - Setups
    
    private func setupNavigationBarApperance() {
        let tintColor = UIColor(hex: 0x009de0)
        UINavigationBar.appearance().tintColor = tintColor
        UINavigationBar.appearance().titleTextAttributes =
            [NSForegroundColorAttributeName: tintColor,
             NSFontAttributeName: UIFont(name: "OpenSans-Bold", size: CGFloat(17.0))!
        ]
    }
    
    private func setupPageControlApperance() {
        UIPageControl.appearance().pageIndicatorTintColor = UIColor.lightGrayColor()
        UIPageControl.appearance().currentPageIndicatorTintColor = UIColor.blackColor()
    }
    
    // MARK: - Helper
    
    private func urlStringForDeeplink(url: NSURL) -> String {
        var urlString = url.absoluteString
        let appPrefix : String = "comsapsailingtracker://"
        
        // FIXME: - Two prefixes are not allowed 
        // Deeplink needs another structure (if possible) because of allowed characters in URL
        // https:// -> http//
        urlString = urlString.hasPrefix(appPrefix) ? urlString.substringFromIndex(appPrefix.endIndex) : urlString
        let httpPrefix : String = "http//"
        urlString = urlString.hasPrefix(httpPrefix) ? "http://" + urlString.substringFromIndex(httpPrefix.endIndex) : urlString
        let httpsPrefix : String = "https//"
        urlString = urlString.hasPrefix(httpsPrefix) ? "https://" + urlString.substringFromIndex(httpsPrefix.endIndex) : urlString
        return urlString
    }
    
}
