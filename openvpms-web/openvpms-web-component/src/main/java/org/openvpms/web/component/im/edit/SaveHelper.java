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

package org.openvpms.web.component.im.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ObjectNotFoundException;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.error.ExceptionHelper;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.util.DefaultIMObjectSaveListener;
import org.openvpms.web.component.im.util.IMObjectDeletionListener;
import org.openvpms.web.component.im.util.IMObjectSaveListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collection;


/**
 * Helper for saving {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class SaveHelper {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(SaveHelper.class);


    /**
     * Saves an editor in a transaction.
     *
     * @param editor the editor to save
     * @return {@code true} if the object was saved successfully
     */
    public static boolean save(final IMObjectEditor editor) {
        Boolean result = null;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    return editor.save();
                }
            });
        } catch (Throwable exception) {
            error(editor, exception);
        }
        return (result != null) && result;
    }

    /**
     * Saves an editor and invokes a callback within a single transaction.
     *
     * @param editor   the editor
     * @param callback the callback
     * @return {@code true} if the object was saved and the callback returned {@code true}
     */
    public static boolean save(final IMObjectEditor editor, final TransactionCallback<Boolean> callback) {
        Boolean result = null;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    boolean result = false;
                    if (editor.save()) {
                        Boolean success = callback.doInTransaction(status);
                        result = success != null && success;
                    }
                    return result;
                }
            });
        } catch (Throwable exception) {
            error(editor, exception);
        }
        return (result != null) && result;
    }

    /**
     * Invokes a callback to save objects.
     *
     * @param displayName the primary display name, for error reporting
     * @param callback    the callback to execute
     * @return {@code true} if the save was successful
     */
    public static boolean save(String displayName, TransactionCallback<Boolean> callback) {
        boolean saved = false;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            Boolean result = template.execute(callback);
            saved = (result != null) && result;
        } catch (Throwable exception) {
            error(displayName, null, exception);
        }
        return saved;
    }

    /**
     * Saves an object.
     *
     * @param object the object to save
     * @return {@code true} if the object was saved; otherwise {@code false}
     */
    public static boolean save(IMObject object) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return save(object, service);
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param service the archetype service
     * @return {@code true} if the object was saved; otherwise {@code false}
     */
    public static boolean save(IMObject object, IArchetypeService service) {
        return save(Arrays.asList(object), service);
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects the objects to save
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(IMObject... objects) {
        return save(Arrays.asList(objects));
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects the objects to save
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(Collection<? extends IMObject> objects) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return save(objects, service);
    }

    /**
     * Saves an object.
     *
     * @param objects the objects to save
     * @param service the archetype service
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(Collection<? extends IMObject> objects, IArchetypeService service) {
        return save(objects, DefaultIMObjectSaveListener.INSTANCE, service);
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects  the objects to save
     * @param listener the listener to notify
     */
    public static boolean save(Collection<? extends IMObject> objects, IMObjectSaveListener listener) {
        return save(objects, listener, ServiceHelper.getArchetypeService());
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects  the objects to save
     * @param listener the listener to notify
     */
    public static boolean save(Collection<? extends IMObject> objects, IMObjectSaveListener listener,
                               IArchetypeService service) {
        boolean saved = false;
        try {
            service.save(objects);
            listener.saved(objects);
            saved = true;
        } catch (Throwable exception) {
            listener.error(objects, exception);
        }
        return saved;
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static boolean delete(IMObject object) {
        return delete(object, new DefaultIMObjectDeletionListener<IMObject>());
    }

    /**
     * Removes an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static <T extends IMObject> boolean delete(T object, IMObjectDeletionListener<T> listener) {
        return delete(object, listener, ServiceHelper.getArchetypeService());
    }

    /**
     * Removes an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     * @param service  the archetype service
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static <T extends IMObject> boolean delete(T object, IMObjectDeletionListener<T> listener,
                                                      IArchetypeService service) {
        boolean removed = false;
        try {
            service.remove(object);
            removed = true;
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
        return removed;
    }

    /**
     * Replace an object, by deleting one instance and inserting another.
     *
     * @param delete the object to delete
     * @param insert the object to insert
     * @return {@code true} if the operation was successful
     */
    public static boolean replace(final IMObject delete,
                                  final IMObject insert) {
        Boolean result = null;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    IArchetypeService service = ServiceHelper.getArchetypeService();
                    service.remove(delete);
                    service.save(insert);
                    return true;
                }
            });
        } catch (Throwable exception) {
            String title = Messages.get("imobject.replace.failed.title");
            ErrorHelper.show(title, exception);
        }
        return (result != null) && result;
    }

    /**
     * Displays an editor save error.
     *
     * @param editor    the editor
     * @param exception the cause
     */
    private static void error(IMObjectEditor editor, Throwable exception) {
        IMObject object = editor.getObject();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : null;
        String context = Messages.format("logging.error.editcontext", object.getObjectReference(),
                                         editor.getClass().getName(), user);
        error(editor.getDisplayName(), context, exception);
    }

    /**
     * Displays a save error.
     *
     * @param displayName the display name of the object that failed to save
     * @param context     the context message. May be {@code null}
     * @param exception   the cause
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private static void error(String displayName, String context, Throwable exception) {
        Throwable cause = ExceptionHelper.getRootCause(exception);
        String title = Messages.format("imobject.save.failed", displayName);
        if (cause instanceof ObjectNotFoundException) {
            // Don't propagate the exception
            String message = Messages.format("imobject.notfound", displayName);
            log.error(message, exception);
            ErrorHelper.show(title, message);
        } else {
            ErrorHelper.show(title, displayName, context, exception);
        }
    }

}
