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

package org.openvpms.web.workspace.patient.problem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.collections.ComparatorUtils;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.PageLocator;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Patient problem query.
 * <p/>
 * This returns <em>act.patientClinicalProblem</em> acts within a date range.
 * <br/>
 * It provides a selector to filter acts items; filtering must be performed by the caller.
 *
 * @author Tim Anderson
 */
public class ProblemQuery extends DateRangeActQuery<Act> {

    /**
     * The set of possible act item short names.
     */
    private String[] shortNames;

    /**
     * The act items to display.
     */
    private String[] selectedShortNames;

    /**
     * Determines if the visit items are being sorted ascending or descending.
     */
    private boolean sortAscending = true;

    /**
     * Button to change the problem items sort order.
     */
    private Button sort;

    /**
     * The short name model.
     */
    private ShortNameListModel model;

    /**
     * The short name selector.
     */
    private SelectField shortNameSelector;

    /**
     * The short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{PatientArchetypes.CLINICAL_PROBLEM};

    /**
     * Sort such that Unresolved acts are displayed first, ordered on most recent timestamp.
     */
    private static final SortConstraint[] SORT = {
            new NodeSortConstraint("status", false),
            new NodeSortConstraint("startTime", false),
            new NodeSortConstraint("id")
    };

    /**
     * Constructs a {@link ProblemQuery}.
     *
     * @param patient the patient to query
     */
    public ProblemQuery(Party patient) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION, SHORT_NAMES, Act.class);
        shortNames = RelationshipHelper.getTargetShortNames(PatientArchetypes.CLINICAL_PROBLEM_ITEM);
        selectedShortNames = shortNames;
        model = new ShortNameListModel(shortNames, true, false);
        shortNameSelector = SelectFieldFactory.create(model);

        ActionListener listener = new ActionListener() {
            public void onAction(ActionEvent event) {
                updateSelectedShortNames();
                onQuery();
            }
        };
        shortNameSelector.addActionListener(listener);
        shortNameSelector.setCellRenderer(new ShortNameListCellRenderer());
        setAuto(true);
        setDefaultSortConstraint(SORT);
    }

    /**
     * Returns the short names of the selected act items.
     *
     * @return the act item short names
     */
    public String[] getActItemShortNames() {
        return selectedShortNames;
    }

    /**
     * Determines if the problem items are being sorted ascending or descending.
     *
     * @param ascending if {@code true} problem items are to be sorted ascending; {@code false} if descending
     */
    public void setSortAscending(boolean ascending) {
        sortAscending = ascending;
        if (sort != null) {
            setSortIcon();
        }
    }

    /**
     * Determines if the problem items are being sorted ascending or descending.
     *
     * @return {@code true} if problem items are being sorted ascending; {@code false} if descending
     */
    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * Determines the page that a problem falls on, excluding any date range constraints.
     *
     * @param problem the problem
     * @return the page that the problem would fall on, if present
     */
    public int getPage(Act problem) {
        int page = 0;
        ActBean bean = new ActBean(problem);
        IMObjectReference patient = bean.getNodeParticipantRef("patient");
        if (patient != null) {
            ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_PROBLEM);
            query.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));
            PageLocator locator = new PageLocator(problem, query, getMaxResults());
            locator.addKey("status", false, ComparatorUtils.naturalComparator());
            locator.addKey("startTime", false, PageLocator.DATE_COMPARATOR);
            page = locator.getPage();
        }
        return page;
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Label typeLabel = LabelFactory.create("query.type");
        container.add(typeLabel);
        container.add(shortNameSelector);

        sort = ButtonFactory.create(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                sortAscending = !sortAscending;
                setSortIcon();
                onQuery();
                FocusHelper.setFocus(sort);
            }
        });
        setSortIcon();

        container.add(sort);
        FocusGroup focusGroup = getFocusGroup();
        focusGroup.add(shortNameSelector);
        focusGroup.add(sort);
        super.doLayout(container);
    }

    /**
     * Updates the short names to query.
     */
    private void updateSelectedShortNames() {
        int index = shortNameSelector.getSelectedIndex();
        if (model.isAll(index)) {
            selectedShortNames = shortNames;
        } else {
            String shortName = model.getShortName(index);
            selectedShortNames = getSelectedShortNames(shortName);
        }
    }

    /**
     * Returns the selected short names.
     *
     * @param shortName the short name
     * @return the corresponding short names
     */
    private String[] getSelectedShortNames(String shortName) {
        if (InvestigationArchetypes.PATIENT_INVESTIGATION.equals(shortName)) {
            return new String[]{shortName, InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION};
        } else if (PatientArchetypes.DOCUMENT_ATTACHMENT.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION};
        } else if (PatientArchetypes.DOCUMENT_IMAGE.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_IMAGE_VERSION};
        } else if (PatientArchetypes.DOCUMENT_LETTER.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_LETTER_VERSION};
        }
        return new String[]{shortName};
    }

    /**
     * Sets the sort button icon.
     */
    private void setSortIcon() {
        String style;
        String toolTip;
        if (sortAscending) {
            style = "sort.ascending";
            toolTip = Messages.get("patient.record.query.sortAscending");
        } else {
            style = "sort.descending";
            toolTip = Messages.get("patient.record.query.sortDescending");
        }
        sort.setStyleName(style);
        sort.setToolTipText(toolTip);
    }
}
