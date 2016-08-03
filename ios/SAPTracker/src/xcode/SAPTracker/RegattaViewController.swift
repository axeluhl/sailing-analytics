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
    
    var countdownTimer: NSTimer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
        update()
    }
    
    override func viewWillAppear(animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }
    
    // MARK: - Setup
    
    private func setup() {
        setupButtons()
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
    
    private func setupCountdownTimer() {
        countdownTimer?.invalidate()
        countdownTimer = NSTimer.scheduledTimerWithTimeInterval(1,
                                                                target: self,
                                                                selector: #selector(RegattaViewController.countdownTimerTick),
                                                                userInfo: nil,
                                                                repeats: true
        )
        countdownTimerTick()
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
    
    // MARK: - Update
    
    private func update() {
        SVProgressHUD.show()
        regattaController.update {
            self.refresh()
            SVProgressHUD.popActivity()
        }
    }
    
    // MARK: - Refresh
    
    private func refresh() {
        refreshCompetitor()
    }
    
    private func refreshCompetitor() {
        competitorNameLabel.text = regatta.competitor.name
        competitorFlagImageView.image = UIImage(named: regatta.competitor.countryCode)
        competitorSailLabel.text = regatta.competitor.sailID
    }
    
    // MARK: - Timer
    
    @objc private func countdownTimerTick() {
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
        uploadTeamImageData(regatta.teamImageData)
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
        let replaceImageAction = UIAlertAction(title: Translation.RegattaView.OptionSheet.ReplaceImageAction.Title.String, style: .Default) { (action) in
            self.showSelectImageAlert()
        }
        let updateAction = UIAlertAction(title: Translation.RegattaView.OptionSheet.UpdateAction.Title.String, style: .Default) { (action) -> Void in
            self.update()
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .Default) { (action) -> Void in
            self.performSegueWithIdentifier(Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(checkOutAction)
        alertController.addAction(replaceImageAction)
        alertController.addAction(updateAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    @IBAction func startTrackingButtonTapped(sender: AnyObject) {
        // TODO: Add or add not WiFi Alert?
        //if SMTWiFiStatus.wifiStatus() == WiFiStatus.On && !AFNetworkReachabilityManager.sharedManager().reachableViaWiFi {
        //    showStartTrackingWiFiAlert(sender)
        //} else {
        startTracking(sender)
        //}
    }
    
    private func startTracking(sender: AnyObject) {
        do {
            try regattaController.startTracking()
            performSegueWithIdentifier(Segue.Tracking, sender: sender)
        } catch let error as LocationManager.LocationManagerError {
            showStartTrackingFailureAlert(error.description)
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
        let yesAction = UIAlertAction(title: Translation.Common.Yes.String, style: .Default) { (action) in
            self.performCheckOut()
        }
        let noAction = UIAlertAction(title: Translation.Common.No.String, style: .Cancel, handler: nil)
        alertController.addAction(yesAction)
        alertController.addAction(noAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func performCheckOut() {
        regattaController.checkOut { (withSuccess) in
            self.performCheckOutCompleted(withSuccess)
        }
    }
    
    private func performCheckOutCompleted(withSuccess: Bool) {
        CoreDataManager.sharedManager.deleteObject(self.regatta)
        CoreDataManager.sharedManager.saveContext()
        self.navigationController!.popViewControllerAnimated(true)
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
    
    private func showStartTrackingWiFiAlert(sender: AnyObject) {
        let alertController = UIAlertController(title: "INFO",
                                                message: "WIFI IS ON BUT NOT CONNECTED",
                                                preferredStyle: .Alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .Default) { (action) in
            UIApplication.sharedApplication().openURL(NSURL(string: "prefs:root=WIFI") ?? NSURL())
        }
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default) { (action) in
            self.startTracking(sender)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(okAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
    private func showStartTrackingFailureAlert(message: String) {
        let alertController = UIAlertController(title: Translation.Common.Warning.String,
                                                message: message,
                                                preferredStyle: .Alert
        )
        let settingsAction = UIAlertAction(title: Translation.Common.Settings.String, style: .Default) { (action) in
            UIApplication.sharedApplication().openURL(NSURL(string: "prefs:root=LOCATION_SERVICES") ?? NSURL())
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .Default, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(cancelAction)
        presentViewController(alertController, animated: true, completion: nil)
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
            dismissViewControllerAnimated(true) {
                self.uploadTeamImage(image)
            }
        } else {
            dismissViewControllerAnimated(true, completion: nil)
        }
    }
    
    private func uploadTeamImage(image: UIImage) {
        teamImageView.image = image
        teamImageAddButton.hidden = true
        teamImageEditButton.hidden = false
        teamImageRetryButton.hidden = true
        regatta.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        CoreDataManager.sharedManager.saveContext()
        uploadTeamImageData(regatta.teamImageData)
    }
    
    private func uploadTeamImageData(imageData: NSData!) {
        SVProgressHUD.show()
        regattaController.postTeamImageData(imageData,
                                            success: { (teamImageURL) in
                                                SVProgressHUD.popActivity()
                                                self.uploadTeamImageDataSuccess(teamImageURL)
            },
                                            failure: { (error) in
                                                SVProgressHUD.popActivity()
                                                self.uploadTeamImageDataFailure(error)
            }
        )
    }
    
    private func uploadTeamImageDataSuccess(teamImageURL: String) {
        regatta.teamImageRetry = false
        regatta.teamImageURL = teamImageURL
        CoreDataManager.sharedManager.saveContext()
        setupTeamImage()
    }
    
    private func uploadTeamImageDataFailure(error: RequestManager.Error) {
        regatta.teamImageRetry = true
        CoreDataManager.sharedManager.saveContext()
        showUploadTeamImageFailureAlert(error)
        setupTeamImage()
    }
    
    // MARK: - Alert
    
    private func showUploadTeamImageFailureAlert(error: RequestManager.Error) {
        let alertController = UIAlertController(title: error.title,
                                                message: error.message,
                                                preferredStyle: .Alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .Default, handler: nil)
        alertController.addAction(okAction)
        presentViewController(alertController, animated: true, completion: nil)
    }
    
}
