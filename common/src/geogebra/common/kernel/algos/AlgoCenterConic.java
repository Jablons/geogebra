/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoMidpointConic.java
 *
 * Created on 11. November 2001, 21:37
 */

package geogebra.common.kernel.algos;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.commands.Commands;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoPoint;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.common.kernel.kernelND.GeoConicNDConstants;
import geogebra.common.kernel.kernelND.GeoPointND;


/**
 * Center of a conic section. 
 */
public class AlgoCenterConic extends AlgoElement {

    protected GeoConicND c; // input
    protected GeoPointND midpoint; // output                 

    public AlgoCenterConic(Construction cons, String label, GeoConicND c) {
        super(cons);
        this.c = c;
        midpoint = newGeoPoint(cons);
        setInputOutput(); // for AlgoElement

        compute();
        midpoint.setLabel(label);
    }
    
    /**
     * 
     * @param cons
     * @return new GeoPoint
     */
    public GeoPointND newGeoPoint(Construction cons){
    	return new GeoPoint(cons);
    }

    @Override
	public Commands getClassName() {
		return Commands.Center;
	}

    @Override
	public int getRelatedModeID() {
    	return EuclidianConstants.MODE_MIDPOINT;
    }
    
    // for AlgoElement
    @Override
	protected void setInputOutput() {
        input = new GeoElement[1];
        input[0] = c;

        super.setOutputLength(1);
        super.setOutput(0, (GeoElement) midpoint);
        setDependencies(); // done by AlgoElement
    }

    GeoConicND getConic() {
        return c;
    }
    public GeoPointND getPoint() {
        return midpoint;
    }

    @Override
	public final void compute() {
        if (!c.isDefined()) {
            midpoint.setUndefined();
            return;
        }

        switch (c.type) {
            case GeoConicNDConstants.CONIC_CIRCLE :
            case GeoConicNDConstants.CONIC_ELLIPSE :
            case GeoConicNDConstants.CONIC_HYPERBOLA :
            case GeoConicNDConstants.CONIC_SINGLE_POINT :
            case GeoConicNDConstants.CONIC_INTERSECTING_LINES :
                setCoords(c.b.getX(), c.b.getY());
                break;

            default :
                // midpoint undefined
                midpoint.setUndefined();
        }
    }
    
    /**
     * set the coords of the midpoint
     * @param x
     * @param y
     */
    protected void setCoords(double x, double y){
    	midpoint.setCoords(x, y, 1.0d);
    }

    @Override
	final public String toString(StringTemplate tpl) {
        // Michael Borcherds 2008-03-30
        // simplified to allow better Chinese translation
    	return loc.getPlain("CenterOfA",c.getLabel(tpl));
    }

	// TODO Consider locusequability
}
