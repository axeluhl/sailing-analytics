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
        case Competitor
        case Event
        case GPSFix
        case Leaderboard
        case Regatta
    }
    
    public class var sharedManager: CoreDataManager {
        struct Singleton {
            static let sharedManager = CoreDataManager()
        }
        return Singleton.sharedManager
    }
    
    // MARK: - Fetch
    
    func fetchRegattas() -> [Regatta]? {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName(Entities.Regatta.rawValue, inManagedObjectContext: managedObjectContext)
        var regattas: [AnyObject]?
        do {
            regattas = try managedObjectContext.executeFetchRequest(fetchRequest)
        } catch {
            print(error)
        }
        return regattas as? [Regatta]
    }
    
    func fetchRegatta(regattaData: RegattaData) -> Regatta? {
        let fetchRequest = NSFetchRequest()
        fetchRequest.entity = NSEntityDescription.entityForName(Entities.Regatta.rawValue, inManagedObjectContext: managedObjectContext)
        fetchRequest.predicate = NSPredicate(format: "event.eventID = %@ AND leaderboard.name = %@ AND competitor.competitorID = %@",
                                             regattaData.eventID,
                                             regattaData.leaderboardName,
                                             regattaData.competitorID)
        do {
            let regattas = try managedObjectContext.executeFetchRequest(fetchRequest)
            if regattas.count == 0 {
                return nil
            } else {
                return regattas[0] as? Regatta
            }
        } catch {
            print(error)
        }
        return nil
    }
    
    func regattaFetchedResultsController() -> NSFetchedResultsController {
        let fetchRequest = NSFetchRequest(entityName: Entities.Regatta.rawValue)
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "leaderboard.name", ascending: true)]
        return NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: managedObjectContext, sectionNameKeyPath: nil, cacheName: nil)
    }
    
    // MARK: - Insert
    
    func newRegatta() -> Regatta {
        return NSEntityDescription.insertNewObjectForEntityForName(Entities.Regatta.rawValue, inManagedObjectContext: managedObjectContext) as! Regatta
    }
    
    func newEvent(regatta: Regatta) -> Event {
        let event = NSEntityDescription.insertNewObjectForEntityForName(Entities.Event.rawValue, inManagedObjectContext: managedObjectContext) as! Event
        event.regatta = regatta
        return event
    }
    
    func newLeaderboard(regatta: Regatta) -> Leaderboard {
        let leaderboard = NSEntityDescription.insertNewObjectForEntityForName(Entities.Leaderboard.rawValue, inManagedObjectContext: managedObjectContext) as! Leaderboard
        leaderboard.regatta = regatta
        return leaderboard
    }
    
    func newCompetitor(regatta: Regatta) -> Competitor {
        let competitor = NSEntityDescription.insertNewObjectForEntityForName(Entities.Competitor.rawValue, inManagedObjectContext: managedObjectContext) as! Competitor
        competitor.regatta = regatta
        return competitor
    }
    
    func newGPSFix(regatta: Regatta) -> GPSFix {
        let gpsFix = NSEntityDescription.insertNewObjectForEntityForName(Entities.GPSFix.rawValue, inManagedObjectContext: managedObjectContext) as! GPSFix
        gpsFix.regatta = regatta
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
            print("Connecting to database...")
            try coordinator.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: options)
            print("Database connection established")
        } catch {
            print("Connecting to database failed: \(error)")
            do {
                print("Removing corrupt database...")
                try NSFileManager.defaultManager().removeItemAtURL(url)
                print("Corrupt database removed")
                do {
                    print("Connecting to new database")
                    try coordinator.addPersistentStoreWithType(NSSQLiteStoreType, configuration: nil, URL: url, options: options)
                    print("Database connection established")
                } catch {
                    print("Connecting to new database failed: \(error)")
                    abort()
                }
            } catch {
                print("Removing corrupt database failed: \(error)")
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
                print("Unresolved error: \(error)")
                abort()
            }
        }
    }
    
}