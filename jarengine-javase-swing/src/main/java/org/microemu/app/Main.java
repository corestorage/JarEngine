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

package org.microemu.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;                                                            
import java.util.List;                                                                
import java.util.NoSuchElementException;                                              
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.midlet.MIDletStateChangeException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
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
import java.awt.event.ItemEvent;

import org.microemu.DisplayAccess;
import org.microemu.DisplayComponent;
import org.microemu.MIDletAccess;
import org.microemu.MIDletBridge;
import org.microemu.app.capture.AnimatedGifEncoder;
import org.microemu.app.classloader.MIDletClassLoader;
import org.microemu.app.ui.DisplayRepaintListener;
import org.microemu.app.ui.Message;
import org.microemu.app.ui.ResponseInterfaceListener;
import org.microemu.app.ui.StatusBarListener;
import org.microemu.app.ui.swing.DropTransferHandler;
import org.microemu.app.ui.swing.ExtensionFileFilter;
import org.microemu.app.ui.swing.MIDletUrlPanel;
import org.microemu.app.ui.swing.RecordStoreManagerDialog;
import org.microemu.app.ui.swing.ResizeDeviceDisplayDialog;
import org.microemu.app.ui.swing.SwingAboutDialog;
import org.microemu.app.ui.swing.SwingDeviceComponent;
import org.microemu.app.ui.swing.SwingDialogWindow;
import org.microemu.app.ui.swing.SwingDisplayComponent;
import org.microemu.app.ui.swing.SwingErrorMessageDialogPanel;
import org.microemu.app.ui.swing.SwingLogConsoleDialog;
import org.microemu.app.ui.swing.SwingSelectDevicePanel;
import org.microemu.app.util.AppletProducer;
import org.microemu.app.util.DeviceEntry;
import org.microemu.app.util.IOUtils;
import org.microemu.app.util.MidletURLReference;
import org.microemu.app.util.MIDletThread;
import org.microemu.device.Device;
import org.microemu.device.DeviceDisplay;
import org.microemu.device.DeviceFactory;
import org.microemu.device.EmulatorContext;
import org.microemu.device.FontManager;
import org.microemu.device.InputMethod;
import org.microemu.device.impl.DeviceDisplayImpl;                                    
import org.microemu.device.impl.DeviceImpl;                                           
import org.microemu.device.impl.Rectangle; 
import org.microemu.device.impl.SoftButton;
import org.microemu.device.j2se.J2SEDevice;
import org.microemu.device.j2se.J2SEDeviceDisplay;
import org.microemu.device.j2se.J2SEFontManager;
import org.microemu.device.j2se.J2SEGraphicsSurface;
import org.microemu.device.j2se.J2SEInputMethod;
import org.microemu.log.Logger;
import org.microemu.log.QueueAppender;
import org.microemu.util.JadMidletEntry;
import org.microemu.app.update.UpdateChecker;
import javax.swing.SwingWorker;
import javax.swing.JToolBar;
import javax.swing.JToggleButton;
import javax.swing.JPopupMenu;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.event.MouseAdapter;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	protected Common common;

	private MIDletUrlPanel midletUrlPanel = null;

	private JFileChooser saveForWebChooser;

	private JFileChooser fileChooser = null;

	private JFileChooser captureFileChooser = null;

	private JFrame scaledDisplayFrame;

	private SwingDeviceComponent devicePanel;

	private SwingLogConsoleDialog logConsoleDialog;

	private RecordStoreManagerDialog recordStoreManagerDialog;

	private QueueAppender logQueueAppender;

	private DeviceEntry deviceEntry;

	private AnimatedGifEncoder encoder;

	private JLabel statusBar = new JLabel("Status");

	private ResizeDeviceDisplayDialog resizeDeviceDisplayDialog = null;

	private JLabel upTimerLabel = new JLabel("00:00:00");
	private long upTimerStartMillis = -1;
	private javax.swing.Timer upTimer;

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

	private ActionListener menuOpenMIDletURLListener = new ActionListener() {
		public void actionPerformed(ActionEvent ev) {
			if (midletUrlPanel == null) {
				midletUrlPanel = new MIDletUrlPanel();
			}
			if (SwingDialogWindow.show(Main.this, "Enter MIDlet URL:", midletUrlPanel, true)) {
				Common.openMIDletUrlSafe(midletUrlPanel.getText());
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
				File appletJarFile = new File(appletJarDir, "microemu-javase-applet.jar");
				if (!appletJarFile.exists()) {
					appletJarFile = null;
				}

				if (appletJarFile == null) {
					// try to get from maven2 repository
					/*
					 * loc/org/microemu/microemulator/2.0.1-SNAPSHOT/microemulator-2.0.1-20070227.080140-1.jar String
					 * version = doRegExpr(mainJarFileName, ); String basePath = "loc/org/microemu/" appletJarFile = new
					 * File(basePath + "microemu-javase-applet/" + version + "/microemu-javase-applet" + version +
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
				DeviceEntry deviceInput = new DeviceEntry("Resizable device", null, org.microemu.device.impl.DeviceImpl.RESIZABLE_LOCATION, true, false);
				if (deviceInput != null && deviceInput.getDescriptorLocation().equals(DeviceImpl.DEFAULT_LOCATION)) {
					deviceInput = null;
				}

				File htmlOutputFile = new File(pathFile, name);
				if (!allowOverride(htmlOutputFile)) {
					return;
				}
				File appletPackageOutputFile = new File(pathFile, "microemu-javase-applet.jar");
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
			org.microemu.cldc.http.Connection.setAllowNetworkConnection(true);
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

	private ActionListener menuLogConsoleListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (logConsoleDialog == null) {
				logConsoleDialog = new SwingLogConsoleDialog(Main.this, Main.this.logQueueAppender);
				logConsoleDialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						// Remove menu field reference - this will be handled by toolbar button
					}
				});
				logConsoleDialog.pack();
				// To avoid NPE on MacOS setFocusableWindowState(false) have to be called after pack()
				logConsoleDialog.setFocusableWindowState(false);
				Rectangle window = Config.getWindow("logConsole", new Rectangle(0, 0, 640, 320));
				logConsoleDialog.setBounds(window.x, window.y, window.width, window.height);
			}
			logConsoleDialog.setVisible(!logConsoleDialog.isVisible());
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

			if (logConsoleDialog != null) {
				Config.setWindow("logConsole", new Rectangle(logConsoleDialog.getX(), logConsoleDialog.getY(),
						logConsoleDialog.getWidth(), logConsoleDialog.getHeight()), logConsoleDialog.isVisible());
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

	private StatusBarListener statusBarListener = new StatusBarListener() {
		public void statusBarChanged(String text) {
			FontMetrics metrics = statusBar.getFontMetrics(statusBar.getFont());
			statusBar.setPreferredSize(new Dimension(metrics.stringWidth(text), metrics.getHeight()));
			statusBar.setText(text);
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
		}
	};

	private ComponentListener componentListener = new ComponentAdapter() {
		Timer timer;

		int count = 0;

		public void componentResized(ComponentEvent e) {
			count++;
			DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) DeviceFactory.getDevice().getDeviceDisplay();
			if (deviceDisplay.isResizable()) {
			    setDeviceSize(deviceDisplay, devicePanel.getWidth(), devicePanel.getHeight());
				devicePanel.revalidate();
				statusBarListener.statusBarChanged("New size: " + deviceDisplay.getFullWidth() + "x"
						+ deviceDisplay.getFullHeight());
				synchronized (statusBarListener) {
					if (timer == null) {
						timer = new Timer();
					}
					timer.schedule(new CountTimerTask(count) {
						public void run() {
							if (counter == count) {
								Config.setDeviceEntryDisplaySize(deviceEntry, new Rectangle(0, 0, devicePanel
										.getWidth(), devicePanel.getHeight()));
								statusBarListener.statusBarChanged("");
								timer.cancel();
								timer = null;
							}
						}
					}, 2000);
				}
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
		this.logQueueAppender = new QueueAppender(1024);
		Logger.addAppender(logQueueAppender);

		// Set window properties
		setTitle("JarEngine");
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/org/microemu/icon.png")));
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
        });

		Config.loadConfig(new DeviceEntry("Resizable device", null, org.microemu.device.impl.DeviceImpl.RESIZABLE_LOCATION, true, false), emulatorContext);
		Logger.setLocationEnabled(Config.isLogConsoleLocationEnabled());
		Rectangle window = Config.getWindow("main", new Rectangle(0, 0, 160, 120));
		this.setLocation(window.x, window.y);

		// --- LXDE-style Bottom Panel with Collapsible In-Panel Menu ---
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
		toolbar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JButton btnMenu = new JButton("☰ Menu");
		btnMenu.setToolTipText("Open main menu");

		// Main container with vertical layout
		JPanel mainContainer = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));

		// Emulator/game area (create first so devicePanel is available)
		Component deviceComponent = createContents(mainContainer);

		// Collapsible menu panel (hidden by default) - create after devicePanel
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
		menuPanel.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(1, 0, 1, 0, toolbar.getBackground().darker()),
			BorderFactory.createEmptyBorder(12, 16, 12, 16)));
		menuPanel.setBackground(toolbar.getBackground().brighter());
		menuPanel.setVisible(false);

		// Preferences section
		JLabel prefLabel = new JLabel("Preferences");
		prefLabel.setFont(prefLabel.getFont().deriveFont(Font.BOLD));
		menuPanel.add(prefLabel);
		menuPanel.add(Box.createVerticalStrut(8));
		// Theme
		JPanel themePanel = new JPanel();
		themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.X_AXIS));
		themePanel.setOpaque(false);
		themePanel.add(new JLabel("Theme: "));
		ButtonGroup themeGroup = new ButtonGroup();
		JRadioButtonMenuItem themeLight = new JRadioButtonMenuItem("Light");
		JRadioButtonMenuItem themeDark = new JRadioButtonMenuItem("Dark");
		themeGroup.add(themeLight);
		themeGroup.add(themeDark);
		themeLight.setSelected(true);
		themePanel.add(themeLight);
		themePanel.add(themeDark);
		themeLight.addActionListener(e -> {
			try {
				UIManager.setLookAndFeel(new FlatLightLaf());
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception ex) {
				Logger.error(ex);
			}
		});
		themeDark.addActionListener(e -> {
			try {
				UIManager.setLookAndFeel(new FlatDarkLaf());
				SwingUtilities.updateComponentTreeUI(this);
			} catch (Exception ex) {
				Logger.error(ex);
			}
		});
		menuPanel.add(themePanel);
		menuPanel.add(Box.createVerticalStrut(8));
		// Resize
		JButton btnResizeMenu = new JButton("Resize");
		btnResizeMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (resizeDeviceDisplayDialog == null) {
					resizeDeviceDisplayDialog = new ResizeDeviceDisplayDialog();
				}
				DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) DeviceFactory.getDevice().getDeviceDisplay();
				resizeDeviceDisplayDialog.setDeviceDisplaySize(deviceDisplay.getFullWidth(), deviceDisplay.getFullHeight());
				if (SwingDialogWindow.show(Main.this, "Enter new size...", resizeDeviceDisplayDialog, true)) {
					int w = resizeDeviceDisplayDialog.getDeviceDisplayWidth();
					int h = resizeDeviceDisplayDialog.getDeviceDisplayHeight();
					if (w > 0 && h > 0) {
						setDeviceSize(deviceDisplay, w, h);
						pack();
						devicePanel.requestFocus();
					} else {
						JOptionPane.showMessageDialog(Main.this, "Invalid size entered.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		menuPanel.add(btnResizeMenu);
		menuPanel.add(Box.createVerticalStrut(16));

		// Tools section
		JLabel toolsLabel = new JLabel("Tools");
		toolsLabel.setFont(toolsLabel.getFont().deriveFont(Font.BOLD));
		menuPanel.add(toolsLabel);
		menuPanel.add(Box.createVerticalStrut(8));
		// Record
		JButton btnRecordMenu = new JButton("Record");
		btnRecordMenu.addActionListener(e -> {
			if (encoder == null) menuStartCaptureListener.actionPerformed(e);
			else menuStopCaptureListener.actionPerformed(e);
		});
		menuPanel.add(btnRecordMenu);
		menuPanel.add(Box.createVerticalStrut(4));
		// Screenshot
		JButton btnScreenshotMenu = new JButton("Screenshot");
		btnScreenshotMenu.addActionListener(e -> {
			try {
				File picturesDir = getDefaultPicturesDirectory();
				String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
				File file = new File(picturesDir, "Screenshot_" + timestamp + ".png");
				java.awt.image.BufferedImage img = ((SwingDisplayComponent) devicePanel.getDisplayComponent()).getGraphicsSurface().getImage();
				javax.imageio.ImageIO.write(img, "png", file);
				javax.swing.JOptionPane.showMessageDialog(Main.this, "Screenshot saved to: " + file.getAbsolutePath(), "Screenshot Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			} catch (Exception ex) {
				javax.swing.JOptionPane.showMessageDialog(Main.this, "Failed to save screenshot: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
			}
		});
		menuPanel.add(btnScreenshotMenu);
		menuPanel.add(Box.createVerticalStrut(4));
		// Performance submenu (collapsible)
		JButton btnPerf = new JButton("Performance");
		JPanel perfPanel = new JPanel();
		perfPanel.setLayout(new BoxLayout(perfPanel, BoxLayout.Y_AXIS));
		perfPanel.setOpaque(false);
		perfPanel.setVisible(false);
		JCheckBoxMenuItem perfDoubleBuffering = new JCheckBoxMenuItem("Double Buffering");
		perfDoubleBuffering.setSelected(devicePanel.isDoubleBuffered());
		perfDoubleBuffering.addActionListener(e -> devicePanel.setDoubleBuffered(perfDoubleBuffering.isSelected()));
		perfPanel.add(perfDoubleBuffering);
		JCheckBoxMenuItem perfMinimizeRepaints = new JCheckBoxMenuItem("Minimize Repaints");
		perfMinimizeRepaints.setSelected(SwingDeviceComponent.isMinimizeRepaints());
		perfMinimizeRepaints.addActionListener(e -> SwingDeviceComponent.setMinimizeRepaints(perfMinimizeRepaints.isSelected()));
		perfPanel.add(perfMinimizeRepaints);
		JCheckBoxMenuItem perfImageCaching = new JCheckBoxMenuItem("Image Caching");
		perfImageCaching.setSelected(SwingDeviceComponent.isImageCaching());
		perfImageCaching.addActionListener(e -> SwingDeviceComponent.setImageCaching(perfImageCaching.isSelected()));
		perfPanel.add(perfImageCaching);
		JCheckBoxMenuItem perfThreadPriority = new JCheckBoxMenuItem("High Thread Priority");
		perfThreadPriority.setSelected(MIDletThread.isHighPriority());
		perfThreadPriority.addActionListener(e -> MIDletThread.setHighPriority(perfThreadPriority.isSelected()));
		perfPanel.add(perfThreadPriority);
		JCheckBoxMenuItem perfObjectPooling = new JCheckBoxMenuItem("Object Pooling");
		perfObjectPooling.setSelected(SwingDeviceComponent.isObjectPooling());
		perfObjectPooling.addActionListener(e -> SwingDeviceComponent.setObjectPooling(perfObjectPooling.isSelected()));
		perfPanel.add(perfObjectPooling);
		btnPerf.addActionListener(e -> perfPanel.setVisible(!perfPanel.isVisible()));
		menuPanel.add(btnPerf);
		menuPanel.add(perfPanel);
		menuPanel.add(Box.createVerticalStrut(4));
		// Timer
		JCheckBoxMenuItem timerToggle = new JCheckBoxMenuItem("Show Timer", true);
		timerToggle.addActionListener(e -> upTimerLabel.setVisible(timerToggle.isSelected()));
		menuPanel.add(timerToggle);
		menuPanel.add(Box.createVerticalStrut(4));
		// Persist
		JCheckBoxMenuItem persistToggle = new JCheckBoxMenuItem("Persist");
		persistToggle.addActionListener(e -> PersistMode.enabled = persistToggle.isSelected());
		menuPanel.add(persistToggle);
		menuPanel.add(Box.createVerticalStrut(4));
		// Proxy
		JButton btnProxy = new JButton("Proxy");
		btnProxy.addActionListener(e -> {
			ProxySettingsDialog dialog = new ProxySettingsDialog(this);
			dialog.setVisible(true);
		});
		menuPanel.add(btnProxy);
		menuPanel.add(Box.createVerticalStrut(8));
		// About and Exit
		JButton btnAbout = new JButton("About");
		btnAbout.addActionListener(menuAboutListener);
		menuPanel.add(btnAbout);
		JButton btnExitMenu = new JButton("Exit");
		btnExitMenu.addActionListener(menuExitListener);
		menuPanel.add(btnExitMenu);

		// Add components in order: menuPanel, deviceComponent, toolbar
		mainContainer.add(menuPanel);
		mainContainer.add(deviceComponent);

		this.common = new Common(emulatorContext);
		this.common.setStatusBarListener(statusBarListener);
		this.common.setResponseInterfaceListener(responseInterfaceListener);
		this.common.loadImplementationsFromConfig();

		// Menu button toggles menuPanel
		btnMenu.addActionListener(e -> {
			menuPanel.setVisible(!menuPanel.isVisible());
			mainContainer.revalidate();
			mainContainer.repaint();
		});
		toolbar.add(btnMenu, 0);
		toolbar.addSeparator();
		// Quick-access buttons (right side)
		JButton btnLog = new JButton("Log");
		btnLog.setToolTipText("Show Log Console");
		btnLog.addActionListener(menuLogConsoleListener);
		toolbar.add(btnLog);
		JButton btnRecordStore = new JButton("Record Store");
		btnRecordStore.setToolTipText("Show Record Store Manager");
		btnRecordStore.addActionListener(menuRecordStoreManagerListener);
		toolbar.add(btnRecordStore);
		JButton btnHelp = new JButton("Help");
		btnHelp.setToolTipText("About");
		btnHelp.addActionListener(menuAboutListener);
		toolbar.add(btnHelp);
		JButton btnExit = new JButton("Exit");
		btnExit.setToolTipText("Exit Emulator");
		btnExit.addActionListener(menuExitListener);
		toolbar.add(btnExit);
		toolbar.add(Box.createHorizontalGlue());
		toolbar.add(upTimerLabel);

		// Add toolbar to the main container (at the very bottom)
		mainContainer.add(toolbar);

		// Set the main container as the content pane
		setContentPane(mainContainer);

		// Status bar (optional, can be merged with toolbar)
		statusBar.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
		// Note: Status bar is now part of the main container layout

		Message.addListener(new SwingErrorMessageDialogPanel(this));
		devicePanel.setTransferHandler(new DropTransferHandler());
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
			ClassLoader classLoader = getClass().getClassLoader();
			if (entry.getFileName() != null) {
				URL[] urls = new URL[1];
				urls[0] = new File(Config.getConfigPath(), entry.getFileName()).toURI().toURL();
				classLoader = Common.createExtensionsClassLoader(urls);
			}

			// TODO font manager have to be moved from emulatorContext into
			// device
			emulatorContext.getDeviceFontManager().init();

			Device device = DeviceImpl.create(emulatorContext, classLoader, entry.getDescriptorLocation(),
					J2SEDevice.class);
			this.deviceEntry = entry;

			DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) device.getDeviceDisplay();
			if (deviceDisplay.isResizable()) {
				Rectangle size = Config.getDeviceEntryDisplaySize(entry);
				if (size != null) {
				    setDeviceSize(deviceDisplay, size.width, size.height);
				}
			}
			common.setDevice(device);
			// (No input method forcing, no dummy listeners, no dummy TextBox, no reflection hacks)
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
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (Exception ex) {
			Logger.error(ex);
		}

		final Main app = new Main();
		SwingUtilities.updateComponentTreeUI(app);
		if (args.length > 0) {
			Logger.debug("arguments", debugArgs.toString());
		}
		
		// Only use resizable device
		if (app.common.initParams(params, new DeviceEntry("Resizable device", null, org.microemu.device.impl.DeviceImpl.RESIZABLE_LOCATION, true, false), J2SEDevice.class)) {
			app.deviceEntry = new DeviceEntry("Resizable device", null, org.microemu.device.impl.DeviceImpl.RESIZABLE_LOCATION, true, false);
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

		if (Config.isWindowOnStart("logConsole")) {
			app.menuLogConsoleListener.actionPerformed(null);
		}
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

		app.addComponentListener(app.componentListener);

		app.responseInterfaceListener.stateChanged(true);
	}

	private abstract class CountTimerTask extends TimerTask {

		protected int counter;

		public CountTimerTask(int counter) {
			this.counter = counter;
		}

	}

    private boolean upTimerRunning = false;
    public void startUpTimerIfNotRunning() {
        if (upTimerRunning) return;
        upTimerStartMillis = System.currentTimeMillis();
        if (upTimer == null) {
            upTimer = new javax.swing.Timer(1000, e -> {
                long elapsed = System.currentTimeMillis() - upTimerStartMillis;
                long days = elapsed / (1000 * 60 * 60 * 24);
                long hours = (elapsed / (1000 * 60 * 60)) % 24;
                long minutes = (elapsed / (1000 * 60)) % 60;
                long seconds = (elapsed / 1000) % 60;
                if (upTimerLabel != null) {
                    upTimerLabel.setText(days + "d" + hours + "h" + minutes + "m" + seconds + "s");
                }
            });
            upTimer.setInitialDelay(0);
        }
        upTimer.start();
        upTimerRunning = true;
        if (upTimerLabel != null) upTimerLabel.setVisible(true);
    }

    public void stopUpTimer() {
        if (upTimer != null) {
            upTimer.stop();
        }
        upTimerRunning = false;
        if (upTimerLabel != null) {
            upTimerLabel.setText("0d0h0m0s");
            upTimerLabel.setVisible(true);
        }
    }

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

    public void notifyDestroyedCallback() {
        stopUpTimer();
    }
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
