package com.sap.sse.gwt.client.context.impl;

import com.sap.sse.gwt.client.context.data.SapSailingContextDataJSO;

public final class SapSailingContextDataFactoryImpl {

	public native SapSailingContextDataJSO getInstance() /*-{
		return $doc.sailingContext;
	}-*/;

}
