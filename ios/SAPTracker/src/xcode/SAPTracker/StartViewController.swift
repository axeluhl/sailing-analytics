//
//  StartViewController.swift
//  SAPTracker
//
//  Created by Raimund Wege on 09.08.17.
//  Copyright © 2017 com.sap.sailing. All rights reserved.
//

import UIKit

class StartViewController: UIViewController {
    
    fileprivate struct Segue {
        static let Regatta = "Regatta"
        static let Training = "Training"
    }
    
    var userName: String?
    
    @IBOutlet weak var regattaImageView: UIImageView!
    @IBOutlet weak var regattaButtonView: UIView!
    @IBOutlet weak var regattaButton: UIButton!
    @IBOutlet weak var trainingImageView: UIImageView!
    @IBOutlet weak var trainingButtonView: UIView!
    @IBOutlet weak var trainingButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setup()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        review()
    }
    
    // MARK: - Setup

    fileprivate func setup() {
        setupButtons()
        setupLocalization()
        setupNavigationBar()
    }
    
    fileprivate func setupButtons() {
        makeRound(view: regattaButtonView, cornerRadius: 4)
        regattaButton.setTitleColor(Colors.BlueButtonTitleColor, for: .normal)
        regattaButton.setBackgroundImage(Images.BlueButton, for: .normal)
        regattaButton.setBackgroundImage(Images.BlueButtonHighlighted, for: .highlighted)
        regattaImageView.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(regattaButtonTapped(_:))))
        regattaImageView.isUserInteractionEnabled = true
        makeRound(view: trainingButtonView, cornerRadius: 4)
        trainingButton.setTitleColor(Colors.BlueButtonTitleColor, for: .normal)
        trainingButton.setBackgroundImage(Images.BlueButton, for: .normal)
        trainingButton.setBackgroundImage(Images.BlueButtonHighlighted, for: .highlighted)
        trainingImageView.addGestureRecognizer(UITapGestureRecognizer.init(target: self, action: #selector(trainingButtonTapped(_:))))
        trainingImageView.isUserInteractionEnabled = true
    }

    fileprivate func setupLocalization() {
        regattaButton.setTitle(Translation.StartView.RegattaButton.Title.String, for: .normal)
        trainingButton.setTitle(Translation.StartView.TrainingButton.Title.String, for: .normal)
    }

    fileprivate func setupNavigationBar() {
        navigationItem.title = Application.Title
        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: UIImageView(image: UIImage(named: "sap_logo")))
    }
    
    // MARK: - Review
    
    fileprivate func review() {
        reviewTerms(completion: { [weak self] in
            logInfo(name: "\(#function)", info: "Review terms done.")
            self?.reviewCodeConvention(completion: { [weak self] in
                logInfo(name: "\(#function)", info: "Review code convention done.")
                self?.reviewGPSFixes(completion: {
                    logInfo(name: "\(#function)", info: "Review GPS fixes done.")
                })
            })
        })
    }
    
    // MARK: 1. Review Terms
    
    fileprivate func reviewTerms(completion: @escaping () -> Void) {
        guard Preferences.termsAccepted == false else {
            completion()
            return
        }
        let alertController = UIAlertController(
            title: Translation.RegattaCheckInListView.TermsAlert.Title.String,
            message: Translation.RegattaCheckInListView.TermsAlert.Message.String,
            preferredStyle: .alert
        )
        let showTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.ShowTermsAction.Title.String, style: .default) { [weak self] (action) in
            UIApplication.shared.openURL(URLs.Terms)
            self?.reviewTerms(completion: completion) // Review terms until user accepted terms
        }
        let acceptTermsAction = UIAlertAction(title: Translation.RegattaCheckInListView.TermsAlert.AcceptTermsAction.Title.String, style: .default) { (action) in
            Preferences.termsAccepted = true
            completion() // Terms accepted
        }
        alertController.addAction(showTermsAction)
        alertController.addAction(acceptTermsAction)
        present(alertController, animated: true, completion: nil)
    }
    
    // MARK: 2. Review Code Convention
    
    fileprivate func reviewCodeConvention(completion: @escaping () -> Void) {
        #if DEBUG
            guard Preferences.codeConventionRead == false else { completion(); return }
            let alertController = UIAlertController(
                title: "Code Convention",
                message: "Please try to respect the code convention which is used for this project.",
                preferredStyle: .alert
            )
            let showCodeConventionAction = UIAlertAction(title: "Code Convention", style: .default) { [weak self] (action) in
                UIApplication.shared.openURL(URLs.CodeConvention)
                self?.reviewCodeConvention(completion: completion)
            }
            let okAction = UIAlertAction(title: "OK", style: .default) { (action) in
                Preferences.codeConventionRead = true
                completion()
            }
            alertController.addAction(showCodeConventionAction)
            alertController.addAction(okAction)
            present(alertController, animated: true, completion: nil)
        #else
            completion()
        #endif
    }
    
    // MARK: 3. Review GPS Fixes
    
    fileprivate func reviewGPSFixes(completion: @escaping () -> Void) {
        SVProgressHUD.show()
        let checkIns = RegattaCoreDataManager.shared.fetchCheckIns() ?? []
        reviewGPSFixes(checkIns: checkIns) { [weak self] in
            self?.reviewGPSFixesCompleted(completion: completion)
        }
    }
    
    fileprivate func reviewGPSFixes(checkIns: [CheckIn], completion: @escaping () -> Void) {
        guard checkIns.count > 0 else { completion(); return }
        let gpsFixController = GPSFixController.init(checkIn: checkIns[0], coreDataManager: RegattaCoreDataManager.shared)
        gpsFixController.sendAll(completion: { [weak self] (withSuccess) in
            self?.reviewGPSFixes(checkIns: Array(checkIns[1..<checkIns.count]), completion: completion)
        })
    }
    
    fileprivate func reviewGPSFixesCompleted(completion: () -> Void) {
        SVProgressHUD.popActivity()
        completion()
    }
    
    // MARK: - Actions

    @IBAction func regattaButtonTapped(_ sender: Any) {
        performSegue(withIdentifier: Segue.Regatta, sender: self)
    }

    @IBAction func trainingButtonTapped(_ sender: Any) {
        if (userName == nil) {
            signUpController.loginWithViewController(self)
        } else {
            performSegue(withIdentifier: Segue.Training, sender: self)
        }
    }
    
    // MARK: - Segues
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        super.prepare(for: segue, sender: sender)
        if (segue.identifier == Segue.Training) {
            if let trainingCheckInTVC = segue.destination as? TrainingCheckInTableViewController {
                trainingCheckInTVC.userName = userName
                trainingCheckInTVC.signUpController = signUpController
            }
        }
    }
    
    // MARK: - Properties
    
    fileprivate lazy var signUpController: SignUpController = {
        let signUpController = SignUpController(baseURLString: "https://ubilabstest.sapsailing.com")
        signUpController.delegate = self
        return signUpController
    }()
    
}

// MARK: - SignUpControllerDelegate

extension StartViewController: SignUpControllerDelegate {
    
    func signUpController(_ controller: SignUpController, didFinishLoginWithUserName userName: String) {
        self.userName = userName
        performSegue(withIdentifier: Segue.Training, sender: self)
    }
    
    func signUpControllerDidCancelLogin(_ controller: SignUpController) {
        userName = nil
    }
    
    func signUpControllerDidLogout(_ controller: SignUpController) {
        userName = nil
        navigationController?.popToRootViewController(animated: true)
    }
    
}
