/*
 * Copyright 2013 JBoss Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.guvnor.udc.service;

import org.guvnor.udc.model.InboxPageRequest;
import org.guvnor.udc.model.InboxPageRow;
import org.jboss.errai.bus.server.annotations.Remote;
import org.uberfire.paging.PageResponse;

@Remote
public interface UDCVfsService extends UDCStorageService {

    void addToIncoming(String itemPath, String note, String userFrom, String userName);

    PageResponse<InboxPageRow> loadInbox(InboxPageRequest request);

    String[] getUsersVfs();

    void addToRecentEdited(String itemPath, String note);

    void addToRecentOpened(String itemPath, String note);

}
