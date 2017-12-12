package com.sap.sse.security.ui.client.component;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.sap.sse.common.Util;
import com.sap.sse.security.ui.client.IconResources;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserList extends CellList<UserDTO> {
    public UserList() {
        super(new UserCell());
    }

    public static class UserCell extends AbstractCell<UserDTO> {
        @Override
        public void render(Context context, UserDTO value, SafeHtmlBuilder sb) {
            if (value == null) {
                return;
            }
            ImageResourceRenderer renderer = new ImageResourceRenderer();
            final ImageResource userImageResource = IconResources.INSTANCE.userIcon();
            sb.appendHtmlConstant("<table>");
            sb.appendHtmlConstant("<tr>");
            sb.appendHtmlConstant("<td>");
            sb.append(renderer.render(userImageResource));
            sb.appendHtmlConstant("</td>");
            sb.appendHtmlConstant("<td>");
            sb.appendHtmlConstant("<div>");
            sb.appendEscaped(value.getName()+" ("+Util.join(", ", value.getUserGroups())+")");
            sb.appendHtmlConstant("</div>");
            sb.appendHtmlConstant("</td>");
            sb.appendHtmlConstant("</tr>");
            sb.appendHtmlConstant("</table>");
        }
        
    }
}
