package org.geotools.iso.geojson;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.geotools.feature.simple.ISOSimpleFeatureBuilder;
import org.geotools.feature.simple.ISOSimpleFeatureTypeBuilder;
import org.geotools.iso.geojson.GeoJSONUtil;
import org.geotools.iso.geojson.feature.FeatureJSON;
import org.geotools.iso.geojson.geom.GeometryJSON;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.Geometry;
import org.opengis.geometry.ISOGeometryBuilder;

/**
 * 
 *
 * @source $URL$
 */
public class FeatureJSONExtendedTest extends GeoJSONTestSupport {

	ISOGeometryBuilder gb;
	GeometryJSON gjson;
    FeatureJSON fjson;
    SimpleFeatureType featureType;
    ISOSimpleFeatureBuilder fb;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        ISOSimpleFeatureTypeBuilder tb = new ISOSimpleFeatureTypeBuilder();
        tb.setName("feature");
        tb.setSRS("EPSG:4326");
        tb.add("int", Integer.class);
        tb.add("double", Double.class);
        tb.add("string", String.class);
        tb.add("date", Date.class);
        tb.add("geometry", Geometry.class);
        
        featureType = tb.buildFeatureType();

        gb = new ISOGeometryBuilder(DefaultGeographicCRS.WGS84);
        gjson = new GeometryJSON(gb);
        fjson = new FeatureJSON();
        fjson.setFeatureType(featureType);
        
        fb = new ISOSimpleFeatureBuilder(featureType);
        
    }
    
    public void testFeatureWrite() throws Exception {
        StringWriter writer = new StringWriter();
        fjson.writeFeature(feature(1), writer);
        
        assertEquals(strip(featureText(1)), writer.toString());
    }
    
    public void testFeatureRead() throws Exception {
        SimpleFeature f1 = feature(1);
        SimpleFeature f2 = fjson.readFeature(reader(strip(featureText(1)))); 
        assertEqualsLax(f1, f2);
    }
    
    public void testFeatureReadMismatched() throws Exception {
        SimpleFeature f1 = feature(1, true);
        SimpleFeature f2 = fjson.readFeature(reader(strip(featureText(1, true)))); 
        assertEqualsLax(f1, f2);
    }
    
    public void testFeatureWriteMismatched() throws Exception {
        StringWriter writer = new StringWriter();
        fjson.writeFeature(feature(1, true), writer);
        
        assertEquals(strip(featureText(1, true, false)), writer.toString());
    }
    
    public void testFeatureWriteMismatchedWithNullProperties() throws Exception {
        StringWriter writer = new StringWriter();
        fjson.setEncodeNullValues(true);
        fjson.writeFeature(feature(1, true), writer);

        assertEquals(strip(featureText(1, true, true)), writer.toString());
    }

    SimpleFeature feature(int val) {
        return feature(val, false);
    }
    
    SimpleFeature feature(int val, boolean excludeString) {
        fb.add(val);
        fb.add(val + 0.1);
        
        if (!excludeString) {
            fb.add(toString(val));
        }
        else {
            fb.add(null);
        }
        
        fb.add(toDate(val));
        fb.add(gb.createPoint(gb.createDirectPosition(new double[] {val+0.1,val+0.1})));
        
        return fb.buildFeature("feature." + val);
    }
    
    String featureText(int val) {
        return featureText(val, false, false);
    }

    String featureText(int val, boolean excludeString) {
        return featureText(val, excludeString, false);
    }
    
    String featureText(int val, boolean excludeString, boolean valIsNull) {
        String text = 
            "{" +
            "  'type': 'Feature'," +
            "  'geometry': {" +
            "     'type': 'Point'," +
            "     'coordinates': [" + (val+0.1) + "," + (val+0.1) + "]" +
            "   }, " +
            "'  properties': {" +
            "     'int': " + val + "," +
            "     'double': " + (val + 0.1) + ",";
        
        if (valIsNull) {
            text += "     'string': null,";
        } else if (!excludeString) {
            text += "     'string': '" + toString(val) + "',";
            
        }
        text += 
            "     'date': '" +  toDateString(val) + "'" + 
            "   }," +
            "   'id':'feature." + val + "'" +
            "}";
        
        return text;
    }
    
    String toDateString(int val) {
        return String.format("%s-%s-%sT%s:%s:%s.000+0000", pad(2000+val), pad(val), pad(val), pad(val), 
                pad(val), pad(val));
    }
    
    Date toDate(int val) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat(GeoJSONUtil.DATE_FORMAT);
            return sdf.parse(toDateString(val));
        } 
        catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    String pad(int val) {
        if (String.valueOf(val).length() < 2) {
            return "0" + val;
        }
        return "" + val;
    }
}
