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

package org.openvpms.web.workspace.supplier.charge;

import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.workspace.supplier.SupplierActCRUDWindow;


/**
 * CRUD window for supplier invoices.
 *
 * @author Tim Anderson
 */
public class ChargeCRUDWindow extends SupplierActCRUDWindow<FinancialAct> {

    /**
     * Constructs a {@link ChargeCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public ChargeCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(archetypes, ActActions.<FinancialAct>edit(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPreviewButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(POST_ID, enable);
        buttons.setEnabled(PREVIEW_ID, enable);
    }

}
