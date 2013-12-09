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

import java.util.List;

import org.uberfire.paging.AbstractPageRow;
import org.uberfire.paging.PageRequest;
import org.uberfire.security.Identity;

public interface PageRowBuilder<REQUEST extends PageRequest, CONTENT> {

    public List<? extends AbstractPageRow> build();

    public void validate();

    public PageRowBuilder<REQUEST, CONTENT> withPageRequest(final REQUEST pageRequest);

    public PageRowBuilder<REQUEST, CONTENT> withIdentity(final Identity identity);

    public PageRowBuilder<REQUEST, CONTENT> withContent(final CONTENT pageRequest);

}
