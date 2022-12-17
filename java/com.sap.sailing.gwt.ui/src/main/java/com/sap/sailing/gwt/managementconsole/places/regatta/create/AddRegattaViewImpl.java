package com.sap.sailing.gwt.managementconsole.places.regatta.create;

import java.io.IOException;
import java.util.Arrays;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RankingMetrics;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.common.client.suggestion.BoatClassMasterdataSuggestOracle;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.RankingMetricTypeFormatter;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;

public class AddRegattaViewImpl extends Composite implements AddRegattaView {

    interface AddRegattaViewUiBinder extends UiBinder<Widget, AddRegattaViewImpl> {
    }

    private static AddRegattaViewUiBinder uiBinder = GWT.create(AddRegattaViewUiBinder.class);

    @UiField
    Button addRegattaButton;
    @UiField
    Anchor back;
    @UiField(provided = true)
    ValueListBox<ScoringSchemeType> scoringSystemListBox;
    @UiField(provided = true)
    SuggestBox boatClassNameTextBox;
    @UiField(provided = true)
    ValueListBox<RankingMetrics> rankingListBox;
    @UiField(provided = true)
    IntegerBox racesInput;
    @UiField
    InputElement regattaNameInput;
    @UiField
    DivElement validationUi;
    
    private Presenter presenter;
    
    public AddRegattaViewImpl() {
        createBoatClassNameTextBox();
        createScoringSystemListBox();
        createRankingListBox();
        createRacesTextBox();
        initWidget(uiBinder.createAndBindUi(this));
        createRegattaDefaults();
      
        back.addClickHandler(e -> presenter.cancelAddRegatta());
        addRegattaButton.addClickHandler(e -> validate());
    }

    private void validate() {
        validationUi.setInnerHTML("");
        if (regattaNameInput.getValue() == null || regattaNameInput.getValue().isEmpty()) {
            showValidationFailure("Please fill in regatta name");
        } 
        if (!isRegattaNameValid()) {
            showValidationFailure("Please change Regatta name. It is not unique");
        }
        if (boatClassNameTextBox.getValue() == null || boatClassNameTextBox.getValue().isEmpty()) {
            showValidationFailure("Please fill in boat class");
        }
        if (validationUi.getInnerHTML().isEmpty()) {
            presenter.addRegatta(regattaNameInput.getValue(), boatClassNameTextBox.getValue(), 
                    rankingListBox.getValue(), racesInput.getValue(), scoringSystemListBox.getValue());
        }
    }
    
    private boolean isRegattaNameValid() {
        return presenter.validateRegattaName(regattaNameInput.getValue());
    }
    
    private void showValidationFailure(String validationMessage) {
        DivElement validationMsg = Document.get().createDivElement();
        validationMsg.setClassName(ManagementConsoleResources.INSTANCE.fonts().text());
        validationMsg.setInnerText(validationMessage);
        validationUi.appendChild(validationMsg);
    }
    
    private void createScoringSystemListBox() {
        scoringSystemListBox = new ValueListBox<ScoringSchemeType>( new Renderer<ScoringSchemeType>() {
            @Override
            public String render(ScoringSchemeType value) {
              return value == null ? "" : ScoringSchemeTypeFormatter.format(value, StringMessages.INSTANCE);
            }
            @Override
            public void render(ScoringSchemeType value, Appendable appendable) throws IOException {
              if ( appendable != null ) {
                appendable.append(value == null ? "" : ScoringSchemeTypeFormatter.format(value, StringMessages.INSTANCE));
              }
            }
          } );
        scoringSystemListBox.ensureDebugId("ScoringSystemListBox");
        scoringSystemListBox.setAcceptableValues(Arrays.asList(ScoringSchemeType.values()));
    }
    
    private void createBoatClassNameTextBox() {
        this.boatClassNameTextBox = new SuggestBox(new BoatClassMasterdataSuggestOracle());
    }
    
    private void createRankingListBox() {
        this.rankingListBox = new ValueListBox<RankingMetrics>( new Renderer<RankingMetrics>() {
            @Override
            public String render(RankingMetrics value) {
              return value == null ? "" : RankingMetricTypeFormatter.format(value, StringMessages.INSTANCE);
            }
            @Override
            public void render(RankingMetrics value, Appendable appendable) throws IOException {
              if ( appendable != null ) {
                appendable.append(value == null ? "" : RankingMetricTypeFormatter.format(value, StringMessages.INSTANCE));
              }
            }
          } );
        rankingListBox.ensureDebugId("rankingListBox");
        rankingListBox.setAcceptableValues(Arrays.asList(RankingMetrics.values()));
    }
    
    private void createRacesTextBox() {
        racesInput = new IntegerBox();
        racesInput.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(!Character.isDigit(event.getCharCode()))
                    ((IntegerBox)event.getSource()).cancelKey();
            }
        });
        racesInput.getElement().getStyle().setProperty("minWidth", "2rem");
        racesInput.setMaxLength(3);
        racesInput.setValue(10);
    }
    
    private void createRegattaDefaults() {
        scoringSystemListBox.setValue(ScoringSchemeType.LOW_POINT);
        boatClassNameTextBox.setValue(BoatClassDTO.DEFAULT_NAME);     
        rankingListBox.setValue(RankingMetrics.ONE_DESIGN);
    }
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResize() {
        // TODO Auto-generated method stub
    }

}