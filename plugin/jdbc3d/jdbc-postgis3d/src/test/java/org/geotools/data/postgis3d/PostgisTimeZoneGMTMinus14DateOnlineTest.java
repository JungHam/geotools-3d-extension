package org.geotools.data.postgis3d;

import java.util.TimeZone;

import org.geotools.data.postgis3d.PostgisDateTestSetup;
import org.geotools.jdbc3d.JDBCDateTestSetup;
import org.geotools.jdbc3d.JDBCTimeZoneDateOnlineTest;

/**
 * 
 *
 * @source $URL$
 */
public class PostgisTimeZoneGMTMinus14DateOnlineTest extends JDBCTimeZoneDateOnlineTest {

    @Override
    protected JDBCDateTestSetup createTestSetup() {
        super.setTimeZone(TimeZone.getTimeZone("Etc/GMT-14"));
        return new PostgisDateTestSetup(new PostGISTestSetup());
    }
    
}
