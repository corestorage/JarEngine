/*
 *  MicroEmulator
 *  Copyright (C) 2001-2006 Bartek Teodorczyk <barteo@barteo.net>
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

package org.jarengine.applet;

import java.applet.Applet;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.microedition.rms.RecordStoreNotOpenException;

//import netscape.javascript.JSObject;

import org.jarengine.MicroEmulator;
import org.jarengine.RecordStoreManager;
import org.jarengine.log.Logger;
import org.jarengine.util.Base64Coder;
import org.jarengine.util.ExtendedRecordListener;
import org.jarengine.util.RecordStoreImpl;

// TODO add JSObject import again
public class CookieRecordStoreManager implements RecordStoreManager {

	private static final int MAX_SPLIT_COOKIES = 5; // max 10

	private static final int MAX_COOKIE_SIZE = 4096 * 3 / 4; // Base64

	private ExtendedRecordListener recordListener = null;

	private Applet applet;

//	private JSObject document;

	private HashMap cookies;

	private String expires;

	public CookieRecordStoreManager(Applet applet) {
		this.applet = applet;

		Calendar c = Calendar.getInstance();
		c.add(java.util.Calendar.YEAR, 1);
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MM-yyyy hh:mm:ss z");
		this.expires = "; Max-Age=" + (60 * 60 * 24 * 365);
		// Logger.debug("CookieRecordStoreManager: " + this.expires);
	}

	public void init(MicroEmulator emulator) {
	}

	public String getName() {
		return this.getClass().toString();
	}

	public void deleteRecordStore(String recordStoreName) throws RecordStoreNotFoundException, RecordStoreException {
		CookieContent cookieContent = (CookieContent) cookies.get(recordStoreName);
		if (cookieContent == null) {
			throw new RecordStoreNotFoundException(recordStoreName);
		}

		removeCookie(recordStoreName, cookieContent);
		cookies.remove(recordStoreName);

		fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_DELETE, recordStoreName);

		// Logger.debug("deleteRecordStore: " + recordStoreName);
	}

	public void deleteStores() {
		for (Iterator it = cookies.keySet().iterator(); it.hasNext();) {
			try {
				deleteRecordStore((String) it.next());
			} catch (RecordStoreException ex) {
				Logger.error(ex);
			}
		}
		// Logger.debug("deleteStores:");
	}

	public void init() {
		String token = getParameter("cookies");
		if (token == null) {
			return;
		}
		int index = token.indexOf("=");
		if (index == -1) {
			return;
		}
		this.expires = token.substring(index + 1);
		// Logger.debug("CookieRecordStoreManager: " + this.expires);

		String[] cookies = token.substring(0, index).split(",");
		for (int i = 0; i < cookies.length; i++) {
			this.cookies.put(cookies[i], null);
		}
		// Logger.debug("init: " + cookies.length);
	}

	public String[] listRecordStores() {
		// Logger.debug("listRecordStores:");
		String[] result = new String[cookies.size()];
		int i = 0;
		for (Object key : cookies.keySet()) {
			result[i++] = (String) key;
		}
		return result;
	}

	private String getParameter(String name) {
		// This is a stub implementation for applet context
		// In a real applet, this would get the parameter from the applet context
		return null;
	}

	public RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary)
			throws RecordStoreNotFoundException {
		RecordStoreImpl result;

		CookieContent load = (CookieContent) cookies.get(recordStoreName);
		if (load != null) {
			try {
				byte[] data = Base64Coder.decode(load.toCharArray());
				result = new RecordStoreImpl(this);
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
				int size = result.readHeader(dis);
				for (int i = 0; i < size; i++) {
					result.readRecord(dis);
				}
				dis.close();
			} catch (IOException ex) {
				Logger.error(ex);
				throw new RecordStoreNotFoundException(ex.getMessage());
			}
			// Logger.debug("openRecordStore: " + recordStoreName + " (" + load.getParts().length + ")");
		} else {
			if (!createIfNecessary) {
				throw new RecordStoreNotFoundException(recordStoreName);
			}
			result = new RecordStoreImpl(this, recordStoreName);
			// Logger.debug("openRecordStore: " + recordStoreName + " (" + load + ")");
		}
		result.setOpen(true);
		if (recordListener != null) {
			result.addRecordListener(recordListener);
		}

		fireRecordStoreListener(ExtendedRecordListener.RECORDSTORE_OPEN, recordStoreName);

		return result;
	}
	
	public void deleteRecord(RecordStoreImpl recordStoreImpl, int recordId) throws RecordStoreNotOpenException, RecordStoreException {
		saveRecord(recordStoreImpl, recordId);
	}
	
	public void loadRecord(RecordStoreImpl recordStoreImpl, int recordId)
			throws RecordStoreNotOpenException, InvalidRecordIDException, RecordStoreException 
	{
		// records are loaded when record store opens
	}

	public void saveRecord(RecordStoreImpl recordStoreImpl, int recordId) throws RecordStoreException {
/*		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			recordStoreImpl.writeHeader(dos);
			RecordEnumeration re = recordStoreImpl.enumerateRecords(null, null, false);
			while (re.hasNextElement()) {
				recordStoreImpl.writeRecord(dos, re.nextRecordId());
			}
			CookieContent cookieContent = new CookieContent(Base64Coder.encode(baos.toByteArray()));

			CookieContent previousCookie = (CookieContent) cookies.get(recordStoreImpl.getName());
			if (previousCookie != null) {
				removeCookie(recordStoreImpl.getName(), previousCookie);
			}

			cookies.put(recordStoreImpl.getName(), cookieContent);

			String[] parts = cookieContent.getParts();
			if (parts.length == 1) {
				document.setMember("cookie", "x" + recordStoreImpl.getName() + "=a" + parts[0] + expires);
			} else {
				for (int i = 0; i < parts.length; i++) {
					document.setMember("cookie", i + recordStoreImpl.getName() + "=a" + parts[i] + expires);
				}
			}

			System.out.println("saveChanges: " + recordStoreImpl.getName() + " (" + cookieContent.getParts().length
					+ ")");
		} catch (IOException ex) {
			Logger.error(ex);
		}*/
	}

	public int getSizeAvailable(RecordStoreImpl recordStoreImpl) {
		int size = MAX_COOKIE_SIZE * MAX_SPLIT_COOKIES;

		size -= recordStoreImpl.getHeaderSize();
		try {
			RecordEnumeration en = recordStoreImpl.enumerateRecords(null, null, false);
			while (en.hasNextElement()) {
				size -= en.nextRecord().length + recordStoreImpl.getRecordHeaderSize();
			}
		} catch (RecordStoreException ex) {
			Logger.error(ex);
		}

		// TODO Auto-generated method stub
		// Logger.debug("getSizeAvailable: " + size);
		return size;
	}

	private void removeCookie(String recordStoreName, CookieContent cookieContent) {
/*		String[] parts = cookieContent.getParts();
		if (parts.length == 1) {
			document.setMember("cookie", "x" + recordStoreName + "=r");
		} else {
			for (int i = 0; i < parts.length; i++) {
				document.setMember("cookie", i + recordStoreName + "=r");
			}
		}
		System.out.println("removeCookie: " + recordStoreName);*/
	}

	private class CookieContent {
		private String[] parts;

		public CookieContent() {
		}

		public CookieContent(char[] buffer) {
			parts = new String[buffer.length / MAX_COOKIE_SIZE + 1];
			// Logger.debug("CookieContent(before): " + parts.length);
			int index = 0;
			for (int i = 0; i < parts.length; i++) {
				int size = MAX_COOKIE_SIZE;
				if (index + size > buffer.length) {
					size = buffer.length - index;
				}
				// Logger.debug("CookieContent: " + i + "," + index + "," + size);
				parts[i] = new String(buffer, index, size);
				index += size;
			}
		}

		public void setPart(int index, String content) {
			if (parts == null) {
				parts = new String[index + 1];
			} else {
				if (parts.length <= index) {
					String[] newParts = new String[index + 1];
					System.arraycopy(parts, 0, newParts, 0, parts.length);
					parts = newParts;
				}
			}
			// Logger.debug("setPart: " + index + "," + parts.length);

			parts[index] = content;
		}

		public String[] getParts() {
			// Logger.debug("getParts: " + parts);
			return parts;
		}

		public char[] toCharArray() {
			int size = 0;
			for (int i = 0; i < parts.length; i++) {
				size += parts[i].length();
			}

			char[] result = new char[size];

			int index = 0;
			for (int i = 0; i < parts.length; i++) {
				// Logger.debug("toCharArray: " + i + "," + index + "," + size + "," + parts[i].length());
				System.arraycopy(parts[i].toCharArray(), 0, result, index, parts[i].length());
				index += parts[i].length();
			}

			return result;
		}
	}

	public void setRecordListener(ExtendedRecordListener recordListener) {
		this.recordListener = recordListener;
	}

	public void fireRecordStoreListener(int type, String recordStoreName) {
		if (recordListener != null) {
			recordListener.recordStoreEvent(type, System.currentTimeMillis(), recordStoreName);
		}
	}
}
