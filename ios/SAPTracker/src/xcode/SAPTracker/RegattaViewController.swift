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
        case CheckOut, Image, UploadFailed
    }
	
	struct RegattaViewUserDefaults {
		static let UploadDidFail = "UploadDidFail"
	}
	    
    var sourceTypes = [UIImagePickerControllerSourceType]()
    var sourceTypeNames = [String]()
    
    @IBOutlet weak var teamImageView: UIImageView!
    @IBOutlet weak var teamImageAddButton: UIButton!
    @IBOutlet weak var teamImageEditButton: UIButton!
	@IBOutlet weak var teamImageRetryButton: UIButton!
    @IBOutlet weak var competitorNameLabel: UILabel!
    @IBOutlet weak var competitorFlagImageView: UIImageView!
    @IBOutlet weak var competitorSailLabel: UILabel!
    @IBOutlet weak var regattaStartLabel: UILabel!
    @IBOutlet weak var countdownView: UIView!
    @IBOutlet weak var countdownViewHeight: NSLayoutConstraint!
    @IBOutlet weak var countdownDaysLabel: UILabel!
    @IBOutlet weak var countdownHoursLabel: UILabel!
    @IBOutlet weak var countdownMinutesLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var eventButton: UIButton!
    @IBOutlet weak var startTrackingButton: UIButton!
    @IBOutlet weak var announcementsLabel: UILabel!
    
    let secondsInDay: Double = 60 * 60 * 24
    let secondsInHour: Double = 60 * 60
    var timer: NSTimer?
	
	var uploadKey: String {
		return RegattaViewUserDefaults.UploadDidFail + "_" + DataManager.sharedManager.selectedCheckIn!.competitorId
	}
    
    override func viewDidLoad() {
        
        // set values
        navigationItem.title = DataManager.sharedManager.selectedCheckIn!.leaderBoardName

        self.eventButton.setTitle(NSLocalizedString("Show Event", comment: ""), forState: UIControlState.Normal)
		
		self.teamImageRetryButton.hidden = !NSUserDefaults.standardUserDefaults().boolForKey(uploadKey)
        
        // Set regatta image, either load it from server or load from core data
        if DataManager.sharedManager.selectedCheckIn?.userImage != nil {
            self.teamImageView.image = UIImage(data:  DataManager.sharedManager.selectedCheckIn!.userImage!)
            self.teamImageAddButton.hidden = true
        } else if DataManager.sharedManager.selectedCheckIn?.imageUrl != nil {
            let imageUrl = NSURL(string: DataManager.sharedManager.selectedCheckIn!.imageUrl!)
            let urlRequest = NSURLRequest(URL: imageUrl!)
            self.teamImageView.setImageWithURLRequest(urlRequest,
                                                      placeholderImage: nil,
                                                      success: { (request:NSURLRequest!,response:NSHTTPURLResponse!, image:UIImage!) -> Void in
                    self.teamImageView.image = image
                    self.teamImageAddButton.hidden = true
                },
                failure: { (request:NSURLRequest!,response:NSHTTPURLResponse!, error:NSError!) -> Void in
                    self.teamImageEditButton.hidden = true
					self.teamImageAddButton.hidden = !self.teamImageRetryButton.hidden
                }
            )
        } else {
            self.teamImageEditButton.hidden = true
			self.teamImageAddButton.hidden = !self.teamImageRetryButton.hidden
        }
        if (DataManager.sharedManager.selectedCheckIn?.competitor != nil) {
            let competitor = DataManager.sharedManager.selectedCheckIn!.competitor!
            self.competitorNameLabel.text = competitor.name
            self.competitorFlagImageView.image = UIImage(named: competitor.countryCode)
            self.competitorSailLabel.text = competitor.sailId
        }
        checkRegattaStatus()
        
        // get image sources
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            sourceTypes.append(UIImagePickerControllerSourceType.Camera)
            sourceTypeNames.append(NSLocalizedString("Camera", comment: ""))
        }
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.PhotoLibrary) {
            sourceTypes.append(UIImagePickerControllerSourceType.PhotoLibrary)
            sourceTypeNames.append(NSLocalizedString("Photo Library", comment: ""))
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
        timer?.invalidate()
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
        
        // reset views
        startTrackingButton.setTitle(NSLocalizedString("Start Tracking", comment: ""), forState: UIControlState.Normal)
        announcementsLabel.text = NSLocalizedString("Please listen for announcements", comment: "")
        
        if DataManager.sharedManager.selectedCheckIn?.event != nil {
            let event = DataManager.sharedManager.selectedCheckIn!.event!
            
            // before race
            if now.timeIntervalSinceDate(event.startDate) < 0 {
                self.regattaStartLabel.text = NSLocalizedString("Regatta will start in", comment: "")

                let delta = floor(now.timeIntervalSinceDate(event.startDate)) * -1
                let days = floor(delta / secondsInDay)
                let hours = floor((delta - days * secondsInDay) / secondsInHour)
                let minutes = floor((delta - days * secondsInDay - hours * secondsInHour) / 60.0)
                self.countdownDaysLabel.text = String(format: "%.0f", arguments: [days])
                self.countdownHoursLabel.text = String(format: "%.0f", arguments: [hours])
                self.countdownMinutesLabel.text = String(format: "%.0f", arguments: [minutes])
                self.countdownView.hidden = false
                self.countdownViewHeight.constant = 60
                timer?.invalidate()
                timer = NSTimer(timeInterval: 60,
                                target: self,
                                selector: #selector(RegattaViewController.checkRegattaStatus),
                                userInfo: nil,
                                repeats: false)
                NSRunLoop.currentRunLoop().addTimer(timer!, forMode:NSRunLoopCommonModes)
            }
                // during race
            else {
                self.regattaStartLabel.text = NSLocalizedString("Regatta in progress", comment: "")
                self.countdownView.hidden = true
                self.countdownViewHeight.constant = 0
            }
        }
    }
    
    @IBAction func showEvent(sender: UIButton) {
        let serverUrl = DataManager.sharedManager.selectedCheckIn!.serverUrl
        let eventId = DataManager.sharedManager.selectedCheckIn!.eventId

        let url = "\(serverUrl)/gwt/Home.html?navigationTab=Regattas#EventPlace:eventId=\(eventId)"
        UIApplication.sharedApplication().openURL(NSURL(string: url)!)
    }


    // MARK: - Menu
    
    @IBAction func showMenuActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Check-Out", "Settings", "Edit Photo", "Cancel")
        actionSheet.tag = ActionSheet.Menu.rawValue
        actionSheet.cancelButtonIndex = 3
        actionSheet.showInView(self.view)
    }
    
    // MARK: - UIActionSheetDelegate
    
    func actionSheet(actionSheet: UIActionSheet, clickedButtonAtIndex buttonIndex: Int) {
        if actionSheet.tag == ActionSheet.Menu.rawValue {
            switch buttonIndex{
            case 0:
                showCheckOutAlertView()
            case 1:
				dispatch_after(dispatch_time(DISPATCH_TIME_NOW, Int64(0.6 * Double(NSEC_PER_SEC))), dispatch_get_main_queue()) { () -> Void in
					self.performSegueWithIdentifier("Settings", sender: actionSheet)
				}
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
        do {
            try LocationManager.sharedManager.startTracking()
            performSegueWithIdentifier("Tracking", sender: sender)
        } catch let error as LocationManager.TrackingError {
            let title = error.description
            let cancelButtonTitle = NSLocalizedString("Cancel", comment: "")
            let alertView = UIAlertView(title: title,
                                        message: nil,
                                        delegate: nil,
                                        cancelButtonTitle: cancelButtonTitle)
            alertView.show()
        } catch {
            print("Unknown error")
        }
    }
    
    // MARK: - Image picker
    
    @IBAction func showImageAlertView(sender: AnyObject) {
        if sourceTypes.count == 1 {
            imagePicker(sourceTypes[0])
        }
        if sourceTypes.count == 2 {
            let alertView = UIAlertView(title: NSLocalizedString("Select a photo for your team", comment: ""), message: "", delegate: self, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""), otherButtonTitles: sourceTypeNames[0], sourceTypeNames[1])
            alertView.tag = AlertView.Image.rawValue;
            alertView.show()
        }
    }
    
    func imagePicker(sourceType: UIImagePickerControllerSourceType) {
        let imagePickerController = UIImagePickerController()
        imagePickerController.delegate = self
        imagePickerController.sourceType = sourceType;
        imagePickerController.mediaTypes = [kUTTypeImage as String];
        imagePickerController.allowsEditing = false
        presentViewController(imagePickerController, animated: true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingImage image: UIImage!, editingInfo: [NSObject : AnyObject]!) {
        dismissViewControllerAnimated(true, completion: nil)
        self.teamImageView.image = image
        self.teamImageAddButton.hidden = true
        self.teamImageEditButton.hidden = false
		self.teamImageRetryButton.hidden = true
        let jpegData = UIImageJPEGRepresentation(image, 0.8)
        DataManager.sharedManager.selectedCheckIn!.userImage =  jpegData
        APIManager.sharedManager.postTeamImage(DataManager.sharedManager.selectedCheckIn!.competitorId,
            imageData: jpegData,
            success: { (responseObject) -> Void in
                // http://wiki.sapsailing.com/wiki/tracking-app/api-v1#Competitor-Information-%28in-general%29
                // "Additional Notes: Competitor profile image left out for now."
                let responseDictionary = responseObject as![String: AnyObject]
                let imageUrl = (responseDictionary["teamImageUri"]) as! String;
                DataManager.sharedManager.selectedCheckIn!.imageUrl = imageUrl;
				
				NSUserDefaults.standardUserDefaults().setBool(false, forKey: self.uploadKey)
				NSUserDefaults.standardUserDefaults().synchronize()
            },
            failure: { (error) -> Void in
                let alertView = UIAlertView(title: NSLocalizedString("Failed to upload image", comment: ""), message: error.localizedDescription, delegate: self, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""))
                alertView.tag = AlertView.UploadFailed.rawValue;
				alertView.show()
				
				NSUserDefaults.standardUserDefaults().setBool(true, forKey: self.uploadKey)
				NSUserDefaults.standardUserDefaults().synchronize()
				self.teamImageRetryButton.hidden = false
        })
    }
		
    // MARK: - Check-out
    func showCheckOutAlertView() {
        let alertView = UIAlertView(title: NSLocalizedString("Check-out of Regatta?", comment: ""), message: "", delegate: self, cancelButtonTitle: NSLocalizedString("Cancel", comment: ""), otherButtonTitles: NSLocalizedString("OK", comment: ""))
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
                    success: { (operation, competitorResponseObject) -> Void in
                    },
                    failure: { (operation, error) -> Void in
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
    
}