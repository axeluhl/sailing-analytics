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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.security.ui.oauth.client.model.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.model.SocialUserDTO;

public interface OAuthLoginServiceAsync
{
    public void     getAuthorizationUrl(CredentialDTO credential, AsyncCallback<String> callback);
    public void verifySocialUser(CredentialDTO credential, AsyncCallback<SocialUserDTO> callback);
    public void fetchMe(String sessionId, AsyncCallback<SocialUserDTO> callback);
    public void     getAccessToken(String sessionId, AsyncCallback<String> callback);
    public void       logout(String sessionId, AsyncCallback<Void> callback);
}
