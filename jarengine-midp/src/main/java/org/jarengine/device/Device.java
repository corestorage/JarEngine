/**
 *  MicroEmulator
 *  Copyright (C) 2002-2005 Bartek Teodorczyk <barteo@barteo.net>
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
package org.jarengine.device;

import java.util.Map;
import java.util.Vector;

import javax.microedition.lcdui.Image;

import org.jarengine.device.ui.UIFactory;

public interface Device {

	void init();

	void destroy();

	String getName();

	InputMethod getInputMethod();

	FontManager getFontManager();

	DeviceDisplay getDeviceDisplay();
	
	UIFactory getUIFactory();

	Image getNormalImage();

	Image getOverImage();

	Image getPressedImage();

	Vector getSoftButtons();

	Vector getButtons();

	boolean hasPointerEvents();

	boolean hasPointerMotionEvents();

	boolean hasRepeatEvents();

	boolean vibrate(int duration);

	Map getSystemProperties();

}