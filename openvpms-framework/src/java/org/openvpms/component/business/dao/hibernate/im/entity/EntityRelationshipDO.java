/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.openvpms.component.business.domain.im.common.EntityRelationship;


/**
 * Data object interface corresponding to the {@link EntityRelationship}
 * class.
 *
 * @author Tim Anderson
 */
public interface EntityRelationshipDO extends SequencedRelationshipDO {

    /**
     * Returns the entity identity.
     *
     * @return the entity identity. May be <tt>null</tt>.
     */
    EntityIdentityDO getIdentity();

    /**
     * Sets the entity identity.
     *
     * @param identity the identity. May be <tt>null</tt>
     */
    void setIdentity(EntityIdentityDO identity);

}
