//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreLocation

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
    
    override func viewDidLoad() {
        
        // set values
        navigationItem.title = DataManager.sharedManager.selectedEvent!.leaderBoard!.name
        
        if DataManager.sharedManager.selectedEvent!.userImage != nil {
            imageView.image = UIImage(data:  DataManager.sharedManager.selectedEvent!.userImage!)
        } else if DataManager.sharedManager.selectedEvent!.imageUrl != nil {
            let imageUrl = NSURL(string: DataManager.sharedManager.selectedEvent!.imageUrl!)
            let urlRequest = NSURLRequest(URL: imageUrl!)
            imageView.setImageWithURLRequest(urlRequest, placeholderImage: nil, success: { (request:NSURLRequest!,response:NSHTTPURLResponse!, image:UIImage!) -> Void in
                self.imageView.image = image
                }, failure: {
                    (request:NSURLRequest!,response:NSHTTPURLResponse!, error:NSError!) -> Void in
            })
        }
        nameLabel.text = DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.displayName
        flagImageView.image = UIImage(named: DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.countryCode)
        sailLabel.text = DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.sailId
        
        // get image sources
        super.viewDidLoad()
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            sourceTypes.append(UIImagePickerControllerSourceType.Camera)
            sourceTypeNames.append("Camera")
        }
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.PhotoLibrary) {
            sourceTypes.append(UIImagePickerControllerSourceType.PhotoLibrary)
            sourceTypeNames.append("Photo Library")
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
                APIManager.sharedManager.checkOut(DataManager.sharedManager.selectedEvent!.leaderBoard!.name, competitorId: DataManager.sharedManager.selectedEvent!.leaderBoard!.competitor!.competitorId, deviceUuid: DeviceUDIDManager.UDID, toMillis: toMillis,
                    success: { (AFHTTPRequestOperation operation, AnyObject competitorResponseObject) -> Void in
                        DataManager.sharedManager.deleteEvent(DataManager.sharedManager.selectedEvent!)
                        DataManager.sharedManager.saveContext()
                        self.navigationController!.popViewControllerAnimated(true)
                    }, failure: { (AFHTTPRequestOperation operation, NSError error) -> Void in
                        let alertView = UIAlertView(title: "Couldn't check-out", message: "", delegate: nil, cancelButtonTitle: "Cancel")
                        alertView.show()
                })
                break
            }
            break
        default:
            break
        }
    }
    
}