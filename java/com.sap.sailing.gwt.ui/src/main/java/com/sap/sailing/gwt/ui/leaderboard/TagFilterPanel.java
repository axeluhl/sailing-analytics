package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterUIFactory;
import com.sap.sailing.gwt.ui.client.shared.filter.FilterWithUI;
import com.sap.sailing.gwt.ui.leaderboard.CompetitorFilterResources.CompetitorFilterCss;
import com.sap.sailing.gwt.ui.shared.TagDTO;

/**
 * 
 * @author Julian Rendl (D067890)
 * 
 */
public class TagFilterPanel extends FlowPanel implements KeyUpHandler, FilterWithUI<TagDTO>{
    
    private final static CompetitorFilterCss css = CompetitorFilterResources.INSTANCE.css();
    private final TextBox searchTextBox;
    private final Button clearTextBoxButton;
    private final Button settingsButton;
    private final Button advancedSettingsButton;
    private final RaceIdentifier selectedRaceIdentifier;
    private final FlowPanel searchBoxPanel;
    private final StringMessages stringMessages;

    public TagFilterPanel(RaceIdentifier selectedRaceIdentifier, StringMessages stringMessages) {
        css.ensureInjected();
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.stringMessages = stringMessages;
        this.setStyleName(css.competitorFilterContainer());
        
        
        settingsButton = new Button();
        settingsButton.ensureDebugId("tagSettingsButton");
        settingsButton.setTitle(stringMessages.settings());
        settingsButton.setStyleName(css.button());
        settingsButton.addStyleName(css.settingsButton());
        settingsButton.addStyleName(css.settingsButtonBackgroundImage());
        
        Button submitButton = new Button();
        submitButton.setStyleName(css.button());
        submitButton.addStyleName(css.searchButton());
        submitButton.addStyleName(css.searchButtonBackgroundImage());
        
        searchTextBox = new TextBox();
        searchTextBox.getElement().setAttribute("placeholder", stringMessages.searchCompetitorsBySailNumberOrName());
        searchTextBox.addKeyUpHandler(this);
        searchTextBox.setStyleName(css.searchInput());
        
        clearTextBoxButton = new Button();
        clearTextBoxButton.setStyleName(css.button());
        clearTextBoxButton.addStyleName(css.clearButton());
        clearTextBoxButton.addStyleName(css.clearButtonBackgroundImage());
        clearTextBoxButton.addStyleName(css.hiddenButton());
        clearTextBoxButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });
        
        advancedSettingsButton = new Button("");
        advancedSettingsButton.setStyleName(css.button());
        advancedSettingsButton.addStyleName(css.filterButton());
        advancedSettingsButton.setTitle(stringMessages.competitorsFilter());
        advancedSettingsButton.addStyleName(css.filterInactiveButtonBackgroundImage());
        advancedSettingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                //showEditCompetitorsFiltersDialog();
            }
        });
        
        searchBoxPanel = new FlowPanel();
        searchBoxPanel.setStyleName(css.searchBox());
        searchBoxPanel.add(submitButton);
        searchBoxPanel.add(searchTextBox);
        searchBoxPanel.add(clearTextBoxButton);
        add(searchBoxPanel);
        add(settingsButton);
        add(advancedSettingsButton);
    }

    public void clearSelection() {
        searchTextBox.setText("");
        clearTextBoxButton.addStyleName(css.hiddenButton());
        onKeyUp(null);
    }
    
    @Override
    public boolean matches(TagDTO object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String validate(StringMessages stringMessages) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return getName();
    }

    @Override
    public FilterWithUI<TagDTO> copy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FilterUIFactory<TagDTO> createUIFactory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        // TODO Auto-generated method stub
        
    }
}