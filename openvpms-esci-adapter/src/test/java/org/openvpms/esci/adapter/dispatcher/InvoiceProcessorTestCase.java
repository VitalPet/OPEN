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
package org.openvpms.esci.adapter.dispatcher;

import org.junit.Test;
import org.openvpms.archetype.rules.workflow.SystemMessageReason;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanFactory;
import org.openvpms.esci.FutureValue;
import org.openvpms.esci.adapter.dispatcher.invoice.InvoiceProcessor;
import org.openvpms.esci.adapter.dispatcher.invoice.SystemMessageInvoiceListener;
import org.openvpms.esci.adapter.map.invoice.AbstractInvoiceTest;
import org.openvpms.esci.adapter.util.ESCIAdapterException;
import org.openvpms.esci.ubl.common.aggregate.DocumentReferenceType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * Tests the {@link InvoiceProcessor} class.
 *
 * @author Tim Anderson
 */
public class InvoiceProcessorTestCase extends AbstractInvoiceTest {

    /**
     * Tests processing an invoice.
     *
     * @throws Exception for any error
     */
    @Test
    public void testProcess() throws Exception {
        // create an author for invoices, and to receive system messages
        User author = initDefaultAuthor();

        InvoiceProcessor processor = createInvoiceProcessor();

        // set up the invoice listener
        final FutureValue<FinancialAct> future = new FutureValue<FinancialAct>();
        SystemMessageInvoiceListener listener = new SystemMessageInvoiceListener() {
            public void receivedInvoice(FinancialAct delivery) {
                super.receivedInvoice(delivery);
                future.set(delivery);
            }
        };
        listener.setBeanFactory(new IMObjectBeanFactory(getArchetypeService()));
        processor.setInvoiceListener(listener);

        // submit an invoice
        InboxDocument invoice = createInvoiceDocument();
        processor.process(invoice, getSupplier(), getStockLocation(), null);

        // verify the delivery was created
        FinancialAct delivery = future.get(1000);
        assertNotNull(delivery);

        // verify a system message was sent to the author
        checkSystemMessage(author, delivery, SystemMessageReason.ORDER_INVOICED);
    }

    /**
     * Verifies that an {@link ESCIAdapterException} is raised if {@link DocumentProcessor#process} encounters
     * an unexpected exception.
     */
    @Test
    public void testFailedToProcessInvoice() {
        getStockLocation().setName("Main Stock");
        getSupplier().setName("Vetshare");
        InboxDocument invoice = createInvoiceDocument();
        InvoiceProcessor processor = new InvoiceProcessor() {
            @Override
            protected void notifyListener(FinancialAct delivery) {
                throw new RuntimeException("Foo");
            }
        };
        processor.setArchetypeService(getArchetypeService());
        processor.setInvoiceMapper(createMapper());

        checkSubmitException(invoice, processor, "ESCIA-0700: Failed to process Invoice 12345 for supplier Vetshare ("
                                                 + getSupplier().getId() + ") and stock location Main Stock ("
                                                 + getStockLocation().getId() + "): Foo");
    }

    /**
     * Verifies that {@link DocumentProcessor#process} fails with the expected message.
     *
     * @param invoice   the invoice
     * @param processor the processor
     * @param expected  the expected message
     */
    private void checkSubmitException(InboxDocument invoice, InvoiceProcessor processor, String expected) {
        try {
            processor.process(invoice, getSupplier(), getStockLocation(), null);
            fail("Expected submitInvoice() to fail");
        } catch (ESCIAdapterException exception) {
            assertEquals(expected, exception.getMessage());
        }
    }

    /**
     * Creates a new invoice service adapter
     *
     * @return a new invoice service adapter
     */
    private InvoiceProcessor createInvoiceProcessor() {
        InvoiceProcessor processor = new InvoiceProcessor();
        processor.setArchetypeService(getArchetypeService());
        processor.setInvoiceMapper(createMapper());
        return processor;
    }

    private InboxDocument createInvoiceDocument() {
        return new InboxDocument(new DocumentReferenceType(), createInvoice());
    }

}
