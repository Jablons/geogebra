package geogebra.factories;

import geogebra.common.factories.Factory;
import geogebra.common.javax.swing.RelationPane;

public class FactoryD extends Factory {

	@Override
	public RelationPane newRelationPane() {
		return new geogebra.javax.swing.RelationPaneD();
	}

}
