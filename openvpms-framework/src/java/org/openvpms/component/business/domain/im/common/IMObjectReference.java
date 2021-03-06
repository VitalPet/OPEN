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


package org.openvpms.component.business.domain.im.common;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.archetype.ArchetypeId;

import java.io.Serializable;


/**
 * This class holds a reference to another {@link IMObject}. To create a
 * valid reference you must supply an archetypeId and the linkId.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public class IMObjectReference implements Serializable, Cloneable {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The archetype identity.
     */
    private ArchetypeId archetypeId;

    /**
     * The persistent identity of the object.
     */
    private long id = -1;

    /**
     * The object link identifier, a UUID used to link objects until they can
     * be made persistent, and to provide support for object equality.
     */
    private String linkId;

    /**
     * The hash code.
     */
    private transient int hashCode;


    /**
     * Default constructor provided for serialization.
     */
    protected IMObjectReference() {
        // do nothing
    }

    /**
     * Constructs an {@link IMObjectReference} from the specified {@link IMObject}.
     *
     * @param object the object
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public IMObjectReference(IMObject object) {
        if (object == null) {
            throw new IllegalArgumentException("Invalid argument 'object'");
        }
        this.archetypeId = object.getArchetypeId();
        setId(object.getId());
        setLinkId(object.getLinkId());
    }

    /**
     * Constructs an {@link IMObjectReference} for the specified archetype and persistent id.
     *
     * @param shortName the archetype short name of the object
     * @param id        the persistent identity of the object
     * @throws IllegalArgumentException if the archetype id is {@code null}
     */
    public IMObjectReference(String shortName, long id) {
        this(new ArchetypeId(shortName), id, null);
    }

    /**
     * Constructs an {@link IMObjectReference} for the specified archetype id and persistent id.
     *
     * @param archetypeId the archetype id of the object
     * @param id          the persistent identity of the object
     * @throws IllegalArgumentException if the archetype id is {@code null}
     */
    public IMObjectReference(ArchetypeId archetypeId, long id) {
        this(archetypeId, id, null);
    }

    /**
     * Constructs an {@link IMObjectReference} for the specified archetype short name, persistent id, and link id.
     *
     * @param shortName the archetype short name of the object
     * @param id        the persistent identity of the object
     * @param linkId    the link identifier. May be {@code null}
     * @throws IllegalArgumentException if the archetype id is {@code null}
     */
    public IMObjectReference(String shortName, long id, String linkId) {
        if (shortName == null) {
            throw new IllegalArgumentException("Invalid argument 'shortName'");
        }
        archetypeId = new ArchetypeId(shortName);
        setId(id);
        setLinkId(linkId);
    }

    /**
     * Constructs an {@link IMObjectReference} for the specified archetype id, persistent id, and link id.
     *
     * @param archetypeId the archetype id of the object
     * @param id          the persistent identity of the object
     * @param linkId      the link identifier. May be {@code null}
     * @throws IllegalArgumentException if the archetype id is {@code null}
     */
    public IMObjectReference(ArchetypeId archetypeId, long id, String linkId) {
        if (archetypeId == null) {
            throw new IllegalArgumentException("Invalid argument 'archetypeId'");
        }
        this.archetypeId = archetypeId;
        setId(id);
        setLinkId(linkId);
    }

    /**
     * Constructs an {@link IMObjectReference} for the specified archetype id
     * and link id.
     *
     * @param archetypeId the archetype id of the object
     * @param linkId      the link of the object. May be {@code null}
     * @throws IllegalArgumentException if the archetype id is {@code null}
     */
    public IMObjectReference(ArchetypeId archetypeId, String linkId) {
        if (archetypeId == null) {
            throw new IllegalArgumentException(
                    "Invalid argument 'archetypeId'");
        }
        this.archetypeId = archetypeId;
        setLinkId(linkId);
    }

    /**
     * Returns the archetype identifier.
     *
     * @return the archetype identifier
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Return the archetypeId as a string.
     *
     * @return String
     */
    @Deprecated
    public String getArchetypeIdAsString() {
        return archetypeId.getQualifiedName();
    }

    /**
     * Returns the object's persistent identifier.
     *
     * @return the object's persistent identifier, or {@code -1} if the object is not persistent.
     */
    public long getId() {
        return id;
    }

    /**
     * Determines if the object is new. A new object is one that has not been made persistent.
     *
     * @return {@code true} if the object is new, {@code false} if it has been made persistent
     */
    public boolean isNew() {
        return id == -1;
    }

    /**
     * Returns the object link identifier.
     * <p/>
     * This is a UUID that is used to link objects until they can be made
     * persistent, and to provide support for object equality.
     *
     * @return the link identifier. May be {@code null}
     */
    public String getLinkId() {
        return linkId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        boolean equal = false;
        if ((obj instanceof IMObjectReference)) {
            IMObjectReference rhs = (IMObjectReference) obj;
            if (ObjectUtils.equals(linkId, rhs.linkId)) {
                equal = ObjectUtils.equals(archetypeId, rhs.archetypeId);
            }
        }
        return equal;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return archetypeId.getShortName() + ':' + id + ((linkId != null) ? ':' + linkId : "");
    }

    /**
     * Constructs a {@link IMObjectReference} from a string.
     *
     * @param value the string form of the reference. May be {@code null}
     * @return the reference or {@code null} if {@code value} is null
     * @throws IllegalArgumentException if the value is invalid
     */
    public static IMObjectReference fromString(String value) {
        IMObjectReference result = null;
        if (value != null) {
            int first = value.indexOf(':');
            if (first == -1) {
                throw new IllegalArgumentException("Invalid object reference: " + value);
            }
            String shortName = value.substring(0, first);
            int second = value.indexOf(':', first + 1);
            String id;
            String linkId = null;
            if (second != -1) {
                id = value.substring(first + 1, second);
                linkId = value.substring(second + 1);
            } else {
                id = value.substring(first + 1);
            }
            try {
                result = new IMObjectReference(shortName, Long.valueOf(id), linkId);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid object reference: " + value);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Sets the archetype identity.
     *
     * @param archetypeId the archetype identity to set
     */
    protected void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * Sets the object's persistent identifier.
     *
     * @param id the persistent identifier
     */
    protected void setId(long id) {
        this.id = id;
        updateHash();
    }

    /**
     * Sets the object identity.
     *
     * @param linkId the object identity to set
     */
    protected void setLinkId(String linkId) {
        this.linkId = linkId;
        updateHash();
    }

    /**
     * Updates the cached hash code.
     */
    private void updateHash() {
        if (linkId != null) {
            hashCode = linkId.hashCode();
        } else {
            hashCode = (int) id;
        }
    }

}
