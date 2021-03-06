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

package org.openvpms.web.component.property;

import java.util.List;


/**
 * Collection property that provides notification on modification.
 *
 * @author Tim Anderson
 */
public interface CollectionProperty extends Property {

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    void add(Object value);

    /**
     * Remove a value.
     *
     * @param value the value to remove
     * @return {@code true} if the value was removed
     */
    boolean remove(Object value);

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    List getValues();

    /**
     * Returns the no. of elements in the collection
     *
     * @return the no. of elements in the collection
     */
    int size();

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    int getMinCardinality();

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or {@code -1} if it is unbounded
     */
    int getMaxCardinality();

    /**
     * Determines the relationship of the elements of the collection to the object.
     *
     * @return {@code true} if the objects are children of the parent object, or {@code false} if they are its peer
     */
    boolean isParentChild();
}
