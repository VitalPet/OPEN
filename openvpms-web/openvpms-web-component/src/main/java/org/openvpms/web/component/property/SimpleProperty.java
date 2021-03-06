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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.echo.text.TextHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Simple implementation of the {@link Property} interface.
 *
 * @author Tim Anderson
 */
public class SimpleProperty extends AbstractProperty {

    /**
     * The property name.
     */
    private final String name;

    /**
     * The property type.
     */
    private final Class type;

    /**
     * The property display name.
     */
    private String displayName;

    /**
     * The property description. May be {@code null}
     */
    private String description;

    /**
     * The property value. May be {@code null}
     */
    private Object value;

    /**
     * The minimum length.
     */
    private int minLength;

    /**
     * The maximum length.
     */
    private int maxLength = 255;

    /**
     * The archetype short names that the property supports
     */
    private String[] shortNames = EMPTY;

    /**
     * Determines if the property is read-only.
     */
    private boolean readOnly;

    /**
     * Determines if the property is hidden.
     */
    private boolean hidden;

    /**
     * Determines if the property is required.
     */
    private boolean required;

    /**
     * Validation errors.
     */
    private List<ValidatorError> validationErrors;

    /**
     * Empty string array.
     */
    private static final String[] EMPTY = new String[0];


    /**
     * Constructs a {@link SimpleProperty}.
     *
     * @param name the property name
     * @param type the property type
     */
    public SimpleProperty(String name, Class type) {
        this(name, null, type);
    }

    /**
     * Constructs a {@link SimpleProperty}.
     *
     * @param name  the property name
     * @param value the property value. May be {@code null}
     * @param type  the property type
     */
    public SimpleProperty(String name, Object value, Class type) {
        this(name, value, type, null);
    }

    /**
     * Constructs a {@link SimpleProperty}.
     *
     * @param name        the property name
     * @param value       the property value. May be {@code null}
     * @param type        the property type
     * @param displayName the display name. May be {@code null}
     */
    public SimpleProperty(String name, Object value, Class type, String displayName) {
        this(name, value, type, displayName, false);
    }

    /**
     * Constructs a {@link SimpleProperty}.
     *
     * @param name        the property name
     * @param value       the property value. May be {@code null}
     * @param type        the property type
     * @param displayName the display name. May be {@code null}
     * @param readOnly    if {@code true}, the property is read-only
     */
    public SimpleProperty(String name, Object value, Class type, String displayName, boolean readOnly) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.displayName = displayName;
        this.readOnly = readOnly;
    }

    /**
     * Constructs a {@link SimpleProperty} from another property.
     *
     * @param property the property
     */
    public SimpleProperty(Property property) {
        this.name = property.getName();
        this.type = property.getType();
        this.displayName = property.getDisplayName();
        this.description = property.getDescription();
        this.value = property.getValue();
        this.minLength = property.getMinLength();
        this.maxLength = property.getMaxLength();
        this.readOnly = property.isReadOnly();
        this.hidden = property.isHidden();
        this.required = property.isRequired();
        this.shortNames = property.getArchetypeRange();
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = TextHelper.unCamelCase(name);
        }
        return displayName;
    }

    /**
     * Sets the property display name.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the property description.
     *
     * @param description the description. May be {@code null}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return {@code true} if the value was set, {@code false} if it
     *         cannot be set due to error, or is the same as the existing value
     */
    public boolean setValue(Object value) {
        boolean set = false;
        checkModifiable();
        try {
            if (!ObjectUtils.equals(this.value, value)) {
                value = getTransformer().apply(value);
                this.value = value;
                set = true;
                modified();
            } else if (validationErrors != null) {
                // a previous set triggered an error, and didn't update the value. If a new update occurs
                // but has the same value, need to clear any errors
                modified();
            }
        } catch (OpenVPMSException exception) {
            invalidate(exception);
        }
        return set;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of the property.
     *
     * @param length the minimum length
     */
    public void setMinLength(int length) {
        minLength = length;
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length, or {@code -1} if it is unbounded
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of the property.
     *
     * @param length the maximum length. Use {@code -1} to indicate unbounded length
     */
    public void setMaxLength(int length) {
        maxLength = length;
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class getType() {
        return type;
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return {@code true} if it is a boolean
     */
    public boolean isBoolean() {
        return Boolean.class == type || boolean.class == type;
    }

    /**
     * Determines if the property is a string.
     *
     * @return {@code true} if it is a string
     */
    public boolean isString() {
        return String.class == type;
    }

    /**
     * Determines if the property is numeric.
     *
     * @return {@code true} if it is numeric
     */
    public boolean isNumeric() {
        return Number.class.isAssignableFrom(type)
               || byte.class == type
               || short.class == type
               || int.class == type
               || long.class == type
               || float.class == type
               || double.class == type;
    }

    /**
     * Determines if the property is a date.
     *
     * @return {@code true} if it is a date
     */
    public boolean isDate() {
        return Date.class == type;
    }

    /**
     * Determines if the property is a money property.
     *
     * @return {@code true} it is a money property
     */
    public boolean isMoney() {
        return Money.class == type;
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return {@code true} if it is an object reference
     */
    public boolean isObjectReference() {
        return IMObjectReference.class.isAssignableFrom(type);
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return {@code true} if it is a lookup
     */
    public boolean isLookup() {
        return false;
    }

    /**
     * Determines if the property is a collection.
     *
     * @return {@code true} if it is a collection
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * Returns the archetype short names that this property may support.
     *
     * @return the archetype short names
     */
    public String[] getArchetypeRange() {
        return shortNames;
    }

    /**
     * Sets the archetype short names that this property may support.
     * <p/>
     * Wildcards are expanded.
     *
     * @param shortNames the archetype short names
     */
    public void setArchetypeRange(String[] shortNames) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames);
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return {@code true} if the value is derived, otherwise {@code false}
     */
    public boolean isDerived() {
        return false;
    }

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets if the property is read-only.
     *
     * @param readOnly {@code true} if the property is read-only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Determines if the property is hidden.
     *
     * @return {@code true} if the property is hidden; otherwise {@code false}
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * Sets if the property is hidden.
     *
     * @param hidden {@code true} if the property is hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Determines if the property is required.
     *
     * @return {@code true} if the property is required; otherwise
     *         {@code false}
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Determines if the property is required.
     *
     * @param required if {@code true} the property is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the property descriptor.
     *
     * @return {@code null}
     */
    public NodeDescriptor getDescriptor() {
        return null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        List<ValidatorError> errors = null;
        if (validationErrors == null) {
            PropertyTransformer transformer = getTransformer();
            try {
                transformer.apply(getValue());
            } catch (OpenVPMSException exception) {
                invalidate(exception);
            }
        }
        if (validationErrors != null) {
            errors = validationErrors;
        } else if (isRequired() && getValue() == null) {
            validationErrors = new ArrayList<ValidatorError>();
            validationErrors.add(new ValidatorError(this, Messages.format("property.error.required", getDisplayName())));
            errors = validationErrors;
        }
        if (errors != null) {
            validator.add(this, errors);
        }
        return (errors == null);
    }

    private void invalidate(OpenVPMSException exception) {
        if (validationErrors != null) {
            validationErrors.clear();
        } else {
            validationErrors = new ArrayList<ValidatorError>();
        }
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause == null) {
            cause = exception;
        }
        ValidatorError error = new ValidatorError(this, cause.getMessage());
        validationErrors.add(error);
        resetValid();
    }

    /**
     * Invoked when this is modified. Updates flags, and notifies the
     * listeners.
     */
    private void modified() {
        validationErrors = null;
        refresh();
    }

}
