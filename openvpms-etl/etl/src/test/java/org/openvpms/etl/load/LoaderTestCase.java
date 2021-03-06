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

package org.openvpms.etl.load;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Tests the {@link Loader}.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("/applicationContext.xml")
public class LoaderTestCase extends AbstractJUnit4SpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * The DAO.
     */
    private ETLLogDAO dao;


    /**
     * Tests a single object.
     *
     * @throws Exception for any error
     */
    @Test
    public void testObject() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);

        String loaderName = "CUSTLOAD";
        Loader loader = createLoader(loaderName, mappings);
        String legacyId = "ID1";
        ETLRow row = createCustomerRow(legacyId, "Foo", "Bar");

        List<IMObject> objects = loader.load(row);
        loader.close();
        assertEquals(1, objects.size());
        IMObject object = objects.get(0);
        IMObjectBean bean = new IMObjectBean(object, service);
        assertEquals("Foo", bean.getString("firstName"));
        assertEquals("Bar", bean.getString("lastName"));

        List<ETLLog> logs = dao.get(loaderName, legacyId, null);
        assertEquals(1, logs.size());
        checkLog(logs, loaderName, legacyId, "party.customerperson", -1);
    }

    /**
     * Tests a collection node.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCollection() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping addressMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>address");
        Mapping suburbMap = createMapping(
                "SUBURB",
                "<party.customerperson>contacts[0]<contact.location>suburb");
        Mapping phoneMap = createMapping("PHONE",
                                         "<party.customerperson>contacts[1]<contact.phoneNumber>telephoneNumber");
        mappings.addMapping(addressMap);
        mappings.addMapping(suburbMap);
        mappings.addMapping(phoneMap);

        String loaderName = "CUSTLOAD";
        Loader loader = createLoader(loaderName, mappings);
        String legacyId = "ID1";
        ETLRow row = new ETLRow(legacyId);
        row.add("ADDRESS", "49 Foo St Bar");
        row.add("SUBURB", "Coburg");
        row.add("PHONE", "123456789");

        List<IMObject> objects = loader.load(row);
        loader.close();
        assertEquals(3, objects.size());
        IMObjectBean customer = new IMObjectBean(objects.get(0));
        IMObjectBean location = new IMObjectBean(objects.get(1));
        IMObjectBean phone = new IMObjectBean(objects.get(2));
        assertTrue(customer.isA("party.customerperson"));
        assertTrue(location.isA("contact.location"));
        assertTrue(phone.isA("contact.phoneNumber"));

        List<ETLLog> logs = dao.get(loaderName, legacyId, null);
        assertEquals(1, logs.size());
        checkLog(logs, loaderName, legacyId, "party.customerperson", -1);
    }

    /**
     * Tests a collection heirarchy.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCollectionHeirarchy() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        getClassification("lookup.contactPurpose", "MAILING");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        Mapping suburbMap = createMapping(
                "ADDRESS",
                "<party.customerperson>contacts[0]<contact.location>purposes[1]");
        String ref = "<lookup.contactPurpose>code=MAILING";
        suburbMap.setValue(ref);
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);
        mappings.addMapping(suburbMap);

        Loader loader = createLoader("CUSTLOAD", mappings);
        ETLRow row = createCustomerRow("ID1", "Foo", "Bar");
        row.add("ADDRESS", "49 Foo St Bar");
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(2, objects.size());

        IMObjectBean customer = new IMObjectBean(objects.get(0));
        IMObjectBean location = new IMObjectBean(objects.get(1));
        assertTrue(customer.isA("party.customerperson"));
        assertTrue(location.isA("contact.location"));
    }

    /**
     * Verifies that the string <em>$value</em> is expanded with the input
     * value.
     *
     * @throws Exception for any error
     */
    @Test
    public void testValueExpansion() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("INVOICEID");

        Mapping mapping = createMapping(
                "INVOICEID",
                "<act.customerAccountChargesInvoice>items[0]<actRelationship.customerAccountInvoiceItem>source");
        mapping.setValue("<act.customerAccountChargesInvoice>$value");
        mappings.addMapping(mapping);

        Loader loader = createLoader("ACTLOAD", mappings);
        ETLRow row = new ETLRow("INVOICE1");
        row.add("INVOICEID", "INVOICE1");
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(2, objects.size());
        IMObjectBean act = new IMObjectBean(objects.get(0));
        IMObjectBean rel = new IMObjectBean(objects.get(1));
        assertTrue(act.isA("act.customerAccountChargesInvoice"));
        assertTrue(rel.isA("actRelationship.customerAccountInvoiceItem"));
        List<ActRelationship> items
                = act.getValues("items", ActRelationship.class);
        assertEquals(items.size(), 1);
        assertEquals(rel.getObject(), items.get(0));
        assertEquals(act.getObject().getObjectReference(),
                     rel.getValue("source"));
    }

    /**
     * Verifies that an object isn't created if 'excludeNull' is specified
     * and the input field is null.
     *
     * @throws Exception for any error
     */
    @Test
    public void testExcludeNull() throws Exception {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("CUSTID");

        Mapping mapping = createMapping(
                "TELEPHONE",
                "<party.customerperson>contacts[0]<contact.phoneNumber>telephoneNumber");
        mapping.setExcludeNull(true);
        mappings.addMapping(mapping);

        Loader loader = createLoader("CUSTLOAD", mappings);
        ETLRow row = new ETLRow("CUSTID");
        row.add("TELEPHONE", null);
        List<IMObject> objects = loader.load(row);
        loader.close();

        assertEquals(0, objects.size());
    }

    /**
     * Verifies that wildcards can be specified for archetype/legacyId
     * references.
     */
    @Test
    public void testReferenceWildcards() {
        // create a product
        Product product = (Product) service.create("product.medication");
        assertNotNull(product);
        product.setName("XLoaderTestCaseProduct" + System.currentTimeMillis());
        service.save(product);

        // add a log for the product, so it can be referenced
        ETLLog productLog = new ETLLog("PRODLOADER", "PROD1",
                                       "product.medication");
        productLog.setReference(product.getObjectReference());
        dao.save(productLog);

        // create a product participation mapping
        Mappings mappings = new Mappings();
        mappings.setIdColumn("INVOICEID");
        Mapping productMap = createMapping(
                "PRODUCTID",
                "<act.customerAccountInvoiceItem>product[0]<participation.product>entity",
                "<product.*>$value");
        mappings.addMapping(productMap);

        // load a (partial) invoice item
        Loader loader = createLoader("INVLOAD", mappings);
        ETLRow row = new ETLRow("INVOICEID");
        row.add("INVOICEID", "INV1");
        row.add("PRODUCTID", "PROD1");
        List<IMObject> objects = loader.load(row);
        loader.close();

        // verify the invoice and participation objects were created and
        // that they reference each other
        assertEquals(2, objects.size());
        IMObjectBean act = new IMObjectBean(objects.get(0));
        IMObjectBean participation = new IMObjectBean(objects.get(1));
        assertTrue(act.isA("act.customerAccountInvoiceItem"));
        assertTrue(participation.isA("participation.product"));

        List<Participation> participations
                = act.getValues("product", Participation.class);
        assertEquals(1, participations.size());
        assertEquals(participations.get(0), participation.getObject());
        assertEquals(product.getObjectReference(),
                     participation.getValue("entity"));
    }

    /**
     * Verifies that lookups and lookup relationships are saved after each
     * row loads.
     */
    @Test
    public void testLookups() {
        // create a patient mapping containg species and breed mapping
        Mappings mappings = new Mappings();
        mappings.setIdColumn("PATIENTID");
        Mapping speciesMap = createMapping(
                "SPECIES", "<party.patientpet>species");
        Mapping breedMap = createMapping("BREED", "<party.patientpet>breed");
        mappings.addMapping(speciesMap);
        mappings.addMapping(breedMap);

        String species1 = "S1" + System.currentTimeMillis();
        String species2 = "S2" + System.currentTimeMillis();
        String breed1 = "B1" + System.currentTimeMillis();
        String breed2 = "B2" + System.currentTimeMillis();

        // load (partial) patients and verify the lookups have been created
        Loader loader = createLoader("PATIENTLOAD", mappings);
        ETLRow row1 = createPatientRow("patient1", species1, breed1);
        loader.load(row1);
        checkSpeciesBreedLookups(species1, breed1);

        ETLRow row2 = createPatientRow("patient2", species2, breed2);
        loader.load(row2);
        checkSpeciesBreedLookups(species2, breed2);
    }

    /**
     * Verifies that lookup.local codes are passed through and not uppercased.
     */
    @Test
    public void testLocalLookups() {
        Mappings mappings = new Mappings();
        mappings.setIdColumn("TEMPLATEID");
        Mapping archetypeMap = createMapping("ARCHETYPE", "<entity.documentTemplate>archetype");
        mappings.addMapping(archetypeMap);

        Loader loader = createLoader("TEMPLATELOAD", mappings);
        ETLRow row1 = createTemplateRow("document1", "act.patientDocumentForm");
        List<IMObject> objects = loader.load(row1);
        assertEquals(1, objects.size());
        assertEquals("act.patientDocumentForm", objects.get(0).getDetails().get("archetype"));

        ETLRow row2 = createTemplateRow("document2", "REPORT");
        objects = loader.load(row2);
        assertEquals(1, objects.size());
        assertEquals("REPORT", objects.get(0).getDetails().get("archetype"));
        loader.close();
    }

    /**
     * Tests mapping references.
     * This adds two prices to an existing product.
     */
    @Test
    public void testMappingReference() {
        // create a product
        Product product = (Product) service.create("product.medication");
        assertNotNull(product);
        product.setName("XLoaderTestCaseProduct" + System.currentTimeMillis());
        service.save(product);

        // add a log for the product, so it can be referenced
        ETLLog productLog = new ETLLog("PRODLOADER", "PROD1",
                                       "product.medication");
        productLog.setReference(product.getObjectReference());
        dao.save(productLog);

        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping priceMap = createMapping(
                "PRICE",
                "$PRODID<product.medication>prices[1]<productPrice.unitPrice>price");
        mappings.addMapping(priceMap);

        String loaderName = "PRICELOAD";
        Loader loader = createLoader(loaderName, mappings);

        // create two rows for different prices that reference the same product
        String priceId1 = "ID1";
        String priceId2 = "ID2";
        BigDecimal price1 = new BigDecimal("1.5");
        BigDecimal price2 = new BigDecimal("0.95");
        ETLRow row1 = createPriceRow(priceId1, "PROD1", price1);
        ETLRow row2 = createPriceRow(priceId2, "PROD1", price2);

        List<IMObject> objects1 = loader.load(row1);
        List<IMObject> objects2 = loader.load(row2);
        loader.close();
        assertEquals(2, objects1.size());
        assertEquals(product, objects1.get(0));

        assertEquals(2, objects2.size());
        assertEquals(product, objects2.get(0));

        IMObjectBean price1Bean = new IMObjectBean(objects1.get(1), service);
        assertTrue(price1Bean.isA("productPrice.unitPrice"));
        assertEquals(0, price1.compareTo(price1Bean.getBigDecimal("price")));

        IMObjectBean price2Bean = new IMObjectBean(objects2.get(1), service);
        assertTrue(price2Bean.isA("productPrice.unitPrice"));
        assertEquals(0, price2.compareTo(price2Bean.getBigDecimal("price")));

        List<ETLLog> logs = dao.get(loaderName, priceId1, null);
        assertEquals(1, logs.size());
        checkLog(logs, loaderName, priceId1, "product.medication", -1);

        logs = dao.get(loaderName, priceId2, null);
        assertEquals(1, logs.size());
        checkLog(logs, loaderName, priceId2, "product.medication", -1);
    }

    /**
     * Verifies that a formatted error message is logged and that the registered
     * error handler is invoked if an object fails to be processed.
     */
    @Test
    public void testErrorLogging() {
        final String expectedError = "Failed to validate Last Name of Customer: value is required";

        Mappings mappings = new Mappings();
        mappings.setIdColumn("LEGACY_ID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);

        String loaderName = "CUSTLOAD";
        Loader loader = new Loader(loaderName, mappings, dao, service,
                                   new DefaultObjectHandler(loaderName,
                                                            mappings, dao,
                                                            service));
        final String legacyId = "ID1";
        ETLRow row = createCustomerRow(legacyId, "Foo", null);

        // register an error listener
        Listener listener = new Listener(legacyId, expectedError,
                                         ValidationException.class);
        loader.setErrorListener(listener);

        loader.load(row);
        loader.close();

        // verify the error listener was invoked once
        assertEquals(1, listener.getRowErrorCount());

        // verify there is a single log, with the expected error message
        checkLogError(loaderName, legacyId, expectedError);
    }

    /**
     * Verifies that failure processing one row doesn't prevent processing of
     * other rows.
     */
    @Test
    public void testPartialFailure() {
        final String expectedError = "Failed to validate Last Name of Customer: value is required";

        // create a patient
        Party patient = (Party) service.create("party.patientpet");
        IMObjectBean bean = new IMObjectBean(patient, service);
        bean.setValue("species", "Foo");
        bean.setValue("breed", "Bar");
        patient.setName("XLoaderTestCasePatient" + System.currentTimeMillis());
        service.save(patient);

        // add a log for the patientt, so it can be referenced
        ETLLog patientLog = new ETLLog("PATIENTLOADER", "PATIENT1",
                                       "party.patientpet");
        patientLog.setReference(patient.getObjectReference());
        dao.save(patientLog);

        Mappings mappings = new Mappings();
        mappings.setBatchSize(50);
        mappings.setIdColumn("CUSTOMERID");

        Mapping firstNameMap = createMapping("FIRST_NAME",
                                             "<party.customerperson>firstName");
        Mapping lastNameMap = createMapping("LAST_NAME",
                                            "<party.customerperson>lastName");
        Mapping sourceMap = createMapping("CUSTOMERID",
                                          "<entityRelationship.patientOwner>source",
                                          "<party.customerperson>$value");
        Mapping targetMap = createMapping("PATIENTID",
                                          "<entityRelationship.patientOwner>target",
                                          "<party.patientpet>$value");
        mappings.addMapping(firstNameMap);
        mappings.addMapping(lastNameMap);
        mappings.addMapping(sourceMap);
        mappings.addMapping(targetMap);

        String loaderName = "CUSTLOAD";
        Loader loader = new Loader(loaderName, mappings, dao, service,
                                   new DefaultObjectHandler(loaderName,
                                                            mappings, dao,
                                                            service));
        ETLRow row1 = createCustomerRow("ID1", "Foo", "Bar");
        row1.add("PATIENTID", "PATIENT1");
        ETLRow row2 = createCustomerRow("ID2", "Pippi", null);
        row2.add("PATIENTID", "PATIENT1");
        ETLRow row3 = createCustomerRow("ID3", "Rin", "Bar");
        row3.add("PATIENTID", "PATIENT1");

        // register an error listener that checks for failure of row ID2
        Listener listener = new Listener("ID2", expectedError,
                                         ValidationException.class);
        loader.setErrorListener(listener);

        // load the rows
        loader.load(row1);
        loader.load(row2);
        loader.load(row3);
        loader.close();

        // verify the error listener was invoked once
        assertEquals(1, listener.getRowErrorCount());

        // verify there is a 2 logs present for each row, with expected
        // values for error message
        checkLogError(loaderName, "ID1", null, 2);
        checkLogError(loaderName, "ID2", expectedError, 2);
        checkLogError(loaderName, "ID3", null, 2);
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        service = (IArchetypeService) applicationContext.getBean("archetypeService");
        dao = new ETLLogDAOTestImpl();
    }

    /**
     * Verifies a log contains the expected data.
     *
     * @param logs       the logs
     * @param loaderName the expected loader name
     * @param legacyId   the expected legacy row identifier
     * @param archetype  the expected archetype
     * @param index      the expected index
     */
    private void checkLog(List<ETLLog> logs, String loaderName,
                          String legacyId, String archetype, int index) {
        boolean found = false;
        for (ETLLog log : logs) {
            if (log.getLoader().equals(loaderName)
                && log.getRowId().equals(legacyId)
                && log.getArchetype().equals(archetype)
                && log.getIndex() == index) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Verifies that a single log exists for a loader and legacyId,
     * with the specified error message (or null if it isn't in error).
     *
     * @param loaderName the loader name
     * @param legacyId   the legacy row identifier
     * @param message    the expected error message. May be <tt>null</tt>
     */
    private void checkLogError(String loaderName, String legacyId,
                               String message) {
        checkLogError(loaderName, legacyId, message, 1);
    }

    /**
     * Verifies that a the expected no. of logs exists for a loader and
     * legacyId, with the specified error message (or null if it isn't in
     * error).
     *
     * @param loaderName the loader name
     * @param legacyId   the legacy row identifier
     * @param message    the expected error message. May be <tt>null</tt>
     * @param count      the expected no. of logs
     */
    private void checkLogError(String loaderName, String legacyId,
                               String message, int count) {
        // verify there is a single log, with the expected error message
        List<ETLLog> logs = dao.get(loaderName, legacyId, null);
        assertEquals(count, logs.size());
        for (ETLLog log : logs) {
            assertEquals(message, log.getErrors());
            if (message != null) {
                assertEquals(-1, log.getId());
            } else {
                assertTrue(log.getId() != -1);
            }
        }
    }

    /**
     * Verifies that species and breed lookups have been created, with a
     * relationship between each
     *
     * @param speciesCode the species code
     * @param breedCode   the breed code
     */
    private void checkSpeciesBreedLookups(String speciesCode,
                                          String breedCode) {
        ILookupService service = LookupServiceHelper.getLookupService();
        Lookup species = service.getLookup("lookup.species", speciesCode);
        assertNotNull(species);
        Lookup breed = service.getLookup("lookup.breed", breedCode);
        assertNotNull(breed);

        // make sure the relationship exists
        Set<LookupRelationship> speciesRelationships
                = species.getLookupRelationships();
        assertEquals(1, speciesRelationships.size());
        LookupRelationship s = speciesRelationships.iterator().next();
        assertEquals(species.getObjectReference(), s.getSource());
        assertEquals(breed.getObjectReference(), s.getTarget());

        Set<LookupRelationship> breedRelationships
                = breed.getLookupRelationships();
        assertEquals(1, breedRelationships.size());
        LookupRelationship b = breedRelationships.iterator().next();
        assertEquals(species.getObjectReference(), b.getSource());
        assertEquals(breed.getObjectReference(), b.getTarget());
    }

    /**
     * Helper to create a row containing a customer first name and last name.
     *
     * @param legacyId  the customer legacy id
     * @param firstName the first name
     * @param lastName  the last name
     * @return a new row
     */
    private ETLRow createCustomerRow(String legacyId, String firstName,
                                     String lastName) {
        ETLRow row = new ETLRow(legacyId);
        row.add("CUSTOMERID", legacyId);
        row.add("FIRST_NAME", firstName);
        row.add("LAST_NAME", lastName);
        return row;
    }

    /**
     * Helper to create a row containing a patient id, species and breed.
     *
     * @param rowId   the patient id
     * @param species the species
     * @param breed   the breed
     * @return a new row
     */
    private ETLRow createPatientRow(String rowId, String species,
                                    String breed) {
        ETLRow row = new ETLRow(rowId);
        row.add("PATIENTID", rowId);
        row.add("SPECIES", species);
        row.add("BREED", breed);
        return row;
    }

    /**
     * Helper to create a row containing a price id, product id and price.
     *
     * @param legacyId  the price legacy id
     * @param productId the product legacy id
     * @param price     the price
     * @return a new row
     */
    private ETLRow createPriceRow(String legacyId, String productId,
                                  BigDecimal price) {
        ETLRow row = new ETLRow(legacyId);
        row.add("PRICE", price);
        row.add("PRODID", productId);
        return row;
    }

    /**
     * Helper to create a row containing a document template id and archetype.
     *
     * @param rowId     the document template id
     * @param archetype the archetype
     * @return a new row
     */
    private ETLRow createTemplateRow(String rowId, String archetype) {
        ETLRow row = new ETLRow(rowId);
        row.add("TEMPLATEID", rowId);
        row.add("ARCHETYPE", archetype);
        return row;
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target) {
        return createMapping(source, target, null);
    }

    /**
     * Helper to create a new mapping.
     *
     * @param source the source to map
     * @param target the target to map to
     * @param value  the value. May be <tt>null</tt>
     * @return a new mapping
     */
    private Mapping createMapping(String source, String target, String value) {
        Mapping mapping = new Mapping();
        mapping.setSource(source);
        mapping.setTarget(target);
        mapping.setValue(value);
        return mapping;
    }

    /**
     * Gets a classification lookup, creating it if it doesn't exist.
     *
     * @param shortName the clasification short name
     * @param code      the classification code
     * @return the classification
     */
    private Lookup getClassification(String shortName, String code) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("code", code));
        query.setMaxResults(1);
        QueryIterator<Lookup> iter = new IMObjectQueryIterator<Lookup>(query);
        if (iter.hasNext()) {
            return iter.next();
        }
        Lookup classification = (Lookup) service.create(shortName);
        classification.setCode(code);
        service.save(classification);
        return classification;
    }

    private Loader createLoader(String loaderName, Mappings mappings) {
        return new Loader(loaderName, mappings, dao, service,
                          new TestObjectHandler(loaderName, mappings, dao,
                                                service));
    }


    private class TestObjectHandler extends DefaultObjectHandler {

        public TestObjectHandler(String loaderName, Mappings mappings,
                                 ETLLogDAO dao, IArchetypeService service) {
            super(loaderName, mappings, dao, service);
        }

        /**
         * Saves a set of mapped objects.
         *
         * @param objects   the objects to save
         * @param logs      the logs for each object, keyed on link identifier
         * @param saveError if <tt>true</tt>, save any error in the supplied
         *                  logs otherwise just notify if an error occurs
         * @return <tt>true</tt> if the save was successful, otherwise
         *         <tt>false</tt>
         */
        @Override
        protected boolean save(Collection<IMObject> objects,
                               Map<String, List<ETLLog>> logs,
                               boolean saveError) {
            for (List<ETLLog> logList : logs.values()) {
                dao.save(logList);
            }
            return true;
        }
    }

    private class Listener implements ErrorListener {

        private String expectedRowId;

        private String expectedMessage;

        private Class expectedException;

        private int count;

        public Listener(String rowId, String message, Class exception) {
            this.expectedRowId = rowId;
            this.expectedMessage = message;
            this.expectedException = exception;

        }

        /**
         * Returns the no. of times an error occurred for a row.
         *
         * @return the error count
         */
        public int getRowErrorCount() {
            return count;
        }

        /**
         * Invoked when an error occurs for a particular row.
         *
         * @param rowId     the identifier of the row that triggered the error
         * @param message   the error message
         * @param exception the exception
         */
        public void error(String rowId, String message, Throwable exception) {
            ++count;
            assertEquals(expectedRowId, rowId);
            assertEquals(expectedMessage, message);
            assertEquals(expectedException, exception.getClass());
        }

        /**
         * Invoked when an error occurs.
         *
         * @param message   the error message
         * @param exception the exception
         */
        public void error(String message, Throwable exception) {
            assertEquals(expectedMessage, message);
            assertEquals(expectedException, exception.getClass());
        }
    }
}
