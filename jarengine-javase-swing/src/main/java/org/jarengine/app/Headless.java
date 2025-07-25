/**
 *  MicroEmulator
 *  Copyright (C) 2008 Bartek Teodorczyk <barteo@barteo.net>
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
 */
package org.jarengine.app;

import java.io.InputStream;
import java.util.ArrayList;

import org.jarengine.DisplayComponent;
import org.jarengine.MIDletBridge;
import org.jarengine.app.ui.Message;
import org.jarengine.app.ui.noui.NoUiDisplayComponent;
import org.jarengine.app.util.DeviceEntry;
import org.jarengine.device.DeviceDisplay;
import org.jarengine.device.EmulatorContext;
import org.jarengine.device.FontManager;
import org.jarengine.device.InputMethod;
import org.jarengine.device.impl.DeviceImpl;
import org.jarengine.device.j2se.J2SEDevice;
import org.jarengine.device.j2se.J2SEDeviceDisplay;
import org.jarengine.device.j2se.J2SEFontManager;
import org.jarengine.device.j2se.J2SEInputMethod;
import org.jarengine.log.Logger;

public class Headless {

	private Common emulator;

	private EmulatorContext context = new EmulatorContext() {

		private DisplayComponent displayComponent = new NoUiDisplayComponent();

		private InputMethod inputMethod = new J2SEInputMethod();

		private DeviceDisplay deviceDisplay = new J2SEDeviceDisplay(this);

		private FontManager fontManager = new J2SEFontManager();

		public DisplayComponent getDisplayComponent() {
			return displayComponent;
		}

		public InputMethod getDeviceInputMethod() {
			return inputMethod;
		}

		public DeviceDisplay getDeviceDisplay() {
			return deviceDisplay;
		}

		public FontManager getDeviceFontManager() {
			return fontManager;
		}

		public InputStream getResourceAsStream(Class origClass, String name) {
            return MIDletBridge.getCurrentMIDlet().getClass().getResourceAsStream(name);
		}
		
		public boolean platformRequest(final String URL) {
			new Thread(new Runnable() {
				public void run() {
					Message.info("MIDlet requests that the device handle the following URL: " + URL);
				}
			}).start();

			return false;
		}
	};

	public Headless() {
		emulator = new Common(context);
	}

	public static void main(String[] args) {
		StringBuffer debugArgs = new StringBuffer();
		ArrayList params = new ArrayList();

		// Allow to override in command line
		// Non-persistent RMS
		params.add("--rms");
		params.add("memory");

		for (int i = 0; i < args.length; i++) {
			params.add(args[i]);
			if (debugArgs.length() != 0) {
				debugArgs.append(", ");
			}
			debugArgs.append("[").append(args[i]).append("]");
		}

		if (args.length > 0) {
			Logger.debug("headless arguments", debugArgs.toString());
		}

		Headless app = new Headless();

		DeviceEntry resizableDevice = new DeviceEntry("Resizable device", null, DeviceImpl.RESIZABLE_LOCATION, true, false);

		app.emulator.initParams(params, resizableDevice, J2SEDevice.class);
		app.emulator.initMIDlet(true);
	}

}
