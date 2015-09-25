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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: BigDecimalOperationGreaterThan.java 4227 2011-05-03 11:00:00Z tanderson $
 */


package org.openvpms.component.system.common.jxpath;

import org.apache.commons.jxpath.ri.compiler.Expression;


/**
 * Implementation of {@link Expression} for the operation "&gt;" using BigDecimal.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2011-05-03 08:00:00 -0300 (Tue, 03 May 2011) $
 */
public class BigDecimalOperationGreaterThan extends BigDecimalOperationRelationalExpression {

    /**
     * Constructs a <tt>BigDecimalOperationGreaterThan</tt>.
     *
     * @param arg1 left operand
     * @param arg2 right operand
     */
    public BigDecimalOperationGreaterThan(Expression arg1, Expression arg2) {
        super(new Expression[]{arg1, arg2});
    }

    protected boolean evaluateCompare(int compare) {
        return compare > 0;
    }

    public String getSymbol() {
        return ">";
    }

}
