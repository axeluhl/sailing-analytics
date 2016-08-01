//
//  RegattaViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class RegattaViewController : UIViewController, UINavigationControllerDelegate {
    
    private struct Segue {
        static let About = "About"
        static let Leaderboard = "Leaderboard"
        static let Settings = "Settings"
        static let Tracking = "Tracking"
    }
    
    private struct TeamImageKeys {
        static let TeamImageURL = "teamImageUri"
    }
    
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
    @IBOutlet weak var countdownDaysTitleLabel: UILabel!
    @IBOutlet weak var countdownHoursLabel: UILabel!
    @IBOutlet weak var countdownHoursTitleLabel: UILabel!
    @IBOutlet weak var countdownMinutesLabel: UILabel!
    @IBOutlet weak var countdownMinutesTitleLabel: UILabel!
    @IBOutlet weak var leaderboardButton: UIButton!
    @IBOutlet weak var eventButton: UIButton!
    @IBOutlet weak var startTrackingButton: UIButton!
    @IBOutlet weak var announcementLabel: UILabel!
    
    var regatta: Regatta!
    var requestManager: RequestManager!
    
    var countdownTimer: NSTimer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        requestManager = RequestManager(baseURLString: regatta.serverURL)
        setup()
        
        // FIXME: - UI refresh?!
        regattaController.update()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupButtons()
        setupCompetitor()
        setupCountdownTimer()
        setupLocalization()
        setupNavigationBar()
        setupTeamImage()
    }
    
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
    
    private func setupLocalization() {
        countdownDaysTitleLabel.text = Translation.RegattaView.CountdownDaysTitleLabel.Text.String
        countdownHoursTitleLabel.text = Translation.RegattaView.CountdownHoursTitleLabel.Text.String
        countdownMinutesTitleLabel.text = Translation.RegattaView.CountdownMinutesTitleLabel.Text.String
        eventButton.setTitle(Translation.RegattaView.EventButton.Title.String, forState: .Normal)
        leaderboardButton.setTitle(Translation.LeaderboardView.Title.String, forState: .Normal)
        startTrackingButton.setTitle(Translation.RegattaView.StartTrackingButton.Title.String, forState: .Normal)
        announcementLabel.text = Translation.RegattaView.AnnouncementLabel.Text.String
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
            regattaStartLabel.text = Translation.RegattaView.RegattaStartLabel.Text.BeforeRegattaDidStart.String
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
            regattaStartLabel.text = Translation.RegattaView.RegattaStartLabel.Text.AfterRegattaDidStart.String
            countdownView.hidden = true
            countdownViewHeight.constant = 0
        }
    }
    
    // MARK: - Actions
    
    @IBAction func teamImageAddButtonTapped(sender: AnyObject) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageEditButtonTapped(sender: AnyObject) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageRetryButtonTapped(sender: AnyObject) {
        postTeamImageData(regatta.teamImageData)
    }
    
    @IBAction func eventButtonTapped(sender: UIButton) {
        UIApplication.sharedApplication().openURL(regatta.eventURL() ?? NSURL())
    }
    
    @IBAction func optionButtonTapped(sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .ActionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .Default) { (action) in
            self.performSegueWithIdentifier(Segue.Settings, sender: self)
        }
        let checkOutAction = UIAlertAction(title: Translation.RegattaView.OptionSheet.CheckOutAction.Title.String, style: .Default) { (action) in
            self.showCheckOutAlert()
        }
        
        
        // FIXME: Replace or edit or a mix of both?
        let replaceImageAction = UIAlertAction(title: Translation.RegattaView.OptionSheet.ReplaceImageAction.Title.String, style: .Default) { (action) in
            self.showSelectImageAlert()
        }
        
        
        let refreshAction = UIAlertAction(title: Translation.RegattaView.OptionSheet.RefreshAction.Title.String, style: .Default) { (action) -> Void in
            self.regattaController.update()
        }
        let aboutAction = UIAlertAction(title: Translation.AboutView.Title.String, style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier(Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(checkOutAction)
        alertController.addAction(replaceImageAction)
        alertController.addAction(refreshAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        do {
            try regattaController.startTracking()
            performSegueWithIdentifier(Segue.Tracking, sender: sender)
        } catch let error as LocationManager.LocationManagerError {
            let alertController = UIAlertController(title: error.description, message: nil, preferredStyle: .Alert)
            let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
        } catch {
            print("Unknown error")
        }
    }
    
    // MARK: - Alerts
    
    private func showCheckOutAlert() {
        let alertController = UIAlertController(title: Translation.Common.Warning.String,
                                                message: Translation.RegattaView.CheckOutAlert.Message.String,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.Yes.String, style: .Default) { (action) in self.preformCheckOut() }
        let cancelAction = UIAlertAction(title: Translation.Common.No.String, style: .Cancel, handler: nil)
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
    
    private func showSelectImageAlert() {
        if UIImagePickerController.isSourceTypeAvailable(.Camera) && UIImagePickerController.isSourceTypeAvailable(.PhotoLibrary) {
            let alertController = UIAlertController(title: Translation.RegattaView.SelectImageAlert.Title.String,
                                                    message: Translation.RegattaView.SelectImageAlert.Message.String,
                                                    preferredStyle: .Alert
            )
            let cameraAction = UIAlertAction(title: Translation.RegattaView.SelectImageAlert.CameraAction.Title.String, style: .Default) { (action) in
                self.showImagePicker(.Camera)
            }
            let photoLibraryAction = UIAlertAction(title: Translation.RegattaView.SelectImageAlert.PhotoLibraryAction.Title.String, style: .Default) { (action) in
                self.showImagePicker(.PhotoLibrary)
            }
            let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
            alertController.addAction(cameraAction)
            alertController.addAction(photoLibraryAction)
            alertController.addAction(cancelAction)
            presentViewController(alertController, animated: true, completion: nil)
        } else if UIImagePickerController.isSourceTypeAvailable(.Camera) {
            showImagePicker(.Camera)
        } else if UIImagePickerController.isSourceTypeAvailable(.PhotoLibrary) {
            showImagePicker(.PhotoLibrary)
        }
    }
    
    // MARK: - Segue
    
    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if (segue.identifier == Segue.Tracking) {
            let trackingNC = segue.destinationViewController as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.regatta = regatta
            trackingVC.regattaController = regattaController
        } else if (segue.identifier == Segue.Leaderboard) {
            let leaderboardNC = segue.destinationViewController as! UINavigationController
            let leaderboardVC = leaderboardNC.viewControllers[0] as! LeaderboardViewController
            leaderboardVC.regatta = regatta
        }
    }
    
    // MARK: - Properties
    
    private lazy var regattaController: RegattaController = {
        return RegattaController(regatta: self.regatta)
    }()
    
}

// MARK: - UIImagePickerControllerDelegate

extension RegattaViewController: UIImagePickerControllerDelegate {
    
    private func showImagePicker(sourceType: UIImagePickerControllerSourceType) {
        let imagePickerController = UIImagePickerController()
        imagePickerController.delegate = self
        imagePickerController.sourceType = sourceType;
        imagePickerController.mediaTypes = [kUTTypeImage as String];
        imagePickerController.allowsEditing = false
        presentViewController(imagePickerController, animated: true, completion: nil)
    }
    
    func imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            dismissViewControllerAnimated(true, completion: { self.postTeamImage(image)} )
        } else {
            dismissViewControllerAnimated(true, completion: nil)
        }
    }
    
    private func postTeamImage(image: UIImage) {
        teamImageView.image = image
        teamImageAddButton.hidden = true
        teamImageEditButton.hidden = false
        teamImageRetryButton.hidden = true
        regatta.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        CoreDataManager.sharedManager.saveContext()
        postTeamImageData(regatta.teamImageData)
    }
    
    private func postTeamImageData(imageData: NSData!) {
        SVProgressHUD.show()
        requestManager.postTeamImageData(imageData,
                                         competitorId: regatta.competitor.competitorID,
                                         success: { (responseObject) in self.postTeamImageDataSuccess(responseObject) },
                                         failure: { (error) in self.postTeamImageDataFailure(error) })
    }
    
    private func postTeamImageDataSuccess(responseObject: AnyObject) {
        SVProgressHUD.popActivity()
        
        // Save image URL and upload success
        let teamImageDictionary = responseObject as! [String: AnyObject]
        let teamImageURL = (teamImageDictionary[TeamImageKeys.TeamImageURL]) as! String
        regatta.teamImageURL = teamImageURL
        regatta.teamImageRetry = false
        CoreDataManager.sharedManager.saveContext()
        
        // Setup team image
        setupTeamImage()
    }
    
    private func postTeamImageDataFailure(error: AnyObject) {
        SVProgressHUD.popActivity()
        
        // Save image upload failure
        regatta.teamImageRetry = true
        CoreDataManager.sharedManager.saveContext()
        
        // Show alert
        let alertController = UIAlertController(title: Translation.RegattaView.ImageUploadFailureAlert.Title.String,
                                                message: error.localizedDescription,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default, handler: nil)
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
        
        // Setup team image
        setupTeamImage()
    }
    
}
