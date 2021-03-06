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

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.resource.i18n.Messages;

/**
 * A viewer for repeat expressions produced by {@link RepeatOnDateEditor}.
 *
 * @author Tim Anderson
 */
class RepeatOnDateViewer extends CronRepeatExpressionViewer {

    /**
     * Constructs a {@link RepeatOnDateViewer}.
     *
     * @param expression the expression
     */
    public RepeatOnDateViewer(CronRepeatExpression expression) {
        super(expression);
    }

    /**
     * Returns a component representing the expression.
     *
     * @return a new component
     */
    public Component getComponent() {
        StringBuilder buffer = new StringBuilder();
        CronRepeatExpression expression = getExpression();
        buffer.append(Messages.get("workflow.scheduling.appointment.onthe"));
        buffer.append(" ");
        buffer.append(expression.getDayOfMonth().day());
        buffer.append(" ");
        buffer.append(getMonth());
        buffer.append(" ");
        buffer.append(Messages.format("workflow.scheduling.appointment.everyyear", expression.getYear().getInterval()));
        Label label = LabelFactory.create();
        label.setText(buffer.toString());
        return label;
    }
}
