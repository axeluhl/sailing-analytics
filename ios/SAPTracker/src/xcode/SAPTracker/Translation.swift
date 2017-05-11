//
//  Translation.swift
//  SAPTracker
//
//  Created by Raimund Wege on 27.07.16.
//  Copyright © 2016 com.sap.sailing. All rights reserved.
//

import UIKit

class Translation: NSObject {
    
    struct Common { static let Key = "\(Common.self)"
        struct Cancel { static let Key = "\(Common.Key).\(Cancel.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct OK { static let Key = "\(Common.Key).\(OK.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Info { static let Key = "\(Common.Key).\(Info.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Settings { static let Key = "\(Common.Key).\(Settings.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Warning { static let Key = "\(Common.Key).\(Warning.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Error { static let Key = "\(Common.Key).\(Error.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Yes { static let Key = "\(Common.Key).\(Yes.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct No { static let Key = "\(Common.Key).\(No.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - CheckInController
    
    struct CheckInController { static let Key = "\(CheckInController.self)"
        struct WelcomeAlert { static let Key = "\(CheckInController.Key).\(WelcomeAlert.self)"
            struct Title { static let Key = "\(WelcomeAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { static let Key = "\(WelcomeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct CancelAction { static let Key = "\(WelcomeAlert.Key).\(CancelAction.self)"
                struct Title { static let Key = "\(CancelAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
    }
    
    // MARK: - GPSFixController
    
    struct GPSFixController { static let Key = "\(GPSFixController.self)"
        struct Mode { static let Key = "\(GPSFixController.Key).\(Mode.self)"
            static let String = NSLocalizedString(Key, comment: "")
            struct Error { static let Key = "\(Mode.Key).\(Error.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct BatterySaving { static let Key = "\(Mode.Key).\(BatterySaving.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct None { static let Key = "\(Mode.Key).\(None.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Online { static let Key = "\(Mode.Key).\(Online.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Offline { static let Key = "\(Mode.Key).\(Offline.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - LocationManager
    
    struct LocationManager { static let Key = "\(LocationManager.self)"
        struct LocationServicesDeniedError { static let Key = "\(LocationManager.Key).\(LocationServicesDeniedError.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct LocationServicesDisabledError { static let Key = "\(LocationManager.Key).\(LocationServicesDisabledError.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct Status { static let Key = "\(LocationManager.Key).\(Status.self)"
            static let String = NSLocalizedString(Key, comment: "")
            struct Tracking { static let Key = "\(Status.Key).\(Tracking.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct NotTracking { static let Key = "\(Status.Key).\(NotTracking.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - RequestManagerError
    
    struct RequestManagerError { static let Key = "\(RequestManagerError.self)"
        struct CommunicationFailed { static let Key = "\(RequestManagerError.Key).\(CommunicationFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct CheckInDataIsIncomplete { static let Key = "\(RequestManagerError.Key).\(CheckInDataIsIncomplete.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetCompetitorFailed { static let Key = "\(RequestManagerError.Key).\(GetCompetitorFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetEventFailed { static let Key = "\(RequestManagerError.Key).\(GetEventFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetLeaderboardFailed { static let Key = "\(RequestManagerError.Key).\(GetLeaderboardFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetMarkFailed { static let Key = "\(RequestManagerError.Key).\(GetMarkFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct GetTeamFailed { static let Key = "\(RequestManagerError.Key).\(GetTeamFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostCheckInFailed { static let Key = "\(RequestManagerError.Key).\(PostCheckInFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostCheckOutFailed { static let Key = "\(RequestManagerError.Key).\(PostCheckOutFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostGPSFixFailed { static let Key = "\(RequestManagerError.Key).\(PostGPSFixFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PostTeamImageFailed { static let Key = "\(RequestManagerError.Key).\(PostTeamImageFailed.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct TeamImageURLIsInvalid { static let Key = "\(RequestManagerError.Key).\(TeamImageURLIsInvalid.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - AboutView
    
    struct AboutView { static let Key = "\(AboutView.self)"
        struct Title { static let Key = "\(AboutView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct PartnershipTextView { static let Key = "\(AboutView.Key).\(PartnershipTextView.self)"
            struct Text { static let Key = "\(PartnershipTextView.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TermsButton { static let Key = "\(AboutView.Key).\(TermsButton.self)"
            struct Title { static let Key = "\(TermsButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct VersionTitleLabel { static let Key = "\(AboutView.Key).\(VersionTitleLabel.self)"
            struct Text { static let Key = "\(VersionTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - CourseView
    
    struct CourseView { static let Key = "\(CourseView.self)"
        struct TitleLabel { static let Key = "\(CourseView.Key).\(TitleLabel.self)"
            struct Text { static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - HomeView
    
    struct HomeView { static let Key = "\(HomeView.self)"
        struct HeaderTitleLabel { static let Key = "\(HomeView.Key).\(HeaderTitleLabel.self)"
            struct Text { static let Key = "\(HeaderTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct FooterTextView { static let Key = "\(HomeView.Key).\(FooterTextView.self)"
            struct Text { static let Key = "\(FooterTextView.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct InfoCodeLabel { static let Key = "\(HomeView.Key).\(InfoCodeLabel.self)"
            struct Text { static let Key = "\(InfoCodeLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TermsAlert { static let Key = "\(HomeView.Key).\(TermsAlert.self)"
            struct Title { static let Key = "\(TermsAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { static let Key = "\(TermsAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct ShowTermsAction { static let Key = "\(TermsAlert.Key).\(ShowTermsAction.self)"
                struct Title { static let Key = "\(ShowTermsAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct AcceptTermsAction { static let Key = "\(TermsAlert.Key).\(AcceptTermsAction.self)"
                struct Title { static let Key = "\(AcceptTermsAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct NoCameraAlert { static let Key = "\(HomeView.Key).\(NoCameraAlert.self)"
            struct Message { static let Key = "\(NoCameraAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct NoCodeAlert { static let Key = "\(HomeView.Key).\(NoCodeAlert.self)"
            struct Title { static let Key = "\(NoCodeAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { static let Key = "\(NoCodeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - LeaderboardView
    
    struct LeaderboardView { static let Key = "\(LeaderboardView.self)"
        struct Title { static let Key = "\(LeaderboardView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - LicenseView
    
    struct LicenseView { static let Key = "\(LicenseView.self)"
        struct Title { static let Key = "\(LicenseView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
    }
    
    // MARK: - CompetitorView
    
    struct CompetitorView { static let Key = "\(CompetitorView.self)"
        struct CountdownDaysTitleLabel { static let Key = "\(CompetitorView.Key).\(CountdownDaysTitleLabel.self)"
            struct Text { static let Key = "\(CountdownDaysTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CountdownHoursTitleLabel { static let Key = "\(CompetitorView.Key).\(CountdownHoursTitleLabel.self)"
            struct Text { static let Key = "\(CountdownHoursTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct CountdownMinutesTitleLabel { static let Key = "\(CompetitorView.Key).\(CountdownMinutesTitleLabel.self)"
            struct Text { static let Key = "\(CountdownMinutesTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct RegattaStartLabel { static let Key = "\(CompetitorView.Key).\(RegattaStartLabel.self)"
            struct Text { static let Key = "\(RegattaStartLabel.Key).\(Text.self)"
                struct BeforeRegattaDidStart { static let Key = "\(Text.Key).\(BeforeRegattaDidStart.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
                struct AfterRegattaDidStart { static let Key = "\(Text.Key).\(AfterRegattaDidStart.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct AnnouncementLabel { static let Key = "\(CompetitorView.Key).\(AnnouncementLabel.self)"
            struct Text { static let Key = "\(AnnouncementLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct EventButton { static let Key = "\(CompetitorView.Key).\(EventButton.self)"
            struct Title { static let Key = "\(EventButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TeamImageAddButton { static let Key = "\(CompetitorView.Key).\(TeamImageAddButton.self)"
            struct Title { static let Key = "\(TeamImageAddButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct TeamImageUploadRetryButton { static let Key = "\(CompetitorView.Key).\(TeamImageUploadRetryButton.self)"
            struct Title { static let Key = "\(TeamImageUploadRetryButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StartTrackingButton { static let Key = "\(CompetitorView.Key).\(StartTrackingButton.self)"
            struct Title { static let Key = "\(StartTrackingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct OptionSheet { static let Key = "\(CompetitorView.Key).\(OptionSheet.self)"
            struct CheckOutAction { static let Key = "\(OptionSheet.Key).\(CheckOutAction.self)"
                struct Title { static let Key = "\(CheckOutAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct ReplaceImageAction { static let Key = "\(OptionSheet.Key).\(ReplaceImageAction.self)"
                struct Title { static let Key = "\(ReplaceImageAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct UpdateAction { static let Key = "\(OptionSheet.Key).\(UpdateAction.self)"
                struct Title { static let Key = "\(UpdateAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct CheckOutAlert { static let Key = "\(CompetitorView.Key).\(CheckOutAlert.self)"
            struct Message { static let Key = "\(CheckOutAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct SelectImageAlert { static let Key = "\(CompetitorView.Key).\(SelectImageAlert.self)"
            struct Title { static let Key = "\(SelectImageAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { static let Key = "\(SelectImageAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct CameraAction { static let Key = "\(SelectImageAlert.Key).\(CameraAction.self)"
                struct Title { static let Key = "\(CameraAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct PhotoLibraryAction { static let Key = "\(SelectImageAlert.Key).\(PhotoLibraryAction.self)"
                struct Title { static let Key = "\(PhotoLibraryAction.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct UploadTeamImageFailureAlert { static let Key = "\(CompetitorView.Key).\(UploadTeamImageFailureAlert.self)"
            struct Title { static let Key = "\(UploadTeamImageFailureAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - ScanView
    
    struct ScanView { static let Key = "\(ScanView.self)"
        struct Title { static let Key = "\(ScanView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct IncorrectCodeAlert { static let Key = "\(ScanView.Key).\(IncorrectCodeAlert.self)"
            struct Message { static let Key = "\(IncorrectCodeAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - SettingsView
    
    struct SettingsView { static let Key = "\(SettingsView.self)"
        struct Title { static let Key = "\(SettingsView.Key).\(Title.self)"
            static let String = NSLocalizedString(Key, comment: "")
        }
        struct TableView { static let Key = "\(SettingsView.Key).\(TableView.self)"
            struct BatterySavingSection { static let Key = "\(TableView.Key).\(BatterySavingSection.self)"
                struct Title { static let Key = "\(BatterySavingSection.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
            struct OtherSettingsSection { static let Key = "\(TableView.Key).\(OtherSettingsSection.self)"
                struct Title { static let Key = "\(OtherSettingsSection.Key).\(Title.self)"
                    static let String = NSLocalizedString(Key, comment: "")
                }
            }
        }
        struct BatterySavingTitleLabel { static let Key = "\(SettingsView.Key).\(BatterySavingTitleLabel.self)"
            struct Text { static let Key = "\(BatterySavingTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct BatterySavingDescriptionLabel { static let Key = "\(SettingsView.Key).\(BatterySavingDescriptionLabel.self)"
            struct Text { static let Key = "\(BatterySavingDescriptionLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct DeviceIdentifierTitleLabel { static let Key = "\(SettingsView.Key).\(DeviceIdentifierTitleLabel.self)"
            struct Text { static let Key = "\(DeviceIdentifierTitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - SpeedView
    
    struct SpeedView { static let Key = "\(SpeedView.self)"
        struct TitleLabel { static let Key = "\(SpeedView.Key).\(TitleLabel.self)"
            struct Text { static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TimerView
    
    struct TimerView { static let Key = "\(TimerView.self)"
        struct TitleLabel { static let Key = "\(TimerView.Key).\(TitleLabel.self)"
            struct Text { static let Key = "\(TitleLabel.Key).\(Text.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
    // MARK: - TrackingView
    
    struct TrackingView { static let Key = "\(TrackingView.self)"
        struct TableView { static let Key = "\(TrackingView.Key).\(TableView.self)"
            struct GPSAccuracyCell { static let Key = "\(TableView.Key).\(GPSAccuracyCell.self)"
                struct GPSAccuracyTitleLabel { static let Key = "\(GPSAccuracyCell.Key).\(GPSAccuracyTitleLabel.self)"
                    struct Text { static let Key = "\(GPSAccuracyTitleLabel.Key).\(Text.self)"
                        static let String = NSLocalizedString(Key, comment: "")
                    }
                }
                struct GPSAccuracyLabel { static let Key = "\(GPSAccuracyCell.Key).\(GPSAccuracyLabel.self)"
                    struct Text { static let Key = "\(GPSAccuracyLabel.Key).\(Text.self)"
                        struct NoGPS { static let Key = "\(Text.Key).\(NoGPS.self)"
                            static let String = NSLocalizedString(Key, comment: "")
                        }
                    }
                }
            }
            struct GPSFixesCell { static let Key = "\(TableView.Key).\(GPSFixesCell.self)"
                struct GPSFixesTitleLabel { static let Key = "\(GPSFixesCell.Key).\(GPSFixesTitleLabel.self)"
                    struct Text { static let Key = "\(GPSFixesTitleLabel.Key).\(Text.self)"
                        static let String = NSLocalizedString(Key, comment: "")
                    }
                }
            }
        }
        struct StopTrackingButton { static let Key = "\(TrackingView.Key).\(StopTrackingButton.self)"
            struct Title { static let Key = "\(StopTrackingButton.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
        struct StopTrackingAlert { static let Key = "\(TrackingView.Key).\(StopTrackingAlert.self)"
            struct Title { static let Key = "\(StopTrackingAlert.Key).\(Title.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
            struct Message { static let Key = "\(StopTrackingAlert.Key).\(Message.self)"
                static let String = NSLocalizedString(Key, comment: "")
            }
        }
    }
    
}
