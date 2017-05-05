//
//  CoreDataManager.swift
//  SAPTracker
//
//  Created by computing on 21/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

public class CoreDataManager: NSObject {
    
    private enum Entities: String {
        case CheckIn
        case CompetitorCheckIn
        case Event
        case GPSFix
        case Leaderboard
        case MarkCheckIn
    }
    
    public class var sharedManager: CoreDataManager {
        struct Singleton {
            static let sharedManager = CoreDataManager()
        }
        return Singleton.sharedManager
    }
    
    // MARK: - Fetch
    
    func fetchCheckIns() -> [CheckIn]? {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName(Entities.CheckIn.rawValue, inManagedObjectContext: managedObjectContext)
        fetchRequest.includesSubentities = true
        var checkIns: [AnyObject]?
        do {
            checkIns = try managedObjectContext.executeFetchRequest(fetchRequest)
        } catch {
            logError("\(#function)", error: error)
        }
        return checkIns as? [CheckIn]
    }

    func fetchCheckIn(regattaData: RegattaData) -> CheckIn? {
        switch regattaData.type() {
        case .Competitor:
            return fetchCompetitorCheckIn(
                regattaData.eventID,
                leaderboardName: regattaData.leaderboardName,
                competitorID: regattaData.competitorID!
            )
        case .Mark:
            return fetchMarkCheckIn(
                regattaData.eventID,
                leaderboardName: regattaData.leaderboardName,
                markID: regattaData.markID!
            )
        default:
            return nil
        }
    }

    func fetchCompetitorCheckIn(eventID: String, leaderboardName: String, competitorID: String) -> CompetitorCheckIn? {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName(
            Entities.CompetitorCheckIn.rawValue,
            inManagedObjectContext: managedObjectContext
        )
        fetchRequest.predicate = NSPredicate(
            format: "event.eventID = %@ AND leaderboard.name = %@ AND competitorID = %@",
            eventID,
            leaderboardName,
            competitorID
        )
        do {
            let checkIns = try managedObjectContext.executeFetchRequest(fetchRequest)
            if checkIns.count == 0 {
                return nil
            } else {
                return checkIns[0] as? CompetitorCheckIn
            }
        } catch {
            logError("\(#function)", error: error)
        }
        return nil
    }

    func fetchMarkCheckIn(eventID: String, leaderboardName: String, markID: String) -> MarkCheckIn? {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName(
            Entities.MarkCheckIn.rawValue,
            inManagedObjectContext: managedObjectContext
        )
        fetchRequest.predicate = NSPredicate(
            format: "event.eventID = %@ AND leaderboard.name = %@ AND markID = %@",
            eventID,
            leaderboardName,
            markID
        )
        do {
            let checkIns = try managedObjectContext.executeFetchRequest(fetchRequest)
            if checkIns.count == 0 {
                return nil
            } else {
                return checkIns[0] as? MarkCheckIn
            }
        } catch {
            logError("\(#function)", error: error)
        }
        return nil
    }

    func checkInFetchedResultsController() -> NSFetchedResultsController {
        let fetchRequest = NSFetchRequest(entityName: Entities.CheckIn.rawValue)
        fetchRequest.predicate = NSPredicate(format: "event != nil AND leaderboard != nil")
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "leaderboard.name", ascending: true)]
        return NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: managedObjectContext, sectionNameKeyPath: nil, cacheName: nil)
    }
    
    // MARK: - Insert
    
    func newCompetitorCheckIn() -> CompetitorCheckIn {
        let checkIn = NSEntityDescription.insertNewObjectForEntityForName(Entities.CompetitorCheckIn.rawValue, inManagedObjectContext: managedObjectContext) as! CompetitorCheckIn
        checkIn.initialize()
        return checkIn
    }

    func newMarkCheckIn() -> MarkCheckIn {
        let checkIn = NSEntityDescription.insertNewObjectForEntityForName(Entities.MarkCheckIn.rawValue, inManagedObjectContext: managedObjectContext) as! MarkCheckIn
        checkIn.initialize()
        return checkIn
    }

    func newEvent(checkIn: CheckIn) -> Event {
        let event = NSEntityDescription.insertNewObjectForEntityForName(Entities.Event.rawValue, inManagedObjectContext: managedObjectContext) as! Event
        event.checkIn = checkIn
        return event
    }
    
    func newLeaderboard(checkIn: CheckIn) -> Leaderboard {
        let leaderboard = NSEntityDescription.insertNewObjectForEntityForName(Entities.Leaderboard.rawValue, inManagedObjectContext: managedObjectContext) as! Leaderboard
        leaderboard.checkIn = checkIn
        return leaderboard
    }

    func newGPSFix(checkIn: CheckIn) -> GPSFix {
        let gpsFix = NSEntityDescription.insertNewObjectForEntityForName(Entities.GPSFix.rawValue, inManagedObjectContext: managedObjectContext) as! GPSFix
        gpsFix.checkIn = checkIn
        return gpsFix
    }

    // MARK: - Delete
    
    func deleteObject(object: AnyObject?) {
        guard let o = object as? NSManagedObject else { return }
        managedObjectContext.deleteObject(o)
    }
    
    func deleteObjects(objects: Array<AnyObject>?) {
        objects?.forEach { (o) in deleteObject(o) }
    }
    
    // MARK: - Core Data stack
    
    lazy var applicationDocumentsDirectory: NSURL = {
        let urls = NSFileManager.defaultManager().URLsForDirectory(.DocumentDirectory, inDomains: .UserDomainMask)
        return urls[urls.count-1]
    }()
    
    lazy var managedObjectModel: NSManagedObjectModel = {
        let modelURL = NSBundle.mainBundle().URLForResource("CoreData", withExtension: "momd")!
        return NSManagedObjectModel(contentsOfURL: modelURL)!
    }()
    
    lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator = {
        let coordinator = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
        let url = self.applicationDocumentsDirectory.URLByAppendingPathComponent("CoreData.sqlite")
        let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
        do {
            logInfo("\(#function)", info: "Connecting to database...")
            try coordinator.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: options)
            logInfo("\(#function)", info: "Database connection established")
        } catch {
            logInfo("\(#function)", info: "Connecting to database failed")
            logError("\(#function)", error: error)
            do {
                logInfo("\(#function)", info: "Removing corrupt database...")
                try NSFileManager.defaultManager().removeItemAtURL(url)
                logInfo("\(#function)", info: "Corrupt database removed")
                do {
                    logInfo("\(#function)", info: "Connecting to new database...")
                    try coordinator.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: options)
                    logInfo("\(#function)", info: "Database connection established")
                } catch {
                    logInfo("\(#function)", info: "Connecting to new database failed")
                    logError("\(#function)", error: error)
                    abort()
                }
            } catch {
                logInfo("\(#function)", info: "Removing corrupt database failed")
                logError("\(#function)", error: error)
                abort()
            }
        }
        return coordinator
    }()
    
    lazy var managedObjectContext: NSManagedObjectContext = {
        let coordinator = self.persistentStoreCoordinator
        var managedObjectContext = NSManagedObjectContext(concurrencyType: .MainQueueConcurrencyType)
        managedObjectContext.persistentStoreCoordinator = coordinator
        return managedObjectContext
    }()
    
    // MARK: - Core Data Saving support
    
    func saveContext () {
        if managedObjectContext.hasChanges {
            do {
                try managedObjectContext.save()
            } catch {
                logError("\(#function)", error: error)
                abort()
            }
        }
    }
    
}