//
//  CompetitorViewController.swift
//  SAPTracker
//
//  Created by computing on 10/11/14.
//  Copyright (c) 2014 com.sap.sailing. All rights reserved.
//

import Foundation

class CompetitorViewController : SessionViewController, UINavigationControllerDelegate {
    
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
    @IBOutlet weak var announcementLabel: UILabel!
    
    var competitorCheckIn: CompetitorCheckIn!
    
    var countdownTimer: Timer?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        delegate = self
        setup()
        update()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        refresh()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupCountdownTimer()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        eventButton.setBackgroundImage(Images.BlueHighlighted, for: .highlighted)
        leaderboardButton.setBackgroundImage(Images.BlueHighlighted, for: .highlighted)
        startTrackingButton.setBackgroundImage(Images.GreenHighlighted, for: .highlighted)
    }
    
    fileprivate func setupCountdownTimer() {
        countdownTimer?.invalidate()
        countdownTimer = Timer.scheduledTimer(
            timeInterval: 1,
            target: self,
            selector: #selector(CompetitorViewController.countdownTimerTick),
            userInfo: nil,
            repeats: true
        )
        countdownTimerTick()
    }
    
    fileprivate func setupLocalization() {
        announcementLabel.text = Translation.CompetitorView.AnnouncementLabel.Text.String
        countdownDaysTitleLabel.text = Translation.CompetitorView.CountdownDaysTitleLabel.Text.String
        countdownHoursTitleLabel.text = Translation.CompetitorView.CountdownHoursTitleLabel.Text.String
        countdownMinutesTitleLabel.text = Translation.CompetitorView.CountdownMinutesTitleLabel.Text.String
        eventButton.setTitle(Translation.CompetitorView.EventButton.Title.String, for: .normal)
        leaderboardButton.setTitle(Translation.LeaderboardView.Title.String, for: .normal)
        startTrackingButton.setTitle(Translation.CompetitorView.StartTrackingButton.Title.String, for: .normal)
        teamImageAddButton.setTitle(Translation.CompetitorView.TeamImageAddButton.Title.String, for: .normal)
        teamImageRetryButton.setTitle(Translation.CompetitorView.TeamImageUploadRetryButton.Title.String, for: .normal)
    }
    
    fileprivate func setupNavigationBar() {
        navigationItem.titleView = TitleView(title: competitorCheckIn.event.name, subtitle: competitorCheckIn.leaderboard.name)
        navigationController?.navigationBar.setNeedsLayout()
    }
    
    // MARK: - Update
    
    fileprivate func update() {
        SVProgressHUD.show()
        competitorSessionController.update {
            self.refresh()
            SVProgressHUD.popActivity()
        }
    }
    
    // MARK: - Refresh
    
    fileprivate func refresh() {
        refreshCompetitor()
        refreshTeamImage()
    }
    
    fileprivate func refreshCompetitor() {
        competitorNameLabel.text = competitorCheckIn.name
        competitorFlagImageView.image = UIImage(named: competitorCheckIn.countryCode)
        competitorSailLabel.text = competitorCheckIn.sailID
    }
    
    fileprivate func refreshTeamImage() {
        if let imageData = competitorCheckIn.teamImageData {
            teamImageView.image = UIImage(data: imageData as Data)
            if competitorCheckIn.teamImageRetry {
                refreshTeamImageButtons(showAddButton: false, showEditButton: true, showRetryButton: true)
            } else {
                refreshTeamImageButtons(showAddButton: false, showEditButton: true, showRetryButton: false)
                setTeamImageWithURLRequest(urlString: competitorCheckIn.teamImageURL, completion: { (withSuccess) in
                    self.refreshTeamImageButtons(showAddButton: false, showEditButton: true, showRetryButton: false)
                })
            }
        } else {
            self.refreshTeamImageButtons(showAddButton: true, showEditButton: false, showRetryButton: false)
            setTeamImageWithURLRequest(urlString: competitorCheckIn.teamImageURL, completion: { (withSuccess) in
                self.refreshTeamImageButtons(showAddButton: !withSuccess, showEditButton: withSuccess, showRetryButton: false)
            })
        }
    }
    
    fileprivate func refreshTeamImageButtons(showAddButton: Bool, showEditButton: Bool, showRetryButton: Bool) {
        teamImageAddButton.isHidden = !showAddButton
        teamImageEditButton.isHidden = !showEditButton
        teamImageRetryButton.isHidden = !showRetryButton
    }
    
    // MARK: - TeamImage

    fileprivate func setTeamImageWithURLRequest(urlString: String?, completion: @escaping (_ withSuccess: Bool) -> Void) {
        setTeamImageWithURLRequest(urlString: urlString, success: { () in
            completion(true)
        }) {
            completion(false)
        }
    }
    
    fileprivate func setTeamImageWithURLRequest(urlString: String?, success: @escaping () -> Void, failure: @escaping () -> Void) {
        guard let string = urlString else { failure(); return }
        guard let url = URL(string: string) else { failure(); return }
        teamImageView.setImageWith(
            URLRequest(url: url),
            placeholderImage: nil,
            success: { (request, response, image) in self.setTeamImageWithURLSuccess(image: image, success: success) },
            failure: { (request, response, error) in self.setTeamImageWithURLFailure(error: error, failure: failure) }
        )
    }
    
    fileprivate func setTeamImageWithURLSuccess(image: UIImage, success: () -> Void) {
        teamImageView.image = image
        competitorCheckIn.teamImageRetry = false
        competitorCheckIn.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        CoreDataManager.sharedManager.saveContext()
        success()
    }
    
    fileprivate func setTeamImageWithURLFailure(error: Error, failure: () -> Void) {
        logError(name: "\(#function)", error: error)
        failure()
    }
    
    // MARK: - Timer
    
    @objc fileprivate func countdownTimerTick() {
        if competitorCheckIn.event.startDate - Date().timeIntervalSince1970 > 0 {
            regattaStartLabel.text = Translation.CompetitorView.RegattaStartLabel.Text.BeforeRegattaDidStart.String
            let duration = competitorCheckIn.event.startDate - Date().timeIntervalSince1970
            let days = Int(duration / (60 * 60 * 24))
            let hours = Int(duration / (60 * 60)) - (days * 24)
            let minutes = Int(duration / 60) - (days * 24 * 60) - (hours * 60)
            countdownDaysLabel.text = String(format: "%02d", days)
            countdownHoursLabel.text = String(format: "%02d", hours)
            countdownMinutesLabel.text = String(format: "%02d", minutes)
            countdownView.isHidden = false
            countdownViewHeight.constant = 60
        } else {
            regattaStartLabel.text = Translation.CompetitorView.RegattaStartLabel.Text.AfterRegattaDidStart.String
            countdownView.isHidden = true
            countdownViewHeight.constant = 0
        }
    }
    
    // MARK: - Actions
    
    @IBAction func teamImageAddButtonTapped(_ sender: AnyObject) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageEditButtonTapped(_ sender: AnyObject) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageRetryButtonTapped(_ sender: AnyObject) {
        if let data = competitorCheckIn.teamImageData {
            uploadTeamImageData(imageData: data)
        }
    }
    
    @IBAction func eventButtonTapped(_ sender: UIButton) {
        if let eventURL = competitorCheckIn.eventURL() {
            UIApplication.shared.openURL(eventURL)
        }
    }
    
    @IBAction func optionButtonTapped(_ sender: AnyObject) {
        let alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        if let popoverController = alertController.popoverPresentationController {
            popoverController.barButtonItem = sender as? UIBarButtonItem
        }
        let settingsAction = UIAlertAction(title: Translation.SettingsView.Title.String, style: .default) { (action) in
            self.performSegue(withIdentifier: Segue.Settings, sender: self)
        }
        let checkOutAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.CheckOutAction.Title.String, style: .default) { (action) in
            self.checkOut()
        }
        let replaceImageAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.ReplaceImageAction.Title.String, style: .default) { (action) in
            self.showSelectImageAlert()
        }
        let updateAction = UIAlertAction(title: Translation.CompetitorView.OptionSheet.UpdateAction.Title.String, style: .default) { (action) -> Void in
            self.update()
        }
        let aboutAction = UIAlertAction(title: Translation.Common.Info.String, style: .default) { (action) -> Void in
            self.performSegue(withIdentifier: Segue.About, sender: alertController)
        }
        let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
        alertController.addAction(settingsAction)
        alertController.addAction(checkOutAction)
        alertController.addAction(replaceImageAction)
        alertController.addAction(updateAction)
        alertController.addAction(aboutAction)
        alertController.addAction(cancelAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: - Alerts
    
    fileprivate func showSelectImageAlert() {
        if UIImagePickerController.isSourceTypeAvailable(.camera) && UIImagePickerController.isSourceTypeAvailable(.photoLibrary) {
            let alertController = UIAlertController(title: Translation.CompetitorView.SelectImageAlert.Title.String,
                                                    message: Translation.CompetitorView.SelectImageAlert.Message.String,
                                                    preferredStyle: .alert
            )
            let cameraAction = UIAlertAction(title: Translation.CompetitorView.SelectImageAlert.CameraAction.Title.String, style: .default) { (action) in
                self.showImagePicker(sourceType: .camera)
            }
            let photoLibraryAction = UIAlertAction(title: Translation.CompetitorView.SelectImageAlert.PhotoLibraryAction.Title.String, style: .default) { (action) in
                self.showImagePicker(sourceType: .photoLibrary)
            }
            let cancelAction = UIAlertAction(title: Translation.Common.Cancel.String, style: .cancel, handler: nil)
            alertController.addAction(cameraAction)
            alertController.addAction(photoLibraryAction)
            alertController.addAction(cancelAction)
            present(alertController, animated: true, completion: nil)
        } else if UIImagePickerController.isSourceTypeAvailable(.camera) {
            showImagePicker(sourceType: .camera)
        } else if UIImagePickerController.isSourceTypeAvailable(.photoLibrary) {
            showImagePicker(sourceType: .photoLibrary)
        }
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if (segue.identifier == Segue.Tracking) {
            let trackingNC = segue.destination as! UINavigationController
            let trackingVC = trackingNC.viewControllers[0] as! TrackingViewController
            trackingVC.checkIn = competitorCheckIn
            trackingVC.sessionController = competitorSessionController
        } else if (segue.identifier == Segue.Leaderboard) {
            let leaderboardNC = segue.destination as! UINavigationController
            let leaderboardVC = leaderboardNC.viewControllers[0] as! LeaderboardViewController
            leaderboardVC.checkIn = competitorCheckIn
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var competitorSessionController: CompetitorSessionController = {
        return CompetitorSessionController(checkIn: self.competitorCheckIn)
    }()
    
}

// MARK: - UIImagePickerControllerDelegate

extension CompetitorViewController: UIImagePickerControllerDelegate {
    
    fileprivate func showImagePicker(sourceType: UIImagePickerControllerSourceType) {
        let imagePickerController = UIImagePickerController()
        imagePickerController.delegate = self
        imagePickerController.sourceType = sourceType;
        imagePickerController.mediaTypes = [kUTTypeImage as String];
        imagePickerController.allowsEditing = false
        present(imagePickerController, animated: true, completion: nil)
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        if let image = info[UIImagePickerControllerOriginalImage] as? UIImage {
            dismiss(animated: true) { self.pickedImage(image: image) }
        } else {
            dismiss(animated: true, completion: nil)
        }
    }
    
    fileprivate func pickedImage(image: UIImage) {
        teamImageView.image = image
        competitorCheckIn.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        CoreDataManager.sharedManager.saveContext()
        if let data = competitorCheckIn.teamImageData {
            uploadTeamImageData(imageData: data)
        }
    }
    
    // MARK: - Upload
    
    fileprivate func uploadTeamImageData(imageData: Data!) {
        SVProgressHUD.show()
        competitorSessionController.postTeamImageData(imageData: imageData, competitorID: competitorCheckIn.competitorID,
                                            success: { (teamImageURL) in
                                                SVProgressHUD.popActivity()
                                                self.uploadTeamImageDataSuccess(teamImageURL: teamImageURL)
            },
                                            failure: { (error) in
                                                SVProgressHUD.popActivity()
                                                self.uploadTeamImageDataFailure(error: error)
            }
        )
    }
    
    fileprivate func uploadTeamImageDataSuccess(teamImageURL: String) {
        competitorCheckIn.teamImageRetry = false
        competitorCheckIn.teamImageURL = teamImageURL
        CoreDataManager.sharedManager.saveContext()
        refreshTeamImage()
    }
    
    fileprivate func uploadTeamImageDataFailure(error: Error) {
        competitorCheckIn.teamImageRetry = true
        CoreDataManager.sharedManager.saveContext()
        showUploadTeamImageFailureAlert(error: error)
        refreshTeamImage()
    }
    
    // MARK: - Alerts
    
    fileprivate func showUploadTeamImageFailureAlert(error: Error) {
        let alertController = UIAlertController(
            title: Translation.CompetitorView.UploadTeamImageFailureAlert.Title.String,
            message: error.localizedDescription,
            preferredStyle: .alert
        )
        let okAction = UIAlertAction(title: Translation.Common.OK.String, style: .default, handler: nil)
        alertController.addAction(okAction)
        present(alertController, animated: true, completion: nil)
    }
    
}

// MARK: SessionViewControllerDelegate

extension CompetitorViewController: SessionViewControllerDelegate {

    func performCheckOut() {
        competitorSessionController.checkOut { (withSuccess) in
            self.performCheckOutCompleted(withSuccess: withSuccess)
        }
    }
    
    fileprivate func performCheckOutCompleted(withSuccess: Bool) {
        CoreDataManager.sharedManager.deleteObject(object: competitorCheckIn)
        CoreDataManager.sharedManager.saveContext()
        self.navigationController!.popViewController(animated: true)
    }

    func startTracking() throws {
        try competitorSessionController.startTracking()
    }
    
}
