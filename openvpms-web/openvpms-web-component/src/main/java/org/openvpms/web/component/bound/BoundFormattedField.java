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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.text.TextComponent;
import org.openvpms.web.echo.text.TextDocument;
import org.openvpms.web.echo.text.TextField;

import java.text.Format;
import java.text.ParseException;


/**
 * Binds a {@link Property} to a {@link TextField}, providing formatting.
 *
 * @author Tim Anderson
 */
public class BoundFormattedField extends TextField implements BoundProperty {

    /**
     * The formatter.
     */
    private final Format format;

    /**
     * The binder.
     */
    private final Binder binder;


    /**
     * Construct a new <tt>BoundFormattedField</tt>.
     *
     * @param property the property to bind
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, Format format) {
        super(new TextDocument());
        this.format = format;
        binder = new FormattingBinder(this, property);
    }

    /**
     * Construct a new <tt>BoundFormattedField</tt>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display.
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, int columns,
                               Format format) {
        this(property, format);
        setWidth(new Extent(columns, Extent.EX));
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is added to a registered hierarchy.
     */
    @Override
    public void init() {
        super.init();
        binder.bind();
    }

    /**
     * Life-cycle method invoked when the <tt>Component</tt> is removed from a registered hierarchy.
     */
    @Override
    public void dispose() {
        super.dispose();
        binder.unbind();
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    @Override
    public Property getProperty() {
        return binder.getProperty();
    }

    /**
     * Parses the field value.
     *
     * @param value the value to parse
     * @return the parsed value, or <tt>value</tt> if it can't be parsed
     */
    protected Object parse(String value) {
        Object result = null;
        if (value != null) {
            try {
                result = format.parseObject(value);
            } catch (ParseException exception) {
                // failed to parse, so return the field unchanged
                result = value;
            }
        }
        return result;
    }

    /**
     * Returns the format.
     *
     * @return the format
     */
    protected Format getFormat() {
        return format;
    }

    private class FormattingBinder extends TextComponentBinder {

        /**
         * Construct a new <tt>FormattingtBinder</tt>.
         *
         * @param component the component to bind
         * @param property  the property to bind
         */
        public FormattingBinder(TextComponent component, Property property) {
            super(component, property);
        }

        /**
         * Returns the value of the field.
         *
         * @return the value of the field
         */
        @Override
        protected Object getFieldValue() {
            String value = (String) super.getFieldValue();
            return parse(value);
        }

        /**
         * Sets the value of the field.
         *
         * @param value the value to set
         */
        @Override
        protected void setFieldValue(Object value) {
            if (value != null) {
                try {
                    value = format.format(value);
                } catch (IllegalArgumentException ignore) {
                    // failed to format, so set the field unchanged
                }
            }
            super.setFieldValue(value);
        }
    }

}
