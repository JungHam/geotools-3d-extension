/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2015, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.postgis3d;

import java.io.StringWriter;

import org.geotools.data.jdbc3d.FilterToSQLException;
import org.geotools.data.jdbc3d.SQLFilterTestSupport;
import org.geotools.data.postgis3d.PostGISDialect;
import org.geotools.data.postgis3d.PostgisFilterToSQL;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.junit.Before;
import org.junit.Test;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class PostgisFilterToSQLTest extends SQLFilterTestSupport {

    public PostgisFilterToSQLTest(String name) {
        super(name);
    }

    private static FilterFactory2 ff;

    private static GeometryFactory gf = new GeometryFactory();

    private PostGISDialect dialect;

    PostgisFilterToSQL filterToSql;

    StringWriter writer;

    @Before
    public void setUp() throws IllegalAttributeException, SchemaException {
        ff = CommonFactoryFinder.getFilterFactory2();
        dialect = new PostGISDialect(null);
        filterToSql = new PostgisFilterToSQL(dialect);
        writer = new StringWriter();
        filterToSql.setWriter(writer);

        prepareFeatures();
    }

    /**
     * Test for GEOS-5167.
     * Checks that geometries are wrapped with ST_Envelope when used
     * with overlapping operator, when the encodeBBOXFilterAsEnvelope is true. 
     * 
     * @throws FilterToSQLException
     * 
     */
    @Test
    public void testEncodeBBOXFilterAsEnvelopeEnabled() throws FilterToSQLException {
        filterToSql.setEncodeBBOXFilterAsEnvelope(true);
        filterToSql.setFeatureType(testSchema);

        Intersects filter = ff.intersects(
                ff.property("testGeometry"),
                ff.literal(gf.createPolygon(gf.createLinearRing(new Coordinate[] {
                        new Coordinate(0, 0), new Coordinate(0, 2), new Coordinate(2, 2),
                        new Coordinate(2, 0), new Coordinate(0, 0) }))));
        filterToSql.encode(filter);
        assertTrue(writer.toString().toLowerCase().contains("st_envelope"));
    }

    /**
     * Test for GEOS-5167.
     * Checks that geometries are NOT wrapped with ST_Envelope when used
     * with overlapping operator, when the encodeBBOXFilterAsEnvelope is false.
     * 
     * @throws FilterToSQLException
     * 
     */
    @Test
    public void testEncodeBBOXFilterAsEnvelopeDisabled() throws FilterToSQLException {
        filterToSql.setEncodeBBOXFilterAsEnvelope(false);
        filterToSql.setFeatureType(testSchema);

        Intersects filter = ff.intersects(
                ff.property("testGeometry"),
                ff.literal(gf.createPolygon(gf.createLinearRing(new Coordinate[] {
                        new Coordinate(0, 0), new Coordinate(0, 2), new Coordinate(2, 2),
                        new Coordinate(2, 0), new Coordinate(0, 0) }))));
        filterToSql.encode(filter);
        assertFalse(writer.toString().toLowerCase().contains("st_envelope"));
    }
}
