/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/
package org.geogebra.web.web.gui.dialog;

import org.geogebra.common.gui.InputHandler;
import org.geogebra.common.gui.view.algebra.DialogType;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.web.html5.main.AppW;

import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AngleInputDialog extends InputDialogW {
	

	public static final int DEFAULT_COLUMNS = 30;
	public static final int DEFAULT_ROWS = 10;

	protected RadioButton rbCounterClockWise, rbClockWise;

	public boolean success = true;
	protected String inputText = null;

	/**
	 * Input Dialog for a GeoAngle object.
	 */
	public AngleInputDialog(AppW app,  String message, String title, String initString,
					boolean autoComplete, InputHandler handler, boolean modal) {
		super(modal, app);
		inputHandler = handler;
		this.initString = initString;

		// create radio buttons for "clockwise" and "counter clockwise"
		String id = DOM.createUniqueId();
		rbCounterClockWise = new RadioButton(id, app.getPlain("counterClockwise"));
		rbClockWise = new RadioButton(id, app.getPlain("clockwise"));
		rbCounterClockWise.setValue(true);

		HorizontalPanel rbPanel = new HorizontalPanel();
		rbPanel.setStyleName("DialogRbPanel");
		rbPanel.add(rbCounterClockWise);
		rbPanel.add(rbClockWise);
		
		createGUI(title, message, autoComplete, DEFAULT_COLUMNS, 1, true, false, false, false, DialogType.GeoGebraEditor);

		VerticalPanel centerPanel = new VerticalPanel();
		centerPanel.add(messagePanel);
		centerPanel.add(inputPanel);
		centerPanel.add(errorPanel);
		centerPanel.add(rbPanel);
		((VerticalPanel) wrappedPopup.getWidget()).insert(centerPanel, 0);
		
		wrappedPopup.center();
		inputPanel.getTextComponent().getTextField().setFocus(true);
		
	}

	public boolean isCounterClockWise() {
		return rbCounterClockWise.getValue();
	}

	
	@Override
	protected void actionPerformed(DomEvent e) {
		Object source = e.getSource();

		success=true;
		try {

			if (source == btOK || sourceShouldHandleOK(source)) {
				inputText = inputPanel.getText();
				inputPanel.getTextComponent().hideTablePopup();

				// negative orientation ?
				if (rbClockWise.getValue()) {
					inputText = "-(" + inputText + ")";
				}

				inputHandler.processInput(inputText, this,
						new AsyncOperation<Boolean>() {

							@Override
							public void callback(Boolean ok) {
								afterActionPerformed(ok);

							}
						});
			} else if (source == btCancel) {
				afterActionPerformed(true);
				success=false;
				inputPanel.getTextComponent().hideTablePopup();
			}
		} catch (Exception ex) {
			afterActionPerformed(false);
			// do nothing on uninitializedValue		
			success=false;
		}

	}

	void afterActionPerformed(boolean finished) {
		if (finished) {
			wrappedPopup.hide();
			app.getActiveEuclidianView().requestFocusInWindow();
		} else {
			wrappedPopup.show();
		}

	}
}
