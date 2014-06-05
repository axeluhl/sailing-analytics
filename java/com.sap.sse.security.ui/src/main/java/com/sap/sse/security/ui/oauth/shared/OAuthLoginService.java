/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.sap.sse.security.ui.oauth.shared;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.sap.sse.security.ui.oauth.client.model.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.model.SocialUserDTO;


@RemoteServiceRelativePath("OAuthLoginService")
public interface OAuthLoginService extends RemoteService
{
    public String     getAuthorizationUrl(CredentialDTO credential) throws OAuthException;
    public SocialUserDTO verifySocialUser(CredentialDTO credential) throws OAuthException;
    public SocialUserDTO fetchMe(String sessionId) throws OAuthException;
    public String     getAccessToken(String sessionId) throws OAuthException;
    public void       logout(String sessionId) throws OAuthException;
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util
    {
        private static OAuthLoginServiceAsync instance;

        public static OAuthLoginServiceAsync getInstance()
        {
            if (instance == null)
            {
                instance=GWT.create(OAuthLoginService.class);
            }
            return instance;
        }
    }
}
