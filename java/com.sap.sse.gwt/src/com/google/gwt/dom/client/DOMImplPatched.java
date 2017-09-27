package com.google.gwt.dom.client;

abstract class DOMImplPatched extends DOMImpl {

    public int getScrollLeft(Document doc) {
      return ensureDocumentScrollingElement(doc).getScrollLeft();
    }

    public int getScrollTop(Document doc) {
      return ensureDocumentScrollingElement(doc).getScrollTop();
    }

    public void setScrollLeft(Document doc, int left) {
      ensureDocumentScrollingElement(doc).setScrollLeft(left);
    }

    public void setScrollTop(Document doc, int top) {
      ensureDocumentScrollingElement(doc).setScrollTop(top);
    }

    private Element ensureDocumentScrollingElement(Document document) {
      // In some case (e.g SVG document and old Webkit browsers), getDocumentScrollingElement can
      // return null. In this case, default to documentElement.
      Element scrollingElement = getDocumentScrollingElement(document);
      return scrollingElement != null ? scrollingElement : document.getDocumentElement();
    }

    Element getDocumentScrollingElement(Document doc)  {
      return doc.getViewportElement();
    }
}
