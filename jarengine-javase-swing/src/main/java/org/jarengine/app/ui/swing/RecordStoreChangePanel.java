/**
 *  MicroEmulator
 *  Copyright (C) 2006-2007 Bartek Teodorczyk <barteo@barteo.net>
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

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jarengine.app.Common;
import org.jarengine.app.util.FileRecordStoreManager;

public class RecordStoreChangePanel extends SwingDialogPanel {

	private static final long serialVersionUID = 1L;

	private Common common;

	private JComboBox selectStoreCombo = new JComboBox(new String[] { "File record store", "Memory record store" });

	public RecordStoreChangePanel(Common common) {
		this.common = common;

		add(new JLabel("Record store type:"));
		add(selectStoreCombo);
	}

	protected void showNotify() {
		if (common.getRecordStoreManager() instanceof FileRecordStoreManager) {
			selectStoreCombo.setSelectedIndex(0);
		} else {
			selectStoreCombo.setSelectedIndex(1);
		}
	}

	public String getSelectedRecordStoreName() {
		return (String) selectStoreCombo.getSelectedItem();
	}

}
