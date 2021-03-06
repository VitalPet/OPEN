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

package org.openvpms.web.workspace.patient.visit;

import nextapp.echo2.app.Component;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.order.OrderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.OrderChargeManager;
import org.openvpms.web.workspace.customer.order.OrderCharger;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;


/**
 * Visit charge CRUD window.
 *
 * @author Tim Anderson
 */
public class VisitChargeCRUDWindow extends AbstractCRUDWindow<FinancialAct> implements VisitEditorTab {

    /**
     * Completed button identifier.
     */
    public static final String COMPLETED_ID = "button.completed";

    /**
     * In Progress button identifier.
     */
    public static final String IN_PROGRESS_ID = "button.inprogress";

    /**
     * Invoice orders button identifier.
     */
    public static final String INVOICE_ORDERS_ID = "button.invoiceOrders";

    /**
     * The event.
     */
    private final Act event;

    /**
     * The charge editor.
     */
    private VisitChargeEditor editor;

    /**
     * The order charge manager.
     */
    private final OrderChargeManager manager;

    /**
     * Determines if the charge is posted.
     */
    private boolean posted;

    /**
     * The container.
     */
    private Component container = ColumnFactory.create();

    /**
     * The tab identifier.
     */
    private int id;

    /**
     * Determines if orders should be automatically charged.
     */
    private boolean autoChargeOrders = true;


    /**
     * Constructs a {@link VisitChargeCRUDWindow}.
     *
     * @param event   the event
     * @param context the context
     * @param help    the help context
     */
    public VisitChargeCRUDWindow(Act event, Context context, HelpContext help) {
        super(Archetypes.create(CustomerAccountArchetypes.INVOICE, FinancialAct.class), ActActions.<FinancialAct>edit(),
              context, help);
        this.event = event;
        OrderRules rules = ServiceHelper.getBean(OrderRules.class);
        OrderCharger charger = new OrderCharger(context.getCustomer(), context.getPatient(), rules, context,
                                                help.subtopic("order"));
        manager = new OrderChargeManager(charger, container);
    }

    /**
     * Returns the identifier of this tab.
     *
     * @return the tab identifier
     */
    @Override
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier of this tab.
     *
     * @param id the tab identifier
     */
    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(FinancialAct object) {
        container.removeAll();
        if (object != null) {
            posted = ActStatus.POSTED.equals(object.getStatus());
            if (posted) {
                IMObjectViewer viewer = new IMObjectViewer(object, new DefaultLayoutContext(getContext(),
                                                                                            getHelpContext()));
                container.add(viewer.getComponent());
                editor = null;
            } else {
                HelpContext edit = createEditTopic(object);
                editor = createVisitChargeEditor(object, event, createLayoutContext(edit));
                manager.clear();
                container.add(editor.getComponent());
            }
        } else {
            editor = null;
        }
        super.setObject(object);
    }

    /**
     * Returns the charge editor.
     *
     * @return the charge editor. May be {@code null}
     */
    public VisitChargeEditor getEditor() {
        return editor;
    }

    /**
     * Creates and edits a new object.
     */
    @Override
    public void create() {
        if (editor == null) {
            IArchetypeService archetypeService = ServiceHelper.getArchetypeService();
            FinancialAct invoice = (FinancialAct) archetypeService.create(CustomerAccountArchetypes.INVOICE);
            setObject(invoice);
        }
    }

    /**
     * Invoked when the tab is displayed.
     */
    @Override
    public void show() {
        if (editor != null) {
            if (autoChargeOrders) {
                manager.charge(editor);
            } else {
                manager.check();
            }
            editor.getFocusGroup().setFocus();
        }
    }

    /**
     * Saves the invoice.
     *
     * @return {@code true} if the invoice was saved
     */
    public boolean save() {
        boolean result;
        if (editor != null && !posted) {
            Validator validator = new DefaultValidator();
            if (editor.validate(validator)) {
                result = SaveHelper.save(editor, new TransactionCallback<Boolean>() {
                    @Override
                    public Boolean doInTransaction(TransactionStatus status) {
                        return manager.save();
                    }
                });
                if (result) {
                    manager.clear();
                }
                posted = ActStatus.POSTED.equals(getObject().getStatus());
            } else {
                result = false;
                ValidationHelper.showError(validator);
            }
        } else {
            result = true;
        }
        manager.check();
        return result;
    }

    /**
     * Returns the CRUD window for the tab.
     *
     * @return the CRUD window for the tab, or {@code null} if the tab doesn't provide one
     */
    @Override
    public CRUDWindow<? extends Act> getWindow() {
        return this;
    }

    /**
     * Marks the charge IN_PROGRESS and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean inProgress() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.IN_PROGRESS);
            result = save();
        }
        return result;
    }

    /**
     * Marks the charge COMPLETED and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean complete() {
        boolean result = false;
        if (editor != null && !posted) {
            editor.setStatus(ActStatus.COMPLETED);
            result = save();
        }
        return result;
    }

    /**
     * Charges orders.
     */
    public void chargeOrders() {
        if (!posted) {
            manager.chargeSelected(editor);
        }
    }

    /**
     * Determines if customer orders should automatically be charged when the window is displayed.
     *
     * @param charge if {@code true}, automatically charge customer orders
     */
    public void setAutoChargeOrders(boolean charge) {
        autoChargeOrders = charge;
    }

    /**
     * Determines if customer orders should automatically be charged when the window is displayed.
     *
     * @return {@code true} if customer orders will be automatically charged
     */
    public boolean isAutoChargeOrders() {
        return autoChargeOrders;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        enableButtons(getButtons(), getObject() != null);
        return container;
    }

    /**
     * Creates a new visit charge editor.
     *
     * @param charge  the charge
     * @param event   the clinical event
     * @param context the layout context
     * @return a new visit charge editor
     */
    protected VisitChargeEditor createVisitChargeEditor(FinancialAct charge, Act event, LayoutContext context) {
        return new VisitChargeEditor(charge, event, context);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        // button layout is handled by the parent dialog
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        if (buttons != null) {
            if (enable) {
                enable = !posted;
            }
            buttons.setEnabled(IN_PROGRESS_ID, enable);
            buttons.setEnabled(COMPLETED_ID, enable);
            buttons.setEnabled(INVOICE_ORDERS_ID, enable);
        }
    }

}
