package com.sap.sse.gwt.client.controls.tabbar;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author zhorvath
 *
 */
public class BreadcrumbPane extends Widget {

	interface Resources extends ClientBundle {
		@Source("Breadcrumb.css")
		Style style();
	}

	interface Style extends CssResource {

		String event_header__breadcrumb();

		String event_header__breadcrumb__item();

	}

	private static Resources resources;

	private static Resources getResources() {
		if (resources == null) {
			resources = GWT.create(Resources.class);
			resources.style().ensureInjected();
		}
		return resources;
	}

	private final Style style = getResources().style();

	private final DivElement breadcrumDiv;

	public BreadcrumbPane() {
		breadcrumDiv = Document.get().createDivElement();
		breadcrumDiv.addClassName(style.event_header__breadcrumb());

		setElement(breadcrumDiv);
	}

	public void addBreadcrumbItem(String title, String link, final Runnable runnable) {

		AnchorElement breadcrumItem = Document.get().createAnchorElement();
		breadcrumItem.setHref(link);
		breadcrumItem.setInnerText(title);
		breadcrumItem.addClassName(style.event_header__breadcrumb__item());

		breadcrumDiv.appendChild(breadcrumItem);

		Event.sinkEvents(breadcrumItem, Event.ONCLICK);
		Event.setEventListener(breadcrumItem, new EventListener() {
			@Override
			public void onBrowserEvent(Event event) {
				if (event.getTypeInt() == Event.ONCLICK) {
					event.stopPropagation();
					event.preventDefault();
					runnable.run();
				}
			}
		});
	}
}
