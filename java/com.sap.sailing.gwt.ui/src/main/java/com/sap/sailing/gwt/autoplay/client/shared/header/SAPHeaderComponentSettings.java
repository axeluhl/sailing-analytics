package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.StringSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class SAPHeaderComponentSettings extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    private static final long serialVersionUID = -4504543757235801224L;
    
    private StringSetting title;

    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        title = new StringSetting("title", this,"");
    }
    
    public SAPHeaderComponentSettings(String title) {
        super(null);
        this.title.setValue(title);
    }

    public String getTitle() {
        return title.getValue();
    }
}
