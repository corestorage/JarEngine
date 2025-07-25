/**
 *  MicroEmulator
 *  Copyright (C) 2006-2007 Bartek Teodorczyk <barteo@barteo.net>
 *  Copyright (C) 2006-2007 Vlad Skarzhevskyy
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
package org.jarengine;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author vlads
 * 
 * See src/test/ssl/read-me.txt
 * 
 */
public class SSLContextSetup {
	
	private static boolean initialized = false;
	
	public static synchronized void setUp() {
    	if (initialized) {
    		return;
    	}
    	InputStream is = null;
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            is = SSLContextSetup.class.getResourceAsStream("/test-servers.keystore"); 
            if (is == null) {
            	new Error("keystore not found");
            }
            trustStore.load(is, "microemu2006".toCharArray());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");  
            trustManagerFactory.init(trustStore);  
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustManagers, secureRandom);
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
            initialized = true;
        } catch (Throwable e) {
            throw new Error(e);
        } finally {
        	if (is != null) {
        		try {
					is.close();
				} catch (IOException ignore) {
				}
        	}
        }
    }
}
