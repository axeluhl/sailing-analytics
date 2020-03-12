package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Map;
import java.util.function.Function;

import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.orc.ORCCertificate;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.Util;

public class ORCCertificateDisplayPanel extends DockPanel {
    public ORCCertificateDisplayPanel(ORCCertificate certificate, StringMessages stringMessages) {
        final VerticalPanel panel = new VerticalPanel();
        add(panel, DockPanel.CENTER);
        // header
        final CaptionPanel headerCaptionPanel = new CaptionPanel();
        final Grid headerData = new Grid(10, 2);
        headerCaptionPanel.add(headerData);
        panel.add(headerCaptionPanel);
        int row=0;
        headerData.setWidget(row, 0, new Label(stringMessages.certificateIssuingDate()));
        headerData.setWidget(row++, 1, new Label(certificate.getIssueDate().toString()));
        headerData.setWidget(row, 0, new Label(stringMessages.issuingCountry()));
        final CountryCode issuingCountry = certificate.getIssuingCountry();
        headerData.setWidget(row++, 1,
                new Label(issuingCountry==null ? stringMessages.empty() : (issuingCountry.getThreeLetterIOCCode() + " (" + issuingCountry.getName() + ")")));
        headerData.setWidget(row, 0, new Label(stringMessages.orcFileId()));
        headerData.setWidget(row++, 1, new Label(certificate.getFileId()));
        headerData.setWidget(row, 0, new Label(stringMessages.orcCertificateReferenceNumber()));
        headerData.setWidget(row++, 1, new Label(certificate.getReferenceNumber()));
        headerData.setWidget(row, 0, new Label(stringMessages.boat()));
        headerData.setWidget(row++, 1, new Label(certificate.getBoatName()));
        headerData.setWidget(row, 0, new Label(stringMessages.sailID()));
        headerData.setWidget(row++, 1, new Label(certificate.getSailNumber()));
        headerData.setWidget(row, 0, new Label(stringMessages.boatClass()));
        headerData.setWidget(row++, 1, new Label(certificate.getBoatClassName()));
        headerData.setWidget(row, 0, new Label(stringMessages.gph()));
        headerData.setWidget(row++, 1, new Label(Util.padPositiveValue(certificate.getGPHInSecondsToTheMile(), 1, 1, /* round */ true)));
        headerData.setWidget(row, 0, new Label(stringMessages.cdl()));
        headerData.setWidget(row++, 1, new Label(Util.padPositiveValue(certificate.getCDL(), 1, 3, /* round */ true)));
        headerData.setWidget(row, 0, new Label(stringMessages.lengthOverAllInMeters()));
        headerData.setWidget(row++, 1, new Label(Util.padPositiveValue(certificate.getLengthOverAll().getMeters(), 1, 3, /* round */ true)));
        // time allowances
        final CaptionPanel timeAllowancesCaptionPanel = new CaptionPanel(stringMessages.timeAllowances());
        panel.add(timeAllowancesCaptionPanel);
        // the number of rows is one for the wind velocity header, plus the number of TWA values, plus beat VMG,
        // plus run VMG, plus four allowances for the selected courses, plus beat angles, plus gybe angles
        final Grid timeAllowancesData = new Grid(1+certificate.getTrueWindAngles().length+1+1+4+1+1, 1+certificate.getTrueWindSpeeds().length);
        timeAllowancesCaptionPanel.add(timeAllowancesData);
        row=0;
        fillLine(certificate, stringMessages.windSpeed(), row++, timeAllowancesData, tws->Util.padPositiveValue(tws.getKnots(), 1, 1, /* round */ true)+stringMessages.knotsUnit());
        final Map<Speed, Duration> beatAllowances = certificate.getBeatAllowances();
        fillLine(certificate, stringMessages.beatVMG(), row++, timeAllowancesData, tws->Util.padPositiveValue(beatAllowances.get(tws).asSeconds(), 1, 1, /* round */ true));
        for (final Bearing twa : certificate.getTrueWindAngles()) {
            final Map<Speed, Map<Bearing, Speed>> allowances = certificate
                    .getVelocityPredictionPerTrueWindSpeedAndAngle();
            fillLine(certificate, Util.padPositiveValue(twa.getDegrees(), 1, 1, /* round */ true), row++,
                    timeAllowancesData, tws -> getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(allowances.get(tws).get(twa)));
        }
        final Map<Speed, Duration> runAllowances = certificate.getRunAllowances();
        fillLine(certificate, stringMessages.runVMG(), row++, timeAllowancesData, tws->Util.padPositiveValue(runAllowances.get(tws).asSeconds(), 1, 1, /* round */ true));
        // selected courses:
        final Map<Speed, Speed> windwardLeewardSpeedPrediction = certificate.getWindwardLeewardSpeedPrediction();
        fillLine(certificate, "Windward/Leeward", row++, timeAllowancesData, tws->getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(windwardLeewardSpeedPrediction.get(tws)));
        final Map<Speed, Speed> circularRandomSpeedPrediction = certificate.getCircularRandomSpeedPredictions();
        fillLine(certificate, "Circular Random", row++, timeAllowancesData, tws->getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(circularRandomSpeedPrediction.get(tws)));
        final Map<Speed, Speed> coastalLongDistanceSpeedPrediction = certificate.getLongDistanceSpeedPredictions();
        fillLine(certificate, "Coastal / Long Distance", row++, timeAllowancesData, tws->getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(coastalLongDistanceSpeedPrediction.get(tws)));
        final Map<Speed, Speed> nonSpinnakerSpeedPrediction = certificate.getNonSpinnakerSpeedPredictions();
        fillLine(certificate, "Non Spinnaker", row++, timeAllowancesData, tws->getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(nonSpinnakerSpeedPrediction.get(tws)));
        final Map<Speed, Bearing> beatAngles = certificate.getBeatAngles();
        fillLine(certificate, stringMessages.beatAngles(), row++, timeAllowancesData, tws->Util.padPositiveValue(beatAngles.get(tws).getDegrees(), 1, 1, /* round */ true));
        final Map<Speed, Bearing> runAngles = certificate.getRunAngles();
        fillLine(certificate, stringMessages.runAngles(), row++, timeAllowancesData, tws->Util.padPositiveValue(runAngles.get(tws).getDegrees(), 1, 1, /* round */ true));
        final Anchor linkToCertificate = new Anchor(new SafeHtmlBuilder().appendEscaped(stringMessages.linkToOrcCertificate()).toSafeHtml(),
                "https://data.orc.org/public/WPub.dll/CC/"+certificate.getReferenceNumber(), /* target */ "_blank");
        panel.add(linkToCertificate);
    }
    
    private String getVelocityPredictionAsSecondsToTheNauticalMileRoundedToOneTenth(Speed velocityPrediction) {
        return Util.padPositiveValue(ORCCertificate.NAUTICAL_MILE.atSpeed(velocityPrediction).asSeconds(), 1, 1, /* round */ true);
    }

    private void fillLine(ORCCertificate certificate, String lineLabel, int row, final Grid grid, Function<Speed, String> twsToValueLabel) {
        int column=0;
        grid.setWidget(row, column++, new Label(lineLabel));
        for (final Speed trueWindSpeed : certificate.getTrueWindSpeeds()) {
            final Label valueLabel = new Label(twsToValueLabel.apply(trueWindSpeed));
            valueLabel.setHorizontalAlignment(ALIGN_RIGHT);
            grid.setWidget(row, column++, valueLabel);
        }
    }
}
