/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.util;

import org.geotools.factory.Hints;
import org.geotools.geometry.iso.io.wkt.WKTReader;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.Geometry;

/**
 * Converter factory performing converstions among geometric types.
 * <p>
 * Supported conversions:
 * <ul>
 * 	<li>{@link String} to {@link org.opengis.geometry.Geometry}
 *  <li>{@link org.opengis.geometry.Geometry} to {@link String}
 * 	<li>{@link org.opengis.geometry.Envelope} to {@link org.opengis.geometry.Geometry}
 *  <li>{@link org.opengis.geometry.Geometry} to {@link org.opengis.geometry.Envelope} 
 *  <li>
 * </ul>
 * </p>
 * @author Hyung-Gyu Ryoo, Pusan National University
 * @since 2.4
 *
 *
 *
 * @source $URL$
 */
public class ISOGeometryConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
	
		if ( Geometry.class.isAssignableFrom( target ) ) {
			
			//String to Geometry
			if ( String.class.equals( source ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
					    return new WKTReader(hints).read((String) source);
					}
				};
			}
			
			//Envelope to Geometry
			if ( Envelope.class.isAssignableFrom( source ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Envelope e = (Envelope) source;
						//TODO
						
						throw new UnsupportedOperationException();
						/*GeometryFactory factory = new GeometryFactory();
						return factory.createPolygon(
							factory.createLinearRing( 
								new Coordinate[] {
									new Coordinate( e.getMinX(), e.getMinY() ),
									new Coordinate( e.getMaxX(), e.getMinY() ), 
									new Coordinate( e.getMaxX(), e.getMaxY() ), 
									new Coordinate( e.getMinX(), e.getMaxY() ),
									new Coordinate( e.getMinX(), e.getMinY() )
								}
							), null
						);*/
					}
				};
			}
		}
		
		if ( Geometry.class.isAssignableFrom( source ) ) {
			//Geometry to envelope
			if ( Envelope.class.equals( target ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Geometry geometry = (Geometry) source;
						return geometry.getEnvelope();
					}
				};
			}
			
			//Geometry to String
			if ( String.class.equals( target ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
						Geometry geometry = (Geometry) source;
						return geometry.toString();
					}
				};
			}
		}
		
		return null;
	}
}
