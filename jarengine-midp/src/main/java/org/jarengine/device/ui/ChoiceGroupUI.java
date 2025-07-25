/**
 *  MicroEmulator
 *  Copyright (C) 2009 Bartek Teodorczyk <barteo@barteo.net>
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
 *  @version $Id: ChoiceGroupUI.java 1918 2009-01-21 12:56:43Z barteo $
 */

package org.jarengine.device.ui;

import javax.microedition.lcdui.Image;

public interface ChoiceGroupUI extends ItemUI {

	void delete(int elementNum);
	
	void deleteAll();

	void setSelectedIndex(int elementNum, boolean selected);

	int getSelectedIndex();

	void insert(int elementNum, String stringPart, Image imagePart);
	
	boolean isSelected(int elementNum);

	void setSelectedFlags(boolean[] selectedArray);

	int getSelectedFlags(boolean[] selectedArray);
	
	String getString(int elementNum);

	void set(int elementNum, String stringPart, Image imagePart);
	
	int size();

}
