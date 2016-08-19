/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.jdbc.JoinInfo3D.JoinPart;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

/**
 * Feature reader that wraps multiple feature readers in a join query.
 * 
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class JDBCJoiningFeatureReader3D extends JDBCFeatureReader3D {

    List<JDBCFeatureReader3D> joinReaders;
    SimpleFeatureBuilder joinFeatureBuilder;
    
    public JDBCJoiningFeatureReader3D(String sql, Connection cx, JDBCFeatureSource3D featureSource,
        SimpleFeatureType featureType, JoinInfo3D join, Hints hints) 
        throws SQLException, IOException {

        //super(sql, cx, featureSource, retype(featureType, join), hints);
        super(sql, cx, featureSource, featureType, hints);

        init(cx, featureSource, featureType, join, hints);
    }
    
    public JDBCJoiningFeatureReader3D(PreparedStatement st, Connection cx, JDBCFeatureSource3D featureSource,
        SimpleFeatureType featureType, JoinInfo3D join, Hints hints) 
        throws SQLException, IOException {

        super(st, cx, featureSource, featureType, hints);

        init(cx, featureSource, featureType, join, hints);
    }

    void init(Connection cx, JDBCFeatureSource3D featureSource, SimpleFeatureType featureType, 
        JoinInfo3D join, Hints hints) throws SQLException, IOException {
        joinReaders = new ArrayList<JDBCFeatureReader3D>();
        int offset = featureType.getAttributeCount()
                + getPrimaryKeyOffset(featureSource, getPrimaryKey(), featureType);

        for (JoinPart part : join.getParts()) {
            SimpleFeatureType ft = part.getQueryFeatureType();
            JDBCFeatureReader3D joinReader = new JDBCFeatureReader3D(rs, cx, offset, featureSource.getDataStore()
                    .getAbsoluteFeatureSource(ft.getTypeName()), ft, hints) {
                @Override
                protected void finalize() throws Throwable {
                    // Do nothing.
                    //
                    // This override protects the injected result set and connection from being
                    // closed by the garbage collector, which is unwanted because this is a
                    // delegate which uses resources that will be closed elsewhere, or so it
                    // is claimed in the comment in the close() method below. See GEOT-4204.
                }
            };
            joinReaders.add(joinReader);
            offset += ft.getAttributeCount()
                    + getPrimaryKeyOffset(featureSource, joinReader.getPrimaryKey(), ft);
        }

        //builder for the final joined feature
        joinFeatureBuilder = new SimpleFeatureBuilder(retype(featureType, join));
    }

    private int getPrimaryKeyOffset(JDBCFeatureSource3D featureSource, PrimaryKey pk,
            SimpleFeatureType featureType) {
        // if we are not exposing them, they are all extras
        int pkSize = pk.getColumns().size();
        if (!featureSource.isExposePrimaryKeyColumns()) {
            return pkSize;
        }

        // otherwise, we have to check if they are requested or not, as we are going to
        // have them anyways as part of the sql query, but not necessarily in the requested ft
        int requestedPkColumns = 0;
        for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
            if (ad.getUserData().get(JDBCDataStore.JDBC_PRIMARY_KEY_COLUMN) == Boolean.TRUE) {
                requestedPkColumns++;
            }
        }

        return pkSize - requestedPkColumns;
    }

    @Override
    public boolean hasNext() throws IOException {
        boolean next = super.hasNext();
        for (JDBCFeatureReader3D r : joinReaders) {
            r.setNext(next);
        }
        return next;
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        //read the regular feature
        SimpleFeature f = super.next();

        //rebuild it with the join feature type
        joinFeatureBuilder.init(f);
        f = joinFeatureBuilder.buildFeature(f.getID());

        //add additional attributes for joined features
        for (int i = 0; i < joinReaders.size(); i++) {
            JDBCFeatureReader3D r = joinReaders.get(i);
            f.setAttribute(f.getAttributeCount() - joinReaders.size() + i, r.next());
        }

        return f;
    }

    @Override
    public void close() throws IOException {
        super.close();

        //we don't need to close the delegate readers because they share the same result set 
        // and connection as this reader
    }

    static SimpleFeatureType retype(SimpleFeatureType featureType, JoinInfo3D join) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.init(featureType);
        
        for (JoinPart part : join.getParts()) {
            b.add(part.getAttributeName(), SimpleFeature.class);
        }
        return b.buildFeatureType();
    }
}