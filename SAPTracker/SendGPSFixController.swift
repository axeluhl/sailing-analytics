//
//  UploadLocationController.swift
//  SAPTracker
//
//  Created by computing on 19/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class SendGPSFixController: NSObject {
    
    struct NotificationType {
        static let gpsFixesSynced = "gpsFixesSynced"
    }
    
    private struct SyncPeriod {
        /* Normal rate of upload is every 3s */
        static let Normal: NSTimeInterval = 3
        
        /* To save battery, upload rate is lowered to every 30s */
        static let BatterySaving: NSTimeInterval = 30
    }
    
    /* Number of seconds between syncs */
    private var syncPeriod: NSTimeInterval {
        get {
            return BatteryManager.sharedManager.batterySaving ? SyncPeriod.BatterySaving : SyncPeriod.Normal
        }
    }
    
    var trackingEvent: Event?
    
    /* Singleton */
    class var sharedManager: SendGPSFixController {
        struct Singleton {
            static let sharedManager = SendGPSFixController()
        }
        return Singleton.sharedManager
    }
    
    // MARK: - Timer
    
    /* See if any rows need to be uploaded. Schedule timer again. */
    func timer() {
        let loop = NSTimer.scheduledTimerWithTimeInterval(syncPeriod, target:self, selector:"timer", userInfo:nil, repeats:false)
        NSRunLoop.currentRunLoop().addTimer(loop, forMode:NSRunLoopCommonModes)

        // get last 100 locations
        let lastestGPSFixes = DataManager.sharedManager.latestLocations()
        
        // build a list of locations going to one server
        var gpsFixesForServer = [GPSFix]()
        
        // server URL
        var serverUrl: String?
        
        for gpsFix in lastestGPSFixes {
            
            // If currently tracking, don't upload locations for another (previous) event
            if trackingEvent != nil && gpsFix.event != trackingEvent {
                break
            }
            
            // location for another server
            if serverUrl != nil && serverUrl != gpsFix.event.serverUrl {
                sendGPSFixes(serverUrl!, gpsFixes: gpsFixesForServer)
                gpsFixesForServer = []
            }
            serverUrl = gpsFix.event.serverUrl
            gpsFixesForServer.append(gpsFix)
        }
        if gpsFixesForServer.count > 0 {
            sendGPSFixes(serverUrl!, gpsFixes:gpsFixesForServer)
        }
    }
    
    /* Send GPS fixes to specified server. */
    func sendGPSFixes(serverUrl: String, gpsFixes:[GPSFix]) {
        APIManager.sharedManager.initManager(serverUrl)
        if APIManager.sharedManager.networkAvailable {
            APIManager.sharedManager.postGPSFixes(DeviceUDIDManager.UDID, gpsFixes: gpsFixes,
                success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                    println("sent GPS fixes")
                    for gpsFix in gpsFixes {
                        DataManager.sharedManager.managedObjectContext!.deleteObject(gpsFix)
                    }
                    let notification = NSNotification(name: NotificationType.gpsFixesSynced, object: self)
                    NSNotificationQueue.defaultQueue().enqueueNotification(notification, postingStyle: NSPostingStyle.PostASAP)
                },
                failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                    println("error sending GPS fixes")
            })
        }
    }
    
}