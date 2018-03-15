//
//  CompetitorViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 13.09.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class CompetitorViewController: UIViewController {
    
    weak var competitorCheckIn: CompetitorCheckIn!
    weak var competitorCoreDataManager: CoreDataManager!
    weak var competitorSessionController: CompetitorSessionController!
    
    @IBOutlet weak var teamImageView: UIImageView!
    @IBOutlet weak var teamImageAddButton: UIButton!
    @IBOutlet weak var teamImageEditButton: UIButton!
    @IBOutlet weak var teamImageRetryButton: UIButton!
    @IBOutlet weak var competitorNameLabel: UILabel!
    @IBOutlet weak var competitorFlagImageView: UIImageView!
    @IBOutlet weak var competitorSailIDLabel: UILabel!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    // MARK: - Setup
    
    fileprivate func setup() {
        setupButtons()
        setupLocalization()
    }
    
    fileprivate func setupButtons() {
        makeTranslucent(button: teamImageAddButton)
        makeTranslucent(button: teamImageRetryButton)
    }
    
    fileprivate func setupLocalization() {
        teamImageAddButton.setTitle(Translation.CompetitorView.TeamImageAddButton.Title.String, for: .normal)
        teamImageRetryButton.setTitle(Translation.CompetitorView.TeamImageUploadRetryButton.Title.String, for: .normal)
    }
    
    // MARK: - Refresh
    
    func refresh(_ animated: Bool) {
        refreshCompetitor(animated)
        refreshTeamImage(animated)
    }

    fileprivate func refreshCompetitor(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshCompetitor() }
        } else {
            refreshCompetitor()
        }
    }
    
    fileprivate func refreshCompetitor() {
        competitorNameLabel.text = competitorCheckIn.name
        competitorFlagImageView.image = UIImage(named: competitorCheckIn.countryCode)
        competitorSailIDLabel.text = competitorCheckIn.sailID
    }
    
    fileprivate func refreshTeamImage(_ animated: Bool) {
        if (animated) {
            UIView.animate(withDuration: 0.5) { self.refreshTeamImage() }
        } else {
            refreshTeamImage()
        }
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
        teamImageView.setImageWith(URLRequest(url: url), placeholderImage: nil, success: { (request, response, image) in
            self.setTeamImageWithURLSuccess(image: image, success: success)
        }) { (request, response, error) in
            self.setTeamImageWithURLFailure(error: error, failure: failure)
        }
    }
    
    fileprivate func setTeamImageWithURLSuccess(image: UIImage, success: () -> Void) {
        teamImageView.image = image
        competitorCheckIn.teamImageRetry = false
        competitorCheckIn.teamImageData = UIImageJPEGRepresentation(image, 0.8)
        competitorCoreDataManager.saveContext()
        success()
    }
    
    fileprivate func setTeamImageWithURLFailure(error: Error, failure: () -> Void) {
        logError(name: "\(#function)", error: error)
        failure()
    }
    
    // MARK: - Actions
    
    @IBAction func teamImageAddButtonTapped(_ sender: Any) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageEditButtonTapped(_ sender: Any) {
        showSelectImageAlert()
    }
    
    @IBAction func teamImageRetryButtonTapped(_ sender: Any) {
        if let data = competitorCheckIn.teamImageData {
            uploadTeamImageData(imageData: data)
        }
    }
    
    // MARK: - Alerts
    
    func showSelectImageAlert() {
        if UIImagePickerController.isSourceTypeAvailable(.camera) && UIImagePickerController.isSourceTypeAvailable(.photoLibrary) {
            let alertController = UIAlertController(
                title: Translation.CompetitorView.SelectImageAlert.Title.String,
                message: Translation.CompetitorView.SelectImageAlert.Message.String,
                preferredStyle: .alert
            )
            let cameraAction = UIAlertAction(title: Translation.CompetitorView.SelectImageAlert.CameraAction.Title.String, style: .default) { [weak self] (action) in
                self?.showImagePicker(sourceType: .camera)
            }
            let photoLibraryAction = UIAlertAction(title: Translation.CompetitorView.SelectImageAlert.PhotoLibraryAction.Title.String, style: .default) { [weak self] (action) in
                self?.showImagePicker(sourceType: .photoLibrary)
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
    
}

// MARK: - UINavigationControllerDelegate

extension CompetitorViewController: UINavigationControllerDelegate {
    
    
    
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
        competitorCoreDataManager.saveContext()
        if let data = competitorCheckIn.teamImageData {
            uploadTeamImageData(imageData: data)
        }
    }
    
    // MARK: - Upload
    
    fileprivate func uploadTeamImageData(imageData: Data!) {
        SVProgressHUD.show()
        competitorSessionController.postTeamImageData(imageData: imageData, competitorID: competitorCheckIn.competitorID, success: { (teamImageURL) in
            SVProgressHUD.popActivity()
            self.uploadTeamImageDataSuccess(teamImageURL: teamImageURL)
        }) { (error) in
            SVProgressHUD.popActivity()
            self.uploadTeamImageDataFailure(error: error)
        }
    }
    
    fileprivate func uploadTeamImageDataSuccess(teamImageURL: String) {
        competitorCheckIn.teamImageRetry = false
        competitorCheckIn.teamImageURL = teamImageURL
        competitorCoreDataManager.saveContext()
        refreshTeamImage()
    }
    
    fileprivate func uploadTeamImageDataFailure(error: Error) {
        competitorCheckIn.teamImageRetry = true
        competitorCoreDataManager.saveContext()
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
