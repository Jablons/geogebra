package org.geogebra.common.kernel.implicit;

import java.util.TreeSet;

import org.geogebra.common.kernel.MyPoint;
import org.geogebra.common.kernel.Matrix.CoordSys;
import org.geogebra.common.kernel.arithmetic.Equation;
import org.geogebra.common.kernel.arithmetic.ExpressionValue;
import org.geogebra.common.kernel.arithmetic.FunctionNVar;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoList;
import org.geogebra.common.kernel.geos.GeoLocusND;
import org.geogebra.common.kernel.kernelND.GeoElementND;
import org.geogebra.common.kernel.kernelND.GeoPointND;

public interface GeoImplicit extends GeoElementND {

	double[][] getCoeff();

	void setCoeff(double[][] coeff);

	void setCoeff(double[][][] coeff);

	void setDefined();

	int getDeg();

	boolean isOnScreen();

	GeoLocusND<? extends MyPoint> getLocus();

	public TreeSet<GeoElement> getAllChildren();

	int getDegX();

	int getDegY();

	void setInputForm();

	boolean isOnPath(GeoPointND r);

	void translate(double d, double e);

	FunctionNVar getExpression();

	boolean isValidInputForm();

	boolean isInputForm();

	void setExtendedForm();

	void fromEquation(Equation equation, double[][] coeff);

	void preventPathCreation();

	void setCoeff(ExpressionValue[][] coeff);

	void throughPoints(GeoList p);

	CoordSys getCoordSys();

	double derivativeX(double x, double y);

	double derivativeY(double x, double y);

	double evaluateImplicitCurve(double x, double y);

}

