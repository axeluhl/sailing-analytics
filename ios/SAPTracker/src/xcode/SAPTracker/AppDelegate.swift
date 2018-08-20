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
    
    func application(_ application: UIApplication, performFetchWithCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        logInfo(name: "\(#function)", info: "Background fetch started...")
        var noData = true
        var allSuccess = true
        if let checkIns = CoreDataManager.shared.fetchCheckIns() {
            checkIns.forEach({ (checkIn) in
                if let gpsFixes = checkIn.gpsFixes {
                    if gpsFixes.count > 0 {
                        noData = false
                        let gpsFixController = GPSFixController(checkIn: checkIn, coreDataManager: CoreDataManager.shared)
                        gpsFixController.sendAll(completion: { (withSuccess) in
                            allSuccess = allSuccess && withSuccess
                        })
                    }
                }
            })
        }
        let fetchResult: UIBackgroundFetchResult = noData ? .noData : allSuccess ? .newData : .failed
        logInfo(name: "\(#function)", info: "Background fetch completed with result: \(fetchResult.rawValue)")
        completionHandler(fetchResult)
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {
        setup()
        return true
    }
    
    func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        Preferences.newCheckInURL = urlStringForDeeplink(url: url)
        return true
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        CoreDataManager.shared.saveContext()
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        CoreDataManager.shared.saveContext()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupAFNetworking()
        setupBackgroundFetch()
        setupCoreData()
        setupNavigationBarApperance()
        setupPageControlApperance()
        setupSVProgressHUD()
    }
    
    fileprivate func setupAFNetworking() {
        AFNetworkActivityIndicatorManager.shared().isEnabled = true
        AFNetworkReachabilityManager.shared().startMonitoring()
    }
    
    fileprivate func setupBackgroundFetch() {
        UIApplication.shared.setMinimumBackgroundFetchInterval(UIApplicationBackgroundFetchIntervalMinimum)
    }
    
    fileprivate func setupCoreData() {
        _ = CoreDataManager.shared // Initialize core data, migrate database if needed, or delete if migration needed but not possible
    }
    
    fileprivate func setupNavigationBarApperance() {
        UINavigationBar.appearance().tintColor = Colors.NavigationBarTintColor
    }
    
    fileprivate func setupPageControlApperance() {
        UIPageControl.appearance().pageIndicatorTintColor = UIColor.lightGray
        UIPageControl.appearance().currentPageIndicatorTintColor = UIColor.black
    }
    
    fileprivate func setupSVProgressHUD() {
        SVProgressHUD.setDefaultMaskType(.clear)
    }
    
    // MARK: - Helper
    
    fileprivate func urlStringForDeeplink(url: URL) -> String {
        var urlString = url.absoluteString
        let appPrefix : String = "comsapsailingtracker://"
        
        // FIXME: - Two prefixes are not allowed
        // Deeplink needs another structure (if possible) because of allowed characters in URL
        // https:// -> http//
        
        urlString = urlString.hasPrefix(appPrefix) ? urlString.substring(from: appPrefix.endIndex) : urlString
        let httpPrefix : String = "http//"
        urlString = urlString.hasPrefix(httpPrefix) ? "http://" + urlString.substring(from: httpPrefix.endIndex) : urlString
        let httpsPrefix : String = "https//"
        urlString = urlString.hasPrefix(httpsPrefix) ? "https://" + urlString.substring(from: httpsPrefix.endIndex) : urlString
        return urlString
    }
    
}
