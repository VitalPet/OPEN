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
 *  $Id: IMObjectListResultSet.java 4996 2013-04-30 13:39:10Z tanderson $
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectSorter;

import java.util.List;


/**
 * Paged result set where the results are pre-loaded from a list.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2013-04-30 10:39:10 -0300 (Tue, 30 Apr 2013) $
 */
public class IMObjectListResultSet<T extends IMObject>
    extends AbstractListResultSet<T> {

    /**
     * The sort criteria.
     */
    private SortConstraint[] sort = EMPTY;

    /**
     * Determines if the set is sorted ascending or descending.
     */
    private boolean sortAscending = true;

    /**
     * Optional transformer to apply to objects when sorting.
     */
    private final Transformer transformer;

    /**
     * Empty sort constraint.
     */
    private static final SortConstraint[] EMPTY = new SortConstraint[0];


    /**
     * Constructs an <tt>IMObjectListResultSet</tt>.
     *
     * @param objects  the objects
     * @param pageSize the maximum no. of results per page
     */
    public IMObjectListResultSet(List<T> objects, int pageSize) {
        this(objects, pageSize, null);
    }

    /**
     * Constructs a new <tt>IMObjectListResultSet</tt>.
     *
     * @param objects     the objects
     * @param pageSize    the maximum no. of results per page
     * @param transformer a transformer to apply to objects when sorting.
     *                    May be <tt>null</tt>
     */
    public IMObjectListResultSet(List<T> objects, int pageSize,
                                 Transformer transformer) {
        this(objects, pageSize, null, transformer);
    }

    /**
     * Constructs a new <tt>IMObjectListResultSet</tt>.
     *
     * @param objects     the objects
     * @param pageSize    the maximum no. of results per page
     * @param transformer a transformer to apply to objects when sorting.
     *                    May be <tt>null</tt>
     * @param sort        the sort criteria. If specified, the objects must have been sorted on this.
     *                    May be <tt>null</tt>
     */
    public IMObjectListResultSet(List<T> objects, int pageSize, SortConstraint[] sort, Transformer transformer) {
        super(objects, pageSize);
        if (sort != null) {
            this.sort = sort;
            if (sort.length > 0) {
                sortAscending = sort[0].isAscending();
            }
        }
        this.transformer = transformer;
    }

    /**
     * Sorts the set. This resets the iterator.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     */
    public void sort(SortConstraint[] sort) {
        if (sort != null && sort.length > 0 && !getObjects().isEmpty()) {
            if (transformer != null) {
                IMObjectSorter.sort(getObjects(), sort, transformer);
            } else {
                IMObjectSorter.sort(getObjects(), sort);
            }
            sortAscending = sort[0].isAscending();
        }
        this.sort = sort;
        reset();
    }

    /**
     * Determines if the node is sorted ascending or descending.
     *
     * @return <code>true</code> if the node is sorted ascending or no sort
     *         constraint was specified; <code>false</code> if it is sorted
     *         descending
     */
    public boolean isSortedAscending() {
        return sortAscending;
    }

    /**
     * Returns the sort criteria.
     *
     * @return the sort criteria. Never null
     */
    public SortConstraint[] getSortConstraints() {
        return sort != null ? sort : EMPTY;
    }

}
