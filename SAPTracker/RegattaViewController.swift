//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation
import CoreLocation

class RegattaViewController : UIViewController, UIActionSheetDelegate, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
    
    enum ActionSheet: Int {
        case Menu, Image
    }
    var sourceTypes = [UIImagePickerControllerSourceType]()
    var sourceTypeNames = [String]()
    
    // var regatta: Regatta
    
    @IBOutlet weak var imageView: UIImageView!
    @IBOutlet weak var nameLabel: UILabel!
    @IBOutlet weak var flagImageView: UIImageView!
    @IBOutlet weak var sailLabel: UILabel!
    
    override func viewDidLoad() {
    
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
    
    @IBAction func showMenuActionSheet(sender: AnyObject) {
        let actionSheet = UIActionSheet(title: nil, delegate: self, cancelButtonTitle: nil, destructiveButtonTitle: nil, otherButtonTitles: "Checkout", "Settings", "Edit Photo", "Cancel")
        actionSheet.tag = ActionSheet.Menu.rawValue
        actionSheet.cancelButtonIndex = 3
        actionSheet.showInView(self.view)
    }
    
    func actionSheet(actionSheet: UIActionSheet!, clickedButtonAtIndex buttonIndex: Int) {
        if actionSheet.tag == ActionSheet.Menu.rawValue {
            switch buttonIndex{
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
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        let errorMessage = LocationManager.sharedManager.startTracking()
        if errorMessage != nil {
            let alertView = UIAlertView(title: errorMessage, message: nil, delegate: nil, cancelButtonTitle: "Cancel")
            alertView.show()
        } else {
            performSegueWithIdentifier("Tracking", sender: sender)
        }
    }
    
    // MARK:- Image picker
    
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
        let selectedImage : UIImage = image
        imageView.image = image
    }
}