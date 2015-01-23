//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreLocation
import Darwin

class RegattaViewController : UIViewController, UIActionSheetDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate, UIAlertViewDelegate {
    
    enum ActionSheet: Int {
        case Menu
    }
    enum AlertView: Int {
        case CheckOut, Image
    }
    
    var sourceTypes = [UIImagePickerControllerSourceType]()
    var sourceTypeNames = [String]()
    
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var yourTeamPhotoButton: UIButton!
    @IBOutlet weak var editTeamPhotoButton: UIButton!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var flagImageView: UIImageView!
    @IBOutlet weak var sailLabel: UILabel!
    @IBOutlet weak var regattaStartLabel: UILabel!
    @IBOutlet weak var daysHeight: NSLayoutConstraint!
    @IBOutlet weak var daysLabel: UILabel!
    @IBOutlet weak var hoursHeight: NSLayoutConstraint!
    @IBOutlet weak var hoursLabel: UILabel!
    @IBOutlet weak var minutesHeight: NSLayoutConstraint!
    @IBOutlet weak var minutesLabel: UILabel!
    @IBOutlet weak var lastSyncLabel: UILabel!
    @IBOutlet weak var leaderBoardButton: UIButton!
    @IBOutlet weak var startTrackingButton: UIButton!
    @IBOutlet weak var leaderBoardButtonHeight: NSLayoutConstraint!
    @IBOutlet weak var announcementsLabel: PaddedLabel!
    
    var dateFormatter: NSDateFormatter
    
    var isFinished: Bool = false
    let secondsInDay: Double = 60 * 60 * 24
    let secondsInHour: Double = 60 * 60
    var loop: NSTimer?
    
    /* Setup date formatter for last sync. */
    required init(coder aDecoder: NSCoder) {
        dateFormatter = NSDateFormatter()
        dateFormatter.timeStyle = NSDateFormatterStyle.ShortStyle
        dateFormatter.dateStyle = NSDateFormatterStyle.MediumStyle
        super.init(coder: aDecoder)
    }
    
    override func viewDidLoad() {
        
        // set values
        navigationItem.title = DataManager.sharedManager.selectedCheckIn!.leaderBoardName
        
        // set regatta image, either load it from server or load from core data
        if DataManager.sharedManager.selectedCheckIn?.userImage? != nil {
            imageView.image = UIImage(data:  DataManager.sharedManager.selectedCheckIn!.userImage!)
            self.yourTeamPhotoButton.hidden = true
        } else if DataManager.sharedManager.selectedCheckIn?.imageUrl? != nil {
            let imageUrl = NSURL(string: DataManager.sharedManager.selectedCheckIn!.imageUrl!)
            let urlRequest = NSURLRequest(URL: imageUrl!)
            imageView.setImageWithURLRequest(urlRequest,
                placeholderImage: nil,
                success: { (request:NSURLRequest!,response:NSHTTPURLResponse!, image:UIImage!) -> Void in
                    self.imageView.image = image
                    self.yourTeamPhotoButton.hidden = true
                },
                failure: { (request:NSURLRequest!,response:NSHTTPURLResponse!, error:NSError!) -> Void in
                    self.editTeamPhotoButton.hidden = true
                }
            )
        } else {
            self.editTeamPhotoButton.hidden = true
        }
        if (DataManager.sharedManager.selectedCheckIn?.competitor? != nil) {
            let competitor = DataManager.sharedManager.selectedCheckIn!.competitor!
            nameLabel.text = competitor.name
            flagImageView.image = UIImage(named: competitor.countryCode)
            sailLabel.text = competitor.sailId
        }
        checkRegattaStatus()
        
        // get image sources
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            sourceTypes.append(UIImagePickerControllerSourceType.Camera)
            sourceTypeNames.append("Camera")
        }
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.PhotoLibrary) {
            sourceTypes.append(UIImagePickerControllerSourceType.PhotoLibrary)
            sourceTypeNames.append("Photo Library")
        }
        
        // point to events API server
        APIManager.sharedManager.initManager(DataManager.sharedManager.selectedCheckIn!.serverUrl)
        
        super.viewDidLoad()
    }
    
    override func viewDidAppear(animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    override func viewWillDisappear(animated: Bool) {
        super.viewWillDisappear(animated)
        loop?.invalidate()
    }
    
    override func didRotateFromInterfaceOrientation(fromInterfaceOrientation: UIInterfaceOrientation) {
        checkRegattaStatus()
    }
    
    // MARK: -
    func checkRegattaStatus() {
        if DataManager.sharedManager.selectedCheckIn == nil {
            return
        }
        
        let now = NSDate()
        
        isFinished = false
        
        // reset views
        lastSyncLabel.hidden = true
        if DataManager.sharedManager.selectedCheckIn!.lastSyncDate != nil {
            lastSyncLabel.text = "Last sync: " + dateFormatter.stringFromDate(DataManager.sharedManager.selectedCheckIn!.lastSyncDate!)
        } else {
            lastSyncLabel.text = nil
        }
        startTrackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
        announcementsLabel.text = "Please listen for announcements"
        
        if DataManager.sharedManager.selectedCheckIn?.event? != nil {
            let event = DataManager.sharedManager.selectedCheckIn!.event!
            // finished
            if now.timeIntervalSinceDate(event.endDate) > 0 {
                isFinished = true
                regattaStartLabel.text = "Thank you for participating!"
                leaderBoardButtonHeight.constant = ButtonHeight.smallButtonPortrait
                daysHeight.constant = 0
                hoursHeight.constant = 0
                minutesHeight.constant = 0
                startTrackingButton.setTitle("Check-Out", forState: UIControlState.Normal)
                startTrackingButton.backgroundColor = UIColor(hex: 0xEFAD00)
                announcementsLabel.text = " "
            }
                // before race
            else if now.timeIntervalSinceDate(event.startDate) < 0 {
                regattaStartLabel.text = "Regatta will start in"
                lastSyncLabel.hidden = false
                leaderBoardButtonHeight.constant = 0
                let delta = floor(now.timeIntervalSinceDate(event.startDate)) * -1
                let days = floor(delta / secondsInDay)
                let hours = floor((delta - days * secondsInDay) / secondsInHour)
                let minutes = floor((delta - days * secondsInDay - hours * secondsInHour) / 60.0)
                daysLabel.text = String(format: "%.0f", arguments: [days])
                hoursLabel.text = String(format: "%.0f", arguments: [hours])
                minutesLabel.text = String(format: "%.0f", arguments: [minutes])
                loop?.invalidate()
                loop = NSTimer(timeInterval: 60, target: self, selector: "checkRegattaStatus", userInfo: nil, repeats: false)
                NSRunLoop.currentRunLoop().addTimer(loop!, forMode:NSRunLoopCommonModes)
            }
                // during race
            else {
                regattaStartLabel.text = "Regatta in progress"
                daysHeight.constant = 0
                hoursHeight.constant = 0
                minutesHeight.constant = 0
                leaderBoardButtonHeight.constant = ButtonHeight.smallButtonPortrait
                lastSyncLabel.hidden = false
            }
        }
    }
    
    // MARK: - Menu
    
    @IBAction func showMenuActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Check-Out", "Settings", "Edit Photo", "Cancel")
        actionSheet.tag = ActionSheet.Menu.rawValue
        actionSheet.cancelButtonIndex = 3
        actionSheet.showInView(self.view)
    }
    
    // MARK: - UIActionSheetDelegate
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        if actionSheet.tag == ActionSheet.Menu.rawValue {
            switch buttonIndex{
            case 0:
                showCheckOutAlertView()
            case 1:
                performSegueWithIdentifier("Settings", sender: actionSheet)
                break
            case 2:
                showImageAlertView(actionSheet)
                break
            default:
                break
            }
        }
    }
    
    // MARK: - Start tracking
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        if isFinished {
            DataManager.sharedManager.deleteCheckIn(DataManager.sharedManager.selectedCheckIn!)
            DataManager.sharedManager.saveContext()
            navigationController!.popViewControllerAnimated(true)
            return
        }
        let errorMessage = LocationManager.sharedManager.startTracking()
        if errorMessage != nil {
            let alertView = UIAlertView(title: errorMessage, message: nil, delegate: nil, cancelButtonTitle: "Cancel")
            alertView.show()
        } else {
            performSegueWithIdentifier("Tracking", sender: sender)
        }
    }
    
    // MARK: - Image picker
    
    @IBAction func showImageAlertView(sender: AnyObject) {
        if sourceTypes.count == 1 {
            imagePicker(sourceTypes[0])
        }
        if sourceTypes.count == 2 {
            let alertView = UIAlertView(title: "Select a photo for your team", message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: sourceTypeNames[0], sourceTypeNames[1])
            alertView.tag = AlertView.Image.rawValue;
            alertView.show()
        }
    }
    
    func imagePicker(sourceType: UIImagePickerControllerSourceType) {
        let imagePickerController = UIImagePickerController()
        imagePickerController.delegate = self
        imagePickerController.sourceType = sourceType;
        imagePickerController.mediaTypes = [kUTTypeImage];
        imagePickerController.allowsEditing = false
        presentViewController(imagePickerController, animated: true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController!, didFinishPickingImage image: UIImage!, editingInfo: NSDictionary!) {
        dismissViewControllerAnimated(true, completion: nil)
        imageView.image = image
        yourTeamPhotoButton.hidden = true
        editTeamPhotoButton.hidden = false
        DataManager.sharedManager.selectedCheckIn!.userImage = UIImageJPEGRepresentation(image, 0.8)
    }
    
    // MARK: - Check-out
    func showCheckOutAlertView() {
        let alertView = UIAlertView(title: "Check-out of Regatta?", message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "OK")
        alertView.tag = AlertView.CheckOut.rawValue;
        alertView.show()
    }
    
    // MARK: - UIAlertViewDelegate
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Check-out
        case AlertView.CheckOut.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break
            default:
                let now = NSDate()
                let toMillis = Int64(now.timeIntervalSince1970 * 1000)
                APIManager.sharedManager.checkOut(DataManager.sharedManager.selectedCheckIn!.leaderBoardName,
                    competitorId: DataManager.sharedManager.selectedCheckIn!.competitorId,
                    deviceUuid: DeviceUDIDManager.UDID,
                    toMillis: toMillis,
                    success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                    },
                    failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                    }
                )
                DataManager.sharedManager.deleteCheckIn(DataManager.sharedManager.selectedCheckIn!)
                DataManager.sharedManager.saveContext()
                self.navigationController!.popViewControllerAnimated(true)
                break
            }
            break
        case AlertView.Image.rawValue:
            if buttonIndex != alertView.cancelButtonIndex {
                imagePicker(sourceTypes[buttonIndex - 1])
            }
            break
        default:
            break
        }
    }
    
    func navigationController(navigationController: UINavigationController, willShowViewController viewController: UIViewController, animated: Bool) {
        UIApplication.sharedApplication().statusBarStyle = UIStatusBarStyle.LightContent
    }
}