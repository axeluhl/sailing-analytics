//
//  DataManager.swift
//  SAPTracker
//
//  Created by computing on 21/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class DataManager: NSObject {

    class var sharedManager: DataManager {
        struct Singleton {
            static let sharedDataManager = DataManager()
        }
        return Singleton.sharedDataManager
    }

    override init() {
        super.init()
        
        println(managedObjectContext!)
        
        // store new locations to database
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "newLocation:", name: LocationManager.NotificationType.newLocation, object: nil)
        
        // save context when done tracking
        NSNotificationCenter.defaultCenter().addObserver(self, selector: "trackingStopped:", name: LocationManager.NotificationType.trackingStopped, object: nil)
    }

    deinit {
        NSNotificationCenter.defaultCenter().removeObserver(self)
    }
    
    // MARK: - access methods
    
    func event(eventId: String) -> Event {
        let fetchRequest = NSFetchRequest(entityName: "Event")
        fetchRequest.predicate = NSPredicate(format: "eventId = %s", eventId)
        var error: NSError? = nil
        let results = self.managedObjectContext!.executeFetchRequest(fetchRequest, error: &error)
        if (results != nil && results!.count > 0) {
            return results![0] as Event
        } else {
            return NSEntityDescription.insertNewObjectForEntityForName("Event", inManagedObjectContext: self.managedObjectContext!) as Event
        }
    }
    
    func leaderboard(event: Event) -> LeaderBoard {
        var leaderBoard = NSEntityDescription.insertNewObjectForEntityForName("LeaderBoard", inManagedObjectContext: self.managedObjectContext!) as LeaderBoard
        leaderBoard.event = event
        return leaderBoard
    }
    
    func competitor(leaderBoard: LeaderBoard) -> Competitor {
        var competitor = NSEntityDescription.insertNewObjectForEntityForName("Competitor", inManagedObjectContext: self.managedObjectContext!) as Competitor
        competitor.leaderBoard = leaderBoard
        return competitor
    }
    
    // MARK: - notification callbacks

    /* New location detected, store to database. */
    private func newLocation(notification: NSNotification) {
        let gpsFix = NSEntityDescription.insertNewObjectForEntityForName("GPSFix", inManagedObjectContext: self.managedObjectContext!) as GPSFix
        gpsFix.initWithDictionary(notification.userInfo!)
    }
    
    /* Tracking stopped, save data to disk. */
    private func trackingStopped(notification: NSNotification) {
        saveContext()
    }

    // MARK: - public database access

    /* Get latest locations. Limited by the max number of objects that can be sent. */
    func latestLocations() -> [GPSFix] {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName("GPSFix", inManagedObjectContext: self.managedObjectContext!)
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "timeMillis", ascending: false)]
        fetchRequest.fetchLimit = APIManager.Constants.maxSendGPSFix
        var error: NSError? = nil
        let results = self.managedObjectContext!.executeFetchRequest(fetchRequest, error: &error)
        return results as [GPSFix]
    }

    // MARK: - Core Data stack
    
    lazy var applicationDocumentsDirectory: NSURL = {
        // The directory the application uses to store the Core Data store file. This code uses a directory named "com.sap.sailing.ios.CoreData" in the application's documents Application Support directory.
        let urls = NSFileManager.defaultManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask)
        return urls[urls.count-1] as NSURL
        }()
    
    lazy var managedObjectModel: NSManagedObjectModel = {
        // The managed object model for the application. This property is not optional. It is a fatal error for the application not to be able to find and load its model.
        let modelURL = NSBundle.mainBundle().URLForResource("CoreData", withExtension: "momd")!
        return NSManagedObjectModel(contentsOfURL: modelURL)!
        }()
    
    lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator? = {
        // The persistent store coordinator for the application. This implementation creates and return a coordinator, having added the store for the application to it. This property is optional since there are legitimate error conditions that could cause the creation of the store to fail.
        // Create the coordinator and store
        var coordinator: NSPersistentStoreCoordinator? = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
        let url = self.applicationDocumentsDirectory.URLByAppendingPathComponent("CoreData.sqlite")
        var error: NSError? = nil
        
        // http://stackoverflow.com/a/8890373
        // Check if we already have a persistent store
        if (NSFileManager.defaultManager().fileExistsAtPath(url.path!)) {
            let existingPersistentStoreMetadata = NSPersistentStoreCoordinator.metadataForPersistentStoreOfType(NSSQLiteStoreType, URL: url, error: &error)
            if (existingPersistentStoreMetadata == nil) {
                // Something *really* bad has happened to the persistent store
                NSException.raise(NSInternalInconsistencyException, format: "Failed to read metadata for persistent store %@: %@", arguments:getVaList([url, error!]));
            }
            
            if ( !self.managedObjectModel.isConfiguration(nil, compatibleWithStoreMetadata: existingPersistentStoreMetadata)) {
                if (!NSFileManager.defaultManager().removeItemAtURL(url, error: &error)) {
                    NSLog("*** Could not delete persistent store, %@", error!);
                } // else the existing persistent store is compatible with the current model - nice!
            } // else no database file yet
        }
        
        let options = [NSMigratePersistentStoresAutomaticallyOption: 1, NSInferMappingModelAutomaticallyOption: 1]
        var failureReason = "There was an error creating or loading the application's saved data."
        if coordinator!.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: options, error: &error) == nil {
            coordinator = nil
            // Report any error we got.
            let dict = NSMutableDictionary()
            dict[NSLocalizedDescriptionKey] = "Failed to initialize the application's saved data"
            dict[NSLocalizedFailureReasonErrorKey] = failureReason
            dict[NSUnderlyingErrorKey] = error
            error = NSError(domain: "YOUR_ERROR_DOMAIN", code: 9999, userInfo: dict)
            // Replace this with code to handle the error appropriately.
            // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
            NSLog("Unresolved error \(error), \(error!.userInfo)")
            //abort()
            NSFileManager.defaultManager().removeItemAtURL(url, error:nil);
            return nil
        }
        
        return coordinator
        }()
    
    lazy var managedObjectContext: NSManagedObjectContext? = {
        // Returns the managed object context for the application (which is already bound to the persistent store coordinator for the application.) This property is optional since there are legitimate error conditions that could cause the creation of the context to fail.
        let coordinator = self.persistentStoreCoordinator
        if coordinator == nil {
            return nil
        }
        var managedObjectContext = NSManagedObjectContext()
        managedObjectContext.persistentStoreCoordinator = coordinator
        return managedObjectContext
        }()
    
    // MARK: - Core Data Saving support
    
    func saveContext () {
        if let moc = self.managedObjectContext {
            var error: NSError? = nil
            if moc.hasChanges && !moc.save(&error) {
                // Replace this implementation with code to handle the error appropriately.
                // abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                NSLog("Unresolved error \(error), \(error!.userInfo)")
                abort()
            }
        }
    }
    
}