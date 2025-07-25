/**
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
 */

package org.jarengine.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;                                                            
import java.util.List;                                                                
import java.util.NoSuchElementException;
import java.util.TimerTask;

import javax.microedition.midlet.MIDletStateChangeException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.jarengine.DisplayAccess;
import org.jarengine.DisplayComponent;
import org.jarengine.MIDletAccess;
import org.jarengine.MIDletBridge;
import org.jarengine.app.capture.AnimatedGifEncoder;
import org.jarengine.app.classloader.MIDletClassLoader;
import org.jarengine.app.ui.DisplayRepaintListener;
import org.jarengine.app.ui.Message;
import org.jarengine.app.ui.ResponseInterfaceListener;
import org.jarengine.app.ui.swing.DropTransferHandler;
import org.jarengine.app.ui.swing.ExtensionFileFilter;
import org.jarengine.app.ui.swing.RecordStoreManagerDialog;
import org.jarengine.app.ui.swing.SwingAboutDialog;
import org.jarengine.app.ui.swing.SwingDeviceComponent;
import org.jarengine.app.ui.swing.SwingDialogWindow;
import org.jarengine.app.ui.swing.SwingDisplayComponent;
import org.jarengine.app.ui.swing.SwingErrorMessageDialogPanel;
import org.jarengine.app.util.AppletProducer;
import org.jarengine.app.util.DeviceEntry;
import org.jarengine.app.util.IOUtils;
import org.jarengine.device.Device;
import org.jarengine.device.DeviceDisplay;
import org.jarengine.device.DeviceFactory;
import org.jarengine.device.EmulatorContext;
import org.jarengine.device.FontManager;
import org.jarengine.device.InputMethod;
import org.jarengine.device.impl.DeviceDisplayImpl;
import org.jarengine.device.impl.DeviceImpl;
import org.jarengine.device.impl.Rectangle;
import org.jarengine.device.impl.SoftButton;
import org.jarengine.device.j2se.J2SEDevice;
import org.jarengine.device.j2se.J2SEDeviceDisplay;
import org.jarengine.device.j2se.J2SEFontManager;
import org.jarengine.device.j2se.J2SEGraphicsSurface;
import org.jarengine.device.j2se.J2SEInputMethod;
import org.jarengine.log.Logger;
import org.jarengine.log.QueueAppender;
import org.jarengine.util.JadMidletEntry;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;

// Remove FlatLaf import
// import com.formdev.flatlaf.FlatLightLaf;
import org.jarengine.app.ui.swing.SwingSelectDevicePanel;
import org.jarengine.app.ui.swing.RecordStoreChangePanel;
import org.jarengine.device.j2se.ui.J2SETextBoxUI;
import org.jarengine.device.j2se.ui.J2SETextFieldUI;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	protected Common common;

	// Only keep devicePanel and fields needed for minimal UI
	private SwingDeviceComponent devicePanel;

	// Add device selection panel field here
	private SwingSelectDevicePanel selectDevicePanel = null;

	private JFileChooser saveForWebChooser;

	private JFileChooser fileChooser = null;

	private JFileChooser captureFileChooser = null;

	private JFrame scaledDisplayFrame;

	private RecordStoreManagerDialog recordStoreManagerDialog;

	private QueueAppender logQueueAppender;

	private DeviceEntry deviceEntry;

	private AnimatedGifEncoder encoder;

	private File captureFile;

	protected EmulatorContext emulatorContext = new EmulatorContext() {

		private InputMethod inputMethod = new J2SEInputMethod();

		private DeviceDisplay deviceDisplay = new J2SEDeviceDisplay(this);

		private FontManager fontManager = new J2SEFontManager();

		public DisplayComponent getDisplayComponent() {
			return devicePanel.getDisplayComponent();
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

	private ActionListener menuOpenMIDletFileListener = new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
			if (fileChooser == null) {
				ExtensionFileFilter fileFilter = new ExtensionFileFilter("MIDlet files");
				fileFilter.addExtension("jad");
				fileFilter.addExtension("jar");
				fileChooser = new JFileChooser();
				fileChooser.setFileFilter(fileFilter);
				fileChooser.setDialogTitle("Open MIDlet File...");
				fileChooser.setCurrentDirectory(new File(Config.getRecentDirectory("recentJadDirectory")));
			}

			int returnVal = fileChooser.showOpenDialog(Main.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				Config.setRecentDirectory("recentJadDirectory", fileChooser.getCurrentDirectory().getAbsolutePath());
				String url = IOUtils.getCanonicalFileURL(fileChooser.getSelectedFile());
				Common.openMIDletUrlSafe(url);
				if (recordStoreManagerDialog != null) {
					recordStoreManagerDialog.refresh();
				}
			}
		}
	};

	private ActionListener menuCloseMidletListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			stopUpTimer();
			common.startLauncher(MIDletBridge.getMIDletContext());
		}
	};

	private ActionListener menuSaveForWebListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (saveForWebChooser == null) {
				ExtensionFileFilter fileFilter = new ExtensionFileFilter("HTML files");
				fileFilter.addExtension("html");
				saveForWebChooser = new JFileChooser();
				saveForWebChooser.setFileFilter(fileFilter);
				saveForWebChooser.setDialogTitle("Save for Web...");
				saveForWebChooser.setCurrentDirectory(new File(Config.getRecentDirectory("recentSaveForWebDirectory")));
			}
			if (saveForWebChooser.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
				Config.setRecentDirectory("recentSaveForWebDirectory", saveForWebChooser.getCurrentDirectory()
						.getAbsolutePath());
				File pathFile = saveForWebChooser.getSelectedFile().getParentFile();

				String name = saveForWebChooser.getSelectedFile().getName();
				if (!name.toLowerCase().endsWith(".html") && name.indexOf('.') == -1) {
					name = name + ".html";
				}

				// try to get from distribution home location
				String resource = MIDletClassLoader.getClassResourceName(this.getClass().getName());
				URL url = this.getClass().getClassLoader().getResource(resource);
				String path = url.getPath();
				int prefix = path.indexOf(':');
				String mainJarFileName = path.substring(prefix + 1, path.length() - resource.length());
				File appletJarDir = new File(new File(mainJarFileName).getParent(), "lib");
				File appletJarFile = new File(appletJarDir, "jarengine-javase-applet.jar");
				if (!appletJarFile.exists()) {
					appletJarFile = null;
				}

				if (appletJarFile == null) {
					// try to get from maven2 repository
					/*
					 * loc/org/jarengine/microemulator/2.0.1-SNAPSHOT/microemulator-2.0.1-20070227.080140-1.jar String
					 * version = doRegExpr(mainJarFileName, ); String basePath = "loc/org/jarengine/" appletJarFile = new
					 * File(basePath + "jarengine-javase-applet/" + version + "/jarengine-javase-applet" + version +
					 * ".jar"); if (!appletJarFile.exists()) { appletJarFile = null; }
					 */
				}

				if (appletJarFile == null) {
					ExtensionFileFilter fileFilter = new ExtensionFileFilter("JAR packages");
					fileFilter.addExtension("jar");
					JFileChooser appletChooser = new JFileChooser();
					appletChooser.setFileFilter(fileFilter);
					appletChooser.setDialogTitle("Select MicroEmulator applet jar package...");
					appletChooser.setCurrentDirectory(new File(Config.getRecentDirectory("recentAppletJarDirectory")));
					if (appletChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
						Config.setRecentDirectory("recentAppletJarDirectory", appletChooser.getCurrentDirectory()
								.getAbsolutePath());
						appletJarFile = appletChooser.getSelectedFile();
					} else {
						return;
					}
				}

				JadMidletEntry jadMidletEntry;
				Iterator it = common.jad.getMidletEntries().iterator();
				if (it.hasNext()) {
					jadMidletEntry = (JadMidletEntry) it.next();
				} else {
					Message.error("MIDlet Suite has no entries");
					return;
				}

				String midletInput = common.jad.getJarURL();
				DeviceEntry deviceInput = new DeviceEntry("Resizable device", null, org.jarengine.device.impl.DeviceImpl.RESIZABLE_LOCATION, true, false);
				if (deviceInput != null && deviceInput.getDescriptorLocation().equals(DeviceImpl.DEFAULT_LOCATION)) {
					deviceInput = null;
				}

				File htmlOutputFile = new File(pathFile, name);
				if (!allowOverride(htmlOutputFile)) {
					return;
				}
				File appletPackageOutputFile = new File(pathFile, "jarengine-javase-applet.jar");
				if (!allowOverride(appletPackageOutputFile)) {
					return;
				}
				File midletOutputFile = new File(pathFile, midletInput.substring(midletInput.lastIndexOf("/") + 1));
				if (!allowOverride(midletOutputFile)) {
					return;
				}
				File deviceOutputFile = null;
				if (deviceInput != null && deviceInput.getFileName() != null) {
					deviceOutputFile = new File(pathFile, deviceInput.getFileName());
					if (!allowOverride(deviceOutputFile)) {
						return;
					}
				}

				try {
					AppletProducer.createHtml(htmlOutputFile, (DeviceImpl) DeviceFactory.getDevice(), jadMidletEntry
							.getClassName(), midletOutputFile, appletPackageOutputFile, deviceOutputFile);
					AppletProducer.createMidlet(new URL(midletInput), midletOutputFile);
					IOUtils.copyFile(appletJarFile, appletPackageOutputFile);
					if (deviceInput != null && deviceInput.getFileName() != null) {
						IOUtils.copyFile(new File(Config.getConfigPath(), deviceInput.getFileName()), deviceOutputFile);
					}
				} catch (IOException ex) {
					Logger.error(ex);
				}
			}
		}

		private boolean allowOverride(File file) {
			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(Main.this, "Override the file:" + file + "?", "Question?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == 1 /* no */) {
					return false;
				}
			}

			return true;
		}
	};

	private ActionListener menuStartCaptureListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			File picturesDir = getDefaultPicturesDirectory();
			String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
			Main.this.captureFile = new File(picturesDir, "Recording_" + timestamp + ".gif");
			encoder = new AnimatedGifEncoder();
			encoder.start(Main.this.captureFile.getAbsolutePath());
			// Remove menu field references - these will be handled by toolbar buttons
			((SwingDisplayComponent) emulatorContext.getDisplayComponent())
				.addDisplayRepaintListener(new DisplayRepaintListener() {
					long start = 0;
					public void repaintInvoked(Object repaintObject) {
						synchronized (Main.this) {
							if (encoder != null) {
								if (start == 0) {
									start = System.currentTimeMillis();
								} else {
									long current = System.currentTimeMillis();
									encoder.setDelay((int) (current - start));
									start = current;
								}
								encoder.addFrame(((J2SEGraphicsSurface) repaintObject).getImage());
							}
						}
					}
				});
			javax.swing.JOptionPane.showMessageDialog(Main.this, "Recording started", "Recording Started", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}

		private boolean allowOverride(File file) {
			if (file.exists()) {
				int answer = JOptionPane.showConfirmDialog(Main.this, "Override the file:" + file + "?", "Question?",
						JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer == 1 /* no */) {
					return false;
				}
			}

			return true;
		}
	};

	private ActionListener menuStopCaptureListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// Remove menu field references - these will be handled by toolbar buttons

			synchronized (Main.this) {
				if (encoder != null) {
					encoder.finish();
					javax.swing.JOptionPane.showMessageDialog(Main.this, "Recording saved to: " + (Main.this.captureFile != null ? Main.this.captureFile.getAbsolutePath() : "(unknown location)"), "Recording Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
					encoder = null;
				}
			}

			// Remove menu field references - these will be handled by toolbar buttons
		}
	};

	private ActionListener menuMIDletNetworkConnectionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			// This will be handled by a toolbar button or settings dialog
			org.jarengine.cldc.http.Connection.setAllowNetworkConnection(true);
		}

	};

	private ActionListener menuRecordStoreManagerListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (recordStoreManagerDialog == null) {
				recordStoreManagerDialog = new RecordStoreManagerDialog(Main.this, common);
				recordStoreManagerDialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						// Remove menu field reference - this will be handled by toolbar button
					}
				});
				recordStoreManagerDialog.pack();
				Rectangle window = Config.getWindow("recordStoreManager", new Rectangle(0, 0, 640, 320));
				recordStoreManagerDialog.setBounds(window.x, window.y, window.width, window.height);
			}
			recordStoreManagerDialog.setVisible(!recordStoreManagerDialog.isVisible());
		}
	};

	private ActionListener menuAboutListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			SwingDialogWindow.show(Main.this, "About", new SwingAboutDialog(), false);
		}
	};

	private ActionListener menuExitListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			stopUpTimer();
			synchronized (Main.this) {
				if (encoder != null) {
					encoder.finish();
					encoder = null;
				}
			}

			if (recordStoreManagerDialog != null) {
				Config.setWindow("recordStoreManager", new Rectangle(recordStoreManagerDialog.getX(),
						recordStoreManagerDialog.getY(), recordStoreManagerDialog.getWidth(), recordStoreManagerDialog
								.getHeight()), recordStoreManagerDialog.isVisible());
			}
			if (scaledDisplayFrame != null) {
				Config.setWindow("scaledDisplay", new Rectangle(scaledDisplayFrame.getX(), scaledDisplayFrame.getY(),
						0, 0), false);
			}
			Config.setWindow("main", new Rectangle(Main.this.getX(), Main.this.getY(), Main.this.getWidth(), Main.this
					.getHeight()), true);

			System.exit(0);
		}
	};

	private ActionListener menuScaledDisplayListener = new ActionListener() {
		private DisplayRepaintListener updateScaledImageListener;

		public void actionPerformed(ActionEvent e) {
			// Remove zoomLevels references - this functionality can be added to toolbar later
			// For now, just show a message that this feature is not yet implemented in the new UI
			JOptionPane.showMessageDialog(Main.this, "Scaled display feature will be available in the toolbar soon.", "Feature Not Available", JOptionPane.INFORMATION_MESSAGE);
		}
	};

	private ResponseInterfaceListener responseInterfaceListener = new ResponseInterfaceListener() {
		public void stateChanged(boolean state) {
			// Remove menu field references - these will be handled by toolbar buttons
			// Update window title when MIDlet is loaded/unloaded
			if (state) {
				// MIDlet is loaded, update title with MIDlet name
				try {
					String midletName = common.getAppProperty("MIDlet-Name");
					if (midletName != null && !midletName.trim().isEmpty()) {
						updateTitle(midletName);
					} else {
						updateTitle(null);
					}
				} catch (Exception e) {
					// Fallback to default title if there's an error
					updateTitle(null);
				}
			} else {
				// No MIDlet loaded, show default title
				updateTitle(null);
			}
			// Restore natural focus logic: just update the window title and focus devicePanel
			if (state) {
				try {
					String midletName = common.getAppProperty("MIDlet-Name");
					if (midletName != null && !midletName.trim().isEmpty()) {
						updateTitle(midletName);
					} else {
						updateTitle(null);
					}
				} catch (Exception e) {
					updateTitle(null);
				}
			} else {
				updateTitle(null);
			}
			if (devicePanel != null && devicePanel.getDisplayComponent() instanceof org.jarengine.app.ui.swing.SwingDisplayComponent) {
				((org.jarengine.app.ui.swing.SwingDisplayComponent) devicePanel.getDisplayComponent()).ensureDevicePanelFocus();
			}
		}
	};

	private WindowAdapter windowListener = new WindowAdapter() {
		public void windowClosing(WindowEvent ev) {
			menuExitListener.actionPerformed(null);
		}

		public void windowIconified(WindowEvent ev) {
			MIDletBridge.getMIDletAccess(MIDletBridge.getCurrentMIDlet()).pauseApp();
		}

		public void windowDeiconified(WindowEvent ev) {
			try {
				MIDletBridge.getMIDletAccess(MIDletBridge.getCurrentMIDlet()).startApp();
			} catch (MIDletStateChangeException ex) {
				Logger.error("Error destroying MIDlet", ex);
			}
		}
	};

	public Main() {
        this(null);
	}

	public Main(DeviceEntry defaultDevice) {
        this.logQueueAppender = new QueueAppender(1024);
        Logger.addAppender(logQueueAppender);

        // Set window properties
        setTitle("JarEngine");
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/org/jarengine/icon.png")));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setLocationRelativeTo(null);
        addWindowListener(windowListener);
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            public void windowGainedFocus(java.awt.event.WindowEvent e) {
                if (devicePanel != null) {
                    devicePanel.requestFocusInWindow();
                }
            }
            public void windowActivated(java.awt.event.WindowEvent e) {
                if (devicePanel != null) {
                    devicePanel.requestFocusInWindow();
                }
            }
        });

        // Device selection panel
        selectDevicePanel = new SwingSelectDevicePanel(emulatorContext);

        Config.loadConfig(defaultDevice, emulatorContext);
        Logger.setLocationEnabled(Config.isLogConsoleLocationEnabled());
        Rectangle window = Config.getWindow("main", new Rectangle(0, 0, 300, 300));
        this.setLocation(window.x, window.y);
        this.setSize(300, 300);

        // Minimal layout with LXDE-style bottom panel
        Component deviceComponent = createContents(null);
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.add(deviceComponent, BorderLayout.CENTER);

        // LXDE-style bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setBackground(new java.awt.Color(230, 230, 230));
        bottomPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(180, 180, 180)));

        JButton menuButton = new JButton("☰ Menu");
        menuButton.setFocusPainted(false);
        menuButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 16, 4, 16));
        menuButton.setAlignmentY(JButton.CENTER_ALIGNMENT);
        bottomPanel.add(menuButton);
        JButton runButton = new JButton("Run");
        runButton.setFocusPainted(false);
        runButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 16, 4, 16));
        runButton.setAlignmentY(JButton.CENTER_ALIGNMENT);
        bottomPanel.add(runButton);

        // Add horizontal glue to push future items to the right
        bottomPanel.add(Box.createHorizontalGlue());

        // --- Add JPopupMenu for Run button ---
        JPopupMenu runMenu = new JPopupMenu();
        ButtonGroup runGroup = new ButtonGroup();
        JRadioButtonMenuItem openMidletItem = new JRadioButtonMenuItem("Open MIDlet File");
        openMidletItem.addActionListener(menuOpenMIDletFileListener);
        runGroup.add(openMidletItem);
        runMenu.add(openMidletItem);
        JRadioButtonMenuItem closeMidletItem = new JRadioButtonMenuItem("Close MIDlet");
        closeMidletItem.addActionListener(menuCloseMidletListener);
        runGroup.add(closeMidletItem);
        runMenu.add(closeMidletItem);
        JRadioButtonMenuItem saveForWebItem = new JRadioButtonMenuItem("Save for Web");
        saveForWebItem.addActionListener(menuSaveForWebListener);
        runGroup.add(saveForWebItem);
        runMenu.add(saveForWebItem);
        JRadioButtonMenuItem startCaptureItem = new JRadioButtonMenuItem("Start Capture");
        startCaptureItem.addActionListener(menuStartCaptureListener);
        runGroup.add(startCaptureItem);
        runMenu.add(startCaptureItem);
        runButton.addActionListener(e -> {
            runMenu.show(runButton, 0, -runMenu.getPreferredSize().height);
            runMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                    if (devicePanel != null) devicePanel.requestFocusInWindow();
                }
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                    if (devicePanel != null) devicePanel.requestFocusInWindow();
                }
            });
        });

        // --- Add JPopupMenu for Menu button ---
        JPopupMenu menuMenu = new JPopupMenu();
        ButtonGroup menuGroup = new ButtonGroup();
        JRadioButtonMenuItem stopCaptureItem = new JRadioButtonMenuItem("Stop Capture");
        stopCaptureItem.addActionListener(menuStopCaptureListener);
        menuGroup.add(stopCaptureItem);
        menuMenu.add(stopCaptureItem);
        JRadioButtonMenuItem midletNetworkItem = new JRadioButtonMenuItem("MIDlet Network Connection");
        midletNetworkItem.addActionListener(menuMIDletNetworkConnectionListener);
        menuGroup.add(midletNetworkItem);
        menuMenu.add(midletNetworkItem);
        JRadioButtonMenuItem recordStoreManagerItem = new JRadioButtonMenuItem("Record Store Manager");
        recordStoreManagerItem.addActionListener(menuRecordStoreManagerListener);
        menuGroup.add(recordStoreManagerItem);
        menuMenu.add(recordStoreManagerItem);
        JRadioButtonMenuItem aboutItem = new JRadioButtonMenuItem("About");
        aboutItem.addActionListener(menuAboutListener);
        menuGroup.add(aboutItem);
        menuMenu.add(aboutItem);
        JRadioButtonMenuItem exitItem = new JRadioButtonMenuItem("Exit");
        exitItem.addActionListener(menuExitListener);
        menuGroup.add(exitItem);
        menuMenu.add(exitItem);
        JRadioButtonMenuItem scaledDisplayItem = new JRadioButtonMenuItem("Scaled Display");
        scaledDisplayItem.addActionListener(menuScaledDisplayListener);
        menuGroup.add(scaledDisplayItem);
        menuMenu.add(scaledDisplayItem);
        JRadioButtonMenuItem proxySettingsItem = new JRadioButtonMenuItem("Proxy Settings");
        proxySettingsItem.addActionListener(e -> {
            ProxySettingsDialog dialog = new ProxySettingsDialog(Main.this);
            dialog.setVisible(true);
        });
        menuGroup.add(proxySettingsItem);
        menuMenu.add(proxySettingsItem);
        JRadioButtonMenuItem deviceSelectionItem = new JRadioButtonMenuItem("Device Selection");
        deviceSelectionItem.addActionListener(e -> {
            selectDevicePanel.setVisible(true);
        });
        menuGroup.add(deviceSelectionItem);
        menuMenu.add(deviceSelectionItem);
        JRadioButtonMenuItem logConsoleItem = new JRadioButtonMenuItem("Log Console");
        logConsoleItem.addActionListener(e -> {
            org.jarengine.app.ui.swing.SwingLogConsoleDialog logDialog = new org.jarengine.app.ui.swing.SwingLogConsoleDialog((java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(Main.this), logQueueAppender);
            logDialog.setVisible(true);
        });
        menuGroup.add(logConsoleItem);
        menuMenu.add(logConsoleItem);
        JRadioButtonMenuItem changeRecordStoreTypeItem = new JRadioButtonMenuItem("Change Record Store Type");
        changeRecordStoreTypeItem.addActionListener(e -> {
            if (recordStoreManagerDialog == null) {
                recordStoreManagerDialog = new RecordStoreManagerDialog(Main.this, common);
            }
            RecordStoreChangePanel panel = new RecordStoreChangePanel(common);
            org.jarengine.app.ui.swing.SwingDialogWindow.show(Main.this, "Change Record Store...", panel, true);
        });
        menuGroup.add(changeRecordStoreTypeItem);
        menuMenu.add(changeRecordStoreTypeItem);
        menuButton.addActionListener(e -> {
            menuMenu.show(menuButton, 0, -menuMenu.getPreferredSize().height);
            menuMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
                public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
                public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                    if (devicePanel != null) devicePanel.requestFocusInWindow();
                }
                public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
                    if (devicePanel != null) devicePanel.requestFocusInWindow();
                }
            });
        });

        mainContainer.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(mainContainer);

        Message.addListener(new SwingErrorMessageDialogPanel(this));
        devicePanel.setTransferHandler(new DropTransferHandler());

        this.common = new Common(emulatorContext);
        // Ensure focus on devicePanel when window is shown
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (devicePanel != null) devicePanel.requestFocusInWindow();
            }
        });
	}

	// Method to update title when MIDlet is loaded
	public void updateTitle(String midletName) {
		if (midletName != null && !midletName.trim().isEmpty()) {
			setTitle(midletName);
		} else {
			setTitle("JarEngine");
		}
	}

	protected Component createContents(Container parent) {
		devicePanel = new SwingDeviceComponent();
		devicePanel.addKeyListener(devicePanel);
		addKeyListener(devicePanel);

		return devicePanel;
	}

	public boolean setDevice(DeviceEntry entry) {
		if (DeviceFactory.getDevice() != null) {
			// ((J2SEDevice) DeviceFactory.getDevice()).dispose();
		}
		final String errorTitle = "Error creating device";
		try {
			ClassLoader classLoader = Main.class.getClassLoader();
			if (entry.getFileName() != null) {
				URL[] urls = new URL[1];
				urls[0] = new File(Config.getConfigPath(), entry.getFileName()).toURI().toURL();
				classLoader = Common.createExtensionsClassLoader(urls);
			}

			// TODO font manager have to be moved from emulatorContext into device
			emulatorContext.getDeviceFontManager().init();

			Device device = DeviceImpl.create(emulatorContext, classLoader, entry.getDescriptorLocation(), J2SEDevice.class);
			System.out.println("[DEBUG] Buttons: " + device.getButtons().size());
			this.deviceEntry = entry;

			DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) device.getDeviceDisplay();
			if (deviceDisplay.isResizable()) {
				Rectangle size = Config.getDeviceEntryDisplaySize(entry);
				if (size != null) {
				    setDeviceSize(deviceDisplay, size.width, size.height);
				}
			}
			common.setDevice(device);
			updateDevice();
			return true;
		} catch (MalformedURLException e) {
			Message.error(errorTitle, errorTitle + ", " + Message.getCauseMessage(e), e);
		} catch (IOException e) {
			Message.error(errorTitle, errorTitle + ", " + Message.getCauseMessage(e), e);
		} catch (Throwable e) {
			Message.error(errorTitle, errorTitle + ", " + Message.getCauseMessage(e), e);
		}
		return false;
	}
	
	protected void setDeviceSize(DeviceDisplayImpl deviceDisplay, int width, int height) {
        if (devicePanel == null || deviceDisplay == null) {
            return;
        }
        // move the soft buttons
        int menuh = 0;
        Enumeration en = DeviceFactory.getDevice().getSoftButtons().elements();
        while (en.hasMoreElements()) {
            SoftButton button = (SoftButton) en.nextElement();
            Rectangle paintable = button.getPaintable();
            paintable.y = height - paintable.height;
            menuh = paintable.height;
        }
        // resize the display area
        deviceDisplay.setDisplayPaintable(new Rectangle(0, 0, width, height - menuh));
        deviceDisplay.setDisplayRectangle(new Rectangle(0, 0, width, height));
        ((SwingDisplayComponent) devicePanel.getDisplayComponent()).init();
        // update display
        MIDletAccess ma = MIDletBridge.getMIDletAccess();
        if (ma == null) {
            return;
        }
        DisplayAccess da = ma.getDisplayAccess();
        if (da != null) {
            da.sizeChanged();
            deviceDisplay.repaint(0, 0, deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight());
        }
        // After resizing, pack the window and focus device panel
        pack();
        devicePanel.requestFocus();
	}

	protected void updateDevice() {
		devicePanel.init();
		if (((DeviceDisplayImpl) DeviceFactory.getDevice().getDeviceDisplay()).isResizable()) {
			setResizable(true);
		} else {
			setResizable(false);
		}

		pack();

		devicePanel.requestFocus();
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(() -> {
			List params = new ArrayList();
			StringBuffer debugArgs = new StringBuffer();
			for (int i = 0; i < args.length; i++) {
				params.add(args[i]);
				if (debugArgs.length() != 0) {
					debugArgs.append(", ");
				}
				debugArgs.append("[").append(args[i]).append("]");
			}
			if (params.contains("--headless")) {
				Headless.main(args);
				return;
			}

			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ex) {
				Logger.error(ex);
			}

			final Main app = new Main();
			if (args.length > 0) {
				Logger.debug("arguments", debugArgs.toString());
			}

			// Use device entry from selectDevicePanel for consistency
			if (app.common.initParams(params, app.selectDevicePanel.getSelectedDeviceEntry(), J2SEDevice.class)) {
				app.deviceEntry = app.selectDevicePanel.getSelectedDeviceEntry();
				DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) DeviceFactory.getDevice().getDeviceDisplay();
				if (deviceDisplay.isResizable()) {
					Rectangle size = Config.getDeviceEntryDisplaySize(app.deviceEntry);
					if (size != null) {
						app.setDeviceSize(deviceDisplay, size.width, size.height);
					}
				}
			}
			app.updateDevice();
			app.validate();
			app.setVisible(true);
			app.devicePanel.requestFocus();

			if (Config.isWindowOnStart("recordStoreManager")) {
				app.menuRecordStoreManagerListener.actionPerformed(null);
			}

			String midletString;
			try {
				midletString = (String) params.iterator().next();
			} catch (NoSuchElementException ex) {
				midletString = null;
			}
			app.common.initMIDlet(true);

			app.responseInterfaceListener.stateChanged(true);
		});
	}

	private abstract class CountTimerTask extends TimerTask {

		protected int counter;

		public CountTimerTask(int counter) {
			this.counter = counter;
		}

	}

    // All timer/status/component remnants removed for minimal clean app
    public void stopUpTimer() {}

    private File getDefaultPicturesDirectory() {
        String userHome = System.getProperty("user.home");
        String os = System.getProperty("os.name").toLowerCase();
        File picturesDir = null;
        // Try XDG for Linux
        if (os.contains("linux")) {
            String xdgPictures = System.getenv("XDG_PICTURES_DIR");
            if (xdgPictures != null && !xdgPictures.isEmpty()) {
                picturesDir = new File(xdgPictures.replace("$HOME", userHome));
            } else {
                picturesDir = new File(userHome, "Pictures");
            }
        } else if (os.contains("win")) {
            picturesDir = new File(userHome, "Pictures");
        } else if (os.contains("mac")) {
            picturesDir = new File(userHome, "Pictures");
        } else {
            picturesDir = new File(userHome, "Pictures");
        }
        if (!picturesDir.exists()) {
            picturesDir = new File(userHome);
        }
        return picturesDir;
    }

    // All timer/status/component remnants removed for minimal clean app
    public void notifyDestroyedCallback() {}
}

// Advanced Proxy Settings Dialog
class ProxySettingsDialog extends JDialog {
    private JComboBox<String> typeCombo;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passField;
    private JCheckBox enableBox;
    private JLabel statusLabel;

    public ProxySettingsDialog(JFrame parent) {
        super(parent, "Proxy Settings", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        typeCombo = new JComboBox<>(new String[] {"HTTP", "SOCKS"});
        hostField = new JTextField(20);
        portField = new JTextField(6);
        userField = new JTextField(15);
        passField = new JPasswordField(15);
        enableBox = new JCheckBox("Enable Proxy");
        statusLabel = new JLabel();

        gbc.gridx = 0; gbc.gridy = 0; add(new JLabel("Proxy Type:"), gbc);
        gbc.gridx = 1; add(typeCombo, gbc);
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Host:"), gbc);
        gbc.gridx = 1; add(hostField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Port:"), gbc);
        gbc.gridx = 1; add(portField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; add(userField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; add(passField, gbc);
        gbc.gridx = 0; gbc.gridy++;
        add(enableBox, gbc);
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2; add(statusLabel, gbc);

        JPanel buttonPanel = new JPanel();
        JButton applyBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2;
        add(buttonPanel, gbc);

        // Load current proxy settings
        loadCurrentSettings();

        applyBtn.addActionListener(e -> {
            if (enableBox.isSelected()) {
                applyProxySettings();
            } else {
                clearProxySettings();
            }
            loadCurrentSettings();
            setVisible(false);
        });
        cancelBtn.addActionListener(e -> setVisible(false));

        setResizable(false);
        pack();
        setLocationRelativeTo(parent);
    }

    private void loadCurrentSettings() {
        String type = System.getProperty("socksProxyHost") != null ? "SOCKS" : "HTTP";
        typeCombo.setSelectedItem(type);
        if (type.equals("SOCKS")) {
            hostField.setText(System.getProperty("socksProxyHost", ""));
            portField.setText(System.getProperty("socksProxyPort", ""));
            userField.setText(System.getProperty("java.net.socks.username", ""));
            passField.setText(System.getProperty("java.net.socks.password", ""));
            enableBox.setSelected(System.getProperty("socksProxyHost") != null);
        } else {
            hostField.setText(System.getProperty("http.proxyHost", ""));
            portField.setText(System.getProperty("http.proxyPort", ""));
            userField.setText(System.getProperty("http.proxyUser", ""));
            passField.setText(System.getProperty("http.proxyPassword", ""));
            enableBox.setSelected(System.getProperty("http.proxyHost") != null);
        }
        updateStatus();
    }

    private void updateStatus() {
        boolean enabled = enableBox.isSelected();
        String type = (String) typeCombo.getSelectedItem();
        String host = hostField.getText();
        String port = portField.getText();
        statusLabel.setText("Proxy " + (enabled ? "enabled" : "disabled") + (enabled ? (" (" + type + ": " + host + ":" + port + ")") : ""));
    }

    private void applyProxySettings() {
        String type = (String) typeCombo.getSelectedItem();
        String host = hostField.getText();
        String port = portField.getText();
        String user = userField.getText();
        String pass = new String(passField.getPassword());
        if (type.equals("SOCKS")) {
            System.setProperty("socksProxyHost", host);
            System.setProperty("socksProxyPort", port);
            if (!user.isEmpty()) System.setProperty("java.net.socks.username", user);
            else System.clearProperty("java.net.socks.username");
            if (!pass.isEmpty()) System.setProperty("java.net.socks.password", pass);
            else System.clearProperty("java.net.socks.password");
            // Clear HTTP proxy
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("http.proxyUser");
            System.clearProperty("http.proxyPassword");
            System.clearProperty("https.proxyHost");
            System.clearProperty("https.proxyPort");
        } else {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port);
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port);
            if (!user.isEmpty()) System.setProperty("http.proxyUser", user);
            else System.clearProperty("http.proxyUser");
            if (!pass.isEmpty()) System.setProperty("http.proxyPassword", pass);
            else System.clearProperty("http.proxyPassword");
            // Clear SOCKS proxy
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
            System.clearProperty("java.net.socks.username");
            System.clearProperty("java.net.socks.password");
        }
    }

    private void clearProxySettings() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        System.clearProperty("socksProxyHost");
        System.clearProperty("socksProxyPort");
        System.clearProperty("java.net.socks.username");
        System.clearProperty("java.net.socks.password");
    }
}
