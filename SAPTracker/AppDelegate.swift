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
    struct NotificationType {
        static let openUrl = "openUrl"
    }
    
    var window: UIWindow?

    func application(application: UIApplication, didFinishLaunchingWithOptions launchOptions: [NSObject: AnyObject]?) -> Bool {
        // set up hockey app, used for crash reports and beta testing
        BITHockeyManager.sharedHockeyManager().configureWithIdentifier("8c2070839f98ada3aff80a0882dd29e0")
        BITHockeyManager.sharedHockeyManager().startManager()
        BITHockeyManager.sharedHockeyManager().authenticator.authenticateInstallation()
  
        // initialize core data, migrate database if needed, or delete if migration needed but not possible
        DataManager.sharedManager
        
        // start timer in case GPS fixes need to be sent
        SendGPSFixController.sharedManager.timer()
        return true
    }
    
    func application(application: UIApplication, openURL url: NSURL, sourceApplication: String, annotation: AnyObject?) -> Bool {
        var rootViewController = self.window!.rootViewController as UINavigationController
        rootViewController.popToRootViewControllerAnimated(false)
        rootViewController.dismissViewControllerAnimated(false, completion: nil)
        var homeViewController = rootViewController.viewControllers[0] as HomeViewController
        let notification = NSNotification(name: NotificationType.openUrl, object: self, userInfo:["url": url.absoluteString!])
        NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
        return true
    }

    func applicationWillResignActive(application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
    }

    func applicationDidEnterBackground(application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        DataManager.sharedManager.saveContext()
    }

    func applicationWillEnterForeground(application: UIApplication) {
        // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
    }

    func applicationDidBecomeActive(application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    }

    func applicationWillTerminate(application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        DataManager.sharedManager.saveContext()
    }


}

