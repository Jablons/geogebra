package org.geogebra.web.html5.event;

import java.util.LinkedList;

import org.geogebra.common.util.debug.Log;

public class KeyEventW extends org.geogebra.common.euclidian.event.KeyEvent {

	public static LinkedList<KeyEventW> pool = new LinkedList<KeyEventW>();
	private com.google.gwt.event.dom.client.KeyPressEvent event;

	private KeyEventW(com.google.gwt.event.dom.client.KeyPressEvent e) {
		Log.debug("possible missing release()");
		this.event = e;
	}

	public static KeyEventW wrapEvent(
	        com.google.gwt.event.dom.client.KeyPressEvent e) {
		if (!pool.isEmpty()) {
			KeyEventW wrap = pool.getLast();
			wrap.event = e;
			pool.removeLast();
			return wrap;
		}
		return new KeyEventW(e);
	}

	public void release() {
		KeyEventW.pool.add(this);
	}

	@Override
	public boolean isEnterKey() {
		return event.getNativeEvent().getKeyCode() == 13
				|| event.getNativeEvent().getKeyCode() == 10
				|| (event.getNativeEvent().getKeyCode() == 0 && event
						.getNativeEvent().getCharCode() == 13);
	}

	@Override
	public boolean isCtrlDown() {
		return event.isControlKeyDown();
	}

	@Override
	public boolean isAltDown() {
		return event.isAltKeyDown();
	}

	@Override
	public char getCharCode() {
		return event.getCharCode();
	}

	@Override
	public void preventDefault() {
		event.preventDefault();
	}

}
