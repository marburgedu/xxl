/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2011 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library;  If not, see <http://www.gnu.org/licenses/>. 

    http://code.google.com/p/xxl/

*/

package xxl.core.io;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import xxl.core.io.fat.DIR;
import xxl.core.io.fat.DirectoryDate;
import xxl.core.io.fat.DirectoryTime;
import xxl.core.io.fat.ExtendedFile;
import xxl.core.io.fat.ExtendedRandomAccessFile;
import xxl.core.io.fat.FAT;
import xxl.core.io.fat.FATDevice;
import xxl.core.io.fat.FileSystem;
import xxl.core.io.fat.errors.DirectoryException;
import xxl.core.io.fat.errors.WrongFATType;
import xxl.core.io.fat.util.StringOperations;
import xxl.core.io.raw.NativeRawAccess;
import xxl.core.io.raw.RAFRawAccess;
import xxl.core.io.raw.RAMRawAccess;
import xxl.core.io.raw.RawAccess;
import xxl.core.io.raw.RawAccessUtils;
import xxl.core.util.XXLSystem;

/////////////////////////////////////////////////////////////////
// I didn't split the classes to different files, because this //
// class was build for test purposes. So all classes for test- //
// ing can be found here.                                      //
/////////////////////////////////////////////////////////////////


/**
 * This class is a bit like the windows-explorer. It can be used to test the functionalty
 * of the file system implementation.
 */
public class RawExplorer extends JFrame
{
	/**
	 * Directoryname, where the files are stored.
	 */
	protected static String outDir;
	
	/**
	 * This file is used to initialize the FileSystem class. All
	 * devices that are listed in that file will be booted.
	 */
	protected static File masterBootRecordFile = null;
	
	/**
	 * Name of the file.
	 */
	protected static String masterBootRecordFileName = "filesystem.txt";
	
	/**
	 * Dummy file for ExtendedRandomAccessFile
	 */
	protected static File dummyFile = null;
	
	/**
	 * Size of a floppy disk in bytes.
	 */
	public static final long DISK_SIZE =  1474560;			//Floppy disk in bytes
	
	/**
	 * Size of a 100 MB Zip-Disk in bytes.
	 */
	public static final long ZIP_DISK_SIZE = 100646912;		//Zip-Disk in bytes
	
	/**
	 * Indicates a RAF, that is a RandomAccessFile.
	 */
	public static final byte RAF = 1;
	
	/**
	 * Indicates a RAM, that is a file in memory.
	 */
	public static final byte RAM = 2;
	
	/**
	 * Indicates a raw access file that can be used on a raw device.
	 */
	public static final byte NATIVE = 3;

	/**
	 * Object of the FileSystem.
	 */
	private FileSystem fileSystem;
	
	/**
	 * The actual selected device.
	 */
	private FATDevice selectedDevice = null;
	
	/**
	 * The actual selected path.
	 */
	private String actualPath = "";			//only the path
	
	/**
	 * The actual selected file name.
	 */
	private String actualFileName = null;	//only the name
	
	/**
	 * The directory tree.
	 */
	private DynamicTree deviceTree = new DynamicTree();

	/**
	 * The text output area.
	 */
	protected JTextArea output;
	
	/**
	 * The scroll pane for the text-output-area.
	 */
	protected JScrollPane outputScrollPane;
	
	/**
	 * The scroll pane for the file table.
	 */
	protected JScrollPane fileTableScrollPane;

	/**
	 * Some operations.
	 */
	protected String[] operations = {"createFile", "createDir", "deleteFile", "renameFile", "readFile", "writeStuff"};
	
	/**
	 * The box which contains the supported operations.
	 */
	JComboBox operationsBox = new JComboBox(operations);
	
	/**
	 * Not used yet.
	 */
	JTextField parameter1 = new JTextField(5);
	
	/**
	 * Not used yet.
	 */
	JTextField parameter2 = new JTextField(5);
	
	
	/**
	 * The file table which lists the informations about the files and directories.
	 */
	JTable fileTable;
	
	/**
	 * The model of the file table, it's used to manage the informations
	 * stored at the file table.
	 */
	FileTableModel fileTableModel;
	

	/**
	 * Create an instance of this object.
	 */
	public RawExplorer()
	{
		outDir = Common.getOutPath();
		dummyFile = new File(outDir+"dummyFile");
		
		try {
			dummyFile.createNewFile();
		}
		catch (IOException e) {}
		
		masterBootRecordFile = new File(outDir+masterBootRecordFileName);
		
		JMenuBar menuBar;
		JMenu informationMenu;
		JMenu fileSystemMenu;
		JMenu changeFsInfoMenu;
		JMenu helpMenu;
		JMenuItem aboutMenuItem, helpMenuItem;
		JMenuItem copyFileToRawMenu, copyFileFromRawMenu;
		JMenu formatSubMenu, formatSubMenu2, formatSubMenuFAT12, formatSubMenuFAT16, formatSubMenuFAT32;
		JMenuItem 	boot, formatDiskRAF, formatDiskNative, formatZipRAF, formatZipNative,
					formatFreeFAT12RAF, formatFreeFAT12RAM, formatFreeFAT12Native,
					formatFreeFAT16RAF, formatFreeFAT16RAM, formatFreeFAT16Native,
					formatFreeFAT32RAF, formatFreeFAT32RAM, formatFreeFAT32Native,
					exit, fastFormat;
		JMenuItem bpbItem, fatItem, fsiItem, rootItem, bootItem, readSectorItem, bpbInfoItem;

		fileTableModel = new FileTableModel(this);
		fileTable = new JTable(fileTableModel);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if (fileSystem != null)
					fileSystem.shutDown();
			}
		});

		//Add regular components to the window, using the default BorderLayout.
		output = new JTextArea();
		output.setEditable(false);
		output.setFont(new Font("Courier", Font.PLAIN, 12));
		outputScrollPane = new JScrollPane(output);

		fileTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = fileTable.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				if (!lsm.isSelectionEmpty())
				{
					int selectedRow = lsm.getMinSelectionIndex();
					//selectedRow is selected
					actualFileName = (String)fileTable.getValueAt(selectedRow, 0);
				}
			}
		});
		fileTableScrollPane = new JScrollPane(fileTable);

		//Create the menu bar.
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		//Build the first menu.
		fileSystemMenu = new JMenu("FileSystem");
		fileSystemMenu.setMnemonic('F');
		fileSystemMenu.getAccessibleContext().setAccessibleDescription(
			"Boot, format, and other stuff for file system."
		);
		menuBar.add(fileSystemMenu);

		//a group of JMenuItems

		boot = new JMenuItem("Boot Device", KeyEvent.VK_B);
		boot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		boot.getAccessibleContext().setAccessibleDescription("Boot a device.");
		boot.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					FATDevice device = bootDevice();
					if (device != null)
						deviceTree.addObject(new NodeInfo(device.getRealDeviceName()));
					else
						JOptionPane.showMessageDialog(RawExplorer.this, "Device was not booted.");
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(RawExplorer.this, ex);
				}
			}
		});
		fileSystemMenu.add(boot);

		formatSubMenu = new JMenu("Create new device");
		formatSubMenu.setMnemonic(KeyEvent.VK_F);
		fileSystemMenu.add(formatSubMenu);

		formatDiskRAF = new JMenuItem("Format Disk RAF ");
		formatDiskRAF.setMnemonic(KeyEvent.VK_D);
		formatDiskRAF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceDiskRAF();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenu.add(formatDiskRAF);
		
		formatDiskNative = new JMenuItem("Format Disk Native");
		formatDiskNative.setMnemonic(KeyEvent.VK_N);
		formatDiskNative.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceDiskNative();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenu.add(formatDiskNative);
		
		formatZipRAF = new JMenuItem("Format Zip-Disk RAF");
		formatZipRAF.setMnemonic(KeyEvent.VK_M);
		formatZipRAF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceZipDiskRAF();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenu.add(formatZipRAF);
		
		formatZipNative = new JMenuItem("Format Zip-Disk Native");
		formatZipNative.setMnemonic(KeyEvent.VK_N);
		formatZipNative.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceZipDiskNative();
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenu.add(formatZipNative);
		
		formatSubMenu2 = new JMenu("Format Free Value");
		formatSubMenu2.setMnemonic(KeyEvent.VK_O);
		formatSubMenu.add(formatSubMenu2);
		
		formatSubMenuFAT12 = new JMenu("FAT12");
		formatSubMenuFAT12.setMnemonic(KeyEvent.VK_F);
		formatSubMenu2.add(formatSubMenuFAT12);
		
		formatSubMenuFAT16 = new JMenu("FAT16");
		formatSubMenuFAT12.setMnemonic(KeyEvent.VK_A);
		formatSubMenu2.add(formatSubMenuFAT16);
		
		formatSubMenuFAT32 = new JMenu("FAT32");
		formatSubMenuFAT32.setMnemonic(KeyEvent.VK_T);
		formatSubMenu2.add(formatSubMenuFAT32);
		
		formatFreeFAT12RAF = new JMenuItem("RAF");
		formatFreeFAT12RAF.setMnemonic(KeyEvent.VK_R);
		formatFreeFAT12RAF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAF(FAT.FAT12);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT12.add(formatFreeFAT12RAF);
		
		formatFreeFAT12RAM = new JMenuItem("RAM");
		formatFreeFAT12RAM.setMnemonic(KeyEvent.VK_A);
		formatFreeFAT12RAM.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAM(FAT.FAT12);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT12.add(formatFreeFAT12RAM);
		
		formatFreeFAT12Native = new JMenuItem("Native");
		formatFreeFAT12Native.setMnemonic(KeyEvent.VK_N);
		formatFreeFAT12Native.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceNative(FAT.FAT12);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT12.add(formatFreeFAT12Native);
		
		formatFreeFAT16RAF = new JMenuItem("RAF");
		formatFreeFAT16RAF.setMnemonic(KeyEvent.VK_R);
		formatFreeFAT16RAF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAF(FAT.FAT16);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT16.add(formatFreeFAT16RAF);
		
		formatFreeFAT16RAM = new JMenuItem("RAM");
		formatFreeFAT16RAM.setMnemonic(KeyEvent.VK_A);
		formatFreeFAT16RAM.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAM(FAT.FAT16);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT16.add(formatFreeFAT16RAM);
		
		formatFreeFAT16Native = new JMenuItem("Native");
		formatFreeFAT16Native.setMnemonic(KeyEvent.VK_N);
		formatFreeFAT16Native.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceNative(FAT.FAT16);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT16.add(formatFreeFAT16Native);

		formatFreeFAT32RAF = new JMenuItem("RAF");
		formatFreeFAT32RAF.setMnemonic(KeyEvent.VK_R);
		formatFreeFAT32RAF.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAF(FAT.FAT32);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT32.add(formatFreeFAT32RAF);
		
		formatFreeFAT32RAM = new JMenuItem("RAM");
		formatFreeFAT32RAM.setMnemonic(KeyEvent.VK_A);
		formatFreeFAT32RAM.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceRAM(FAT.FAT32);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT32.add(formatFreeFAT32RAM);
		
		formatFreeFAT32Native = new JMenuItem("Native");
		formatFreeFAT32Native.setMnemonic(KeyEvent.VK_N);
		formatFreeFAT32Native.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					createDeviceNative(FAT.FAT32);
				}
				catch(Exception ex)
				{
					System.out.println(ex);
				}
			}
		});
		formatSubMenuFAT32.add(formatFreeFAT32Native);
		
		copyFileToRawMenu = new JMenuItem("Copy file to raw", KeyEvent.VK_8);
		copyFileToRawMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ActionEvent.ALT_MASK));
		copyFileToRawMenu.getAccessibleContext().setAccessibleDescription("Copy file from 'normal' file system to this file system implementation");
		copyFileToRawMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					copyFileToRaw();
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(RawExplorer.this, ex);
				}
			}
		});
		fileSystemMenu.add(copyFileToRawMenu);

		copyFileFromRawMenu = new JMenuItem("Copy file from raw", KeyEvent.VK_9);
		copyFileFromRawMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.ALT_MASK));
		copyFileFromRawMenu.getAccessibleContext().setAccessibleDescription("Copy file from this file system implementation to 'normal' file system");
		copyFileFromRawMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					copyFileFromRaw();
				}
				catch(Exception ex)
				{
					JOptionPane.showMessageDialog(RawExplorer.this, ex);
				}
			}
		});
		fileSystemMenu.add(copyFileFromRawMenu);


		fastFormat = new JMenuItem("Fast format device", KeyEvent.VK_F);
		fastFormat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.ALT_MASK));
		fastFormat.getAccessibleContext().setAccessibleDescription("Fast format the actual device.");
		fastFormat.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fastFormat();
			}
		});
		fileSystemMenu.add(fastFormat);

		exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		exit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (fileSystem != null)
					fileSystem.shutDown();
				dispose();
			}
		});

		fileSystemMenu.add(exit);

		//Build second menu in the menu bar.
		informationMenu = new JMenu("Informations");
		informationMenu.setMnemonic('I');
		informationMenu.getAccessibleContext().setAccessibleDescription("This menu does nothing");
		menuBar.add(informationMenu);

		bpbItem = new JMenuItem("Print BPB");
		bpbItem.setMnemonic('B');
		bpbItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		bpbItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printBPB();
			}
		});
		informationMenu.add(bpbItem);

		fatItem = new JMenuItem("Print FAT");
		fatItem.setMnemonic('F');
		fatItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
		fatItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printFAT();
			}
		});

		informationMenu.add(fatItem);

		fsiItem = new JMenuItem("Print FSI");
		fsiItem.setMnemonic('S');
		fsiItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, ActionEvent.ALT_MASK));
		fsiItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printFSI();
			}
		});
		informationMenu.add(fsiItem);

		rootItem = new JMenuItem("Print Root");
		rootItem.setMnemonic('r');
		rootItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, ActionEvent.ALT_MASK));
		rootItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printRoot();
			}
		});
		informationMenu.add(rootItem);

		bootItem = new JMenuItem("Show filesystem");
		bootItem.setMnemonic('S');
		bootItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, ActionEvent.ALT_MASK));
		bootItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printFSInfo();
			}
		});
		informationMenu.add(bootItem);

		readSectorItem = new JMenuItem("Read sectors");
		readSectorItem.setMnemonic('S');
		readSectorItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, ActionEvent.ALT_MASK));
		readSectorItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printSectors();
			}
		});
		informationMenu.add(readSectorItem);

		bpbInfoItem = new JMenuItem("Print BPB-info");
		bpbInfoItem.setMnemonic('I');
		bpbInfoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_9, ActionEvent.ALT_MASK));
		bpbInfoItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				printBPBInfo();
			}
		});
		informationMenu.add(bpbInfoItem);

		operationsBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (selectedDevice != null)
				{
					String selectedItem = (String)operationsBox.getSelectedItem();
					if (selectedItem.equals("createFile"))
					{
						createFile();
					}
					else if (selectedItem.equals("createDir"))
					{
						createDirectory();
					}
					else if (selectedItem.equals("deleteFile"))
					{
						delete();			
					}
					else if (selectedItem.equals("renameFile"))
					{
						rename();
					}
					else if (selectedItem.equals("readFile"))
					{
						readFile();
					}
					else if (selectedItem.equals("writeStuff"))
					{
						writeStuff();
					}
				}
				else
					System.out.println("selectedDeviceName null");
			}
		});
		menuBar.add(operationsBox);
		
		//Build fourth menu in the menu bar.
		changeFsInfoMenu = new JMenu("Change filesystem");
		changeFsInfoMenu.setMnemonic('C');
		changeFsInfoMenu.getAccessibleContext().setAccessibleDescription("Can be used to manipulate the filesystem file.");
		JMenuItem changeFsInfoItem = new JMenuItem("change");
		changeFsInfoItem.setMnemonic('a');
		changeFsInfoItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//getContentPane().add(new BootFileManipulator());
				JFrame frame = new BootFileManipulator();
				frame.setVisible(true);
			}
		});
		changeFsInfoMenu.add(changeFsInfoItem);
		menuBar.add(changeFsInfoMenu);
		
		
		//Build fifth menu in the menu bar.
		helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		helpMenu.getAccessibleContext().setAccessibleDescription("Get some more infos.");
		
		helpMenuItem = new JMenuItem("help");
		helpMenuItem.setMnemonic('e');
		helpMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFrame frame = new HelpFrame();
				frame.setVisible(true);
			}
		});
		helpMenu.add(helpMenuItem);
		
		aboutMenuItem = new JMenuItem("about");
		aboutMenuItem.setMnemonic('a');
		aboutMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JFrame frame = new AboutFrame();
				frame.setVisible(true);
			}
		});
		helpMenu.add(aboutMenuItem);
		menuBar.add(helpMenu);
		
		menuBar.add(javax.swing.Box.createHorizontalGlue());
		menuBar.add(javax.swing.Box.createHorizontalGlue());
		
		//tree and split panes
		
		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		//add the device tree to the panel
		splitPane.setTopComponent(deviceTree);
		
		
		JSplitPane splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane2.setTopComponent(fileTableScrollPane);
		splitPane2.setBottomComponent(outputScrollPane);
		splitPane.setBottomComponent(splitPane2);
		
		splitPane.setDividerLocation(100);
		splitPane.setPreferredSize(new Dimension(500, 300));
		
		//Add the split pane to this frame
		getContentPane().add(splitPane);
		
		//boot file system
		fileSystem = new FileSystem(outDir+masterBootRecordFileName, System.out, dummyFile);

		//add all booted devices to the deviceTree		
		java.util.List devices = fileSystem.getAllDevices();
		if (devices != null)
		{
			for (int i=0; i < devices.size(); i++)
			{
				FATDevice device = ((FileSystem.DeviceInformation)devices.get(i)).getDevice();
				String str = device.getRealDeviceName();
				deviceTree.addObject(new NodeInfo(str));
			}
		
		}
		else
			System.out.println("No devices");
	}	//end constructor

	
	/**
	 * Call to a dialog with the given string as text at the dialog.
	 * @param text to print at the dialog.
	 * @return the result of the dialog.
	 */    
	protected String callDialog(String text)
	{
		CustomDialog customDialog = new CustomDialog(this, text);
		customDialog.pack();
		customDialog.setLocationRelativeTo(this);
		customDialog.setVisible(true);
		return customDialog.getValidatedText();
	}
	
	
	/**
	 * This class represents the about frame.
	 */
	private class AboutFrame extends JFrame
	{
		/**New line seperator*/
		private String newline = System.getProperty("line.separator");
		
		/**About frame*/
		public AboutFrame()
		{
			super("About RawExplorer");
			Container contentPane = getContentPane();
			
			JTextPane textPane = new JTextPane();
			textPane.setEditable(false);
			String[] initString =
			{
				"\tRawExplorer Version 1.0." + newline+newline,		//bold
				"This program was made in the context of an advanced practical training ",//regular
				"at the computer science faculty of the Philipps University of Marburg. Copyright 2002."+newline+newline,//regular
				"Leadership:"+newline,		//bold
				"\tProf. Dr. Bernhard Seeger"+newline,	//italic
				"\tAssistant Martin Schneider."+newline,//italic
				"Programming:"+newline,				//bold
				"\tMarcus Klein (FAT-System)"+newline,	//italic
				"\tHans Schwarzbach (raw access)"	//italic
				
			};
			
			String[] initStyles = 
			{
				"bold",
				"regular",
				"regular",
				"bold",
				"italic",
				"italic",
				"bold",
				"italic",
				"italic"
			};
			
			initStylesForTextPane(textPane);
			
			javax.swing.text.Document doc = textPane.getDocument();
			
			try
			{
				for (int i=0; i < initString.length; i++)
				{
					doc.insertString(doc.getLength(), initString[i],
					textPane.getStyle(initStyles[i]));
				}
			}
			catch (javax.swing.text.BadLocationException ble)
			{
				System.err.println("Couldn't insert initial text.");
			}
			
			contentPane.add(textPane);
			setSize(300,260);
		}
		
		/**Inits the styles for the text panel
		 * @param textPane the panel.*/
		protected void initStylesForTextPane(JTextPane textPane)
		{
			//Initialize some styles
			Style def = StyleContext.getDefaultStyleContext().
			getStyle(StyleContext.DEFAULT_STYLE);
			
			Style regular = textPane.addStyle("regular", def);
			StyleConstants.setFontFamily(def, "SansSerif");
			
			Style s = textPane.addStyle("italic", regular);
			StyleConstants.setItalic(s, true);
			
			s = textPane.addStyle("bold", regular);
			StyleConstants.setBold(s, true);
			
			s = textPane.addStyle("small", regular);
			StyleConstants.setFontSize(s, 10);
			
			s = textPane.addStyle("large", regular);
			StyleConstants.setFontSize(s, 16);
			
		}
	}	//end about frame
	
	
	/**
	 * This class represents the help frame.
	 */
	private class HelpFrame extends JFrame
	{
		/**The name of the help file.*/
		String helpFile = "help.html";
		/**The path of the help file.*/
		String helpPath = "applications" + 
							System.getProperty("file.separator") +
							"release" +
							System.getProperty("file.separator") +
							"io" +
							System.getProperty("file.separator");
		
		/**Creates the help frame*/
		public HelpFrame()
		{
			super("Help");
			Container contentPane = getContentPane();
			JEditorPane editorPane = new JEditorPane();
			editorPane.setEditable(false);
			String s = null;
			try
			{
				s = "file:" +
					System.getProperty("user.dir") +
					System.getProperty("file.separator") +
					helpPath +
					helpFile;
				URL helpURL = new URL(s);
				try
				{
					editorPane.setPage(helpURL);
				}
				catch (IOException e)
				{
					System.err.println("Attempted to read a bad URL: " + helpURL);
				}
			}
			catch (Exception e)
			{
				System.err.println("Couldn't create help URL: " + s);
			}
			
			JScrollPane scrollPane = new JScrollPane(editorPane);			
			contentPane.add(scrollPane);
			setSize(300,260);
		}	//end constructor
	}	//end inner class HelpFrame
	

	/**
	 * This class may be used to manipulate the content of the boot-file.
	 * That is the file which contains informations about the devices
	 * created by the file system.
	 */	
	private class BootFileManipulator extends JFrame
	{
		/**
		 * The name of the file where all bootable devices are listed.
		 */
		String bootFileName = "fsInfo.txt";
		
		/**
		 * Represents a table model.
		 */
		class BootFileTableModel extends AbstractTableModel
		{			
			
			/**
			 * The names of the columns.
			 */
			protected final String[] columnNames = {"Name", "Size", "Type"};
			
			/**
			 * Contains the informations of the rows of the boot-file-table.
			 * Each row is represented by a RowNode which contains the
			 * different informations for one line.
			 */
			protected Vector rowData = new Vector();
			
			/**
			 * Contains the informations to show in the boot-file-table.
			 */
			class RowNode
			{
				/**
				 * The name of the file.
				 */
				String name;
				
				/**
				 * The size of the device.
				 */
				String size;
				
				/**
				 * The type is either "RAF", "RAM", or "NATIVE".
				 */
				String type;
				
				/**
				 * Set a new name.
				 * @param name the new name.
				 */
				void setName(String name)
				{
					this.name = name;
				}
				
		
				/**
				 * Set a new size.
				 * @param size the new size.
				 */
				void setSize(String size)
				{
					this.size = size;
				}
				
				
				/**
				 * Set the type.
				 * @param type the type.
				 */
				void setType(String type)
				{
					this.type = type;
				}
			}
			
			/**
			 * Create an instance of this object.
			 */
			public BootFileTableModel()
			{
			}
			
			
			/**
			 * Return the number of columns.
			 * @return the number of columns.
			 */
			public int getColumnCount()
			{
				return columnNames.length;
			}
			
			
			/**
			 * Return the number of rows.
			 * @return the number of rows.
			 */
			public int getRowCount()
			{
				return rowData.size();
			}
			
			
			/**
			 * Return the column name of column col.
			 * @param col the column number.
			 * @return the column name of colum col.
			 */
			public String getColumnName(int col)
			{
				return columnNames[col];
			}
			
			
			/**
			 * Return the content of the file table at (row, col).
			 * @param row the row.
			 * @param col the column.
			 * @return the content of the file table at (row, col).
			 */
			public Object getValueAt(int row, int col)
			{
				if (row < 0 || row >= rowData.size())
					return null;
				RowNode node = (RowNode)rowData.get(row);
				Object obj;
				switch (col)
				{
					case 0 : {obj = node.name; break;}
					case 1 : {obj = node.size; break;}
					case 2 : {obj = node.type; break;}
					default : {obj = "null";}
				}
				return obj;
			}
			
			
			/**
			 * Return the class at column c.
			 * @param c the column number.
			 * @return the class.
			 */
			public Class getColumnClass(int c)
			{
				return getValueAt(0, c).getClass();
			}
			
			
			/**
			 * Return if the file table cell at (row, col) is editable.
			 * @param row the row.
			 * @param col the column.
			 * @return true if the file table cell[row][col] is editable; otherwise false.
			 */
			public boolean isCellEditable(int row, int col)
			{
				//Note that the data/cell address is constant,
				//no matter where the cell appears onscreen.
				if (col < 2)
					return false;
				else
					return true;
			}
			
			
			/**
			 * Remove all content of the boot-file-table except the column names.
			 */
			public void clear()
			{
				for (int i=0; i < columnNames.length; i++)
					rowData.clear();
			}
		
		
			/**
			 * Add a new row at the end of the file table.
			 * @param line contains the informations that should stored at the new row.
			 */
			public void addRow(String line)
			{
				int row = getRowCount();
				StringTokenizer st = new StringTokenizer(line, "\t");
				for (int i=0; i < columnNames.length; i++)
				{
					String str = "null";
					if (st.hasMoreTokens())
						str = st.nextToken();
					setValueAt(str, row, i);
				}
			}
			
			
			/**
			 * Set the content of cell (row, col) to the given value.
			 * @param value the new content. The value must be a ExtendedFile object.
			 * @param row the row.
			 * @param col the column.
			 */
			public void setValueAt(Object value, int row, int col)
			{	
				RowNode node;
				if (rowData.size() <= row)
					node = new RowNode();
				else
					node = (RowNode)rowData.get(row);
					
				if (col == 0)
					node.setName((String)value);
				else if (col == 1)
					node.setSize((String)value);
				else if (col == 2)
					node.setType((String)value);
				
				if (rowData.size() <= row)
					rowData.add(node);
			}
			
			/**Removes a row from the data
			 * @param rowNumber the number of the row.*/
			public void removeRow(int rowNumber)
			{
				if (rowNumber < 0 || rowNumber >= rowData.size())
					return;
				rowData.remove(rowNumber);
			}
			
		}	//end class BootFileTableModel

		
		/**
		 * Create an instance of this object.
		 */
		public BootFileManipulator()
		{
			super("Boot file manipulator");
			Container contentPane = getContentPane();
			JLabel label = new JLabel("Content of the file "+RawExplorer.masterBootRecordFileName);
			BoxLayout layout = new BoxLayout(contentPane , BoxLayout.Y_AXIS);
			final BootFileTableModel bootFileTableModel = new BootFileTableModel();
			final JTable bootFileTable = new JTable(bootFileTableModel);
			String[] lines = FileSystem.getBootFileContent(outDir+masterBootRecordFileName);
			for (int i=0; i < lines.length; i++)
				bootFileTableModel.addRow(lines[i]);
			
			JButton clearButton = new JButton("clear selected line");
			clearButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					int selectedRow = bootFileTable.getSelectedRow();
					
					String deviceName = (String)bootFileTableModel.getValueAt(selectedRow, 0);
					if (FileSystem.removeLine(deviceName,outDir+masterBootRecordFileName))
						bootFileTableModel.removeRow(selectedRow);
					bootFileTableModel.fireTableDataChanged();
				}
			});
			contentPane.setLayout(layout);
			contentPane.add(label);
			contentPane.add(new JScrollPane(bootFileTable));
			contentPane.add(clearButton);
			setSize(200,300);
		}
	}	//end inner class BootFileManipulator
	
	
	/**
	 * Instances of this class represents nodes of the file tree.
	 */
	private class NodeInfo
	{
		/**
		 * The string shown at the node.
		 */
		public String message;

		/**
		 * A file object which makes it possible to access
		 * informations of the directory represented by this node.
		 */
		ExtendedFile file = null;


		/**
		 * Create an instance of this object.
		 * @param message the text shown at the node.
		 */
		public NodeInfo(String message)
		{
			this.message = message;
		}


		/**
		 * Create an instance of this object.
		 * @param message the text shoen at the node.
		 * @param file the file object stored at this node.
		 */
		public NodeInfo(String message, ExtendedFile file)
		{
			this.message = message;
			this.file = file;
		}


		/**
		 * Set a new file object.
		 * @param file the new file.
		 */
		public void setFile(ExtendedFile file)
		{
			this.file = file;
		}


		/**
		 * Return the file object.
		 * @return the file object.
		 */
		public ExtendedFile getFile()
		{
			return file;
		}


		/**
		 * Remove the file object from the node.
		 */
		public void removeFile()
		{
			file = null;
		}


		/**
		 * Print the message.
		 * @return the message of this node.
		 */
		public String toString()
		{
			return message;
		}
	}	//end inner class NodeInfo


	/**
	 * A dialog for user interaction.
	 */
	class CustomDialog extends JDialog
	{
		/**
		 * The typed text.
		 */
		private String typedText = null;

		/**
		 * The option pane.
		 */
		private JOptionPane optionPane;
		
				
		/**
		 * Create an instance of this object.
		 * @param aFrame the parent frame.
		 * @param message the text that is shown at the dialog.
		 */
		public CustomDialog(Frame aFrame, String message)
		{
			super(aFrame, true);
		
			setTitle("Question!");
		
			final JTextField textField = new JTextField(10);
			Object[] array = new Object[2];
			array[0] = message;
			array[1] = textField;
		
			final String btnString1 = "Enter";
			final String btnString2 = "Cancel";
			Object[] options = {btnString1, btnString2};
		
			optionPane = new JOptionPane(	array,
											JOptionPane.QUESTION_MESSAGE,
											JOptionPane.YES_NO_OPTION,
											null,
											options,
											options[0]);
			setContentPane(optionPane);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent we)
				{
					optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
				}
			});

			textField.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					optionPane.setValue(btnString1);
				}
			});
		
			optionPane.addPropertyChangeListener(new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent e)
				{
					String prop = e.getPropertyName();
		
					if (isVisible() 
						&& (e.getSource() == optionPane)
						&& (prop.equals(JOptionPane.VALUE_PROPERTY) ||
						prop.equals(JOptionPane.INPUT_VALUE_PROPERTY)))
					{
						Object value = optionPane.getValue();
		
						if (value == JOptionPane.UNINITIALIZED_VALUE)
						{
							//ignore reset
							return;
						}
		
						// Reset the JOptionPane's value.
						// If you don't do this, then if the user
						// presses the same button next time, no
						// property change event will be fired.
						optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	
						if (value.equals(btnString1))
						{
							typedText = textField.getText();
							if (!typedText.equals(""))
								setVisible(false);
							else
							{
								JOptionPane.showMessageDialog(
									CustomDialog.this,
									"Please enter a value or name.",
									"Warning.",
									JOptionPane.ERROR_MESSAGE);
								typedText = null;
							}
						}
						else
						{ // user closed dialog or clicked cancel
							setVisible(false);
							typedText = null;
						}
					}
				}
			});
		}	//end constructor
		
		
		/**
		 * Return the typed text.
		 * @return the typed text.
		 */
		public String getValidatedText()
		{
			return typedText;
		}
	}	//end inner class XXDialogXX


	/**
	 * Create a RAF device of the given fat type.
	 * @param fatType the type of FAT for the device.
	 */
	public void createDeviceRAF(byte fatType)
	{		
		String name = callDialog("Please enter the name of the device");
		if (name == null)
			return;
		else if (name.equals(""))
		{	
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		
		long numberOfSectors = 0;
		String lengthString = callDialog("Please enter the number of sectors that\nshould be used for the device");
		if (lengthString == null)
			return;
		else if (lengthString.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		try
		{
			numberOfSectors = (new Long(lengthString)).longValue();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
				
		if (RawAccessUtils.createFileForRaw(name, numberOfSectors))
		{
			RawAccess r = new RAFRawAccess(name);
			createDevice(name, fatType, r);
		}
	}
	
	
	/**
	 * Create a RAM device of the given fat type.
	 * @param fatType the type of fat for the device.
	 */
	public void createDeviceRAM(byte fatType)
	{		
		String name = callDialog("Please enter the name of the device");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		
		long numberOfSectors = 0;
		String lengthString = callDialog("Please enter the number of sectors that\nshould be used for the device");
		if (lengthString == null)
		{
			return;
		}
		else if (lengthString.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		try
		{
			numberOfSectors = (new Long(lengthString)).longValue();
			System.out.println("RawExplorer numberOfSectors "+numberOfSectors);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		
		createDevice(name, fatType, new RAMRawAccess(numberOfSectors));
	}
	
	
	/**
	 * Create a RAF disk device
	 */
	public void createDeviceDiskRAF()
	{
		String name = callDialog("Please enter the name of the device");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		
		if (RawAccessUtils.createFileForRaw(name, DISK_SIZE/512))
			createDevice(name, FAT.FAT12, new RAFRawAccess(name));
		else
			JOptionPane.showMessageDialog(this, "Couldn't create RandomAccessFile.\nDevice will not be created.");
	}
	
	
	/**
	 * Create a native disk device.
	 */
	public void createDeviceDiskNative()
	{
		String name = callDialog("Please enter the name of the device");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		createDevice(name, FAT.FAT12, new NativeRawAccess(name));
	}
	
	
	/**
	 * Create a RAF zip-disk device.
	 */
	public void createDeviceZipDiskRAF()
	{
		String name = callDialog("Please enter the name of the device");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}

		if (RawAccessUtils.createFileForRaw(name, ZIP_DISK_SIZE/512))
			createDevice(name, FAT.FAT16, new RAFRawAccess(name));
		else
			JOptionPane.showMessageDialog(this, "Couldn't create RandomAccessFile. Device will not be created.");
	}
	
	
	/**
	 * Create a native zip-disk device.
	 */
	public void createDeviceZipDiskNative()
	{
		String name = callDialog("Please enter the name of the device");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
		createDevice(name, FAT.FAT16, new NativeRawAccess(name));
	}
	
	
	/**
	 * Create a native device of the given fat type.
	 * @param fatType the type of fat for the device.
	 */
	public void createDeviceNative(byte fatType)
	{		
		String name = callDialog("Please enter the name of the device.\nFor unix: /dev/fd0\nFor win32 \\\\.\\a:");
		if (name == null)
		{
			return;
		}
		else if (name.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be created.");
			return;
		}
				
		createDevice(name, fatType, new NativeRawAccess(name));
	}
	
	
	/**
	 * Create a device of the given naem, fat typeand with the given
	 * rawAccess.
	 * @param name the name of the device.
	 * @param fatType the type of fat for the device.
	 * @param rawAccess the RawAccess object.
	 */
	public void createDevice(String name, byte fatType, RawAccess rawAccess)
	{		
		if (rawAccess instanceof NativeRawAccess)
		{
			int res = JOptionPane.showConfirmDialog(this, "WARNING: You will lose all\n informations from device: "+name, "Important", JOptionPane.YES_NO_OPTION);
			if (res == JOptionPane.NO_OPTION)
				return;
		}
		
		//create the device with the FileSystem Object
		if (fileSystem == null)
			fileSystem = new FileSystem(outDir+masterBootRecordFileName, System.out, dummyFile);
		
		try
		{
			FATDevice device = fileSystem.initialize(name, rawAccess, fatType, FileSystem.FORMAT);
			//insert the device object in the device-tree
			deviceTree.addObject(deviceTree.getRoot(), new NodeInfo(device.getRealDeviceName()));
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, e);
			return;
		}
	}	//end createDevice(String name, byte fatType, RawAccess rawAccess)


	/**
	 * Copy a file from the 'original' file system to this implementated file
	 * system.
	 */
	protected void copyFileToRaw()
	{
		if (selectedDevice == null)
		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			System.out.println("file: "+file.getAbsolutePath());
			if (file.isDirectory())
			{
				String callResult = callDialog("Please enter the destination directory.");
				if (callResult == null)
					return;
				else if (callResult.equals(""))
				{
					JOptionPane.showMessageDialog(this, "Wrong value. No file will be copied.");
					return;
				}
				String destinationFolder = callResult;
				copyDirectoryToRaw(file, destinationFolder);
			}
			else
			{
				String destinationName = actualPath + System.getProperty("file.separator") + file.getName();

				try
				{
					selectedDevice.copyFileToRAW(file, destinationName);
				}
				catch (DirectoryException de)
				{
					JOptionPane.showMessageDialog(this, de);
				}
				catch (FileNotFoundException ffe )
				{
					JOptionPane.showMessageDialog(this, ffe);
				}
				catch (IOException io)
				{
					JOptionPane.showMessageDialog(this, io);
				}
			}
			updateFileTable(deviceTree.getSelectedPath());
		}
	}	//end copyFileToRaw()


	/**Copies a directory from the 'original' file system to this implementated file
	 * system.
	 * @param file the file object.
	 * @param destinationFolder the destination folder.
	 * */
	protected void copyDirectoryToRaw(File file, String destinationFolder)
	{
		try
		{
			File[] dirFiles = file.listFiles();
			for (int i=0; i < dirFiles.length; i++)
			{
				if (dirFiles[i].isDirectory())	//directory
				{
					String newDir = destinationFolder + System.getProperty("file.separator") + dirFiles[i].getName();
					System.out.println("will create directory '"+newDir+"'");
					ExtendedFile ef = selectedDevice.getFile(newDir);
					if (!ef.exists())
					{
						if (!ef.mkdir())
							JOptionPane.showMessageDialog(this, "Couldn't create the directory:\n "+ef.getAbsolutePath());
					}

					copyDirectoryToRaw(dirFiles[i], newDir);
				}
				else	//file
				{
					selectedDevice.copyFileToRAW(
						dirFiles[i],
						destinationFolder + System.getProperty("file.separator") + dirFiles[i].getName()
					);
				}
			}
		}
		catch (DirectoryException de)
		{
			JOptionPane.showMessageDialog(this, de);
		}
		catch (FileNotFoundException ffe )
		{
			JOptionPane.showMessageDialog(this, ffe);
		}
		catch (IOException io)
		{
			JOptionPane.showMessageDialog(this, io);
		}
	}	//end copy DirectoryToRaw


	/**
	 * Copy a file from the implementades file system to the 'original' file
	 * system. A dialog will ask for the name and raw type.
	 */
	public void copyFileFromRaw()
	{
		String sourceName;
		if (actualPath == null || actualPath.equals(""))
		{
			String result = callDialog("Please enter the source name.");
			if (result == null)
				return;
			else if (result.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Wrong value. No file will be copied.");
				return;
			}
			sourceName = result;
		}
		else
			sourceName = actualPath + System.getProperty("file.separator") + actualFileName;
			
		JFileChooser fileChooser = new JFileChooser(actualFileName);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File destinationFile = fileChooser.getSelectedFile();
			
			try
			{
				selectedDevice.copyFileToOriginalFileSystem(sourceName, destinationFile);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, e);
			}
		}
	}	//end copyFileFromRaw


	/**
	 * This method can only be used, when the device formated before.
	 * It will clear the FAT-structure but don't override the bad
	 * cluster marks, it will also reinitialize the root-directory.
	 * After the call of this method, the device should look like
	 * a fresh formated device.
	 */
	protected void fastFormat()
	{
		if (selectedDevice != null)
		{			
			selectedDevice.fastFormat();
			updateFileTable(deviceTree.getSelectedPath());
		}
		else
			JOptionPane.showMessageDialog(this, "No device selected.");
	}


	/**
	 * Create a file. The name is asked by a dialog. The file will be created at the actual
	 * selected device and directory.
	 */
	private void createFile()
	{
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		String result = callDialog("Please enter the name.");
		if (result == null)
		{
			return;
		}
		else if (result.equals(""))
		{
			JOptionPane.showMessageDialog(this, "No name specified. File will not be created.");
			return;
		}
		tmp += result;
		ExtendedFile ef = selectedDevice.getFile(tmp);
		try{
			 ef.createNewFile();
			}
			catch (Exception e) {
				e.printStackTrace();
			} 
		
		updateFileTable(deviceTree.getSelectedPath());
	}	//end createFile()
	
	
	/**
	 * Create a directory. The name will be asked by a dialog. The directory will be
	 * created at the seletced device and directory.
	 */
	public void createDirectory()
	{
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		String result = callDialog("Please enter the name.");
		if (result == null)
		{
			return;
		}
		else if (result.equals(""))
		{
			JOptionPane.showMessageDialog(this, "No name specified. Directory will not be created.");
			return;
		}
		tmp += result;

		ExtendedFile ef = selectedDevice.getFile(tmp);
		if (ef.mkdir())
			JOptionPane.showMessageDialog(this, "Creation of directory was successfull.");
		else
			JOptionPane.showMessageDialog(this, "Creation of directory failed.");
				
		updateFileTable(deviceTree.getSelectedPath());
	}	//end createDirectory
	
	
	/**
	 * Delete the actual selected file or directory. If no file or directory
	 * is selected a dialog will ask for the name.
	 */
	public void delete()
	{
		int row = fileTable.getSelectedRow();
		boolean isDir = fileTableModel.isDirectory(row);
				
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		if (row != -1)
		{
			if (actualFileName != null)
				tmp += actualFileName;
			else
			{
				String result = callDialog("Please enter the name of the file\nthat should be deleted.");
				if (result == null)
				{
					return;
				}
				else if (result.equals(""))
				{
					JOptionPane.showMessageDialog(this, "No name specified. Delete will not be done.");
					return;
				}
				tmp += result;
			}
		}
		
		int res = JOptionPane.showConfirmDialog(this, "WARNING: You will delete '"+tmp+"'.", "Important", JOptionPane.YES_NO_OPTION);
		if (res == JOptionPane.NO_OPTION)
			return;
		
		ExtendedFile ef = selectedDevice.getFile(tmp);
		if (ef.delete())	//delete was successful
		{
			System.out.println("delete was successful.");
			//in case of a directory remove it also from the file tree
			if (isDir)
			{
				deviceTree.removeNode(selectedDevice, tmp);
			}
		}
		else
			System.out.println("delete wasn't successful");
		updateFileTable(deviceTree.getSelectedPath());
	}	//end delete()
	
	
	/**
	 * Rename the actual selected file or directory. If no file or directory
	 * is selected a dialog will ask for the name.
	 */
	public void rename()
	{
		int row = fileTable.getSelectedRow();
		boolean isDir = fileTableModel.isDirectory(row);
				
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		if (row != -1)
		{
			if (actualFileName != null)
				tmp += actualFileName;
			else
			{
				String result = callDialog("Please enter the name of the file\nthat should be renamed.");
				if (result == null)
				{
					return;
				}
				else if (result.equals(""))
				{
					JOptionPane.showMessageDialog(this, "No name specified. Renaming will not be done.");
					return;
				}
				tmp += result;
			}
		}
		
		String destinationName = callDialog("Type the new name (inclusive path).");
		if (destinationName == null)
			return;
		else if (destinationName.equals(""))
		{
			JOptionPane.showMessageDialog(this, "No destination name specified.\nRenaming will not be done.");
			return;
		}
		
		int res = JOptionPane.showConfirmDialog(this, "WARNING: You will rename '"+tmp+"' to\n'"+destinationName+"'.", "Important", JOptionPane.YES_NO_OPTION);
		if (res == JOptionPane.NO_OPTION)
			return;
		
		if (selectedDevice == null)
		{
			JOptionPane.showMessageDialog(this, "Please select a device first.");
			return;
		}
		ExtendedFile ef = selectedDevice.getFile(tmp);
		if (ef.renameTo(selectedDevice.getFile(destinationName)))	//rename was successful
		{
			System.out.println("rename was successful");
			//in case of a directory remove it also from the file tree
			if (isDir)
			{
				deviceTree.removeNode(selectedDevice, tmp);
			}
		}
		else
			System.out.println("rename wasn't successful");
		updateFileTable(deviceTree.getSelectedPath());
	}	//end rename()
	
	
	/**
	 * Boot a device. The name is asked by a dialog.
	 * @return a FATDevice.
	 * @throws Exception
	 */
	public FATDevice bootDevice() throws Exception
	{
		String deviceName = callDialog("Please enter name of device.");
		if (deviceName == null)
		{
			return null;
		}
		else if (deviceName.equals(""))
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be booted.");
			return null;
		}

		String rawType = callDialog("Please enter name raw access type.\n RAF, RAM, or NATIVE.");
		if (rawType == null)
			return null;
		RawAccess ra = null;
		if (rawType.equals("RAM"))
		{
			try
			{
				String result = callDialog("Please enter the number of blocks.");
				if (result == null)
					return null;
				else if (result.equals(""))
				{
					JOptionPane.showMessageDialog(this, "Wrong value. Device will not be booted.");
					return null;
				}
				long numBlocks = (new Long(result)).longValue();
				ra = new RAMRawAccess(numBlocks);
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "Wrong value. Device will not be booted.");
				return null;
			}
		}
		else if (rawType.equals("NATIVE"))
			ra = new NativeRawAccess(deviceName);
		else if (rawType.equals("RAF"))
			ra = new RAFRawAccess(deviceName);
		else
		{
			JOptionPane.showMessageDialog(this, "Wrong value. Device will not be booted.");
			return null;
		}

		if (fileSystem == null)
			fileSystem = new FileSystem(outDir+masterBootRecordFileName, System.out, dummyFile);
		return fileSystem.initialize(deviceName, ra, FAT.UNKNOWN_FAT, FileSystem.BOOT);
	}	//end bootDevice()


	/**
	 * Read the content of the selected file to the text area. If no file is selected
	 * a dialog asks one.
	 */	
	private void readFile()
	{
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		if (actualFileName != null)
			tmp += actualFileName;
		else
		{
			String result = callDialog("Please enter the name of the file\nthat should be read.");
			if (result == null)
			{				
				return;
			}
			else if (result.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Wrong value. File will not be read.");
				return;
			}
			tmp += result;
		}
		try
		{
			ExtendedRandomAccessFile eraf = selectedDevice.getRandomAccessFile(tmp, "rw");
			eraf.seek(0);
			String result = "";
				
			int read;
			byte[] buffer = new byte[512];
			for (;;)
			{
				read = eraf.read(buffer);
				if (read == -1)
					break;
				result += new String(buffer, 0, read);
			}
			
			output.setText(result);
			eraf.close();
		}
		catch (DirectoryException ex)
		{
			System.out.println(ex);
		}
		catch (IOException io)
		{
			System.out.println(io);
		}	//end try
	}	//end readFile()


	/**
	 * Write some characters at the beginning of the selected file.
	 * If no file is selected, a dialog asks a file name.
	 */
	private void writeStuff()
	{
		String tmp = actualPath;
		if (!tmp.endsWith(System.getProperty("file.separator")))
			tmp = tmp + System.getProperty("file.separator");
		if (actualFileName != null)
			tmp += actualFileName;
		else
		{
			String result = callDialog("Please enter the name.");
			if (result == null)
				return;
			else if (result.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Wrong value. Stuff will not be written.");
				return;
			}
			tmp += result;
		}
		
		try
		{
			ExtendedRandomAccessFile eraf = selectedDevice.getRandomAccessFile(tmp, "rw");
			eraf.seek(0);

			String text = "";

			for (int k=0; k < 25; k++)
			{
				for (int i=0; i < 23; i++)
					eraf.write(97+k);
				eraf.write(13);	//cr
				eraf.write(10);	//lf
			}

			output.setText(text);

			eraf.close();

		}
		catch (DirectoryException ex)
		{
			System.out.println(ex);
		}
		catch (IOException io)
		{
			System.out.println(io);
		}	//end try
		updateFileTable(deviceTree.getSelectedPath());
	}	//end writeStuff()


	/**
	 * Print the BPB of the selected device to the text area.
	 */
	private void printBPB()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}
	 	output.setText(printArray(selectedDevice.getBPBSector()));
	}	//end printBPB()


	/**
	 * Print the FSI of the selected device to the text area.
	 */
	private void printFSI()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}
	 	try
	 	{
	 		output.setText(printArray(selectedDevice.getFSI(), selectedDevice.getFSISectorNumber()));
	 	}
	 	catch(WrongFATType e)
	 	{
	 		JOptionPane.showMessageDialog(this, "FSI exists only on FAT32.\nThe selected device is not FAT32.");
			return;
	 	}
	}	//end printFSI()


	/**
	 * Print the FAT of the selected device to the text area. The user can specify
	 * which FAT should be printed. It is not checked, if the user specified FAT
	 * exists.
	 */
	private void printFAT()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}
		String result = callDialog("Please enter which FAT should be read.\n0, 1, ...");
        if (result == null)
        {
        	return;
        }
        else if (result.equals(""))
        {
        	JOptionPane.showMessageDialog(this, "Wrong value.");
        	return;
        }
        try
        {
        	int number = (new Integer(result)).intValue();
        	System.out.println(number);
        	output.setText(printArray(selectedDevice.getFATSectors(number), 512*selectedDevice.getFATSectorNumber(number)));
        }
        catch(Exception e)
        {
        	JOptionPane.showMessageDialog(this, "Wrong value.");
        	return;
        }
	}	//end printFAT()


	/**
	 * Print the root directory of the selected device to the text area.
	 */
	private void printRoot()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}
	 	output.setText(printArray(selectedDevice.getRootDir(), selectedDevice.getRootSectorNumber()*512));
	}	//end printRoot()


	/**
	 * Print the content of the fsInfo.txt file to the text area.
	 */
	private void printFSInfo()
	{
		 String content = "";
		 String[] lines = FileSystem.getBootFileContent(outDir+masterBootRecordFileName);
		 for (int i=0; i < lines.length; i++)
		 	content += lines[i] + "\n";
		 	
		 output.setText(content);
	}	//end printFSInfo()


	/**
	 * Read one or more sectors form device and print them
	 * on the text area.
	 */
	private void printSectors()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}

		long sectorNumber = 0;
		long numSectors = 1;
		try
		{
			String result = callDialog("Please enter the sector number");
			if (result == null)
				return;
			else if (result.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Wrong value.");
				return;
			}
			sectorNumber = (new Long(result)).longValue();
			
			result = callDialog("Please enter the number of sectors to read.");
			if (result == null)
				return;
			else if (result.equals(""))
			{
				JOptionPane.showMessageDialog(this, "Wrong value.");
				return;
			}
			numSectors  = (new Long(result)).longValue();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, "Wrong value.");
			return;
		}

		byte[] data = new byte[(int)numSectors*512];
		for (long i=sectorNumber; i < sectorNumber + numSectors; i++)
		{
			byte[] buffer = new byte[512];
			selectedDevice.readSector(buffer, i);
			System.arraycopy(buffer, 0, data, (int)(i - sectorNumber)*buffer.length, buffer.length);
		}
	 	output.setText(printArray(data, sectorNumber*512));
	}	//end printSectors()


	/**
	 * Print informations about the BPB.
	 */
	private void printBPBInfo()
	{
		if (selectedDevice == null)
   		{
			JOptionPane.showMessageDialog(this, "No device selected.");
			return;
	 	}
	 	output.setText(selectedDevice.getBPBInfo());
	}	//end printBPB()


	/**
	 * Update the file table. All files and directories are listed which belongs
	 * to the last node of treePath.
	 * @param treePath the path of the tree.
	 */
	private void updateFileTable(TreePath treePath)
	{
		if (treePath == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
		if (node == null)
			return;
		NodeInfo nodeInfo = (NodeInfo)node.getUserObject();
		Object[] elements = treePath.getPath();
		String path = "";
		String deviceNameTmp;
		if (elements != null && elements.length > 1)
		{
			deviceNameTmp = ((DefaultMutableTreeNode)elements[1]).getUserObject().toString();	//deviceName
			if (FileSystem.isUnixDeviceName(deviceNameTmp))
				path = deviceNameTmp;
			else
				path = deviceNameTmp + ":";
		}
		else
			return;

		for (int i=2; i < elements.length; i++)
			path += System.getProperty("file.separator") + elements[i];

		if (elements.length == 2)	//add "\\" in case the path consist of a device name only
			path += System.getProperty("file.separator");

		actualPath = StringOperations.removeDeviceName(path);

		FATDevice device = null;
		try
		{
			device = fileSystem.getDevice(deviceNameTmp);
			selectedDevice = device;
		}
		catch(Exception ex)
		{
			System.out.println(ex);
			return;
		}

		if (device != null)
		{
			ExtendedFile file = device.getFile(actualPath);
			nodeInfo.setFile(file);
			ExtendedFile[] files = file.listFiles();
			if (files == null)
				return;
			
			//clear all informations from the file table
			fileTableModel.clear();
					
			//insert the informations of all files of the actual directory to the file table	
			for (int i=0; i < files.length; i++)
			{
				if (files[i].isDirectory())
				{
					//now add the new children
					deviceTree.addObject(new NodeInfo(files[i].getName(), files[i]));
				}
		
				fileTableModel.addRow(files[i]);
			}
			fileTableModel.fireTableDataChanged();
			fileTableScrollPane.repaint();
		}
		else
			System.out.println("device is null");
	}	//end updateFileTable
	
	
	/**
	 * Set ExtendedFile objects to all child nodes at the last treePath node.
	 * @param treePath the path of the tree.
	 */
	private void setExtendedFile(TreePath treePath)
	{
		if (treePath == null)
			return;
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treePath.getLastPathComponent();
						
		Object[] pathElements = treePath.getPath();
		
		//add the ExtendedFile objects to the nodes
		java.util.Enumeration children = node.children();
		while (children.hasMoreElements())
		{
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
			
			//get the device
			String deviceNameTmp;
			FATDevice device = null;
			try
			{
				if (pathElements.length > 1)
				{
					deviceNameTmp = ((DefaultMutableTreeNode)pathElements[1]).getUserObject().toString();	//deviceName
				}
				else
					deviceNameTmp = child.getUserObject().toString();	//deviceName
				device = fileSystem.getDevice(deviceNameTmp);
			}
			catch(Exception ex)
			{
				System.out.println(ex);
				return;
			}
			
			//get the path
			String path = "";
			if (FileSystem.isUnixDeviceName(deviceNameTmp))
				path += deviceNameTmp;
			else
				path += deviceNameTmp + ":";
				
			for (int i=2; i < pathElements.length; i++)
				path += System.getProperty("file.separator") + pathElements[i];
			
			//set the ExtendedFile objects to the NodeInfo object stored at this node
			((NodeInfo)child.getUserObject()).setFile(device.getFile(path));
		}	//end while
	}	//end setExtendedFile(TreePath treePath)


	/**
	 * Contains all possible chars of a hex number.
	 */
	public static char table[] = new char[]{'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	/**
	 * Print the array as a chain of hex numbers the start line number will be zero.
	 * @param arr the array.
	 * @return the array as a String.
	 */
	public static String printArray(byte[] arr)
	{
		return printArray(arr, 0);
	}	//end printArray(arr)
	
	
	/**
	 * Print the given byte array as a chain of hex numbers. You can
	 * specify a line number so that the chain is numbered.
	 * @param arr the array.
	 * @param lineNum a line number.
	 * @return the array as a String.
	 */
	public static String printArray(byte[] arr, long lineNum)
	{
		String res = "";
		for (int i=0; i <= arr.length/16; i++)
		{
			res += printHex(lineNum);
			res += "h: ";
			for(int j=0; j < 16; j++)
			{
				if (i*16+j >= arr.length)
					return res;
					
				res += printHex(arr[i*16 + j]);
				res += " ";
			}
			
			lineNum += 16;
			
			res += "\n";
		}
		return res;
	}	//end printArray(arr, lineNum)
	
	
	/**
	 * Print the given byte as a hex number.
	 * @param b the byte.
	 * @return the byte as a String. 
	 */
	public static String printHex(byte b)
	{
		String res = "";
		res += table[((b >> 4) & 0x0F)];
		res += table[(b & 0x0F)];
		return res;
	}	//end printHex(byte)


	/**
	 * Print the given long as a hex number.
	 * @param l the long.
	 * @return the long as a String. 
	 */
	public static String printHex(long l)
	{
		String res = "";
		res += table[(int)((l >> 32) & 0x000000000000000F)];
		res += table[(int)((l >> 28) & 0x000000000000000F)];
		res += table[(int)((l >> 24) & 0x000000000000000F)];
		res += table[(int)((l >> 20) & 0x000000000000000F)];
		res += table[(int)((l >> 16) & 0x000000000000000F)];
		res += table[(int)((l >> 12) & 0x000000000000000F)];
		res += table[(int)((l >> 8) & 0x000000000000000F)];
		res += table[(int)((l >> 4) & 0x000000000000000F)];
		res += table[(int)(l & 0x000000000000000F)];
		return res;
	}	//end printHex(long)    


	/**The main method
     * @param args the arguments */
    public static void main(String[] args)
    {
		if (XXLSystem.calledFromMainMaker()) {
			System.out.println("RawExplorer: This class is not started from MainMaker.");
			return;
		}
		RawExplorer window = new RawExplorer();
		
		window.setTitle("RawExplorer");
		window.setSize(800, 600);
		window.setVisible(true);
	}	//end main
    
    
	/**
	 * This class represents the tree of active devices that is used by class RawExplorer.
	 */
	protected class DynamicTree extends JPanel
	{
		/**
		 * The root node.
		 */
		protected DefaultMutableTreeNode rootNode;

		/**
		 * The tree model of the tree.
		 */
		protected DefaultTreeModel treeModel;

		/**
		 * The tree himself.
		 */
		protected JTree tree;


		/**
		 * Create an instance of this object.
		 */
		public DynamicTree()
		{
			rootNode = new DefaultMutableTreeNode(new NodeInfo("active devices"));
			treeModel = new DefaultTreeModel(rootNode);

			tree = new JTree(treeModel);
			tree.setEditable(true);
			tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.setShowsRootHandles(true);

			tree.addTreeSelectionListener(new TreeSelectionListener()
			{
				public void valueChanged(TreeSelectionEvent e)
				{
					updateFileTable(e.getPath());
				}
			});

			tree.addTreeExpansionListener(new TreeExpansionListener()
			{
				//in case of tree expansion, all new visible nodes must be initialized with the NodeInfo class
				public void treeExpanded(TreeExpansionEvent e)
				{
					TreePath treePath = e.getPath();
					setExtendedFile(treePath);
				}		//end treeExpanded

				//in case of tree collaps all now no longer visible nodes must be cleared of NodeInfo
				public void treeCollapsed(TreeExpansionEvent e)
				{
					TreePath path = e.getPath();
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
					java.util.Enumeration enumeration = node.children();
					while(enumeration.hasMoreElements())
					{
						NodeInfo nodeInfo = (NodeInfo)((DefaultMutableTreeNode)enumeration.nextElement()).getUserObject();
						nodeInfo.removeFile();
					}
				}	//end treeCollapsed
			});

			JScrollPane scrollPane = new JScrollPane(tree);
			setLayout(new GridLayout(1,0));
			add(scrollPane);
		}


		/**
		 * Remove all nodes except the root node.
		 */
		public void clear()
		{
			rootNode.removeAllChildren();
			treeModel.reload();
		}


		/**
		 * Return the root node.
		 * @return the root node.
		 */
		protected DefaultMutableTreeNode getRoot()
		{
			return rootNode;
		}

		
		/**
		 * Remove the node given by device and path from
		 * the tree.
		 * @param device the device.
		 * @param path the path from the tree
		 */
		public void removeNode(FATDevice device, String path)
		{
			Enumeration enumeration = rootNode.children();
			DefaultMutableTreeNode deviceNode = null;
			boolean found = false;
			while (enumeration.hasMoreElements())
			{
				deviceNode = (DefaultMutableTreeNode)enumeration.nextElement();
				if (((NodeInfo)deviceNode.getUserObject()).message.equals(device.getRealDeviceName()))
				{
					found = true;
					break;
				}
			}
			if (found)
			{
				removeNode(deviceNode, StringOperations.removeDeviceName(path));
			}
		}	//end removeNode(FATDevice device, String path)



		/**
		 * Remove the node given by path from the given node or
		 * of a child of it.
		 * @param node the node where the search starts.
		 * @param path the way through the tree. The last path
		 * component represents the node that should be removed.
		 * @return true if the node could be removed; false otherwise.
		 */
		private boolean removeNode(DefaultMutableTreeNode node, String path)
		{
			StringTokenizer st = new StringTokenizer(path, System.getProperty("file.separator"));
			String name;
			if (st.hasMoreTokens())
				name = st.nextToken();
			else
				return false;

			Enumeration enumeration = node.children();
			while (enumeration.hasMoreElements())
			{
				DefaultMutableTreeNode tmpNode = (DefaultMutableTreeNode)enumeration.nextElement();
				if (((NodeInfo)tmpNode.getUserObject()).message.equals(name))	//we found the searched node
				{
					if (!st.hasMoreTokens())	//we reached the one and only node
					{
						treeModel.removeNodeFromParent(tmpNode);
						return true;
					}
					else if (removeNode(tmpNode, path.substring(name.length())))
						return true;
				}
			}

			return false;
		}


		/**
		 * Remove the currently selected node.
		 */
		public void removeCurrentNode()
		{
			TreePath currentSelection = tree.getSelectionPath();
			if (currentSelection != null)
			{
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)(currentSelection.getLastPathComponent());
				MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
				if (parent != null)
				{
					treeModel.removeNodeFromParent(currentNode);
					return;
				}
			}
		}


		/**
		 * Add child to the currently selected node.
		 * @param child the child.
		 * @return the DefaultMutableTreeNode
		 */
		public DefaultMutableTreeNode addObject(Object child)
		{
			DefaultMutableTreeNode parentNode = null;
			TreePath parentPath = tree.getSelectionPath();

			if (parentPath == null)
			{
				parentNode = rootNode;
			}
			else
			{
				parentNode = (DefaultMutableTreeNode)(parentPath.getLastPathComponent());
			}

			return addObject(parentNode, child, true);
		}


		/**
		 * Add the child to the parent node.
		 * @param parent the parent node.
		 * @param child the child.
		 * @return the DefaultMutableTreeNode
		 */
		public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child)
		{
			return addObject(parent, child, false);
		}


		/**
		 * Add the child to the parent and make it visible depending on shouldBeVisible.
		 * @param parent the parent node.
		 * @param child the child.
		 * @param shouldBeVisible if true then it visible. 
		 * @return the DefaultMutableTreeNode
		 */
		public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible)
		{
			for (int i=0; i < parent.getChildCount(); i++)
			{
				if (((NodeInfo)((DefaultMutableTreeNode)parent.getChildAt(i)).getUserObject()).toString().equals(((NodeInfo)child).toString()))
					return null;
			}

			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

			if (parent == null)
			{
				parent = rootNode;
			}
			treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

			// Make sure the user can see the lovely new node.
			if (shouldBeVisible)
			{
				tree.scrollPathToVisible(new TreePath(childNode.getPath()));

				//initialize the ExtendedFile objects
				setExtendedFile(new TreePath(parent.getPath()));
			}
			return childNode;
		}


		/**
		 * The listener of the tree.
		 */
		class MyTreeModelListener implements TreeModelListener
		{
			/**
			 * Is called if the tree nodes changed.
			 * @param e the tree node.
			 */
			public void treeNodesChanged(TreeModelEvent e)
			{
				DefaultMutableTreeNode node;
				node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

				/*
				 * If the event lists children, then the changed
		    	 * node is the child of the node we've already
				 * gotten.  Otherwise, the changed node and the
				 * specified node are the same.
				 */
				try
				{
					int index = e.getChildIndices()[0];
					node = (DefaultMutableTreeNode)(node.getChildAt(index));
				}
				catch (NullPointerException exc)
				{
				}
			}

			/**
			 * Is calles if tree nodes are inserted.
			 * @param e the tree node.
			 */
			public void treeNodesInserted(TreeModelEvent e)
			{
			}


			/**
			 * Is calles if tree nodes are removed.
			 * @param e the tree node.
			 */
			public void treeNodesRemoved(TreeModelEvent e)
			{
			}


			/**
			 * Is called if the structure of the tree has changed.
			 * @param e the tree node.
			 */
			public void treeStructureChanged(TreeModelEvent e)
			{
			}
		}	//end inner class


		/**
		 * Return the selected path.
		 * @return the selected path.
		 */
		public TreePath getSelectedPath()
		{
			return tree.getSelectionPath();
		}


		/**
		 * Scroll the path to visible.
		 * @param node the node, which path should be scroll to visible. 
		 */
		public void scrollPathToVisible(DefaultMutableTreeNode node)
		{
			tree.scrollPathToVisible(new TreePath(node.getPath()));
		}


		/**
		 * Removes all children from the selected path.
		 */
		public void removeAllChildren()
		{
			TreePath treePath = tree.getSelectionPath();
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)treePath.getLastPathComponent();
			java.util.Enumeration enumeration = selectedNode.children();
			while (enumeration.hasMoreElements())
			{
				DefaultMutableTreeNode n = (DefaultMutableTreeNode)enumeration.nextElement();
				treeModel.removeNodeFromParent(n);	
			}
			
		}
	}	//end class DynamicTree

}	//end class RawExplorer


/**
 * This class is the table model of the file table. It is used to manage the content of
 * the file table.
 */
class FileTableModel extends AbstractTableModel
{

	/**
	 * Instance of RawExplorer
	 */	
	private RawExplorer RawExplorer;
	
	/**
	 * The names of the columns.
	 */
	protected final String[] columnNames = {"Name", "Size", "Type", "Last Modificated", "Created"};
	
	/**
	 * Contains the informations of the rows of the file table.
	 * Each row is represented by a RowNode which contains the
	 * different informations of a file.
	 */
	protected Vector rowData = new Vector();
	
	/**
	 * Contains the informations to show in the file table for a file.
	 */
	class RowNode
	{
		/**
		 * The name of the file or directory.
		 */
		String name;
		
		/**
		 * The size in bytes of the file or directory.
		 */
		long size;
		
		/**
		 * The type is either "DIR", "FILE", "ROOT_DIR", or "UNKNOWN".
		 */
		String type;
		
		/**
		 * The date of the last modification.
		 */
		DirectoryDate lastModificationDate;
		
		/**
		 * The time of the last modification.
		 */
		DirectoryTime lastModificationTime;
		
		/**
		 * The creation date.
		 */
		DirectoryDate creationDate;
		
		/**
		 * The creation time.
		 */
		DirectoryTime creationTime;
		
		/**
		 * Set a new name.
		 * @param name the new name.
		 */
		void setName(String name)
		{
			this.name = name;
		}
		

		/**
		 * Set a new size.
		 * @param size the new size.
		 */
		void setSize(long size)
		{
			this.size = size;
		}
		
		
		/**
		 * Set the type.
		 * @param attribute indicates the type.
		 */
		void setType(byte attribute)
		{
			if (((attribute & DIR.ATTR_LONG_NAME_MASK) != DIR.ATTR_LONG_NAME) &&
				(attribute & (DIR.ATTR_DIRECTORY | DIR.ATTR_VOLUME_ID)) == DIR.ATTR_VOLUME_ID)
				type = "<ROOT-DIR>";
			else if (((attribute & DIR.ATTR_LONG_NAME_MASK) != DIR.ATTR_LONG_NAME) &&
					(attribute & (DIR.ATTR_DIRECTORY | DIR.ATTR_VOLUME_ID)) == DIR.ATTR_DIRECTORY)
				type = "<DIR>";
			else if (((attribute & DIR.ATTR_LONG_NAME_MASK) != DIR.ATTR_LONG_NAME) &&
					(attribute & (DIR.ATTR_DIRECTORY | DIR.ATTR_VOLUME_ID)) == 0x00)
				type = "FILE";
			else
				type = "UNKNOWN";
		}
		
		
		/**
		 * Set the last modification date and time.
		 * @param modDate the new last modification date.
		 * @param modTime the new last modification time.
		 */
		void setModification(DirectoryDate modDate, DirectoryTime modTime)
		{
			lastModificationDate = modDate;
			lastModificationTime = modTime;
		}
		
		
		/**
		 * Set the new creation time.
		 * @param creDate the new creation date.
		 * @param creTime the new creation time.
		 */
		void setCreation(DirectoryDate creDate, DirectoryTime creTime)
		{
			creationDate = creDate;
			creationTime = creTime;
		}
	}
	
	
	/**
	 * Create an instance of this object.
	 * @param fst the RawExplorer.
	 */
	public FileTableModel(RawExplorer fst)
	{
		this.RawExplorer = fst;
	}
	
	
	/**
	 * Return the number of columns.
	 * @return the number of columns.
	 */
	public int getColumnCount()
	{
		return columnNames.length;
	}
	
	
	/**
	 * Return the number of rows.
	 * @return the number of rows.
	 */
	public int getRowCount()
	{
		return rowData.size();
	}
	
	
	/**
	 * Return the column name of column col.
	 * @param col the column number.
	 * @return the column name of colum col.
	 */
	public String getColumnName(int col)
	{
		return columnNames[col];
	}
	
	
	/**
	 * Return the content of the file table at (row, col).
	 * @param row the row.
	 * @param col the column.
	 * @return the content of the file table at (row, col).
	 */
	public Object getValueAt(int row, int col)
	{
		RowNode node = (RowNode)rowData.get(row);
		Object obj;
		switch (col)
		{
			case 0 : {obj = node.name; break;}
			case 1 : {obj = new Long(node.size); break;}
			case 2 : {obj = node.type; break;}
			case 3 : {obj = node.lastModificationDate.toString() + ", " + node.lastModificationTime.toString(); break;}
			case 4 : {obj = node.creationDate.toString() + ", " + node.creationTime.toString(); break;}
			default : {obj = "null";}
		}
		return obj;
	}
	
	
	/**
	 * Return the class at column c.
	 * @param c the column number.
	 * @return the class.
	 */
	public Class getColumnClass(int c)
	{
		return getValueAt(0, c).getClass();
	}
	
	
	/**
	 * Return if the file table cell at (row, col) is editable.
	 * @param row the row.
	 * @param col the column.
	 * @return true if the file table cell[row][col] is editable; otherwise false.
	 */
	public boolean isCellEditable(int row, int col)
	{
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		if (col < 2)
			return false;
		else
			return true;
	}
	
	
	/**
	 * Remove all contnet of the file table except the column names.
	 */
	public void clear()
	{
		for (int i=0; i < columnNames.length; i++)
			rowData.clear();
	}


	/**
	 * Add a new row at the end of the file table.
	 * @param file contains the informations that should stored at the new row.
	 */
	public void addRow(ExtendedFile file)
	{
		int row = getRowCount();
		for (int i=0; i < columnNames.length; i++)
			setValueAt(file, row, i);
	}
	
	
	/**
	 * Set the content of cell (row, col) to the given value.
	 * @param value the new content. The value must be a ExtendedFile object.
	 * @param row the row.
	 * @param col the column.
	 */
	public void setValueAt(Object value, int row, int col)
	{	
		ExtendedFile file = null;
		try
		{
			file = (ExtendedFile)value;
		}
		catch (ClassCastException e)
		{
			JOptionPane.showMessageDialog(RawExplorer, e);
			return;
		}
		
		RowNode node;
		if (rowData.size() <= row)
			node = new RowNode();
		else
			node = (RowNode)rowData.get(row);
			
		if (col == 0)
			node.setName(file.getName());
		else if (col == 1)
			node.setSize(file.length());
		else if (col == 2)
			node.setType(file.getAttribute());
		else if (col == 3)
			node.setModification(file.getLastModifiedDate(), file.getLastModifiedTime());
		else if (col == 4)
			node.setCreation(file.getCreationDate(), file.getCreationTime());
		
		if (rowData.size() <= row)
			rowData.add(node);
	}
	
	
	/**
	 * Return true if the file stored at row number 'row'
	 * is of type directory; false otherwise.
	 * @param row the row number.
	 * @return true if the file stored at row number 'row'
	 * is of type directory; false otherwise.
	 */
	public boolean isDirectory(int row)
	{
		if (row > rowData.size() || row < 0)
			return false;
		return ((String)getValueAt(row, 2)).equals("<DIR>");
	}
	
}	//end class FileTableModel
