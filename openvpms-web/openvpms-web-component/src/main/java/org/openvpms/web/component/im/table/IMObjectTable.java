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
 *  $Id: IMObjectTable.java 1592 2006-12-04 07:03:58Z tanderson $
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Table of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-12-04 05:03:58 -0200 (Mon, 04 Dec 2006) $
 */
public class IMObjectTable<T extends IMObject> extends IMTable<T> {

    /**
     * Construct a new <code>IMObjectTable</code>.
     */
    public IMObjectTable() {
        this(new DefaultIMObjectTableModel<T>());
    }

    /**
     * Construct a new <code>IMObjectTable</code>.
     *
     * @param model the table model
     */
    public IMObjectTable(IMObjectTableModel<T> model) {
        super(model);
    }

}
