/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2019, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotools.referencing.operation.projection;

import java.awt.geom.Point2D;
import static java.lang.Math.toRadians;
import static java.lang.Math.toDegrees;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.MathTransform;

/**
 * Homolosine projection
 *
 * @see <A HREF="https://doi.org/10.2307%2F2560812">Goode, J.P. (1925). "The Homolosine projection -
 *     a new device for portraying the Earth's surface entire". Annals of the Association of
 *     American Geographers. 15 (3): 119–125</A>
 * @see <A HREF="https://en.wikipedia.org/wiki/Goode_homolosine_projection">The Homolosine
 *     projection on Wikipedia</A>
 * @since 14.0 << Check what is this
 * @author Luís M. de Sousa
 */
public class Homolosine extends MapProjection {
    /** For cross-version compatibility. */
    private static final long serialVersionUID = -737778661392950541L;

    private static double LAT_THRESH = toRadians(40 + 44 / 60. + 11.8 / 3600.);
    private static double NORTH_THRESH = 4534778.81;
    //Difference between Mollweide and Sinusoidal at threshold latitude
    private static double MOLL_OFFSET = 0.052803527368572; 

    private static final double[] INTERRUP_NORTH = {toRadians(-180), toRadians(-40), toRadians(180)};
    private static final double[] INTERRUP_SOUTH = {toRadians(-180), toRadians(-100), toRadians(-20), 
    											    toRadians(80), toRadians(180)};
    private static final double[] CENTRAL_MERID_NORTH = {toRadians(-100), toRadians(30)};
    private static final double[] CENTRAL_MERID_SOUTH = {toRadians(-160), toRadians(-60), 
    													 toRadians(20), toRadians(140)};

    ParameterDescriptorGroup descriptors;
    ParameterValueGroup parameters; // stored locally to skip computations in parent

    Mollweide moll;
    Sinusoidal sinu;

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Homolosine(
            final ParameterDescriptorGroup descriptors, final ParameterValueGroup parameters)
            throws ParameterNotFoundException {

        super(parameters, descriptors.descriptors());
        this.descriptors = descriptors;
        this.parameters = parameters;
        this.sinu = new Sinusoidal(this.parameters);
        this.moll = new Mollweide(
                    	Mollweide.ProjectionMode.Mollweide, this.descriptors, this.parameters);
//        try 
//        {
//	        Point2D thresh_sinu = sinu.transformNormalized(0., Homolosine.LAT_THRESH, null);
//	        Point2D thresh_moll = moll.transformNormalized(0., Homolosine.LAT_THRESH, null);
//	        this.NORTH_OFFSET = thresh_moll.getY() - thresh_sinu.getY();
//	        System.out.println("###### The difference: " + this.NORTH_OFFSET);
//	        System.out.println("The difference in meters: " + this.NORTH_OFFSET * moll.globalScale);
//	        System.out.println("Scale for Mollweide: " + moll.globalScale);
//	        System.out.println("Scale for Sinusoidal: " + sinu.globalScale + "\n");
//
//	        System.out.println("\n ##### Test points");
//	        
//	        for(double lat = 5; lat <= 40; lat+=5)
//	        {
//	        	Point2D p = sinu.transformNormalized(0., Math.toRadians(lat), null);
//		        System.out.println(lat + ";" + p.getY() * sinu.globalScale);
//	        }
//	        
//	        
//        } catch (ProjectionException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
    }

    /** {@inheritDoc} */
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Provider.PARAMETERS;
    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates (units in
     * radians) and stores the result in {@code ptDst} (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double lam, double phi, Point2D ptDst)
            throws ProjectionException {

        double[] interruptions;
        double[] central_merids;
        double offset = 0;
        int i = 0;
        double central_merid = 0;
        double lam_shift = 0;
        Point2D p;
        Point2D shift;
        
        System.out.println("\nCoordinates recieved: " + Math.toDegrees(lam) + " " + Math.toDegrees(phi));

        if (phi >= 0) {
        	interruptions = INTERRUP_NORTH;
        	central_merids = CENTRAL_MERID_NORTH;
        	offset = this.MOLL_OFFSET;
        }
        else {
        	interruptions = INTERRUP_SOUTH;
        	central_merids = CENTRAL_MERID_SOUTH;
        	offset = - this.MOLL_OFFSET;
        }

        while (lam > interruptions[++i]); 
    	central_merid = central_merids[i - 1];
        lam_shift = lam - central_merid;
        
        System.out.println("lobe_shift: " + toDegrees(central_merid));
        System.out.println("Coordinates to transform: " + Math.toDegrees(lam_shift) + " " + Math.toDegrees(phi));
        
        if (phi > LAT_THRESH || phi < -LAT_THRESH) { // Mollweide
        	System.out.println("Will use Mollweide");			
            p = moll.transformNormalized(lam_shift, phi, ptDst);
            p.setLocation(p.getX(), p.getY() - offset);
        } else { // Sinusoidal
    	    System.out.println("Will use Sinusoidal");	
            p = sinu.transformNormalized(lam_shift, phi, ptDst);
        }
        
        System.out.println("Coordinates transformed: " + p.getX() * sinu.globalScale + " " + p.getY() * sinu.globalScale);
        
        //Point2D shift_sin = sinu.transformNormalized(central_merid, 0., null);
        //System.out.println("Lobe shift Sinusoidal 0: " + shift_sin.getX() * sinu.globalScale + " " + shift_sin.getY() * sinu.globalScale);
        //Point2D shift_sin_phi = sinu.transformNormalized(central_merid, phi, null);
        //System.out.println("Lobe shift Sinusoidal phi: " + shift_sin_phi.getX() * sinu.globalScale + " " + shift_sin_phi.getY() * sinu.globalScale);
        //double east = p.getX() + shift_sin.getX();
        //System.out.println("Final easting: " + east);
        
        shift = sinu.transformNormalized(central_merid, 0., null);
        System.out.println("Lobe shift: " + shift.getX() * sinu.globalScale + " " + shift.getY() * sinu.globalScale);
        p.setLocation(p.getX() + shift.getX(), p.getY());
        System.out.println("Final result: " + p.getX() * sinu.globalScale + " " + p.getY() * sinu.globalScale);
        return p;
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinates and stores the result in
     * {@code ptDst}.
     */
    protected Point2D inverseTransformNormalized(double x, double y, final Point2D ptDst)
            throws ProjectionException {
        double phi;
        double lam;

        if (y > NORTH_THRESH || y < -NORTH_THRESH) { // Mollweide
            return moll.inverseTransformNormalized(x, y, ptDst);
        } else { // Sinusoidal
            return sinu.inverseTransformNormalized(x, y, ptDst);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDERS                                ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The {@linkplain org.geotools.referencing.operation.MathTransformProvider math transform
     * provider} for the Homolosine projection (not part of the EPSG database).
     *
     * @since 14.0
     * @author Mihail Andreev
     * @see org.geotools.referencing.operation.DefaultMathTransformFactory
     */
    public static class Provider extends AbstractProvider {
        /** For cross-version compatibility. */
        private static final long serialVersionUID = 8374488793001927036L;

        /** The parameters group. */
        static final ParameterDescriptorGroup PARAMETERS =
                createDescriptorGroup(
                        new NamedIdentifier[] {
                            new NamedIdentifier(Citations.GEOTOOLS, "Homolosine"),
                            new NamedIdentifier(Citations.ESRI, "Homolosine")
                        },
                        new ParameterDescriptor[] {
                            SEMI_MAJOR, SEMI_MINOR, CENTRAL_MERIDIAN, FALSE_EASTING, FALSE_NORTHING
                        });

        /** Constructs a new provider. */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        protected MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException {
            return new Homolosine(PARAMETERS, parameters);
        }
    }
}
