/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.data.postgis3d;

import org.geotools.jdbc3d.JDBCUuidOnlineTest;
import org.geotools.jdbc3d.JDBCUuidTestSetup;

/**
 *
 * @author kbyte
 */
public class PostGISUuidOnlineTest extends JDBCUuidOnlineTest {

    @Override
    protected JDBCUuidTestSetup createTestSetup() {
        return new PostGISUuidTestSetup(new PostGISTestSetup());
    }

}

