/*
 *  MicroEmulator
 *  Copyright (C) 2006 Bartek Teodorczyk <barteo@barteo.net>
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


package org.jarengine.cldc.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import javax.microedition.io.StreamConnection;

public class ServerSocketConnection implements
		javax.microedition.io.ServerSocketConnection {
	
	private ServerSocket serverSocket;
	
	public ServerSocketConnection() throws IOException {
		serverSocket = new ServerSocket();
	}

	public ServerSocketConnection(int port) throws IOException {
		serverSocket = new ServerSocket(port);
	}

	public String getLocalAddress() throws IOException {
		InetAddress localHost = InetAddress.getLocalHost();
		return localHost.getHostAddress();
	}

	public int getLocalPort() throws IOException {
		return serverSocket.getLocalPort();
	}

	public StreamConnection acceptAndOpen() throws IOException {
		return new SocketConnection(serverSocket.accept());
	}

	public void close() throws IOException {
		serverSocket.close();
	}

}
