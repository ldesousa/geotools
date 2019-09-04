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
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Homolosine projection
 *
 * @see <A HREF="https://doi.org/10.2307%2F2560812">Goode, J.P. (1925). 
 * 		"The Homolosine projection - a new device for portraying the Earth's surface entire". Annals of 
 *		the Association of American Geographers. 15 (3): 119–125</A>
 * @see <A HREF="https://en.wikipedia.org/wiki/Goode_homolosine_projection">The Homolosine projection on
 *     Wikipedia</A>
 * @since 14.0 << Check what is this
 * @author Luís M. de Sousa
 */

public class Homolosine extends MapProjection {
    /** For cross-version compatibility. */
    private static final long serialVersionUID = -737778661392950541L;

	private static double LAT_THRESH = 40 + 44/60. + 11.8/3600.;

    private static final int[] INTERRUP_NORTH = [-180, -40, 180];
    private static final int[] INTERRUP_SOUTH = [-180, -100, -20, 80, 180];


    ParameterDescriptorGroup descriptors;

    /**
     * Constructs a new map projection from the supplied parameters.
     *
     * @param parameters The parameter values in standard units.
     * @throws ParameterNotFoundException if a mandatory parameter is missing.
     */
    protected Homolosine(
            ProjectionMode mode,
            final ParameterDescriptorGroup descriptors,
            final ParameterValueGroup parameters)
            throws ParameterNotFoundException {

        super(parameters, descriptors.descriptors());
        this.descriptors = descriptors;

    }

    /**
     * Transforms the specified (<var>&lambda;</var>,<var>&phi;</var>) coordinates (units in
     * radians) and stores the result in {@code ptDst} (linear distance on a unit sphere).
     */
    protected Point2D transformNormalized(double lam, double phi, Point2D ptDst)
            throws ProjectionException {

		int[] interruptions;
		int i = 0;

		if(phi >= 0) 
			interruptions = INTERRUP_NORTH;
		else 
			interruptions = INTERRUP_SOUTH;

		while (phi > interruptions[i++])	

		lam_shift = lam - (interruptions[i] - interruptions[i-1]) / 2;

 		if(phi > LAT_TRESH || phi < -LAT_TRESH)
		{ // Mollweide
			moll = Mollweide(ProjectionMode.Mollweide, this.descriptors, this.parameters)
			return moll.transformNormalized(lam_shift, phi, ptDst)
		}
		else
		{ // Sinusoidal
			sinu = Sinusoidal(this.parameters)
			return moll.transformNormalized(lam_shift, phi, ptDst)
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
            return new Homolosine(parameters);
        }
    }
}
