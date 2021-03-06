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

package org.openvpms.archetype.function.list;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.function.expression.ExpressionFunctions;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.jxpath.ObjectFunctions;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link ListFunctions} class.
 *
 * @author Tim Anderson
 */
public class ListFunctionsTestCase extends ArchetypeServiceTest {

    /**
     * The JXPath context.
     */
    private JXPathContext ctx;

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        Party pet1 = createPet("C", "Canine");
        Party pet2 = createPet("A", "Feline");
        Party pet3 = createPet("B", "Bovine");

        List<Party> objects = Arrays.asList(pet1, pet2, pet3);
        ctx = createContext(objects);
    }

    /**
     * Tests the {@link ListFunctions#names(Collection)} and {@link ListFunctions#names(Collection, String)} methods.
     */
    @Test
    public void testNames() {
        assertEquals("C, A, B", ctx.getValue("list:names(.)"));
        assertEquals("C;A;B", ctx.getValue("list:names(., ';')"));
    }

    /**
     * Tests the {@link ListFunctions#sortNamesOf(ExpressionContext, String)} method
     */
    @Test
    public void testSortNamesOf() {
        Party vet = (Party) create(SupplierArchetypes.SUPPLIER_VET);
        vet.addClassification(createVetSpecialty("Surgery"));
        vet.addClassification(createVetSpecialty("Large Animals"));
        vet.addClassification(createVetSpecialty("Snakes"));

        JXPathContext ctx = createContext(vet);

        assertEquals("Large Animals, Snakes, Surgery", ctx.getValue("list:sortNamesOf('classifications')"));

        assertEquals("(Large Animals, Snakes, Surgery)",
                     ctx.getValue("expr:concatIf('(', list:sortNamesOf('classifications'), ')')"));
    }

    /**
     * Tests the {@link ListFunctions#sortNames(Collection)} and {@link ListFunctions#sortNames(Collection, String)}
     * methods.
     */
    @Test
    public void testSortNames() {
        assertEquals("A, B, C", ctx.getValue("list:sortNames(.)"));
        assertEquals("A;B;C", ctx.getValue("list:sortNames(., ';')"));
    }

    /**
     * Tests the {@link ListFunctions#join(Collection, String)} and
     * {@link ListFunctions#join(Collection, String, String)} methods.
     */
    @Test
    public void testJoin() {
        assertEquals("CANINE, FELINE, BOVINE", ctx.getValue("list:join(.,'species')"));
        assertEquals("CANINE;FELINE;BOVINE", ctx.getValue("list:join(.,'species',';')"));
    }

    /**
     * Creates a pet.
     *
     * @param name    the pet name
     * @param species the pet species
     * @return a new pet
     */
    private Party createPet(String name, String species) {
        Party pet = TestHelper.createPatient(false);

        pet.setName(name);
        IMObjectBean bean = new IMObjectBean(pet);
        Lookup lookup = TestHelper.getLookup("lookup.species", species.toUpperCase(), species, true);
        bean.setValue("species", lookup.getCode());
        return pet;
    }

    /**
     * Helper to create an <em>lookup.veterinarySpeciality</em>.
     *
     * @param name the speciality name
     * @return a new lookup
     */
    private Lookup createVetSpecialty(String name) {
        Lookup lookup = (Lookup) create("lookup.veterinarySpeciality");
        lookup.setCode(name);
        lookup.setName(name);
        return lookup;
    }

    /**
     * Creates a new JXPathContext.
     *
     * @param object the context object
     * @return a new JXPathContext
     */
    private JXPathContext createContext(Object object) {
        ListFunctions listFunctions = new ListFunctions(getArchetypeService(), getLookupService());
        FunctionLibrary library = new FunctionLibrary();
        library.addFunctions(new ObjectFunctions(listFunctions, "list"));
        library.addFunctions(new ExpressionFunctions("expr"));
        return JXPathHelper.newContext(object, library);
    }
}
