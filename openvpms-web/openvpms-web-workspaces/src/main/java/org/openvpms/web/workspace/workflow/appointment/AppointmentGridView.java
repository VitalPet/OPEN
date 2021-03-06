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

package org.openvpms.web.workspace.workflow.appointment;

import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.Date;
import java.util.List;


/**
 * Provides a time-range view of a set of appointments.
 *
 * @author Tim Anderson
 */
class AppointmentGridView extends AbstractAppointmentGrid {

    /**
     * The grid to filter appointments from.
     */
    private final AppointmentGrid grid;

    /**
     * The starting slot to view from.
     */
    private int startSlot;

    /**
     * The no. of slots from startSlot.
     */
    private int slots;


    /**
     * Constructs an {@link AppointmentGridView}.
     *
     * @param grid      the grid to filter appointments from
     * @param startMins the start time of the view, as minutes from midnight
     * @param endMins   the end time of the view, as minutes from midnight
     * @param rules     the appointment rules
     */
    public AppointmentGridView(AppointmentGrid grid, int startMins, int endMins, AppointmentRules rules) {
        super(grid.getScheduleView(), grid.getStartDate(), startMins, endMins, rules);
        this.grid = grid;
        startSlot = grid.getFirstSlot(startMins);
        if (startSlot == -1) {
            if (grid.getSlots() > 0) {
                startSlot = grid.getSlots() - 1;
            } else {
                startSlot = 0;
            }
        }
        int endSlot;
        if (grid.getEndMins() == endMins) {
            endSlot = grid.getLastSlot(endMins - grid.getSlotSize());
        } else {
            endSlot = grid.getLastSlot(endMins);
        }
        slots = (endSlot - startSlot) + 1;
        setSlotSize(grid.getSlotSize());
    }

    /**
     * Returns the schedules.
     *
     * @return the schedules
     */
    public List<Schedule> getSchedules() {
        return grid.getSchedules();
    }

    /**
     * Returns the appointment for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding appointment, or {@code null} if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot) {
        PropertySet result = grid.getEvent(schedule, startSlot + slot);
        if (result == null && slot == 0) {
            // see if there is an event in an earlier slot that intersects this one
            Date time = getStartTime(schedule, slot);
            result = schedule.getIntersectingEvent(time);
        }
        return result;
    }

    /**
     * Returns the no. of slots at an appointment occupies, from the specified
     * slot.
     * <p/>
     * If the appointment begins prior to the slot, the remaining slots will
     * be returned.
     *
     * @param appointment the appointment
     * @param slot        the starting slot
     * @return the no. of slots that the appointment occupies
     */
    @Override
    public int getSlots(PropertySet appointment, int slot) {
        return grid.getSlots(appointment, startSlot + slot);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    @Override
    public int getSlots() {
        return slots;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot
     */
    @Override
    public Date getStartTime(Schedule schedule, int slot) {
        return grid.getStartTime(schedule, startSlot + slot);
    }

    /**
     * Returns the hour of the specified slot.
     *
     * @param slot the slot
     * @return the hour, in the range 0..23
     */
    @Override
    public int getHour(int slot) {
        return grid.getHour(startSlot + slot);
    }

    /**
     * Returns the first slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the first slot that minutes intersects, or {@code -1} if no
     *         slots intersect
     */
    public int getFirstSlot(int minutes) {
        return grid.getFirstSlot(minutes);
    }

    /**
     * Returns the last slot that has a start time and end time intersecting
     * the specified minutes.
     *
     * @param minutes the minutes
     * @return the last slot that minutes intersects, or {@code -1} if no
     *         slots intersect
     */
    public int getLastSlot(int minutes) {
        return grid.getLastSlot(minutes);
    }

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    @Override
    public int getSlot(Date time) {
        int slot = grid.getSlot(time);
        return (slot >= startSlot) ? slot - startSlot : -1;
    }
}
