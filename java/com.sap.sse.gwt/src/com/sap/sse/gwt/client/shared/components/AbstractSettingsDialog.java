package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public abstract class AbstractSettingsDialog<SettingsType extends Settings> extends DataEntryDialog<SettingsType> {
    private final SettingsDialogComponent<SettingsType> settingsDialogComponent;

    private LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator;
    private Anchor shareAnchor;
    
    protected AbstractSettingsDialog(final String shortName, SettingsDialogComponent<SettingsType> dialogComponent,
            StringMessages stringMessages, boolean animationEnabled, LinkWithSettingsGenerator<SettingsType> linkWithSettingsGenerator, final DialogCallback<SettingsType> callback) {
        super(stringMessages.settingsForComponent(shortName), null, stringMessages.ok(), stringMessages.cancel(),
                dialogComponent.getValidator(), animationEnabled, callback != null ? callback : new NoOpDialogCallback<SettingsType>());
        this.settingsDialogComponent = dialogComponent;
        
        this.linkWithSettingsGenerator = linkWithSettingsGenerator;
        if(linkWithSettingsGenerator != null) {
            shareAnchor = new Anchor(stringMessages.sharedSettingsLink());
            shareAnchor.getElement().getStyle().setMargin(3, Unit.PX);
            shareAnchor.ensureDebugId("ShareAnchor");
            shareAnchor.setTarget("_blank");
            getLeftButtonPannel().add(shareAnchor);
            
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    onChange(getResult());
                }
            });
        }
    }
    
    @Override
    protected void onChange(SettingsType result) {
        if (linkWithSettingsGenerator != null) {
            String link = linkWithSettingsGenerator.createUrl(result);
            shareAnchor.setHref(link);
        }
    }

    @Override
    protected Widget getAdditionalWidget() {
        return settingsDialogComponent.getAdditionalWidget(this);
    }

    @Override
    protected SettingsType getResult() {
        return settingsDialogComponent.getResult();
    }

    @Override
    protected FocusWidget getInitialFocusWidget() {
        return settingsDialogComponent.getFocusWidget();
    }
    
    private static class NoOpDialogCallback<SettingsType extends Settings> implements DialogCallback<SettingsType> {
        @Override
        public void ok(SettingsType editedObject) {
        }
        @Override
        public void cancel() {
        }
    }
}
