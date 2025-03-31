//
//  CoreDataManager.swift
//  SAPTracker
//
//  Created by computing on 21/10/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreData

class CoreDataManager: NSObject {
    
    fileprivate enum Entities: String {
        case BoatCheckIn
        case CheckIn
        case CompetitorCheckIn
        case Event
        case GPSFix
        case Leaderboard
        case MarkCheckIn
    }
    
    let name: String
    
    init(name: String) {
        self.name = name
        super.init()
    }

    class var shared: CoreDataManager {
        struct Singleton {
            static let shared = CoreDataManager(name: "CoreData")
        }
        return Singleton.shared
    }

    // MARK: - Fetch
    
    func fetchCheckIns() -> [CheckIn]? {
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>()
        fetchRequest.entity = NSEntityDescription.entity(forEntityName: Entities.CheckIn.rawValue, in: managedObjectContext)
        fetchRequest.includesSubentities = true
        var checkIns: [AnyObject]?
        do {
            checkIns = try managedObjectContext.fetch(fetchRequest)
        } catch {
            logError(name: "\(#function)", error: error)
        }
        return checkIns as? [CheckIn]
    }
    
    func fetchCheckIn(checkInData: CheckInData) -> CheckIn? {
        switch checkInData.type {
        case .boat:
            return fetchBoatCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                boatID: checkInData.boatID!
            )
        case .competitor:
            return fetchCompetitorCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                competitorID: checkInData.competitorID!
            )
        case .mark:
            return fetchMarkCheckIn(
                eventID: checkInData.eventID,
                leaderboardName: checkInData.leaderboardName,
                markID: checkInData.markID!
            )
        }
    }

    func fetchBoatCheckIn(eventID: String, leaderboardName: String, boatID: String) -> BoatCheckIn? {
        let fetchRequest = NSFetchRequest<BoatCheckIn>()
        fetchRequest.entity = NSEntityDescription.entity(
            forEntityName: Entities.BoatCheckIn.rawValue,
            in: managedObjectContext
        )
        fetchRequest.predicate = NSPredicate(
            format: "event.eventID = %@ AND leaderboard.name = %@ AND boatID = %@",
            eventID,
            leaderboardName,
            boatID
        )
        do {
            let checkIns = try managedObjectContext.fetch(fetchRequest)
            if checkIns.count == 0 {
                return nil
            } else {
                return checkIns[0]
            }
        } catch {
            logError(name: "\(#function)", error: error)
        }
        return nil
    }

    func fetchCompetitorCheckIn(serverURL: String, boatClassName: String) -> [CompetitorCheckIn]? {
        let fetchRequest = NSFetchRequest<NSFetchRequestResult>()
        fetchRequest.entity = NSEntityDescription.entity(forEntityName: Entities.CompetitorCheckIn.rawValue, in: managedObjectContext)
        fetchRequest.includesSubentities = true
        fetchRequest.predicate = NSPredicate(format: "serverURL = %@ AND boatClassName = %@", serverURL, boatClassName)
        var checkIns: [AnyObject]?
        do {
            checkIns = try managedObjectContext.fetch(fetchRequest)
        } catch {
            logError(name: "\(#function)", error: error)
        }
        return checkIns as? [CompetitorCheckIn]
    }
    
    func fetchCompetitorCheckIn(eventID: String, leaderboardName: String, competitorID: String) -> CompetitorCheckIn? {
        let fetchRequest = NSFetchRequest<CompetitorCheckIn>()
        fetchRequest.entity = NSEntityDescription.entity(
            forEntityName: Entities.CompetitorCheckIn.rawValue,
            in: managedObjectContext
        )
        fetchRequest.predicate = NSPredicate(
            format: "event.eventID = %@ AND leaderboard.name = %@ AND competitorID = %@",
            eventID,
            leaderboardName,
            competitorID
        )
        do {
            let checkIns = try managedObjectContext.fetch(fetchRequest)
            if checkIns.count == 0 {
                return nil
            } else {
                return checkIns[0]
            }
        } catch {
            logError(name: "\(#function)", error: error)
        }
        return nil
    }

    func fetchMarkCheckIn(eventID: String, leaderboardName: String, markID: String) -> MarkCheckIn? {
        let fetchRequest = NSFetchRequest<MarkCheckIn>()
        fetchRequest.entity = NSEntityDescription.entity(
            forEntityName: Entities.MarkCheckIn.rawValue,
            in: managedObjectContext
        )
        fetchRequest.predicate = NSPredicate(
            format: "event.eventID = %@ AND leaderboard.name = %@ AND markID = %@",
            eventID,
            leaderboardName,
            markID
        )
        do {
            let checkIns = try managedObjectContext.fetch(fetchRequest)
            if checkIns.count == 0 {
                return nil
            } else {
                return checkIns[0]
            }
        } catch {
            logError(name: "\(#function)", error: error)
        }
        return nil
    }

    func checkInFetchedResultsController() -> NSFetchedResultsController<CheckIn> {
        let fetchRequest = NSFetchRequest<CheckIn>(entityName: Entities.CheckIn.rawValue)
        fetchRequest.predicate = NSPredicate(format: "event != nil AND leaderboard != nil")
        fetchRequest.sortDescriptors = [NSSortDescriptor(key: "leaderboard.name", ascending: true)]
        return NSFetchedResultsController(fetchRequest: fetchRequest, managedObjectContext: managedObjectContext, sectionNameKeyPath: nil, cacheName: nil)
    }
    
    // MARK: - Insert

    func newBoatCheckIn() -> BoatCheckIn {
        let checkIn = NSEntityDescription.insertNewObject(forEntityName: Entities.BoatCheckIn.rawValue, into: managedObjectContext) as! BoatCheckIn
        checkIn.event = newEvent(checkIn: checkIn)
        checkIn.leaderboard = newLeaderboard(checkIn: checkIn)
        return checkIn
    }

    func newCompetitorCheckIn() -> CompetitorCheckIn {
        let checkIn = NSEntityDescription.insertNewObject(forEntityName: Entities.CompetitorCheckIn.rawValue, into: managedObjectContext) as! CompetitorCheckIn
        checkIn.event = newEvent(checkIn: checkIn)
        checkIn.leaderboard = newLeaderboard(checkIn: checkIn)
        return checkIn
    }

    func newMarkCheckIn() -> MarkCheckIn {
        let checkIn = NSEntityDescription.insertNewObject(forEntityName: Entities.MarkCheckIn.rawValue, into: managedObjectContext) as! MarkCheckIn
        checkIn.event = newEvent(checkIn: checkIn)
        checkIn.leaderboard = newLeaderboard(checkIn: checkIn)
        return checkIn
    }

    func newEvent(checkIn: CheckIn) -> Event {
        let event = NSEntityDescription.insertNewObject(forEntityName: Entities.Event.rawValue, into: managedObjectContext) as! Event
        event.checkIn = checkIn
        return event
    }
    
    func newLeaderboard(checkIn: CheckIn) -> Leaderboard {
        let leaderboard = NSEntityDescription.insertNewObject(forEntityName: Entities.Leaderboard.rawValue, into: managedObjectContext) as! Leaderboard
        leaderboard.checkIn = checkIn
        return leaderboard
    }

    func newGPSFix(checkIn: CheckIn) -> GPSFix {
        let gpsFix = NSEntityDescription.insertNewObject(forEntityName: Entities.GPSFix.rawValue, into: managedObjectContext) as! GPSFix
        gpsFix.checkIn = checkIn
        return gpsFix
    }

    // MARK: - Delete
    
    func deleteObject(object: AnyObject?) {
        guard let o = object as? NSManagedObject else { return }
        managedObjectContext.delete(o)
    }
    
    func deleteObjects(objects: Array<AnyObject>?) {
        objects?.forEach { (o) in deleteObject(object: o) }
    }
    
    // MARK: - Core Data stack
    
    lazy var applicationDocumentsDirectory: URL = {
        let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        return urls[urls.count-1]
    }()
    
    lazy var managedObjectModel: NSManagedObjectModel = {
        let modelURL = Bundle.main.url(forResource: "CoreData", withExtension: "momd")!
        return NSManagedObjectModel(contentsOf: modelURL)!
    }()
    
    lazy var persistentStoreCoordinator: NSPersistentStoreCoordinator = {
        let coordinator = NSPersistentStoreCoordinator(managedObjectModel: self.managedObjectModel)
        let url = self.applicationDocumentsDirectory.appendingPathComponent("\(self.name).sqlite")
        let options = [NSMigratePersistentStoresAutomaticallyOption: true, NSInferMappingModelAutomaticallyOption: true]
        do {
            logInfo(name: "\(#function)", info: "Connecting to database...")
            try coordinator.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: url, options: options)
            logInfo(name: "\(#function)", info: "Database connection established")
        } catch {
            logInfo(name: "\(#function)", info: "Connecting to database failed")
            logError(name: "\(#function)", error: error)
            do {
                logInfo(name: "\(#function)", info: "Removing corrupt database...")
                try FileManager.default.removeItem(at: url)
                logInfo(name: "\(#function)", info: "Corrupt database removed")
                do {
                    logInfo(name: "\(#function)", info: "Connecting to new database...")
                    try coordinator.addPersistentStore(ofType: NSSQLiteStoreType, configurationName: nil, at: url, options: options)
                    logInfo(name: "\(#function)", info: "Database connection established")
                } catch {
                    logInfo(name: "\(#function)", info: "Connecting to new database failed")
                    logError(name: "\(#function)", error: error)
                    abort()
                }
            } catch {
                logInfo(name: "\(#function)", info: "Removing corrupt database failed")
                logError(name: "\(#function)", error: error)
                abort()
            }
        }
        return coordinator
    }()
    
    lazy var managedObjectContext: NSManagedObjectContext = {
        let coordinator = self.persistentStoreCoordinator
        var managedObjectContext = NSManagedObjectContext(concurrencyType: .mainQueueConcurrencyType)
        managedObjectContext.persistentStoreCoordinator = coordinator
        return managedObjectContext
    }()
    
    // MARK: - Core Data Saving support
    
    func saveContext () {
        if managedObjectContext.hasChanges {
            do {
                try managedObjectContext.save()
            } catch {
                logError(name: "\(#function)", error: error)
                abort()
            }
        }
    }
    
}
