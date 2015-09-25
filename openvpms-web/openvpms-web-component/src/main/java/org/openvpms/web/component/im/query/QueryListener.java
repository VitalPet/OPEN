/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: QueryListener.java 748 2006-04-11 04:09:07Z tanderson $
 */

package org.openvpms.web.component.im.query;

import java.util.EventListener;


/**
 * {@link Query} event listener.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-11 01:09:07 -0300 (Tue, 11 Apr 2006) $
 */
public interface QueryListener extends EventListener {

    /**
     * Invoked when a query is performed.
     */
    void query();
}
