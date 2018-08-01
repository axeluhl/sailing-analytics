package com.sap.sse.datamining.ui.client.presentation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.ui.client.FilterSelectionPresenter;
import com.sap.sse.datamining.ui.client.FilterSelectionProvider;
import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.AbstractComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class PlainFilterSelectionPresenter extends AbstractComponent<AbstractSettings> implements FilterSelectionPresenter {

    private final FilterSelectionProvider filterSelectionProvider;

    private final HorizontalPanel mainPanel;
    private final VerticalPanel presentationPanel;

    public PlainFilterSelectionPresenter(Component<?> parent, ComponentContext<?> context,
            StringMessages stringMessages, FilterSelectionProvider filterSelectionProvider) {
        super(parent, context);
        this.filterSelectionProvider = filterSelectionProvider;
        this.filterSelectionProvider.addSelectionChangedListener(this);

        Label currentSelectionLabel = new Label(stringMessages.currentFilterSelection());
        currentSelectionLabel.setWidth("75px");
        currentSelectionLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        presentationPanel = new VerticalPanel();

        mainPanel = new HorizontalPanel();
        mainPanel.add(currentSelectionLabel);
        mainPanel.add(presentationPanel);
    }

    @Override
    public void selectionChanged() {
        presentationPanel.clear();

        Map<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> selection = filterSelectionProvider
                .getSelection();
        List<DataRetrieverLevelDTO> sortedLevels = new ArrayList<>(selection.keySet());
        Collections.sort(sortedLevels);
        boolean first = true;
        for (DataRetrieverLevelDTO retrieverLevel : sortedLevels) {
            Map<FunctionDTO, HashSet<? extends Serializable>> levelSelection = selection.get(retrieverLevel);
            RetrieverLevelFilterSelectionPresenter levelSelectionPresenter = new RetrieverLevelFilterSelectionPresenter(
                    retrieverLevel, levelSelection);
            if (!first) {
                levelSelectionPresenter.getEntryWidget().getElement().getStyle().setMarginTop(5, Unit.PX);
            }
            presentationPanel.add(levelSelectionPresenter.getEntryWidget());
            first = false;
        }
    }

    private class RetrieverLevelFilterSelectionPresenter {

        private final HorizontalPanel mainPanel;

        public RetrieverLevelFilterSelectionPresenter(DataRetrieverLevelDTO retrieverLevel,
                Map<FunctionDTO, HashSet<? extends Serializable>> levelSelection) {

            Label levelLabel = new Label(retrieverLevel.getRetrievedDataType().getDisplayName());
            levelLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            levelLabel.setWidth("100px");

            VerticalPanel presentationPanel = new VerticalPanel();
            List<FunctionDTO> sortedDimensions = new ArrayList<>(levelSelection.keySet());
            Collections.sort(sortedDimensions);
            for (FunctionDTO dimension : sortedDimensions) {
                Collection<? extends Serializable> dimensionSelection = levelSelection.get(dimension);
                DimensionFilterSelectionPresenter dimensionSelectionPresenter = new DimensionFilterSelectionPresenter(
                        dimension, dimensionSelection);
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
    public SettingsDialogComponent<AbstractSettings> getSettingsDialogComponent(AbstractSettings settings) {
        return null;
    }

    @Override
    public void updateSettings(AbstractSettings newSettings) {
    }

    @Override
    public AbstractSettings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "PlainFilterSelectionPresenter";
    }
}
