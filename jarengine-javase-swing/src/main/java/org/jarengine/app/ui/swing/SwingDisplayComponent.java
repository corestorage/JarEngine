/*
 *  MicroEmulator
 *  Copyright (C) 2001 Bartek Teodorczyk <barteo@barteo.net>
 *
 *  It is licensed under the following two licenses as alternatives:
 *    1. GNU Lesser General Public License (the "LGPL") version 2.1 or any newer version
 *    2. Apache License (the "AL") Version 2.0
 *
 *  You may not use this file except in compliance with at least one of
 *  the above two licenses.
 *
 *  You may obtain a copy of the LGPL at
 *      http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 *  You may obtain a copy of the AL at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the LGPL or the AL for the specific language governing permissions and
 *  limitations.
 *  
 *  @version $Id$
 */

package org.jarengine.app.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Enumeration;
import java.util.Iterator;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Screen;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.jarengine.DisplayAccess;
import org.jarengine.DisplayComponent;
import org.jarengine.MIDletAccess;
import org.jarengine.MIDletBridge;
import org.jarengine.app.Common;
import org.jarengine.app.ui.DisplayRepaintListener;
import org.jarengine.device.Device;
import org.jarengine.device.DeviceDisplay;
import org.jarengine.device.DeviceFactory;
import org.jarengine.device.impl.ButtonName;
import org.jarengine.device.impl.InputMethodImpl;
import org.jarengine.device.impl.SoftButton;
import org.jarengine.device.impl.ui.CommandManager;
import org.jarengine.device.j2se.J2SEButton;
import org.jarengine.device.j2se.J2SEDeviceDisplay;
import org.jarengine.device.j2se.J2SEGraphicsSurface;
import org.jarengine.device.j2se.J2SEInputMethod;

public class SwingDisplayComponent extends JComponent implements DisplayComponent {
	private static final long serialVersionUID = 1L;

	private SwingDeviceComponent deviceComponent;

	private J2SEGraphicsSurface graphicsSurface;

	private SoftButton initialPressedSoftButton;

	private DisplayRepaintListener displayRepaintListener;

	private boolean showMouseCoordinates = false;

	private Point pressedPoint = new Point();

	private MouseAdapter mouseListener = new MouseAdapter() {

		public void mousePressed(MouseEvent e) {
			deviceComponent.requestFocusInWindow();
			pressedPoint = e.getPoint();

			if (MIDletBridge.getCurrentMIDlet() == null) {
				return;
			}

			if (SwingUtilities.isMiddleMouseButton(e)) {
				// fire
				KeyEvent event = new KeyEvent(deviceComponent, 0, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER,
						KeyEvent.CHAR_UNDEFINED);
				deviceComponent.keyPressed(event);
				deviceComponent.keyReleased(event);
				return;
			}

			Device device = DeviceFactory.getDevice();
			J2SEInputMethod inputMethod = (J2SEInputMethod) device.getInputMethod();
			// if the displayable is in full screen mode, we should not
			// invoke any associated commands, but send the raw key codes
			// instead
			boolean fullScreenMode = device.getDeviceDisplay().isFullScreenMode();

			if (device.hasPointerEvents()) {
				if (!fullScreenMode) {
					Iterator it = device.getSoftButtons().iterator();
					while (it.hasNext()) {
						SoftButton button = (SoftButton) it.next();
						if (button.isVisible()) {
							org.jarengine.device.impl.Rectangle pb = button.getPaintable();
							if (pb != null && pb.contains(e.getX(), e.getY())) {
								initialPressedSoftButton = button;
								button.setPressed(true);
								repaintRequest(pb.x, pb.y, pb.width, pb.height);
								break;
							}
						}
					}
				}
				Point p = deviceCoordinate(device.getDeviceDisplay(), e.getPoint());
				inputMethod.pointerPressed(p.x, p.y);
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (MIDletBridge.getCurrentMIDlet() == null) {
				return;
			}

			Device device = DeviceFactory.getDevice();
			J2SEInputMethod inputMethod = (J2SEInputMethod) device.getInputMethod();
			boolean fullScreenMode = device.getDeviceDisplay().isFullScreenMode();
			if (device.hasPointerEvents()) {
				if (!fullScreenMode) {
					if (initialPressedSoftButton != null && initialPressedSoftButton.isPressed()) {
						initialPressedSoftButton.setPressed(false);
						org.jarengine.device.impl.Rectangle pb = initialPressedSoftButton.getPaintable();
						if (pb != null) {
							repaintRequest(pb.x, pb.y, pb.width, pb.height);
							if (pb.contains(e.getX(), e.getY())) {
								MIDletAccess ma = MIDletBridge.getMIDletAccess();
								if (ma == null) {
									return;
								}
								DisplayAccess da = ma.getDisplayAccess();
								if (da == null) {
									return;
								}
								Displayable d = da.getCurrent();
								Command cmd = initialPressedSoftButton.getCommand();
								if (cmd != null) {
									if (cmd.equals(CommandManager.CMD_MENU)) {
										CommandManager.getInstance().commandAction(cmd);
									} else {
										da.commandAction(cmd, d);
									}
								} else {
									if (d != null && d instanceof Screen) {
										if (initialPressedSoftButton.getName().equals("up")) {
											da.keyPressed(getButtonByButtonName(ButtonName.UP).getKeyCode());
										} else if (initialPressedSoftButton.getName().equals("down")) {
											da.keyPressed(getButtonByButtonName(ButtonName.DOWN).getKeyCode());
										}
									}
								}
							}
						}
					}
					initialPressedSoftButton = null;
				}
				Point p = deviceCoordinate(device.getDeviceDisplay(), e.getPoint());
				inputMethod.pointerReleased(p.x, p.y);
			}
		}

	};

	private MouseMotionListener mouseMotionListener = new MouseMotionListener() {

		public void mouseDragged(MouseEvent e) {
			if (showMouseCoordinates) {
				StringBuffer buf = new StringBuffer();
				int width = e.getX() - pressedPoint.x;
				int height = e.getY() - pressedPoint.y;
				Point p = deviceCoordinate(DeviceFactory.getDevice().getDeviceDisplay(), pressedPoint);
				buf.append(p.x).append(",").append(p.y).append(" ").append(width).append("x").append(height);
				Common.setStatusBar(buf.toString());
			}

			Device device = DeviceFactory.getDevice();
			InputMethodImpl inputMethod = (InputMethodImpl) device.getInputMethod();
			boolean fullScreenMode = device.getDeviceDisplay().isFullScreenMode();
			if (device.hasPointerMotionEvents()) {
				if (!fullScreenMode) {
					if (initialPressedSoftButton != null) {
						org.jarengine.device.impl.Rectangle pb = initialPressedSoftButton.getPaintable();
						if (pb != null) {
							if (pb.contains(e.getX(), e.getY())) {
								if (!initialPressedSoftButton.isPressed()) {
									initialPressedSoftButton.setPressed(true);
									repaintRequest(pb.x, pb.y, pb.width, pb.height);
								}
							} else {
								if (initialPressedSoftButton.isPressed()) {
									initialPressedSoftButton.setPressed(false);
									repaintRequest(pb.x, pb.y, pb.width, pb.height);
								}
							}
						}
					}
				}
				Point p = deviceCoordinate(device.getDeviceDisplay(), e.getPoint());
				inputMethod.pointerDragged(p.x, p.y);
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (showMouseCoordinates) {
				StringBuffer buf = new StringBuffer();
				Point p = deviceCoordinate(DeviceFactory.getDevice().getDeviceDisplay(), e.getPoint());
				buf.append(p.x).append(",").append(p.y);
				Common.setStatusBar(buf.toString());
			}
		}

	};

	private MouseWheelListener mouseWheelListener = new MouseWheelListener() {

		public void mouseWheelMoved(MouseWheelEvent ev) {
			if (ev.getWheelRotation() > 0) {
				// down
				KeyEvent event = new KeyEvent(deviceComponent, 0, System.currentTimeMillis(), 0, KeyEvent.VK_DOWN,
						KeyEvent.CHAR_UNDEFINED);
				deviceComponent.keyPressed(event);
				deviceComponent.keyReleased(event);
			} else {
				// up
				KeyEvent event = new KeyEvent(deviceComponent, 0, System.currentTimeMillis(), 0, KeyEvent.VK_UP,
						KeyEvent.CHAR_UNDEFINED);
				deviceComponent.keyPressed(event);
				deviceComponent.keyReleased(event);
			}
		}

	};

	SwingDisplayComponent(SwingDeviceComponent deviceComponent) {
		this.deviceComponent = deviceComponent;

		setFocusable(false);

		addMouseListener(mouseListener);
		addMouseMotionListener(mouseMotionListener);
		addMouseWheelListener(mouseWheelListener);
	}

	public void init() {
		synchronized (this) {
			graphicsSurface = null;
			initialPressedSoftButton = null;
		}
	}

	public void addDisplayRepaintListener(DisplayRepaintListener l) {
		displayRepaintListener = l;
	}

	public void removeDisplayRepaintListener(DisplayRepaintListener l) {
		if (displayRepaintListener == l) {
			displayRepaintListener = null;
		}
	}

	public Dimension getPreferredSize() {
		Device device = DeviceFactory.getDevice();
		if (device == null) {
			return new Dimension(0, 0);
		}

		return new Dimension(device.getDeviceDisplay().getFullWidth(), device.getDeviceDisplay().getFullHeight());
	}

	protected void paintComponent(Graphics g) {
		if (graphicsSurface != null) {
			synchronized (graphicsSurface) {
				g.drawImage(graphicsSurface.getImage(), 0, 0, null);
			}
		}
	}

	public void repaintRequest(int x, int y, int width, int height) {
		MIDletAccess ma = MIDletBridge.getMIDletAccess();
		if (ma == null) {
			return;
		}
		DisplayAccess da = ma.getDisplayAccess();
		if (da == null) {
			return;
		}
		Displayable current = da.getCurrent();
		if (current == null) {
			return;
		}

		Device device = DeviceFactory.getDevice();
		if (device != null) {
			J2SEDeviceDisplay deviceDisplay = (J2SEDeviceDisplay) device.getDeviceDisplay();

			synchronized (this) {
				if (graphicsSurface == null) {
					graphicsSurface = new J2SEGraphicsSurface(
							device.getDeviceDisplay().getFullWidth(), device.getDeviceDisplay().getFullHeight(), false, 0x000000);
				}

				synchronized (graphicsSurface) {
					deviceDisplay.paintDisplayable(graphicsSurface, x, y, width, height);
					if (!deviceDisplay.isFullScreenMode()) {
						deviceDisplay.paintControls(graphicsSurface.getGraphics());
					}
				}
			}

            if (deviceDisplay.isFullScreenMode()) {
                fireDisplayRepaint(
                        graphicsSurface, x, y, width, height);
			} else {
                fireDisplayRepaint(
                        graphicsSurface, 0, 0, graphicsSurface.getImage().getWidth(), graphicsSurface.getImage().getHeight());
			}
		}
	}

	public void fireDisplayRepaint(J2SEGraphicsSurface graphicsSurface, int x, int y, int width, int height) {
		if (displayRepaintListener != null) {
			displayRepaintListener.repaintInvoked(graphicsSurface);
		}
		
		repaint(x, y, width, height);
	}

	Point deviceCoordinate(DeviceDisplay deviceDisplay, Point p) {
		if (deviceDisplay.isFullScreenMode()) {
			return p;
		} else {
			org.jarengine.device.impl.Rectangle pb = ((J2SEDeviceDisplay) deviceDisplay).getDisplayPaintable();
			return new Point(p.x - pb.x, p.y - pb.y);
		}
	}

	void switchShowMouseCoordinates() {
		showMouseCoordinates = !showMouseCoordinates;
	}

	public J2SEGraphicsSurface getGraphicsSurface() {
		return graphicsSurface;
}

	public MouseAdapter getMouseListener() {
		return mouseListener;
	}

	public MouseMotionListener getMouseMotionListener() {
		return mouseMotionListener;
	}

	public MouseWheelListener getMouseWheelListener() {
		return mouseWheelListener;
	}
	
	private J2SEButton getButtonByButtonName(ButtonName buttonName) {
		J2SEButton result;
		for (Enumeration e = DeviceFactory.getDevice().getButtons().elements(); e.hasMoreElements();) {
			result = (J2SEButton) e.nextElement();
			if (result.getFunctionalName() == buttonName) {
				return result;
			}
		}

		return null;
	}

    // Ensure focus is always set to the device panel after displayable changes
    public void ensureDevicePanelFocus() {
        if (deviceComponent != null) {
            deviceComponent.requestFocusInWindow();
        }
    }
}
