package geogebra3D.kernel3D.commands;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.commands.CmdMidpoint;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoConicND;
import geogebra.common.kernel.kernelND.GeoPointND;
import geogebra.common.kernel.kernelND.GeoSegmentND;



public class CmdMidpoint3D extends CmdMidpoint {
	
	public CmdMidpoint3D(Kernel kernel) {
		super(kernel);
		
		
	}
	
	/*
	@Override
	public GeoElement[] process(Command c) throws MyError {	


		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;



		switch (n) {
		
		case 1:
			break;
			

		}


		return super.process(c);
	}
	 */
	
	@Override
	protected GeoElement[] segment(String label, GeoSegmentND segment){
		
		if (segment.isGeoElement3D()){
				GeoElement[] ret = { (GeoElement) kernelA.getManager3D().Midpoint(label, segment) };
				return ret;
		}
		
		return super.segment(label, segment);
	}
	
	
	
	@Override
	protected GeoElement[] conic(String label, GeoConicND conic){
		
		if (conic.isGeoElement3D()){
			GeoElement[] ret = { (GeoElement) kernelA.getManager3D().Center(label, conic) };
			return ret;
		}
		
		return super.conic(label, conic);
		
	}
	
	@Override
	protected GeoElement[] twoPoints(String label, GeoPointND p1, GeoPointND p2){

		if (p1.isGeoElement3D() || p2.isGeoElement3D() ){
			GeoElement[] ret = { (GeoElement) kernelA.getManager3D().Midpoint(label,p1,p2) };
			return ret;
		}

		return super.twoPoints(label, p1, p2);

	}
}
