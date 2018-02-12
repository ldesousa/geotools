/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2018, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.hana;

import org.geotools.jdbc.JDBCThreeValuedLogicOnlineTest;
import org.geotools.jdbc.JDBCThreeValuedLogicTestSetup;

/** @author Stefan Uhrig, SAP SE */
public class HanaThreeValuedLogicOnlineTest extends JDBCThreeValuedLogicOnlineTest {

    @Override
    protected JDBCThreeValuedLogicTestSetup createTestSetup() {
        return new HanaThreeValuedLogicTestSetup(new HanaTestSetup());
    }
}
