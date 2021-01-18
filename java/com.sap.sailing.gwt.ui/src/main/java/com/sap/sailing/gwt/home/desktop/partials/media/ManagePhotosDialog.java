package com.sap.sailing.gwt.home.desktop.partials.media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.media.SailingImageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.media.GalleryImageHolder;
import com.sap.sailing.gwt.ui.leaderboard.DialogBoxExt;

public class ManagePhotosDialog extends DialogBoxExt {
    
    private Logger logger = Logger.getLogger(this.getClass().getName());

    private final FlowPanel contentPanel;
    private final SharedResources res;
    private final MediaPageResources local_res;
    private final StringMessages stringMessages;

    public ManagePhotosDialog(StringMessages stringMessages, SharedResources res, MediaPageResources local_res) {
        super(new FlowPanel(), "Manage Photos", true);
        this.res = res;
        this.local_res = local_res;
        this.stringMessages = stringMessages;
        setTitle("Hello");
        setText("Hello");
        setWidth("100%");

        Panel buttonsPanel = new FlowPanel();

        Button confirm = new Button(stringMessages.confirm());
        confirm.addStyleName("gwt-Button");
        confirm.addClickHandler(event -> {
            hide();
        });
        buttonsPanel.add(confirm);

        Button cancel = new Button(stringMessages.cancel());
        cancel.addStyleName("gwt-Button");
        cancel.addClickHandler(event -> {
            hide();
        });
        buttonsPanel.add(cancel);

        
        contentPanel = new FlowPanel();
        contentPanel.setWidth("1200px");
        contentPanel.addStyleName(this.local_res.css().photolist());
        contentPanel.addStyleName(this.res.mediaCss().grid());
        contentPanel.add(buttonsPanel);
        setWidget(contentPanel);
    }
    
    public void show(Collection<SailingImageDTO> photos) {
        Collection<SailingImageDTO> photoList = new ArrayList<SailingImageDTO>(photos);
        contentPanel.clear();
        logger.info("Show mananage photo dialog");
        if (photos != null) {
            for (final SailingImageDTO holder : photoList) {
                if (holder.getSourceRef() != null) {
                    GalleryImageHolder gih = new GalleryImageHolder(holder);
                    gih.addStyleName(res.mediaCss().medium3());
                    gih.addStyleName(res.mediaCss().columns());
                    contentPanel.add(gih);
                }
            }
        }
        center();
    }

}
