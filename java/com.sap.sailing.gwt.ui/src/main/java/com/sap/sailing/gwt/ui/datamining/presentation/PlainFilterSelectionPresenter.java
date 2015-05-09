package com.sap.sailing.gwt.ui.datamining.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.DataRetrieverChainDefinitionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionPresenter;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionProvider;
import com.sap.sailing.gwt.ui.datamining.FilterSelectionChangedListener;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PlainFilterSelectionPresenter implements FilterSelectionPresenter, FilterSelectionChangedListener,
                                                      DataRetrieverChainDefinitionChangedListener {

    private final FilterSelectionProvider filterSelectionProvider;
    private DataRetrieverChainDefinitionDTO retrieverChain;
    
    private final HorizontalPanel mainPanel;
    private final VerticalPanel presentationPanel;
    
    public PlainFilterSelectionPresenter(StringMessages stringMessages, DataRetrieverChainDefinitionProvider retrieverChainProvider,
            FilterSelectionProvider filterSelectionProvider) {
        this.filterSelectionProvider = filterSelectionProvider;
        this.filterSelectionProvider.addSelectionChangedListener(this);
        retrieverChainProvider.addDataRetrieverChainDefinitionChangedListener(this);

        Label currentSelectionLabel = new Label(stringMessages.currentFilterSelection());
        currentSelectionLabel.setWidth("75px");
        currentSelectionLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        presentationPanel = new VerticalPanel();
        
        mainPanel = new HorizontalPanel();
        mainPanel.add(currentSelectionLabel);
        mainPanel.add(presentationPanel);
    }
    
    @Override
    public void dataRetrieverChainDefinitionChanged(DataRetrieverChainDefinitionDTO newDataRetrieverChainDefinition) {
        if (!Objects.equals(retrieverChain, newDataRetrieverChainDefinition)) {
            retrieverChain = newDataRetrieverChainDefinition;
            presentationPanel.clear();
        }
    }
    
    @Override
    public void selectionChanged() {
        presentationPanel.clear();
        
        Map<Integer, Map<FunctionDTO, Collection<? extends Serializable>>> selection = filterSelectionProvider.getSelection();
        List<Integer> sortedLevels = new ArrayList<>(selection.keySet());
        Collections.sort(sortedLevels);
        boolean first = true;
        for (Integer levelIndex : sortedLevels) {
            Map<FunctionDTO, Collection<? extends Serializable>> levelSelection = selection.get(levelIndex);
            LocalizedTypeDTO level = retrieverChain.getRetrievedDataType(levelIndex);
            RetrieverLevelFilterSelectionPresenter levelSelectionPresenter = new RetrieverLevelFilterSelectionPresenter(level, levelSelection);
            if (!first) {
                levelSelectionPresenter.getEntryWidget().getElement().getStyle().setMarginTop(5, Unit.PX);
            }
            presentationPanel.add(levelSelectionPresenter.getEntryWidget());
            first = false;
        }
    }
    
    private class RetrieverLevelFilterSelectionPresenter {
        
        private final HorizontalPanel mainPanel;

        public RetrieverLevelFilterSelectionPresenter(LocalizedTypeDTO level,
                Map<FunctionDTO, Collection<? extends Serializable>> levelSelection) {
            
            Label levelLabel = new Label(level.getDisplayName());
            levelLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            levelLabel.setWidth("75px");
            
            VerticalPanel presentationPanel = new VerticalPanel();
            List<FunctionDTO> sortedDimensions = new ArrayList<>(levelSelection.keySet());
            Collections.sort(sortedDimensions);
            for (FunctionDTO dimension : sortedDimensions) {
                Collection<? extends Serializable> dimensionSelection = levelSelection.get(dimension);
                DimensionFilterSelectionPresenter dimensionSelectionPresenter = new DimensionFilterSelectionPresenter(dimension, dimensionSelection);
                presentationPanel.add(dimensionSelectionPresenter.getEntryWidget());
            }
            
            mainPanel = new HorizontalPanel();
            mainPanel.add(levelLabel);
            mainPanel.add(presentationPanel);
        }

        public Widget getEntryWidget() {
            return mainPanel;
        }
        
    }
    
    private class DimensionFilterSelectionPresenter {
        
        private final HorizontalPanel mainPanel;

        public DimensionFilterSelectionPresenter(FunctionDTO dimension,
                Collection<? extends Serializable> dimensionSelection) {
            mainPanel = new HorizontalPanel();
            Label dimensionLabel = new Label(dimension.getDisplayName() + ":");
            dimensionLabel.getElement().getStyle().setMarginRight(2, Unit.PX);
            mainPanel.add(dimensionLabel);
            
            StringBuilder selectionStringBuilder = new StringBuilder();
            boolean first = true;
            for (Object selection : dimensionSelection) {
                if (!first) {
                    selectionStringBuilder.append(", ");
                }
                selectionStringBuilder.append(selection.toString());
                first = false;
            }
            mainPanel.add(new Label(selectionStringBuilder.toString()));
        }

        public Widget getEntryWidget() {
            return mainPanel;
        }
        
    }

    @Override
    public String getLocalizedShortName() {
        return getClass().getSimpleName();
    }

    @Override
    public Widget getEntryWidget() {
        return mainPanel;
    }

    @Override
    public boolean isVisible() {
        return mainPanel.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        mainPanel.setVisible(visibility);
    }

    @Override
    public String getDependentCssClassName() {
        return "plainFilterSelectionPresenter";
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

}
