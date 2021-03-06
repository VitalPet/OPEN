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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: ArchetypeId.java 2378 2007-09-28 07:08:29Z tanderson $
 */

package org.openvpms.component.business.domain.archetype;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;


/**
 * The archetype identifier uniquely defines an archetype. It consists of the
 * following components:
 * <p><tt>&lt;entityName&gt;.&lt;concept&gt;.&lt;version&gt;</tt>
 * <p>where:
 * <ul>
 * <li>entityName - is the entity name</li>
 * <li>concept - is the concept attached to the archetype</li>
 * <li>version - is the version of the archetype, and is optional</li>
 * </ul>
 * <p>Examples:
 * <ul>
 * <li>party.customer.1.0</li>
 * <li>contact.phoneNumber.1.0</li>
 * <li>contact.location.1.0</li>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2007-09-28 04:08:29 -0300 (Fri, 28 Sep 2007) $
 */
public class ArchetypeId implements Serializable, Cloneable {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 2L;

    /**
     * The archetype id for a local lookup.
     */
    public static final ArchetypeId LOCAL_LOOKUP_ID
            = new ArchetypeId("lookup.local.1.0");

    /**
     * The namespace
     */
    private String namespace;

    /**
     * The reference model name(i.e. equivalent to the package name)
     */
    private String rmName;

    /**
     * The entity name.
     */
    private String entityName;

    /**
     * The archetype concept.
     */
    private String concept;

    /**
     * The version number. May be <tt>null</tt>
     */
    private String version;

    /**
     * The fully qualified name. The concatenation of the entityName,
     * concept and version (if non-null).
     */
    private String qualifiedName;

    /**
     * The short name. This is the concatenation of the entityName and
     * concept.
     */
    private String shortName;


    /**
     * Constructor provided for serialization purposes.
     */
    protected ArchetypeId() {
        // do nothing
    }

    /**
     * Create an archetypeId from a qualified name.
     *
     * @param qname the qualified name. The version is optional
     * @throws ArchetypeIdException if an illegal archetype id has been
     *                              specified
     */
    public ArchetypeId(String qname) {
        setQualifiedName(qname);
    }

    /**
     * Create an archetype id based on the following components.
     *
     * @param entityName the entity name
     * @param concept    the concept that the archetype denotes
     * @param version    the version of the archetype. May be <tt>null</tt>
     * @throws ArchetypeIdException if a legal archetype id cannot be
     *                              constructed.
     */
    public ArchetypeId(String entityName, String concept, String version) {
        if (StringUtils.isEmpty(concept) || StringUtils.isEmpty(entityName)) {
            throw new ArchetypeIdException(
                    ArchetypeIdException.ErrorCode.EmptyElement);
        }

        this.concept = concept;
        this.entityName = entityName;
        this.version = version;
    }

    /**
     * Returns the archetype entity name.
     *
     * @return the entity name
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the archetype concept.
     *
     * @return the concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * Returns the archetype version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the qualified name. This is the concatenation of the
     * entity name, concept and version (if non-null).
     *
     * @return the qualified name
     */
    public String getQualifiedName() {
        if (qualifiedName == null) {
            StringBuffer buff = new StringBuffer(getShortName());
            if (!StringUtils.isEmpty(version)) {
                buff.append(".").append(version);
            }
            qualifiedName = buff.toString();
        }

        return qualifiedName;
    }

    /**
     * @return the namespace
     * @deprecated no replacement
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @return Returns the rmName.
     * @deprecated no replacement
     */
    @Deprecated
    public String getRmName() {
        return rmName;
    }

    /**
     * Return the short name, which is the concatenation of the rmName and
     * name, concept and version number.
     *
     * @return the archetype short name
     */
    public String getShortName() {
        if (shortName == null) {
            shortName = new StringBuffer()
                    .append(entityName)
                    .append(".")
                    .append(concept)
                    .toString();
        }

        return shortName;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p/>
     * Note that equalivalence is primarily determined by the archetype
     * short name. If the version of both objects being compared is non-null,
     * then this will also be compared. If one or both is null, then the
     * objects will be considered equal if the short names are the same.
     *
     * @return <tt>true</tt> if this equals <tt>obj</tt>
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArchetypeId)) {
            return false;
        }
        ArchetypeId rhs = (ArchetypeId) obj;
        if (ObjectUtils.equals(getShortName(), rhs.getShortName())) {
            if (version != null && rhs.getVersion() != null) {
                return version.equals(rhs.getVersion());
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getShortName().hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ArchetypeId copy = (ArchetypeId) super.clone();
        copy.concept = this.concept;
        copy.entityName = this.entityName;
        copy.qualifiedName = this.qualifiedName;
        copy.shortName = this.shortName;
        copy.version = this.version;

        return copy;
    }

    /**
     * @param namespace The namespace to set.
     * @deprecated no replacement
     */
    @Deprecated
    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * @param rmName The rmName to set.
     * @deprecated no replacement
     */
    @Deprecated
    protected void setRmName(String rmName) {
        this.rmName = rmName;
    }

    /**
     * Sets the entity name.
     *
     * @param entityName the entity name
     */
    @Deprecated
    protected void setEntityName(String entityName) {
        this.entityName = entityName;
        shortName = null;
        qualifiedName = null;
    }

    /**
     * Sets the archetype concept.
     *
     * @param concept the concept to set
     */
    @Deprecated
    protected void setConcept(String concept) {
        this.concept = concept;
        shortName = null;
        qualifiedName = null;
    }

    /**
     * Sets the qualified name.
     *
     * @param qname the qualified name. The version is optional
     * @throws ArchetypeIdException if an illegal archetype id has been
     *                              specified
     */
    protected void setQualifiedName(String qname) {
        QualifiedName cached = QualifiedName.get(qname);
        setShortName(cached.getShortName());
        version = cached.getVersion();
        qualifiedName = cached.getQualifiedName();
    }

    /**
     * Sets the short name.
     *
     * @param shortName the short name to set
     * @throws ArchetypeIdException if an illegal short name has been specified
     */
    protected void setShortName(String shortName) {
        setShortName(ShortName.get(shortName));
    }

    /**
     * Sets the version.
     *
     * @param version the version to set
     */
    protected void setVersion(String version) {
        this.version = version;
        qualifiedName = null;
    }

    /**
     * Sets the short name.
     *
     * @param shortName the short name
     */
    private void setShortName(ShortName shortName) {
        entityName = shortName.getEntityName();
        concept = shortName.getConcept();
        this.shortName = shortName.getShortName();
    }
}