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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.reporting.reminder;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.patient.reminder.ReminderEvent;
import org.openvpms.archetype.rules.patient.reminder.ReminderRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.processor.ProgressBarProcessor;
import org.openvpms.web.system.ServiceHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Abstract implementation of {@link ProgressBarProcessor} for reminders.
 *
 * @author Tim Anderson
 */
abstract class ReminderProgressBarProcessor extends ProgressBarProcessor<List<ReminderEvent>>
        implements ReminderBatchProcessor {

    /**
     * The reminder rules.
     */
    private final ReminderRules rules;

    /**
     * Determines if reminders should be updated on completion.
     */
    private boolean update = true;

    /**
     * The statistics.
     */
    private final Statistics statistics;

    /**
     * The set of completed reminder ids, used to avoid updating reminders that are being reprocessed.
     */
    private Map<Act, State> states = new HashMap<Act, State>();


    /**
     * Constructs a {@link ReminderProgressBarProcessor}.
     *
     * @param items      the reminder items
     * @param statistics the statistics
     * @param title      the progress bar title for display purposes
     */
    public ReminderProgressBarProcessor(List<List<ReminderEvent>> items, Statistics statistics, String title) {
        super(items, count(items), title);
        this.statistics = statistics;
        rules = ServiceHelper.getBean(ReminderRules.class);
    }

    /**
     * Determines if reminders should be updated on completion.
     * <p/>
     * If set, the {@code reminderCount} is incremented the {@code lastSent} timestamp set on completed reminders.
     *
     * @param update if {@code true} update reminders on completion
     */
    public void setUpdateOnCompletion(boolean update) {
        this.update = update;
    }

    /**
     * Processes a set of reminder events.
     *
     * @param events the events to process
     * @throws OpenVPMSException if the events cannot be processed
     */
    protected void process(List<ReminderEvent> events) {
        if (update) {
            // need to cache the reminderCount and lastSent nodes to allow reprocessing with the original values
            for (ReminderEvent event : events) {
                Act reminder = event.getReminder();
                State state = states.get(reminder);
                if (state != null) {
                    // reprocessing a reminder - reset the reminderCount and lastSent nodes
                    if (state.isComplete()) {
                        state.reset(reminder);
                    }
                } else {
                    state = new State(reminder);
                    states.put(reminder, state);
                }
            }
        }
    }

    /**
     * Increments the count of processed reminders.
     *
     * @param events the reminder events
     */
    @Override
    protected void incProcessed(List<ReminderEvent> events) {
        super.incProcessed(events.size());
    }

    /**
     * Invoked when processing of reminder events is complete.
     * <p/>
     * This updates the reminders and statistics.
     *
     * @param events the reminder events
     */
    @Override
    protected void processCompleted(List<ReminderEvent> events) {
        if (update) {
            updateReminders(events);
        }
        updateStatistics(events);
        super.processCompleted(events);
    }

    /**
     * Invoked if an error occurs processing the batch.
     * <p/>
     * This:
     * <ul>
     * <li>updates the error node of each reminder if {@link #setUpdateOnCompletion(boolean)} is {@code true}</li>
     * <li>updates statistics</li>
     * <li>notifies any listeners of the error</li>
     * <li>delegates to the parent {@link #processCompleted(Object)} to continue processing</li>
     * </ul>
     *
     * @param exception the cause
     * @param events    the events being processed
     */
    protected void processError(Throwable exception, List<ReminderEvent> events) {
        for (ReminderEvent event : events) {
            if (update) {
                ReminderHelper.setError(event.getReminder(), exception);
            }
            statistics.incErrors();
        }
        notifyError(exception);
        super.processCompleted(events);
    }

    /**
     * Skips a set of reminders.
     * <p/>
     * This doesn't update the reminders and their statistics.
     *
     * @param events the reminder events
     */
    protected void skip(List<ReminderEvent> events) {
        super.processCompleted(events);
    }

    /**
     * Returns the reminder rules.
     *
     * @return the reminder rules
     */
    protected ReminderRules getRules() {
        return rules;
    }

    /**
     * Updates each reminder that isn't cancelled.
     * <p/>
     * This sets the <em>lastSent</em> node to the current time, and increments the <em>reminderCount</em>.
     *
     * @param events the reminder event
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void updateReminders(List<ReminderEvent> events) {
        Date date = new Date();
        for (ReminderEvent event : events) {
            Act reminder = event.getReminder();
            if (!ActStatus.CANCELLED.equals(reminder.getStatus())) {
                State state = states.get(reminder);
                if (state != null && !state.isComplete()) {
                    if (ReminderHelper.update(reminder, date)) {
                        state.setCompleted(true);
                    } else {
                        statistics.incErrors();
                    }
                }
            }
        }
    }

    /**
     * Updates statistics for a set of reminders.
     *
     * @param events the reminder events
     */
    private void updateStatistics(List<ReminderEvent> events) {
        for (ReminderEvent event : events) {
            statistics.increment(event);
        }
    }

    /**
     * Counts reminders.
     *
     * @param events the reminder events
     * @return the reminder count
     */
    private static int count(List<List<ReminderEvent>> events) {
        int result = 0;
        for (List<ReminderEvent> list : events) {
            result += list.size();
        }
        return result;
    }

    private static class State {

        private boolean completed;

        private int reminderCount;

        private Date lastSent;

        public State(Act reminder) {
            IMObjectBean bean = new IMObjectBean(reminder);
            reminderCount = bean.getInt("reminderCount");
            lastSent = bean.getDate("lastSent");
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public boolean isComplete() {
            return completed;
        }

        public void reset(Act reminder) {
            IMObjectBean bean = new IMObjectBean(reminder);
            bean.setValue("reminderCount", reminderCount);
            bean.setValue("lastSent", lastSent);
        }

    }
}
