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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
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
import org.microemu.app.ui.swing.JMRUMenu;
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
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main extends JFrame {

	private static final long serialVersionUID = 1L;

	protected Common common;

	private MIDletUrlPanel midletUrlPanel = null;

	private JFileChooser saveForWebChooser;

	private JFileChooser fileChooser = null;

	private JFileChooser captureFileChooser = null;

	private JMenuItem menuOpenMIDletFile;

	private JMenuItem menuOpenMIDletURL;

	private JMenuItem menuSaveForWeb;

	private JMenuItem menuStartCapture;

	private JMenuItem menuStopCapture;

	private JCheckBoxMenuItem menuMIDletNetworkConnection;

	private JCheckBoxMenuItem menuLogConsole;

	private JCheckBoxMenuItem menuRecordStoreManager;

	private JFrame scaledDisplayFrame;

	private JCheckBoxMenuItem[] zoomLevels;

	private SwingDeviceComponent devicePanel;

	private SwingLogConsoleDialog logConsoleDialog;

	private RecordStoreManagerDialog recordStoreManagerDialog;

	private QueueAppender logQueueAppender;

	private DeviceEntry deviceEntry;

	private AnimatedGifEncoder encoder;

	private JLabel statusBar = new JLabel("Status");

	private JButton resizeButton = new JButton("Resize");

	private ResizeDeviceDisplayDialog resizeDeviceDisplayDialog = null;

	private JLabel upTimerLabel = new JLabel("00:00:00");
	private long upTimerStartMillis = -1;
	private javax.swing.Timer upTimer;

	private JCheckBoxMenuItem menuTimerToggle;

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
			menuStartCapture.setEnabled(false);
			menuStopCapture.setEnabled(true);
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
			menuStopCapture.setEnabled(false);

			synchronized (Main.this) {
				encoder.finish();
				javax.swing.JOptionPane.showMessageDialog(Main.this, "Recording saved to: " + (Main.this.captureFile != null ? Main.this.captureFile.getAbsolutePath() : "(unknown location)"), "Recording Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
				encoder = null;
			}

			menuStartCapture.setEnabled(true);
		}
	};

	private ActionListener menuMIDletNetworkConnectionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			org.microemu.cldc.http.Connection.setAllowNetworkConnection(menuMIDletNetworkConnection.getState());
		}

	};

	private ActionListener menuRecordStoreManagerListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (recordStoreManagerDialog == null) {
				recordStoreManagerDialog = new RecordStoreManagerDialog(Main.this, common);
				recordStoreManagerDialog.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						menuRecordStoreManager.setState(false);
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
						menuLogConsole.setState(false);
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
			final JCheckBoxMenuItem selectedZoomLevelMenuItem = (JCheckBoxMenuItem) e.getSource();
			if (selectedZoomLevelMenuItem.isSelected()) {
				for (int i = 0; i < zoomLevels.length; ++i) {
					if (zoomLevels[i] != e.getSource()) {
						zoomLevels[i].setSelected(false);
					}
				}
				final int scale = Integer.parseInt(e.getActionCommand());
				if (scaledDisplayFrame != null) {
					((SwingDisplayComponent) emulatorContext.getDisplayComponent())
							.removeDisplayRepaintListener(updateScaledImageListener);
					scaledDisplayFrame.dispose();
				}
				scaledDisplayFrame = new JFrame(getTitle());
				scaledDisplayFrame.setContentPane(new JLabel(new ImageIcon()));
				updateScaledImageListener = new DisplayRepaintListener() {
					public void repaintInvoked(Object repaintObject) {
						updateScaledImage(scale, scaledDisplayFrame);
						scaledDisplayFrame.validate();
					}
				};
				scaledDisplayFrame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent event) {
						selectedZoomLevelMenuItem.setSelected(false);
					}
				});
				scaledDisplayFrame.getContentPane().addMouseListener(new MouseListener() {
					private MouseListener receiver = ((SwingDisplayComponent) emulatorContext.getDisplayComponent())
							.getMouseListener();

					public void mouseClicked(MouseEvent e) {
						receiver.mouseClicked(createAdaptedMouseEvent(e, scale));
					}

					public void mousePressed(MouseEvent e) {
						receiver.mousePressed(createAdaptedMouseEvent(e, scale));
					}

					public void mouseReleased(MouseEvent e) {
						receiver.mouseReleased(createAdaptedMouseEvent(e, scale));
					}

					public void mouseEntered(MouseEvent e) {
						receiver.mouseEntered(createAdaptedMouseEvent(e, scale));
					}

					public void mouseExited(MouseEvent e) {
						receiver.mouseExited(createAdaptedMouseEvent(e, scale));
					}
				});
				scaledDisplayFrame.getContentPane().addMouseMotionListener(new MouseMotionListener() {
					private MouseMotionListener receiver = ((SwingDisplayComponent) emulatorContext
							.getDisplayComponent()).getMouseMotionListener();

					public void mouseDragged(MouseEvent e) {
						receiver.mouseDragged(createAdaptedMouseEvent(e, scale));
					}

					public void mouseMoved(MouseEvent e) {
						receiver.mouseMoved(createAdaptedMouseEvent(e, scale));
					}
				});
				scaledDisplayFrame.getContentPane().addMouseWheelListener(new MouseWheelListener() {
					private MouseWheelListener receiver = ((SwingDisplayComponent) emulatorContext
							.getDisplayComponent()).getMouseWheelListener();

					public void mouseWheelMoved(MouseWheelEvent e) {
						MouseWheelEvent adaptedEvent = createAdaptedMouseWheelEvent(e, scale);
						receiver.mouseWheelMoved(adaptedEvent);
					}
				});
				scaledDisplayFrame.addKeyListener(devicePanel);

				updateScaledImage(scale, scaledDisplayFrame);
				((SwingDisplayComponent) emulatorContext.getDisplayComponent())
						.addDisplayRepaintListener(updateScaledImageListener);
				scaledDisplayFrame.setIconImage(getIconImage());
				scaledDisplayFrame.setResizable(false);
				Point location = getLocation();
				Dimension size = getSize();
				Rectangle window = Config.getWindow("scaledDisplay", new Rectangle(location.x + size.width, location.y,
						0, 0));
				scaledDisplayFrame.setLocation(window.x, window.y);
				Config.setWindow("scaledDisplay", new Rectangle(scaledDisplayFrame.getX(), scaledDisplayFrame.getY(),
						0, 0), false);
				scaledDisplayFrame.pack();
				scaledDisplayFrame.setVisible(true);
			} else {
				((SwingDisplayComponent) emulatorContext.getDisplayComponent())
						.removeDisplayRepaintListener(updateScaledImageListener);
				scaledDisplayFrame.dispose();
			}
		}

		private MouseEvent createAdaptedMouseEvent(MouseEvent e, int scale) {
			return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX() / scale, e
					.getY()
					/ scale, e.getClickCount(), e.isPopupTrigger(), e.getButton());
		}

		private MouseWheelEvent createAdaptedMouseWheelEvent(MouseWheelEvent e, int scale) {
			return new MouseWheelEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), e.getX() / scale, e
					.getY()
					/ scale, e.getClickCount(), e.isPopupTrigger(), e.getScrollType(), e.getScrollAmount(), e
					.getWheelRotation());
		}

		private void updateScaledImage(int scale, JFrame scaledLCDFrame) {
			J2SEGraphicsSurface graphicsSurface = 
					((SwingDisplayComponent) emulatorContext.getDisplayComponent()).getGraphicsSurface();
			
			BufferedImage img = graphicsSurface.getImage();
			BufferedImage scaledImg = new BufferedImage(img.getWidth() * scale, img.getHeight() * scale, img.getType());
			Graphics2D imgGraphics = scaledImg.createGraphics();
			imgGraphics.scale(scale, scale);
			imgGraphics.drawImage(img, 0, 0, null);
			
			((ImageIcon) (((JLabel) scaledLCDFrame.getContentPane()).getIcon())).setImage(scaledImg);
			((JLabel) scaledLCDFrame.getContentPane()).repaint();
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
			menuOpenMIDletFile.setEnabled(state);
			menuOpenMIDletURL.setEnabled(state);
			
			if (common.jad.getJarURL() != null) {
				menuSaveForWeb.setEnabled(state);
			} else {
				menuSaveForWeb.setEnabled(false);
			}
			
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
		this(null);
		// Register destroyed and started callbacks with Common
		if (common != null) {
			common.setDestroyedCallback(this::notifyDestroyedCallback);
			common.setStartedCallback(this::startUpTimerIfNotRunning);
		}
	}

	public Main(DeviceEntry defaultDevice) {

		this.logQueueAppender = new QueueAppender(1024);
		Logger.addAppender(logQueueAppender);

		JMenuBar menuBar = new JMenuBar();

		JMenu menuFile = new JMenu("File");

		menuOpenMIDletFile = new JMenuItem("Open MIDlet File...");
		menuOpenMIDletFile.addActionListener(menuOpenMIDletFileListener);
		menuFile.add(menuOpenMIDletFile);

		menuOpenMIDletURL = new JMenuItem("Open MIDlet URL...");
		menuOpenMIDletURL.addActionListener(menuOpenMIDletURLListener);
		menuFile.add(menuOpenMIDletURL);

		JMenuItem menuItemTmp = new JMenuItem("Close MIDlet");
		menuItemTmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		menuItemTmp.addActionListener(menuCloseMidletListener);
		menuFile.add(menuItemTmp);

		menuFile.addSeparator();

		JMRUMenu urlsMRU = new JMRUMenu("Recent MIDlets...");
		urlsMRU.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event instanceof JMRUMenu.MRUActionEvent) {
					Common.openMIDletUrlSafe(((MidletURLReference) ((JMRUMenu.MRUActionEvent) event).getSourceMRU())
							.getUrl());
					if (recordStoreManagerDialog != null) {
						recordStoreManagerDialog.refresh();
					}
				}
			}
		});

		Config.getUrlsMRU().setListener(urlsMRU);
		menuFile.add(urlsMRU);

		menuFile.addSeparator();

		menuSaveForWeb = new JMenuItem("Save for Web...");
		menuSaveForWeb.addActionListener(menuSaveForWebListener);
		menuFile.add(menuSaveForWeb);

		menuFile.addSeparator();

		JMenuItem menuItem = new JMenuItem("Exit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(menuExitListener);
		menuFile.add(menuItem);

		JMenu menuOptions = new JMenu("Options");

		JMenu menuScaleLCD = new JMenu("Scaled display");
		menuOptions.add(menuScaleLCD);
		zoomLevels = new JCheckBoxMenuItem[3];
		for (int i = 0; i < zoomLevels.length; ++i) {
			zoomLevels[i] = new JCheckBoxMenuItem("x " + (i + 2));
			zoomLevels[i].setActionCommand("" + (i + 2));
			zoomLevels[i].addActionListener(menuScaledDisplayListener);
			menuScaleLCD.add(zoomLevels[i]);
		}

		// --- Theme Menu ---
		JMenu menuTheme = new JMenu("Theme");
		ButtonGroup themeGroup = new ButtonGroup();
		JRadioButtonMenuItem themeLight = new JRadioButtonMenuItem("Light");
		JRadioButtonMenuItem themeDark = new JRadioButtonMenuItem("Dark");
		themeGroup.add(themeLight);
		themeGroup.add(themeDark);
		menuTheme.add(themeLight);
		menuTheme.add(themeDark);
		themeLight.setSelected(true);
		themeLight.addActionListener(e -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
		menuOptions.add(menuTheme);
		// --- End Theme Menu ---

		menuStartCapture = new JMenuItem("Start Recording");
		menuStartCapture.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        File picturesDir = getDefaultPicturesDirectory();
		        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
		        Main.this.captureFile = new File(picturesDir, "Recording_" + timestamp + ".gif");
		        encoder = new AnimatedGifEncoder();
		        encoder.start(Main.this.captureFile.getAbsolutePath());
		        menuStartCapture.setEnabled(false);
		        menuStopCapture.setEnabled(true);
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
		            int answer = JOptionPane.showConfirmDialog(Main.this, "Override the file:" + file + "?", "Question?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		            if (answer == 1 /* no */) {
		                return false;
		            }
		        }
		        return true;
		    }
		});
		menuStopCapture = new JMenuItem("Stop Recording");
		menuStopCapture.setEnabled(false);
		menuStopCapture.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        menuStopCapture.setEnabled(false);
		        synchronized (Main.this) {
		            if (encoder != null) {
		                encoder.finish();
		                javax.swing.JOptionPane.showMessageDialog(Main.this, "Recording saved to: " + (Main.this.captureFile != null ? Main.this.captureFile.getAbsolutePath() : "(unknown location)"), "Recording Saved", javax.swing.JOptionPane.INFORMATION_MESSAGE);
		                encoder = null;
		            }
		        }
		        menuStartCapture.setEnabled(true);
		    }
		});
		menuOptions.add(menuStartCapture);
		menuOptions.add(menuStopCapture);

		menuMIDletNetworkConnection = new JCheckBoxMenuItem("MIDlet Network access");
		menuMIDletNetworkConnection.setState(true);
		menuMIDletNetworkConnection.addActionListener(menuMIDletNetworkConnectionListener);
		menuOptions.add(menuMIDletNetworkConnection);

		menuRecordStoreManager = new JCheckBoxMenuItem("Record Store Manager");
		menuRecordStoreManager.setState(false);
		menuRecordStoreManager.addActionListener(menuRecordStoreManagerListener);
		menuOptions.add(menuRecordStoreManager);

		menuLogConsole = new JCheckBoxMenuItem("Log console");
		menuLogConsole.setState(false);
		menuLogConsole.addActionListener(menuLogConsoleListener);
		menuOptions.add(menuLogConsole);

		menuOptions.addSeparator();
		JCheckBoxMenuItem menuShowMouseCoordinates = new JCheckBoxMenuItem("Mouse coordinates");
		menuShowMouseCoordinates.setState(false);
		menuShowMouseCoordinates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				devicePanel.switchShowMouseCoordinates();
			}
		});
		menuOptions.add(menuShowMouseCoordinates);

		JMenu menuHelp = new JMenu("Help");
		JMenuItem menuAbout = new JMenuItem("About");
		menuAbout.addActionListener(menuAboutListener);
		menuHelp.add(menuAbout);

		JMenu menuTools = new JMenu("Tools");
		JMenu menuRecordTab = new JMenu("Record Tab");
		menuRecordTab.add(menuStartCapture);
		menuRecordTab.add(menuStopCapture);
		menuTools.add(menuRecordTab);
		JMenuItem menuScreenshot = new JMenuItem("Screenshot");
		menuScreenshot.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
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
		    }
		});
		menuTools.add(menuScreenshot);
		JMenu menuPerformance = new JMenu("Performance");
		JCheckBoxMenuItem perfDoubleBuffering = new JCheckBoxMenuItem("Double Buffering");
		JCheckBoxMenuItem perfMinimizeRepaints = new JCheckBoxMenuItem("Minimize Repaints");
		JCheckBoxMenuItem perfImageCaching = new JCheckBoxMenuItem("Image Caching");
		JCheckBoxMenuItem perfThreadPriority = new JCheckBoxMenuItem("High Thread Priority");
		JCheckBoxMenuItem perfObjectPooling = new JCheckBoxMenuItem("Object Pooling");
		menuPerformance.add(perfDoubleBuffering);
		menuPerformance.add(perfMinimizeRepaints);
		menuPerformance.add(perfImageCaching);
		menuPerformance.add(perfThreadPriority);
		menuPerformance.add(perfObjectPooling);
		menuTools.add(menuPerformance);
		JCheckBoxMenuItem menuPersist = new JCheckBoxMenuItem("Persist");
		menuTools.add(menuPersist);
		// Proxy menu item
		JMenuItem menuProxy = new JMenuItem("Proxy");
		menuProxy.addActionListener(e -> {
			ProxySettingsDialog dialog = new ProxySettingsDialog(this);
			dialog.setVisible(true);
		});
		menuTools.add(menuProxy);
		menuBar.add(menuFile);
		menuBar.add(menuOptions);
		menuBar.add(menuTools);
		menuBar.add(menuHelp);
		menuBar.add(Box.createHorizontalGlue()); // Pushes the next component to the right
		menuBar.add(upTimerLabel);
		setJMenuBar(menuBar);

		// UpTimer is not running at launch
		upTimerLabel.setText("0d0h0m0s");
		upTimerLabel.setVisible(true);
		upTimerRunning = false;
		if (upTimer != null) upTimer.stop();

		// Change window title to simple format
		setTitle("JarEngine");

		// Set application icon
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/org/microemu/icon.png")));
		
		// Set professional window properties for better display
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setLocationRelativeTo(null); // Center on screen

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

		getContentPane().add(createContents(getContentPane()), "Center");

		this.common = new Common(emulatorContext);
		this.common.setStatusBarListener(statusBarListener);
		this.common.setResponseInterfaceListener(responseInterfaceListener);
		this.common.loadImplementationsFromConfig();

		this.resizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if (resizeDeviceDisplayDialog == null) {
					resizeDeviceDisplayDialog = new ResizeDeviceDisplayDialog();
				}
				DeviceDisplayImpl deviceDisplay = (DeviceDisplayImpl) DeviceFactory.getDevice().getDeviceDisplay();
				resizeDeviceDisplayDialog.setDeviceDisplaySize(deviceDisplay.getFullWidth(), deviceDisplay
						.getFullHeight());
				if (SwingDialogWindow.show(Main.this, "Enter new size...", resizeDeviceDisplayDialog, true)) {
				    setDeviceSize(deviceDisplay, resizeDeviceDisplayDialog.getDeviceDisplayWidth(), resizeDeviceDisplayDialog.getDeviceDisplayHeight());
					pack();
					devicePanel.requestFocus();
				}
			}
		});

		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(statusBar, "West");
		statusPanel.add(this.resizeButton, "East");

		getContentPane().add(statusPanel, "South");

		Message.addListener(new SwingErrorMessageDialogPanel(this));

		devicePanel.setTransferHandler(new DropTransferHandler());

		perfDoubleBuffering.addActionListener(e -> {
		    devicePanel.setDoubleBuffered(perfDoubleBuffering.isSelected());
		});
		perfDoubleBuffering.setSelected(devicePanel.isDoubleBuffered());

		perfMinimizeRepaints.addActionListener(e -> {
		    SwingDeviceComponent.setMinimizeRepaints(perfMinimizeRepaints.isSelected());
		});
		perfMinimizeRepaints.setSelected(SwingDeviceComponent.isMinimizeRepaints());

		perfImageCaching.addActionListener(e -> {
		    SwingDeviceComponent.setImageCaching(perfImageCaching.isSelected());
		});
		perfImageCaching.setSelected(SwingDeviceComponent.isImageCaching());

		perfThreadPriority.addActionListener(e -> {
		    MIDletThread.setHighPriority(perfThreadPriority.isSelected());
		});
		perfThreadPriority.setSelected(MIDletThread.isHighPriority());

		perfObjectPooling.addActionListener(e -> {
		    SwingDeviceComponent.setObjectPooling(perfObjectPooling.isSelected());
		});
		perfObjectPooling.setSelected(SwingDeviceComponent.isObjectPooling());

		menuPersist.addActionListener(e -> {
		    PersistMode.enabled = menuPersist.isSelected();
		});

		// Timer toggle
		menuTimerToggle = new JCheckBoxMenuItem("Timer");
		menuTimerToggle.setState(true);
		menuTimerToggle.addActionListener(e -> {
			boolean enabled = menuTimerToggle.getState();
			upTimerLabel.setVisible(enabled);
			if (enabled) {
				if (upTimerRunning) {
					upTimer.start();
				}
			} else {
				if (upTimer != null) {
					upTimer.stop();
				}
			}
		});
		menuTools.add(menuTimerToggle);

		// Add Update Emulator menu item
		JMenuItem menuUpdate = new JMenuItem("Update Emulator");
		menuUpdate.addActionListener(e -> {
			new SwingWorker<Void, Void>() {
				protected Void doInBackground() {
					try {
						String currentVersion = null;
						// Try to read version.txt from JAR directory
						try {
							File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
							File versionFile = new File(jar.getParentFile(), "version.txt");
							if (versionFile.exists()) {
								try (BufferedReader br = new BufferedReader(new FileReader(versionFile))) {
									currentVersion = br.readLine().trim();
								}
							} else {
								// Try resource
								try (InputStream in = Main.class.getResourceAsStream("/version.txt")) {
									if (in != null) {
										BufferedReader br = new BufferedReader(new InputStreamReader(in));
										currentVersion = br.readLine().trim();
									}
								}
							}
						} catch (Exception ex) {
							// ignore, will show error below
						}
						final String currentVersionFinal = currentVersion;
						if (currentVersionFinal == null) {
							javax.swing.SwingUtilities.invokeLater(() ->
								JOptionPane.showMessageDialog(Main.this, "Could not determine current version.", "Update Error", JOptionPane.ERROR_MESSAGE)
							);
							return null;
						}
						String latestVersion = UpdateChecker.getLatestVersion();
						final String latestVersionFinal = latestVersion;
						if (!UpdateChecker.isUpdateAvailable(currentVersionFinal, latestVersionFinal)) {
							javax.swing.SwingUtilities.invokeLater(() ->
								JOptionPane.showMessageDialog(Main.this, "You are already running the latest version (" + currentVersionFinal + ").", "No Update Available", JOptionPane.INFORMATION_MESSAGE)
							);
							return null;
						}
						int confirm = JOptionPane.showConfirmDialog(Main.this, "A new version (" + latestVersionFinal + ") is available. Update now?", "Update Available", JOptionPane.YES_NO_OPTION);
						if (confirm != JOptionPane.YES_OPTION) return null;
						File jar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
						File dest = new File(jar.getParentFile(), "JarEngine-" + latestVersionFinal + ".jar.download");
						final File destFinal = dest;
						javax.swing.SwingUtilities.invokeLater(() ->
							JOptionPane.showMessageDialog(Main.this, "Downloading update...\nThis may take a moment.", "Update", JOptionPane.INFORMATION_MESSAGE)
						);
						UpdateChecker.downloadUpdate(latestVersionFinal, destFinal);
						javax.swing.SwingUtilities.invokeLater(() ->
							JOptionPane.showMessageDialog(Main.this, "Update downloaded. The emulator will now restart.", "Update", JOptionPane.INFORMATION_MESSAGE)
						);
						UpdateChecker.applyUpdateAndRestart(destFinal, latestVersionFinal);
					} catch (Exception ex) {
						javax.swing.SwingUtilities.invokeLater(() ->
							JOptionPane.showMessageDialog(Main.this, "Update failed: " + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE)
						);
					}
					return null;
				}
			}.execute();
		});
		menuOptions.add(menuUpdate);
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
			resizeButton.setVisible(true);
		} else {
			setResizable(false);
			resizeButton.setVisible(false);
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

		// Remove FlatLaf setup, use default look and feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			Logger.error(ex);
		}

		final Main app = new Main();
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
			app.menuLogConsole.setSelected(true);
		}
		if (Config.isWindowOnStart("recordStoreManager")) {
			app.menuRecordStoreManagerListener.actionPerformed(null);
			app.menuRecordStoreManager.setSelected(true);
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
