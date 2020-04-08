package com.sap.sse.gwt.client.context.impl;

import com.sap.sse.gwt.client.context.data.SapSailingContextDataJSO;

/**
 * <p>
 * Accessor to the custom object structure used for providing static configuration from the server to the GWT client. 
 * An example can be seen here {@link /com.sap.sailing.gwt.ui/Home.html}
 * </p>
 * <p>
 * {@link SapSailingContextDataJSO} provides access to the individual fields.
 * </p>
 * @see com.sap.sse.gwt.shared.Branding
 * @author Georg Herdt
 *
 */
public final class SapSailingContextDataFactoryImpl {

	public native SapSailingContextDataJSO getInstance() /*-{
		return $doc.sailingContext;
	}-*/;

}
