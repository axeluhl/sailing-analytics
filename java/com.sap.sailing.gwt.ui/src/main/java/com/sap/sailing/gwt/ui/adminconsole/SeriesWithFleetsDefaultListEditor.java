package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.listedit.ListEditorUiStrategy;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SeriesWithFleetsDefaultListEditor extends
                               SeriesWithFleetsListEditor {

                public SeriesWithFleetsDefaultListEditor(List<SeriesDTO> initialValues,
                                               StringMessages stringMessages, ImageResource removeImage,
                                               boolean enableFleetRemoval) {
                               super(initialValues, stringMessages, removeImage, enableFleetRemoval);
                               // TODO Auto-generated constructor stub
                }
                
                protected ListEditorUiStrategy<SeriesDTO> createExpandedUi(StringMessages stringMessages, ImageResource removeImage, boolean enableFleetRemoval){
                               return new ExpandedUiDefault(stringMessages, removeImage, enableFleetRemoval);
                }
                
                private static class ExpandedUiDefault extends ExpandedUi{

                               public ExpandedUiDefault(StringMessages stringMessages,
                                                               ImageResource removeImage, boolean canRemoveItems) {
                                               super(stringMessages, removeImage, canRemoveItems);
                                               // TODO Auto-generated constructor stub
                               }
                               
                               protected Widget createAddWidget() {
                Button addSeriesButton = new Button(stringMessages.setDefaultSeries());
                               addSeriesButton.ensureDebugId("SetDefaultSeriesButton");
                               addSeriesButton.addClickHandler(new ClickHandler() {
                                   @Override
                                   public void onClick(ClickEvent event) {
                                       SeriesWithFleetsDefaultCreateDialog dialog = new SeriesWithFleetsDefaultCreateDialog(Collections
                                               .unmodifiableCollection(context.getValue()), stringMessages, new DialogCallback<SeriesDTO>() {
                                           @Override
                                           public void cancel() {
                                           }

                                           @Override
                                            public void ok(SeriesDTO newSeries) {
                                               addValue(newSeries);
                                           }
                                       });
                                       dialog.ensureDebugId("DefaultSeriesCreateDialog");
                                       dialog.show();
                                   }
                               });
                               return addSeriesButton;
        }
                               
                }
                
                

}

