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
 *  $Id: BrowserListener.java 3794 2010-08-10 00:02:42Z tanderson $
 */

package org.openvpms.web.component.im.query;


/**
 * Event listener for {@link Browser} events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2010-08-09 21:02:42 -0300 (Mon, 09 Aug 2010) $
 */
public interface BrowserListener<T> extends QueryListener {

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    void selected(T object);

    /**
     * Invoked when an object is browsed.
     *
     * @param object the browsed object
     */
    void browsed(T object);
}
