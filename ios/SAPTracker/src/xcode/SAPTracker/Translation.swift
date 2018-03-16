//
//  Translation.swift
//  SAPTracker
//
//  Created by Raimund Wege on 27.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class Translation: NSObject {
    
    struct Common { private static let Key = "\(Common.self)"
        struct Cancel { private static let Key = "\(Common.Key).\(Cancel.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct OK { private static let Key = "\(Common.Key).\(OK.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Info { private static let Key = "\(Common.Key).\(Info.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Settings { private static let Key = "\(Common.Key).\(Settings.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Warning { private static let Key = "\(Common.Key).\(Warning.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Error { private static let Key = "\(Common.Key).\(Error.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Yes { private static let Key = "\(Common.Key).\(Yes.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct No { private static let Key = "\(Common.Key).\(No.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Logout { private static let Key = "\(Common.Key).\(Logout.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }

    // MARK: Endpoint

    struct Endpoint { private static let Key = "\(Endpoint.self)"
        struct Training { private static let Key = "\(Endpoint.Key).\(Training.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }

    // MARK: - StartView

    struct StartView { private static let Key = "\(StartView.self)"
        struct RegattaButton { private static let Key = "\(StartView.Key).\(RegattaButton.self)"
            struct Title { private static let Key = "\(RegattaButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TrainingButton { private static let Key = "\(StartView.Key).\(TrainingButton.self)"
            struct Title { private static let Key = "\(TrainingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }

    // MARK: - RegattaCheckInController
    
    struct RegattaCheckInController { private static let Key = "\(RegattaCheckInController.self)"
        struct WelcomeAlert { private static let Key = "\(RegattaCheckInController.Key).\(WelcomeAlert.self)"
            struct Title { private static let Key = "\(WelcomeAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(WelcomeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct CancelAction { private static let Key = "\(WelcomeAlert.Key).\(CancelAction.self)"
                struct Title { private static let Key = "\(CancelAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
    }
    
    // MARK: - GPSFixController
    
    struct GPSFixController { private static let Key = "\(GPSFixController.self)"
        struct Mode { private static let Key = "\(GPSFixController.Key).\(Mode.self)"
            static let String = NSLocalizedString(Key, comment: "")
            struct Error { private static let Key = "\(Mode.Key).\(Error.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct BatterySaving { private static let Key = "\(Mode.Key).\(BatterySaving.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct None { private static let Key = "\(Mode.Key).\(None.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Online { private static let Key = "\(Mode.Key).\(Online.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Offline { private static let Key = "\(Mode.Key).\(Offline.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - LocationManager
    
    struct LocationManager { private static let Key = "\(LocationManager.self)"
        struct LocationServicesDeniedError { private static let Key = "\(LocationManager.Key).\(LocationServicesDeniedError.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct LocationServicesDisabledError { private static let Key = "\(LocationManager.Key).\(LocationServicesDisabledError.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Status { private static let Key = "\(LocationManager.Key).\(Status.self)"
            static let String = NSLocalizedString(Key, comment: "")
            struct Tracking { private static let Key = "\(Status.Key).\(Tracking.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct NotTracking { private static let Key = "\(Status.Key).\(NotTracking.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - CheckInDataCollectorError
    
    struct CheckInDataCollectorError { private static let Key = "\(CheckInDataCollectorError.self)"
        struct CheckInDataIsIncomplete { private static let Key = "\(CheckInDataCollectorError.Key).\(CheckInDataIsIncomplete.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - CheckInRequestManagerError
    
    struct CheckInRequestManagerError { private static let Key = "\(CheckInRequestManagerError.self)"
        struct CommunicationFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(CommunicationFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetBoatFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetBoatFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetCompetitorFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetCompetitorFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetEventFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetEventFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetLeaderboardFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetLeaderboardFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetMarkFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetMarkFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetTeamFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(GetTeamFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostCheckInFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(PostCheckInFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostCheckOutFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(PostCheckOutFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostGPSFixFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(PostGPSFixFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostTeamImageFailed { private static let Key = "\(CheckInRequestManagerError.Key).\(PostTeamImageFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct TeamImageURLIsInvalid { private static let Key = "\(CheckInRequestManagerError.Key).\(TeamImageURLIsInvalid.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - AboutView
    
    struct AboutView { private static let Key = "\(AboutView.self)"
        struct Title { private static let Key = "\(AboutView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PartnershipTextView { private static let Key = "\(AboutView.Key).\(PartnershipTextView.self)"
            struct Text { private static let Key = "\(PartnershipTextView.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TermsButton { private static let Key = "\(AboutView.Key).\(TermsButton.self)"
            struct Title { private static let Key = "\(TermsButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct VersionTitleLabel { private static let Key = "\(AboutView.Key).\(VersionTitleLabel.self)"
            struct Text { private static let Key = "\(VersionTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - CourseView
    
    struct CourseView { private static let Key = "\(CourseView.self)"
        struct TitleLabel { private static let Key = "\(CourseView.Key).\(TitleLabel.self)"
            struct Text { private static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - RegattaCheckInListView
    
    struct RegattaCheckInListView { private static let Key = "\(RegattaCheckInListView.self)"
        struct Title { private static let Key = "\(RegattaCheckInListView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct HeaderTitleLabel { private static let Key = "\(RegattaCheckInListView.Key).\(HeaderTitleLabel.self)"
            struct Text { private static let Key = "\(HeaderTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct FooterTextView { private static let Key = "\(RegattaCheckInListView.Key).\(FooterTextView.self)"
            struct Text { private static let Key = "\(FooterTextView.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct InfoCodeLabel { private static let Key = "\(RegattaCheckInListView.Key).\(InfoCodeLabel.self)"
            struct Text { private static let Key = "\(InfoCodeLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TermsAlert { private static let Key = "\(RegattaCheckInListView.Key).\(TermsAlert.self)"
            struct Title { private static let Key = "\(TermsAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(TermsAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct ShowTermsAction { private static let Key = "\(TermsAlert.Key).\(ShowTermsAction.self)"
                struct Title { private static let Key = "\(ShowTermsAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct AcceptTermsAction { private static let Key = "\(TermsAlert.Key).\(AcceptTermsAction.self)"
                struct Title { private static let Key = "\(AcceptTermsAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct NoCameraAlert { private static let Key = "\(RegattaCheckInListView.Key).\(NoCameraAlert.self)"
            struct Message { private static let Key = "\(NoCameraAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct NoCodeAlert { private static let Key = "\(RegattaCheckInListView.Key).\(NoCodeAlert.self)"
            struct Title { private static let Key = "\(NoCodeAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(NoCodeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TrainingCheckInListView
    
    struct TrainingCheckInListView { private static let Key = "\(TrainingCheckInListView.self)"
        struct Title { private static let Key = "\(TrainingCheckInListView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct HeaderTitleLabel { private static let Key = "\(TrainingCheckInListView.Key).\(HeaderTitleLabel.self)"
            struct Text { private static let Key = "\(HeaderTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct FooterTextView { private static let Key = "\(TrainingCheckInListView.Key).\(FooterTextView.self)"
            struct Text { private static let Key = "\(FooterTextView.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - CreateTrainingView
    
    struct CreateTrainingView { private static let Key = "\(CreateTrainingView.self)"
        struct Title { private static let Key = "\(CreateTrainingView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct BoatClassNameLabel { private static let Key = "\(CreateTrainingView.Key).\(BoatClassNameLabel.self)"
            struct Text { private static let Key = "\(BoatClassNameLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CreateTrainingButton { private static let Key = "\(CreateTrainingView.Key).\(CreateTrainingButton.self)"
            struct Title { private static let Key = "\(CreateTrainingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - LeaderboardView
    
    struct LeaderboardView { private static let Key = "\(LeaderboardView.self)"
        struct Title { private static let Key = "\(LeaderboardView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - LicenseView
    
    struct LicenseView { private static let Key = "\(LicenseView.self)"
        struct Title { private static let Key = "\(LicenseView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - RegattaView
    
    struct RegattaView { private static let Key = "\(RegattaView.self)"
        struct CountdownDaysTitleLabel { private static let Key = "\(RegattaView.Key).\(CountdownDaysTitleLabel.self)"
            struct Text { private static let Key = "\(CountdownDaysTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CountdownHoursTitleLabel { private static let Key = "\(RegattaView.Key).\(CountdownHoursTitleLabel.self)"
            struct Text { private static let Key = "\(CountdownHoursTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CountdownMinutesTitleLabel { private static let Key = "\(RegattaView.Key).\(CountdownMinutesTitleLabel.self)"
            struct Text { private static let Key = "\(CountdownMinutesTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StartLabel { private static let Key = "\(RegattaView.Key).\(StartLabel.self)"
            struct Text { private static let Key = "\(StartLabel.Key).\(Text.self)"
                struct BeforeRegattaDidStart { private static let Key = "\(Text.Key).\(BeforeRegattaDidStart.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
                struct AfterRegattaDidStart { private static let Key = "\(Text.Key).\(AfterRegattaDidStart.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct AnnouncementLabel { private static let Key = "\(RegattaView.Key).\(AnnouncementLabel.self)"
            struct Text { private static let Key = "\(AnnouncementLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct EventButton { private static let Key = "\(RegattaView.Key).\(EventButton.self)"
            struct Title { private static let Key = "\(EventButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StartTrackingButton { private static let Key = "\(RegattaView.Key).\(StartTrackingButton.self)"
            struct Title { private static let Key = "\(StartTrackingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct OptionSheet { private static let Key = "\(RegattaView.Key).\(OptionSheet.self)"
            struct CheckOutAction { private static let Key = "\(OptionSheet.Key).\(CheckOutAction.self)"
                struct Title { private static let Key = "\(CheckOutAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct CheckOutAlert { private static let Key = "\(RegattaView.Key).\(CheckOutAlert.self)"
            struct Message { private static let Key = "\(CheckOutAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }

    // CompetitorView

    struct CompetitorView { private static let Key = "\(CompetitorView.self)"
        struct TeamImageAddButton { private static let Key = "\(CompetitorView.Key).\(TeamImageAddButton.self)"
            struct Title { private static let Key = "\(TeamImageAddButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TeamImageUploadRetryButton { private static let Key = "\(CompetitorView.Key).\(TeamImageUploadRetryButton.self)"
            struct Title { private static let Key = "\(TeamImageUploadRetryButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct SelectImageAlert { private static let Key = "\(CompetitorView.Key).\(SelectImageAlert.self)"
            struct Title { private static let Key = "\(SelectImageAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(SelectImageAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct CameraAction { private static let Key = "\(SelectImageAlert.Key).\(CameraAction.self)"
                struct Title { private static let Key = "\(CameraAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct PhotoLibraryAction { private static let Key = "\(SelectImageAlert.Key).\(PhotoLibraryAction.self)"
                struct Title { private static let Key = "\(PhotoLibraryAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct UploadTeamImageFailureAlert { private static let Key = "\(CompetitorView.Key).\(UploadTeamImageFailureAlert.self)"
            struct Title { private static let Key = "\(UploadTeamImageFailureAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - ScanView
    
    struct ScanView { private static let Key = "\(ScanView.self)"
        struct Title { private static let Key = "\(ScanView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct IncorrectCodeAlert { private static let Key = "\(ScanView.Key).\(IncorrectCodeAlert.self)"
            struct Message { private static let Key = "\(IncorrectCodeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - SettingsView
    
    struct SettingsView { private static let Key = "\(SettingsView.self)"
        struct Title { private static let Key = "\(SettingsView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct TableView { private static let Key = "\(SettingsView.Key).\(TableView.self)"
            struct BatterySavingSection { private static let Key = "\(TableView.Key).\(BatterySavingSection.self)"
                struct Title { private static let Key = "\(BatterySavingSection.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct DeviceSection { private static let Key = "\(TableView.Key).\(DeviceSection.self)"
                struct Title { private static let Key = "\(DeviceSection.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct TrainingSection { private static let Key = "\(TableView.Key).\(TrainingSection.self)"
                struct Title { private static let Key = "\(TrainingSection.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct BatterySavingTitleLabel { private static let Key = "\(SettingsView.Key).\(BatterySavingTitleLabel.self)"
            struct Text { private static let Key = "\(BatterySavingTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct BatterySavingDescriptionLabel { private static let Key = "\(SettingsView.Key).\(BatterySavingDescriptionLabel.self)"
            struct Text { private static let Key = "\(BatterySavingDescriptionLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct DeviceIdentifierTitleLabel { private static let Key = "\(SettingsView.Key).\(DeviceIdentifierTitleLabel.self)"
            struct Text { private static let Key = "\(DeviceIdentifierTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TrainingServerTitleLabel { private static let Key = "\(SettingsView.Key).\(TrainingServerTitleLabel.self)"
            struct Text { private static let Key = "\(TrainingServerTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - SpeedView
    
    struct SpeedView { private static let Key = "\(SpeedView.self)"
        struct TitleLabel { private static let Key = "\(SpeedView.Key).\(TitleLabel.self)"
            struct Text { private static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TimerView
    
    struct TimerView { private static let Key = "\(TimerView.self)"
        struct TitleLabel { private static let Key = "\(TimerView.Key).\(TitleLabel.self)"
            struct Text { private static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TrackingView
    
    struct TrackingView { private static let Key = "\(TrackingView.self)"
        struct TableView { private static let Key = "\(TrackingView.Key).\(TableView.self)"
            struct GPSAccuracyCell { private static let Key = "\(TableView.Key).\(GPSAccuracyCell.self)"
                struct GPSAccuracyTitleLabel { private static let Key = "\(GPSAccuracyCell.Key).\(GPSAccuracyTitleLabel.self)"
                    struct Text { private static let Key = "\(GPSAccuracyTitleLabel.Key).\(Text.self)"
                        static let String = NSLocalizedString(Key, comment: "")
                    }
                }
                struct GPSAccuracyLabel { private static let Key = "\(GPSAccuracyCell.Key).\(GPSAccuracyLabel.self)"
                    struct Text { private static let Key = "\(GPSAccuracyLabel.Key).\(Text.self)"
                        struct NoGPS { private static let Key = "\(Text.Key).\(NoGPS.self)"
                            static let String = NSLocalizedString(Key, comment: "")
                        }
                    }
                }
            }
            struct GPSFixesCell { private static let Key = "\(TableView.Key).\(GPSFixesCell.self)"
                struct GPSFixesTitleLabel { private static let Key = "\(GPSFixesCell.Key).\(GPSFixesTitleLabel.self)"
                    struct Text { private static let Key = "\(GPSFixesTitleLabel.Key).\(Text.self)"
                        static let String = NSLocalizedString(Key, comment: "")
                    }
                }
            }
        }
        struct StopTrackingButton { private static let Key = "\(TrackingView.Key).\(StopTrackingButton.self)"
            struct Title { private static let Key = "\(StopTrackingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StopTrackingAlert { private static let Key = "\(TrackingView.Key).\(StopTrackingAlert.self)"
            struct Title { private static let Key = "\(StopTrackingAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(StopTrackingAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TrainingView
    
    struct TrainingView { private static let Key = "\(TrainingView.self)"
        struct LeaderboardButton { private static let Key = "\(TrainingView.Key).\(LeaderboardButton.self)"
            struct Title { private static let Key = "\(LeaderboardButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CheckOutAlert { private static let Key = "\(TrainingView.Key).\(CheckOutAlert.self)"
            struct Message { private static let Key = "\(CheckOutAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct OptionSheet { private static let Key = "\(TrainingView.Key).\(OptionSheet.self)"
            struct CheckOutAction { private static let Key = "\(OptionSheet.Key).\(CheckOutAction.self)"
                struct Title { private static let Key = "\(CheckOutAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct ReactivateAlert { private static let Key = "\(TrainingView.Key).\(ReactivateAlert.self)"
            struct Title { private static let Key = "\(ReactivateAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(ReactivateAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct ReactivatedAlert {
            static let Key = "\(TrainingView.Key).\(ReactivatedAlert.self)"
            struct Title { private static let Key = "\(ReactivatedAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StartTrackingButton { private static let Key = "\(TrainingView.Key).\(StartTrackingButton.self)"
            struct Title { private static let Key = "\(StartTrackingButton.Key).\(Title.self)"
                static let StringWhenTrainingIsActive = RegattaView.StartTrackingButton.Title.String
                static let StringWhenTrainingIsInActive = TrainingView.ReactivateAlert.Title.String
            }
        }
        struct FinishButton { private static let Key = "\(TrainingView.Key).\(FinishButton.self)"
            struct Title { private static let Key = "\(FinishButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct FinishAlert {
            static let Key = "\(TrainingView.Key).\(FinishAlert.self)"
            struct Title { private static let Key = "\(FinishAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { private static let Key = "\(FinishAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct FinishedAlert { private static let Key = "\(TrainingView.Key).\(FinishedAlert.self)"
            struct Title { private static let Key = "\(FinishedAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
}
