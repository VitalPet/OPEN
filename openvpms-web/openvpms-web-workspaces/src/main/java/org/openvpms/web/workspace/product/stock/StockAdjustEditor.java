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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;


/**
 * Editor for <em>act.stockAdjust</em> acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class StockAdjustEditor extends ActEditor {

    /**
     * Construct a new <tt>StockAdjustEditor</tt>.
     *
     * @param act     the act to edit
     * @param parent  the parent object. May be <tt>null</tt>
     * @param context the layout context. May be <tt>null</tt>
     */
    public StockAdjustEditor(Act act, IMObject parent,
                             LayoutContext context) {
        super(act, parent, context);
    }

    /**
     * Invoked when layout has completed.
     */
    @Override
    protected void onLayoutCompleted() {
        final ParticipationEditor editor = getParticipationEditor("stockLocation", false);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                stockLocationChanged((Party) editor.getEntity());
            }
        });
    }

    /**
     * Invoked when the stock location changes.
     *
     * @param location the new stock location. May be <tt>null</tt>
     */
    private void stockLocationChanged(Party location) {
        for (IMObjectEditor itemEditor : getItems().getEditors()) {
            StockAdjustItemEditor editor
                    = (StockAdjustItemEditor) itemEditor;
            editor.setStockLocation(location);
        }
    }

}
