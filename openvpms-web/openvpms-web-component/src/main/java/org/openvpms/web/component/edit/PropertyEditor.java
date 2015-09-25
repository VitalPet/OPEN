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
 *  $Id: PropertyEditor.java 891 2006-05-15 04:58:37Z tanderson $
 */

package org.openvpms.web.component.edit;

import org.openvpms.web.component.property.Property;


/**
 * Property editor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-15 04:58:37Z $
 */
public interface PropertyEditor extends Editor {

    /**
     * Returns the property being edited.
     *
     * @return the property being edited
     */
    Property getProperty();
}