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
        case Menu, Image
    }
    enum AlertView: Int {
        case CheckOut
    }
    
    var sourceTypes = [UIImagePickerControllerSourceType]()
    var sourceTypeNames = [String]()
    
    @IBOutlet weak var imageView: UIImageView!
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
        navigationItem.title = DataManager.sharedManager.selectedEvent!.leaderBoard!.name
        
        // set regatta image, either load it from server or load from core data
        if DataManager.sharedManager.selectedEvent!.userImage != nil {
            imageView.image = UIImage(data:  DataManager.sharedManager.selectedEvent!.userImage!)
        } else if DataManager.sharedManager.selectedEvent!.imageUrl != nil {
            let imageUrl = NSURL(string: DataManager.sharedManager.selectedEvent!.imageUrl!)
            let urlRequest = NSURLRequest(URL: imageUrl!)
            imageView.setImageWithURLRequest(urlRequest,
                placeholderImage: nil,
                success: { (request:NSURLRequest!,response:NSHTTPURLResponse!, image:UIImage!) -> Void in
                    self.imageView.image = image
                },
                failure: { (request:NSURLRequest!,response:NSHTTPURLResponse!, error:NSError!) -> Void in
                }
            )
        }
        nameLabel.text = DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.displayName
        flagImageView.image = UIImage(named: DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.countryCode)
        sailLabel.text = DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.sailId
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
        APIManager.sharedManager.initManager(DataManager.sharedManager.selectedEvent!.serverUrl)

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
        if DataManager.sharedManager.selectedEvent == nil {
            return
        }
        
        let now = NSDate()
        
        isFinished = false
        
        // reset views
        lastSyncLabel.hidden = true
        if DataManager.sharedManager.selectedEvent!.lastSyncDate != nil {
            lastSyncLabel.text = "Last sync: " + dateFormatter.stringFromDate(DataManager.sharedManager.selectedEvent!.lastSyncDate!)
        } else {
            lastSyncLabel.text = nil
        }
        startTrackingButton.setTitle("Start Tracking", forState: UIControlState.Normal)
        announcementsLabel.text = "Please listen for announcements"
      
        // finished
        if now.timeIntervalSinceDate(DataManager.sharedManager.selectedEvent!.endDate) > 0 {
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
        else if now.timeIntervalSinceDate(DataManager.sharedManager.selectedEvent!.startDate) < 0 { regattaStartLabel.text = "Regatta will start in"
            lastSyncLabel.hidden = false
            leaderBoardButtonHeight.constant = 0
            let delta = floor(now.timeIntervalSinceDate(DataManager.sharedManager.selectedEvent!.startDate)) * -1
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

    // MARK: - Menu
    
    @IBAction func showMenuActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Check-Out", "Settings", "Edit Photo", "Cancel")
        actionSheet.tag = ActionSheet.Menu.rawValue
        actionSheet.cancelButtonIndex = 3
        actionSheet.showInView(self.view)
    }
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        if actionSheet.tag == ActionSheet.Menu.rawValue {
            switch buttonIndex{
            case 0:
                showCheckOutAlertView()
            case 1:
                performSegueWithIdentifier("Settings", sender: actionSheet)
                break
            case 2:
                showImageActionSheet(actionSheet)
                break
            default:
                break
            }
        } else if actionSheet.tag == ActionSheet.Image.rawValue {
            if buttonIndex < actionSheet.cancelButtonIndex {
                imagePicker(sourceTypes[buttonIndex])
            }
        }
    }
    
    // MARK: - Start tracking
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        if isFinished {
            DataManager.sharedManager.deleteEvent(DataManager.sharedManager.selectedEvent!)
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
    
    @IBAction func showImageActionSheet(sender: AnyObject) {
        if sourceTypes.count == 1 {
            imagePicker(sourceTypes[0])
        }
        if sourceTypes.count == 2 {
            let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: sourceTypeNames[0], sourceTypeNames[1], "Cancel")
            actionSheet.tag = ActionSheet.Image.rawValue
            actionSheet.cancelButtonIndex = 2
            actionSheet.showInView(self.view)
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
        DataManager.sharedManager.selectedEvent!.userImage = UIImageJPEGRepresentation(image, 0.8)
    }
    
    // MARK: - Check-out
    func showCheckOutAlertView() {
        let alertView = UIAlertView(title: "Check-out of Regatta?", message: "", delegate: self, cancelButtonTitle: "Cancel", otherButtonTitles: "OK")
        alertView.tag = AlertView.CheckOut.rawValue;
        alertView.show()
    }
    
    func alertView(alertView: UIAlertView, clickedButtonAtIndex buttonIndex: Int) {
        switch alertView.tag {
            // Check-out
        case AlertView.CheckOut.rawValue:
            switch buttonIndex {
            case alertView.cancelButtonIndex:
                break
            default:
                let now = NSDate()
                let toMillis = round(now.timeIntervalSince1970 * 1000)
                APIManager.sharedManager.checkOut(DataManager.sharedManager.selectedEvent!.leaderBoard!.name,
                    competitorId: DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.competitorId,
                    deviceUuid: DeviceUDIDManager.UDID,
                    toMillis: toMillis,
                    success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                        DataManager.sharedManager.deleteEvent(DataManager.sharedManager.selectedEvent!)
                        DataManager.sharedManager.saveContext()
                        self.navigationController!.popViewControllerAnimated(true)
                    },
                    failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                        let alertView = UIAlertView(title: "Couldn't check-out", message: "", delegate: nil, cancelButtonTitle: "Cancel")
                        alertView.show()
                    }
                )
                break
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