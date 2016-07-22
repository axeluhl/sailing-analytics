//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class RegattaViewController : UIViewController, UINavigationControllerDelegate {
    
    private struct TeamImageKeys {
        static let TeamImageURL = "teamImageUri"
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
    
    var regatta: Regatta!
    var requestManager: RequestManager!
    
    var countdownTimer: NSTimer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        requestManager = RequestManager(baseURLString: regatta.serverURL)
        setupButtons()
        setupCompetitor()
        setupCountdownTimer()
        setupNavigationBar()
        setupImageSourceTypes()
        setupTeamImage()
        
        // Update
        regattaController.update()
    }
    
    // MARK: - Setups
    
    private func setupButtons() {
        eventButton.setBackgroundImage(Images.BlueHighlighted, forState: .Highlighted)
        leaderboardButton.setBackgroundImage(Images.BlueHighlighted, forState: .Highlighted)
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, forState: .Highlighted)
    }
    
    private func setupCompetitor() {
        competitorNameLabel.text = regatta.competitor.name
        competitorFlagImageView.image = UIImage(named: regatta.competitor.countryCode)
        competitorSailLabel.text = regatta.competitor.sailID
    }
    
    private func setupCountdownTimer() {
        countdownTimer?.invalidate()
        countdownTimer = NSTimer.scheduledTimerWithTimeInterval(60,
                                                                target: self,
                                                                selector: #selector(RegattaViewController.refreshCountdown),
                                                                userInfo: nil,
                                                                repeats: true
        )
        refreshCountdown()
    }
    
    private func setupImageSourceTypes() {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.Camera) {
            sourceTypes.append(UIImagePickerControllerSourceType.Camera)
            sourceTypeNames.append(NSLocalizedString("Camera", comment: ""))
        }
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.PhotoLibrary) {
            sourceTypes.append(UIImagePickerControllerSourceType.PhotoLibrary)
            sourceTypeNames.append(NSLocalizedString("Photo Library", comment: ""))
        }
    }
    
    private func setupNavigationBar() {
        navigationItem.title = regatta.leaderboard.name
    }
    
    private func setupTeamImage() {
        if regatta.teamImageData != nil {
            setupTeamImageWithImageData(regatta.teamImageData)
        } else if regatta.teamImageURL != nil {
            setupTeamImageWithURL(regatta.teamImageURL)
        } else {
            setupTeamImageFinished()
        }
    }
    
    private func setupTeamImageWithImageData(data: NSData!) {
        teamImageView.image = UIImage(data: data)
        setupTeamImageFinished()
    }
    
    private func setupTeamImageWithURL(urlString: String!) {
        if let url = NSURL(string: urlString) {
            teamImageView.setImageWithURLRequest(NSURLRequest(URL: url),
                                                 placeholderImage: nil,
                                                 success: { (request, response, image) in self.setupTeamImageWithURLSuccess(image) },
                                                 failure: { (request, response, error) in self.setupTeamImageWithURLFailure() })
        } else {
            setupTeamImageFinished()
        }
    }
    
    private func setupTeamImageWithURLSuccess(image: UIImage) {
        teamImageView.image = image
        regatta.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        regatta.teamImageRetry = false
        CoreDataManager.sharedManager.saveContext()
        setupTeamImageFinished()
    }
    
    private func setupTeamImageWithURLFailure() {
        setupTeamImageFinished()
    }
    
    private func setupTeamImageFinished() {
        if regatta.teamImageData != nil {
            teamImageAddButton.hidden = true
            teamImageEditButton.hidden = regatta.teamImageRetry
            teamImageRetryButton.hidden = !teamImageEditButton.hidden
        } else {
            teamImageAddButton.hidden = false
            teamImageEditButton.hidden = true
            teamImageRetryButton.hidden = true
        }
    }
    
    // MARK: - Refresh
    
    func refreshCountdown() {
        if regatta.event.startDate - NSDate().timeIntervalSince1970 > 0 {
            regattaStartLabel.text = NSLocalizedString("Regatta will start in", comment: "")
            let duration = regatta.event.startDate - NSDate().timeIntervalSince1970
            let days = Int(duration / (60 * 60 * 24))
            let hours = Int(duration / (60 * 60)) - (days * 24)
            let minutes = Int(duration / 60) - (days * 24 * 60) - (hours * 60)
            countdownDaysLabel.text = String(format: "%02d", days)
            countdownHoursLabel.text = String(format: "%02d", hours)
            countdownMinutesLabel.text = String(format: "%02d", minutes)
            countdownView.hidden = false
            countdownViewHeight.constant = 60
        } else {
            regattaStartLabel.text = NSLocalizedString("Regatta in progress", comment: "")
            countdownView.hidden = true
            countdownViewHeight.constant = 0
        }
    }
    
    // MARK: - Actions
    
    @IBAction func teamImageAddButtonTapped(sender: AnyObject) {
        showImageAlertController()
    }
    
    @IBAction func teamImageEditButtonTapped(sender: AnyObject) {
        showImageAlertController()
    }
    
    @IBAction func teamImageRetryButtonTapped(sender: AnyObject) {
        postTeamImageData(regatta.teamImageData)
    }
    
    @IBAction func eventButtonTapped(sender: UIButton) {
        // FIXME: - Alert?
        guard let url = regatta.eventURL() else { return }
        UIApplication.sharedApplication().openURL(url)
    }
    
    @IBAction func showMenuActionSheet(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let checkOutTitle = NSLocalizedString("Check-Out", comment: "")
        let checkOutAction = UIAlertAction(title: checkOutTitle, style: .Default) { (action) in
            self.showCheckOutAlertController()
        }
        let settingsTitle = NSLocalizedString("Settings", comment: "")
        let settingsAction = UIAlertAction(title: settingsTitle, style: .Default) { (action) in
            self.performSegueWithIdentifier("Settings", sender: self)
        }
        let editPhotoTitle = NSLocalizedString("Replace Team Photo", comment: "")
        let editPhotoAction = UIAlertAction(title: editPhotoTitle, style: .Default) { (action) in
            self.showImageAlertController()
        }
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        alertController.addAction(checkOutAction)
        alertController.addAction(settingsAction)
        alertController.addAction(editPhotoAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        do {
            try regattaController.startTracking()
            performSegueWithIdentifier("Tracking", sender: sender)
        } catch let error as LocationManager.LocationManagerError {
            let alertController = UIAlertController(title: error.description, message: nil, preferredStyle: .Alert)
            let cancelTitle = NSLocalizedString("Cancel", comment: "")
            let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
        } catch {
            print("Unknown error")
        }
    }
    
    // MARK: - Alerts
    
    private func showCheckOutAlertController() {
        let alertTitle = NSLocalizedString("Check-out of Regatta?", comment: "")
        let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .Alert)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default) { (action) in self.preformCheckOut() }
        let cancelTitle = NSLocalizedString("Cancel", comment: "")
        let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func preformCheckOut() {
        requestManager.postCheckOut(regatta.leaderboard.name,
                                    competitorId: regatta.competitor.competitorID,
                                    success: { (operation, responseObject) in },
                                    failure: { (operation, error) in
        })
        CoreDataManager.sharedManager.deleteObject(regatta)
        CoreDataManager.sharedManager.saveContext()
        navigationController!.popViewControllerAnimated(true)
    }
    
    func showImageAlertController() {
        if sourceTypes.count == 1 {
            imagePicker(sourceTypes[0])
        }
        if sourceTypes.count == 2 {
            let alertTitle = NSLocalizedString("Select a photo for your team", comment: "")
            let alertController = UIAlertController(title: alertTitle, message: nil, preferredStyle: .Alert)
            let sourceType1Action = UIAlertAction(title: sourceTypeNames[0], style: .Default) { (action) in
                self.imagePicker(self.sourceTypes[0])
            }
            let sourceType2Action = UIAlertAction(title: sourceTypeNames[1], style: .Default) { (action) in
                self.imagePicker(self.sourceTypes[1])
            }
            let cancelTitle = NSLocalizedString("Cancel", comment: "")
            let cancelAction = UIAlertAction(title: cancelTitle, style: .Cancel, handler: nil)
            alertController.addAction(sourceType1Action)
            alertController.addAction(sourceType2Action)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
        }
    }
    
    // MARK: - Segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == "Tracking") {
            let trackingNC = segue.destinationViewController as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.regatta = regatta
        } else if (segue.identifier == "Leaderboard") {
            let leaderboardNC = segue.destinationViewController as! UINavigationController
            let leaderboardVC = leaderboardNC.viewControllers[0] as! LeaderboardViewController
            leaderboardVC.regatta = regatta
        }
    }
    
    // MARK: - Properties
    
    lazy var regattaController: RegattaController = {
        return RegattaController(regatta: self.regatta)
    }()
    
}

// MARK: - UIImagePickerControllerDelegate

extension RegattaViewController: UIImagePickerControllerDelegate {
    
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
        teamImageView.image = image
        teamImageAddButton.hidden = true
        teamImageEditButton.hidden = false
        teamImageRetryButton.hidden = true
        regatta.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        CoreDataManager.sharedManager.saveContext()
        postTeamImageData(regatta.teamImageData)
    }
    
    private func postTeamImageData(imageData: NSData!) {
        requestManager.postTeamImageData(imageData,
                                         competitorId: regatta.competitor.competitorID,
                                         success: { (responseObject) in self.postTeamImageSuccess(responseObject) },
                                         failure: { (error) in self.postTeamImageFailure(error) })
    }
    
    private func postTeamImageSuccess(responseObject: AnyObject) {
        
        // Save image URL and upload success
        let teamImageDictionary = responseObject as! [String: AnyObject]
        let teamImageURL = (teamImageDictionary[TeamImageKeys.TeamImageURL]) as! String
        regatta.teamImageURL = teamImageURL
        regatta.teamImageRetry = false
        CoreDataManager.sharedManager.saveContext()
        
        // Setup team image
        setupTeamImage()
    }
    
    private func postTeamImageFailure(error: AnyObject) {
        
        // Save image upload failure
        regatta.teamImageRetry = true
        CoreDataManager.sharedManager.saveContext()
        
        // Show alert
        let alertTitle = NSLocalizedString("Failed to upload image", comment: "")
        let alertController = UIAlertController(title: alertTitle, message: error.localizedDescription, preferredStyle: .Alert)
        let okTitle = NSLocalizedString("OK", comment: "")
        let okAction = UIAlertAction(title: okTitle, style: .Default, handler: nil)
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
        
        // Setup team image
        setupTeamImage()
    }
    
}
