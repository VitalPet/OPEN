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

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.math.Currency;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.product.ProductPrice;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;

/**
 * Product helper methods.
 *
 * @author Tim Anderson
 */
public class ProductHelper {

    /**
     * Returns the price for a product price, multiplied by the service ratio if there is one.
     *
     * @param price    the price
     * @param currency the currency, used to round prices
     * @return the price
     */
    public static BigDecimal getPrice(ProductPrice price, BigDecimal serviceRatio, Currency currency) {
        BigDecimal result = price.getPrice();
        if (result == null) {
            result = BigDecimal.ZERO;
        }
        if (!MathRules.equals(serviceRatio, ONE)) {
            result = currency.roundPrice(result.multiply(serviceRatio));
        }
        return result;
    }

}