package com.sap.sailing.gwt.home.desktop.partials.searchresult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

public interface SearchResultResources extends ClientBundle {
    public static final SearchResultResources INSTANCE = GWT.create(SearchResultResources.class);

    @Source("SearchResult.gss")
    LocalCss css();
    
    @Source("paging-icons.png")
    ImageResource paging();
    
    @Source("search-icon-grey.png")
    ImageResource search();

    public interface LocalCss extends CssResource {
        String paging();
        String paging_perpage();
        String paging_link();
        String paging_link_label();
        String paging_linkperpage();
        String paging_linkactive();
        String paging_linkprevious();
        String paging_linknext();
        String dropdown();
        String dropdown_content();
        String dropdown_head();
        String dropdown_head_title();
        String dropdown_head_titlebutton();
        String dropdown_content_link();
        String dropdown_content_link_title();
        String dropdown_content_link_subtitle();
        String searchresult();
        String searchresult_form();
        String searchresult_form_label();
        String searchresult_form_input();
        String searchresult_form_button();
        String searchresult_header();
        String searchresult_header_formlabel();
        String searchresult_header_input();
        String searchresult_content();
        String searchresult_content_amount();
        String searchresult_content_amount_relevance();
        String searchresult_content_amount_relevance_label();
        String searchresult_content_item();
        String searchresult_content_item_headline();
        String searchresult_content_item_description();
        String searchresult_content_item_description_morelink();
        String searchresult_content_item_footer();
        String searchresult_content_item_footer_link();
        String searchresult_content_item_footer_separator();
        String searchresult_footer();
    }
}
