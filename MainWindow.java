package uk.ac.liv.iib.pgb.neo;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import java.util.*;


// A Formatted JFileChooser
class NewFileChooser extends JFileChooser
{
	private static final long serialVersionUID = -6120691117434635908L;

	public NewFileChooser( )
	{
		super();

		JTextArea help = new JTextArea(6,14);
		help.setLineWrap(true);
		help.setWrapStyleWord(true);
		help.setText("You should load a Fasta file format database"); 
		//		setAccessory(help);

	}

}


public class MainWindow extends JFrame  
{
	final boolean TRYPTIC_OUTPUT = false;
	private static final long serialVersionUID = 7722552335555737726L;
	static File settingsFile = new File(".settings.txt");

	private void writePair(String key, String value, BufferedWriter wr ) throws IOException
	{
		//		String output ="\""+key+"\",\""+value+"\"\n";
		//		wr.write(output);
	}


//Saves all settings to properties file
// These are automatically reloaded when program restarts 
	public void saveSettings()
	{
		try {
			Properties prop = new Properties();


			// set the properties value
			prop.setProperty("database", databaseName.getText());

			BufferedWriter wr = new BufferedWriter(new FileWriter(settingsFile));


			wr.write(databaseName.getText()+"\n");
			String column = ""+setColumn+"\n";
			if (setColumn == 0)
				column = autoDetect+"\n";
			writePair("column",column,wr);

			wr.write(column);
			wr.write(setDelim+"\n");
			writePair("delimiter",column,wr);

			writePair("inputFile",inputName.getText(),wr);
			writePair("outputFile",outputName.getText(),wr);
			writePair("processedOutputFile",outputTrypticName.getText(),wr);
			writePair("searchoption",""+Options.DBSearch,wr);


			wr.write(inputName.getText()+"\n");
			wr.write(outputName.getText()+"\n");
			wr.write(outputTrypticName.getText()+"\n");
			wr.write(""+Options.DBSearch+"\n");
			wr.write(""+Options.removeUnused+"\n");
			wr.write(Options.getSearch()+"\n");
			wr.write(""+Options.sameFolder+"\n");
			wr.write(""+Options.sameDriveLetter+"\n");
			
			
			wr.write(""+Options.matchingPeptides+"\n");
			wr.write(""+Options.getMIN()+"\n");
			wr.write(""+Options.getFDR()+"\n");
			
			wr.write(""+Options.useLogRatio+"\n");
			
			wr.close();

		} catch (IOException e) 
		{
			e.printStackTrace();
		}


	}

	public void loadSettings()
	{
		if (hide)
			System.err.println("Loading "+settingsFile);
		Scanner s;
		try {
			s = new Scanner(settingsFile);


			if (s.hasNext())
			{
				String fileName = s.nextLine();
				databaseFile  = new File(fileName);
				databaseName.setText(fileName);

				if (hide)
					System.err.println("Database = "+fileName);

				fcDB.setCurrentDirectory(databaseFile);
			}
			if (s.hasNext())
			{
				String popColumn = s.nextLine();
				setColumnCB.setSelectedItem(popColumn);
				//for command-line mode
				if (popColumn.equals(autoDetect))
				{
					setColumn = 0;
				}
				else
					setColumn = Integer.parseInt(popColumn);


			}
			if (s.hasNext())
			{
				String popColumn = s.nextLine();
				setDelimCB.setSelectedItem(popColumn);
				setDelim = popColumn; 
			}

			if (s.hasNext())
			{
				String fileName = s.nextLine();
				inputFile  = new File(fileName);
				inputName.setText(fileName);
				fcInput.setCurrentDirectory(inputFile);

				if (hide)
					System.err.println("Input = "+fileName);


			}

			if (s.hasNext())
			{
				String fileName = s.nextLine();
				outputFile  = new File(fileName);
				outputName.setText(fileName);
				fcOutput.setCurrentDirectory(outputFile);
				if (hide)
					System.err.println("Output = "+fileName);

			}

			if (s.hasNext())
			{
				String fileName = s.nextLine();
				outputTrypticFile  = new File(fileName);
				outputTrypticName.setText(fileName);
				fcOutputTryptic.setCurrentDirectory(outputFile);
				if (hide)
					System.err.println("Tryptic Output = "+fileName);

			}

			if (s.hasNext())
			{
				String searchDisabled = s.nextLine();
				if (searchDisabled.trim().toLowerCase().equals("true"))
				{
					optionPanel.setSearch(true);
				}

			}
			if (s.hasNext())
			{
				String searchDisabled = s.nextLine();
				if (searchDisabled.trim().toLowerCase().equals("true"))
				{
					optionPanel.setUnused(true);
				}

			}
			if (s.hasNext())
			{
				String setSearchString = s.nextLine();
				Options.setSearchString(setSearchString);


			}

			if (s.hasNext())
			{
				String searchSame = s.nextLine();
				if (searchSame.trim().toLowerCase().equals("true"))
				{
					optionPanel.setSameFolder(true);
				}
				else
					optionPanel.setSameFolder(false);


			}


			if (s.hasNext())
			{
				String searchSame = s.nextLine();
				if (searchSame.trim().toLowerCase().equals("true"))
				{
					optionPanel.setSameDriveLetter(true);
				}
				else
					optionPanel.setSameDriveLetter(false);


			}

			
			
			
			if (s.hasNext())
			{
				String searchSame = s.nextLine();
				if (searchSame.trim().toLowerCase().equals("true"))
				{
					optionPanel.setMatching(true);
				}
				else
					optionPanel.setMatching(false);


			}
			if (s.hasNext())
			{
				String value = s.nextLine();
				Options.setMIN(value);
				 
			}
			
			if (s.hasNext())
			{
				String value = s.nextLine();
				Options.setFDR(value);
				 
			}

		

			if (s.hasNext())
			{
				String searchSame = s.nextLine();
				if (searchSame.trim().toLowerCase().equals("true"))
				{
					optionPanel.setLogRatio(true);
				}
				else
					optionPanel.setLogRatio(false);


			}
			
			
			if (Options.sameDriveLetter)
			{
				remapFiles();
			}

			appendStatus("Settings loaded\n");
			s.close();

		} catch (FileNotFoundException e) {
			appendStatus("Settings file not found, a new file will be created.\n");

		}
	}


	// Drive letters may change between uses
	// common when using portable hard disks
	void remapFiles()
	{

		File f= new File(".");
		String path = f.getAbsolutePath();
		
		if (path.length() < 2)
			return;
		if (path.charAt(1) != ':')
			return;


		String input = inputName.getText();

		if ( (input.length() > 1) &&  (input.charAt(1) == ':') )
		{
		 
			
			String newpath = path.substring(0,1) + input.substring(1);
			inputFile  = new File(newpath);
			inputName.setText(newpath);
			fcInput.setCurrentDirectory(inputFile);
		}

 
		input = databaseName.getText();

		if ( (input.length() > 1) &&  (input.charAt(1) == ':') )
		{
			String newpath = path.substring(0,1) + input.substring(1);
			databaseFile  = new File(newpath);
			databaseName.setText(newpath);

			fcDB.setCurrentDirectory(databaseFile);

		}
		
		input = outputTrypticName.getText();

		if ( (input.length() > 1) &&  (input.charAt(1) == ':') )
		{
			String newpath = path.substring(0,1) + input.substring(1);
			outputTrypticFile  = new File(newpath);
			outputTrypticName.setText(newpath);
			fcOutputTryptic.setCurrentDirectory(outputFile);
			}
		
		input = outputName.getText();

		if ( (input.length() > 1) &&  (input.charAt(1) == ':') )
		{
			String newpath = path.substring(0,1) + input.substring(1);
			outputFile  = new File(newpath);
			outputName.setText(newpath);
			fcOutput.setCurrentDirectory(outputFile);
		}
		
	}
	
	
	public MainWindow()
	{
		fcDB = new NewFileChooser();
		fcInput = new NewFileChooser();
		fcOutput = new NewFileChooser();
		fcOutputTryptic = new NewFileChooser();

		buildGUI();
		pack();

		if (hide)
		{
			startProcess();
			System.exit(0);
		}
		else
		{
			setVisible(true);
		}

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	static JFileChooser fcDB ; //= new NewFileChooser();
	static JFileChooser fcInput ; //= new NewFileChooser();
	static JFileChooser fcOutput ; //= new NewFileChooser();
	static JFileChooser fcOutputTryptic ; //= new NewFileChooser();
	public void changeDatabase()
	{
		databaseFile = changeFile(fcDB,databaseName);
	}


	public void changeInput()
	{
		inputFile = changeFile(fcInput,inputName);
		if (inputFile == null)
			return;

		String fname = inputFile.getPath().replaceAll(".csv","");

		if ((outputFile == null) || (Options.sameFolder) )
		{
			outputFile = new File(fname+"_with_filter.csv");
			outputName.setText(outputFile.getPath());	
		}

		if ((outputTrypticFile == null) || (Options.sameFolder) )
		{

			outputTrypticFile = new File(fname+"_processed.csv");
			outputTrypticName.setText(outputTrypticFile.getPath());	
		}


	}

	public void changeOutput()
	{
		fcOutput.setPreferredSize(new Dimension(getWidth()*7/8,getHeight()*4/5) );

		if (fcOutput.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) 
		{
			outputFile = fcOutput.getSelectedFile();
			if (inputFile != null)
				if (inputFile.getPath().toLowerCase().endsWith(".csv"))
					if (!outputFile.getPath().toLowerCase().endsWith(".csv"))
					{
						outputFile = new File(outputFile.getPath()+".csv");

					}

			outputName.setText(outputFile.getPath());
		}


	}

	public void changeOutputTryptic()
	{
		fcOutputTryptic.setPreferredSize(new Dimension(getWidth()*7/8,getHeight()*4/5) );

		if (fcOutputTryptic.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) 
		{
			outputTrypticFile = fcOutputTryptic.getSelectedFile();

			if (inputFile != null)
				if (inputFile.getPath().toLowerCase().endsWith(".csv"))
					if (!outputTrypticFile.getPath().toLowerCase().endsWith(".csv"))
					{
						outputTrypticFile = new File(outputTrypticFile.getPath()+".csv");

					}

			outputTrypticName.setText(outputTrypticFile.getPath());
		}


	}




	public File changeFile(JFileChooser fc, JTextField fname)
	{
		fc.setPreferredSize(new Dimension(getWidth()*7/8,getHeight()*4/5) );
		int returnVal = fc.showOpenDialog(this);

		File newFile = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) 
		{
			newFile =  fc.getSelectedFile();
		}

		if (newFile == null)
			return null;


		fname.setText(newFile.getPath());
		return newFile;
	}




	String autoDetect = "Auto-Detect";
	static int setColumn = 0;
	File databaseFile;
	JTextField databaseName = new JTextField();


	File inputFile;
	JTextField inputName = new JTextField();

	File outputFile;
	JTextField outputName = new JTextField();

	File outputTrypticFile;
	JTextField outputTrypticName = new JTextField();


	JComboBox<String> setColumnCB;
	JComboBox<String> setDelimCB;
	String setDelim="";
	String[] headingsDelim = {autoDetect, "Tab", "Comma"};


	private JPanel makeTopPanel()
	{
		JPanel top = new JPanel();
		top.setBorder(new TitledBorder("Settings"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL ;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.ipady = 10;


		top.setLayout(new GridBagLayout());
		top.add(new JLabel("Database File"),c);
		c.gridx = 1;
		c.weightx = 4;

		databaseName.setToolTipText("<html>A fasta file to search against for tryptic peptides.</html>");

		databaseName.setEditable(false);
		top.add(databaseName,c);
		c.weightx = 1;

		c.gridx=2;
		JButton changeDB = new JButton("Change Database");
		changeDB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						changeDatabase();
						checkReady();

					}
				}
				);
		top.add(changeDB,c);
		c.gridy++;
		c.gridx = 0;
		top.add(new JLabel("Peptide Column"),c);
		c.gridx++;
		int maxColumn = 99;
		String[] headings = new String[maxColumn+1];
		headings[0]=autoDetect;
		for (int i=1; i <= maxColumn; i++)
			headings[i] =""+i;

		setColumnCB = new JComboBox<String>(headings);
		top.add(setColumnCB,c);
		setColumnCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
	
						JComboBox<String> cb = (JComboBox<String>)e.getSource();
						String name = (String)cb.getSelectedItem();
						if (name.equals(autoDetect))
						{
							setColumn = 0;

						}
						else
							setColumn = Integer.parseInt(name);
					}
				}
				);


		c.gridy++;
		c.gridx = 0;
		top.add(new JLabel("Delimiter"),c);
		c.gridx++;

		setDelimCB = new JComboBox<String>(headingsDelim);
		top.add(setDelimCB,c);
		setDelimCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						@SuppressWarnings("unchecked")
						JComboBox<String> cb = (JComboBox<String>)e.getSource();
						String name = (String)cb.getSelectedItem();
						setDelim = name;
					}
				}
				);



		return top;

	}







	private JPanel makeFilesPanel()
	{
		JPanel top = new JPanel();
		top.setBorder(new TitledBorder("Files"));

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL ;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.ipady = 10;


		top.setLayout(new GridBagLayout());
		top.add(new JLabel("Input File"),c);
		c.gridx = 1;
		c.weightx = 6;


		inputName.setToolTipText("<html>This file contains the Progenesis peptides output csv.</html>");

		inputName.setEditable(false);
		top.add(inputName,c);
		c.weightx = 1;

		c.gridx=4;
		JButton changeDB = new JButton("Change File");
		changeDB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						changeInput();
						checkReady();

					}
				}
				);
		top.add(changeDB,c);


		c.gridx++;
		JButton viewInput = new JButton("Open");
		viewInput.setToolTipText("<html>Opens with external program</html>");

		top.add(viewInput,c);

		viewInput.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{


						if (Desktop.isDesktopSupported()) 
						{
							Desktop  desktop = Desktop.getDesktop();
							try
							{
								desktop.open(inputFile);
							}catch(Exception xe) {xe.printStackTrace();}

						}

					}
				}
				);














		c.gridy++;
		c.gridx = 0;

		top.add(new JLabel("Output File"),c);
		c.gridx = 1;
		c.weightx = 6;

		outputName.setEditable(false);
		outputName.setToolTipText("<html>This file will be written to with the Progenesis peptides output csv including additional columns.</html>");

		top.add(outputName,c);
		c.weightx = 1;

		c.gridx=4;
		JButton changeOutputB = new JButton("Change File");
		changeOutputB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						changeOutput();
						checkReady();

					}
				}
				);
		top.add(changeOutputB,c);
		c.gridx++;
		JButton viewOutputB = new JButton("Open");
		viewOutputB.setToolTipText("<html>Opens with external program</html>");
		top.add(viewOutputB,c);

		viewOutputB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{


						if (Desktop.isDesktopSupported()) 
						{
							Desktop  desktop = Desktop.getDesktop();
							try
							{
								desktop.open(outputFile);
							}catch(Exception xe) {xe.printStackTrace();}

						}

					}
				}
				);


		c.gridy++;
		c.gridx = 0;


		if (true)
		{
			top.add(new JLabel("Processed Output"),c);
			c.gridx = 1;
			c.weightx = 6;

			outputTrypticName.setEditable(false);
			outputTrypticName.setToolTipText("<html>This file will be written to with the 'normalised' neopeptide values.</html>");
			top.add(outputTrypticName,c);
			c.weightx = 1;

			c.gridx=4;
			JButton changeTrypticOutputB = new JButton("Change File");
			changeTrypticOutputB.addActionListener(
					new ActionListener() 
					{
						public void actionPerformed(ActionEvent e)
						{
							changeOutputTryptic();
							checkReady();

						}
					}
					);
			top.add(changeTrypticOutputB,c);


			c.gridx++;
			JButton viewTrypticeOutputB = new JButton("Open");
			viewTrypticeOutputB.setToolTipText("<html>Opens with external program</html>");

			top.add(viewTrypticeOutputB,c);

			viewTrypticeOutputB.addActionListener(
					new ActionListener() 
					{
						public void actionPerformed(ActionEvent e)
						{


							if (Desktop.isDesktopSupported()) 
							{
								Desktop  desktop = Desktop.getDesktop();
								try
								{
									desktop.open(outputTrypticFile);
								}catch(Exception xe) {xe.printStackTrace();}

							}

						}
					}
					);

		}


		return top;

	}

	JTextArea statusArea = new JTextArea();
	private JPanel makeStatusPanel()
	{
		JPanel statusPanel = new JPanel();
		statusPanel.setLayout(new BorderLayout());
		statusPanel.setBorder(new TitledBorder("Status"));
		statusArea.setEditable(false);
		JScrollPane scroller = new JScrollPane(statusArea);
		statusPanel.add(scroller);
		return statusPanel;

	}

	JButton startButton = new JButton("Process neopeptides");
	JButton cancelButton = new JButton("Stop process");
	JButton quitButton = new JButton("Exit");


	void checkReady()
	{
		boolean ready = false;
		if (inputFile != null)
			if (outputFile != null)
				if (databaseFile != null)
				{
					ready = true;
				}
		startButton.setEnabled(ready);

	}

	private JPanel makeButtonPanel()
	{
		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		top.setBorder(new TitledBorder("Actions"));


		cancelButton.setEnabled(false);

		cancelButton.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{

						doCancel();
					}
				}
				);


		quitButton.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{

						doExit();
					}
				}
				);


		startButton.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						startButton.setEnabled(false);
						cancelButton.setEnabled(true);
						startProcess();

					}
				}
				);


		top.add(startButton,BorderLayout.NORTH);
		//	top.add(cancelButton,BorderLayout.SOUTH);

		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BorderLayout());
		subPanel.add(quitButton,BorderLayout.EAST);
		subPanel.add(cancelButton,BorderLayout.WEST);
		subPanel.add(startButton,BorderLayout.CENTER);
		//	
		top.add(subPanel,BorderLayout.SOUTH);


		return top;

	}

	void buildMenus()
	{
		JMenuBar mb = new JMenuBar();


		JMenu root = new JMenu("Tools");
		JMenuItem check = new JMenuItem("Check peptides against database");
		root.add(check);


		mb.add(root);

		check.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						startCheckProcess();
					}
				}
				);

		JMenuItem showOptions = new JMenuItem("Show / Hide Options");
		root.add(showOptions);
		showOptions.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{

						optionPanel.setVisible(!optionPanel.isVisible());

					}
				}
				);

		setJMenuBar(mb);
	}

	void appendStatus(String text)
	{
		statusArea.append(text);
		statusArea.setCaretPosition(statusArea.getText().length());
	}

	void updateStatus(String text)
	{
		String allText = statusArea.getText();
		if(allText.lastIndexOf("\n")>0) {
			allText =  allText.substring(0, allText.lastIndexOf("\n"));
		} 

		statusArea.setText(allText+"\n");
		statusArea.append(text);
	}
	Options optionPanel = new Options();
	private void buildGUI()
	{
		JPanel containerPanel = new JPanel();

		JPanel mainPanel = new JPanel();
		JPanel topPanel = makeTopPanel();
		JPanel inputPanel = makeFilesPanel();

		JPanel statusPanel = makeStatusPanel();
		JPanel buttonPanel = makeButtonPanel();





		mainPanel.setLayout(new BorderLayout());

		JPanel topSub = new JPanel();
		topSub.setLayout(new GridLayout(2,1));

		topSub.add(topPanel);
		topSub.add(inputPanel);
		mainPanel.add(topSub, BorderLayout.NORTH);


		JPanel botSub = new JPanel();
		botSub.setLayout(new BorderLayout());

		botSub.add(statusPanel,BorderLayout.CENTER);
		botSub.add(buttonPanel,BorderLayout.SOUTH);

		mainPanel.add(botSub, BorderLayout.CENTER);


		int h = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int w = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();

 
		w = Math.max(1100, w*1/2);
	 
		h = Math.min(800, h*7/8);
		
	
		setPreferredSize(new Dimension(w,h));
		containerPanel.setLayout(new BorderLayout());
		containerPanel.add(mainPanel,BorderLayout.CENTER);
 
		containerPanel.add(optionPanel,BorderLayout.NORTH);

		optionPanel.setVisible(true);

		add(containerPanel);

		loadSettings();

		buildMenus();
		checkReady();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				doExit();

			}
		});
	}


	void doExit()
	{
		saveSettings();
		System.err.println("Closing");    

		System.exit(0);
	}

	void startProcess()
	{

		int delimiter = 0;
		if (setDelim.equals(headingsDelim[1]))
			delimiter = 1;
		if (setDelim.equals(headingsDelim[2]))
			delimiter = 2;

		int column = setColumn;
		AutoDetectSettings settings = new AutoDetectSettings();
		settings.peptideColumn = column;
		settings.tabDelimiter = (delimiter == 1);
		appendStatus("\nStarting...\n");

		settings = AutoDetect.autoDetect(inputFile, delimiter, column);
		if ((delimiter == 0) || (column == 0) )
		{


			String update = "Autodetect:  Delimiter = ";
			if (settings.tabDelimiter)
			{
				update += "Tabs";
			}
			else 
				update += "Commas";

			if (settings.peptideColumn == 0)
			{
				update += ": \nAuto-detect FAILED, please check file and enter column manually";
			}
			else
			{
				char c = (char) ('A'-1+settings.peptideColumn);
				update+= ":  Peptide column = "+settings.peptideColumn+ " ("+c+")";

			}
			appendStatus(update+"\n");
		}
		//				File input, int delimiter, int column)


		int cleavages = countCleavages(settings);

		final Database proteins = new Database(databaseFile, cleavages);
		final AutoDetectSettings passSettings = settings;

		appendStatus("Building tryptic database with "+(cleavages-1)+" missed cleavages \n");


		class ProcessWorker extends SwingWorker<String, Object> {
			@Override
			public String doInBackground() {
				processInput(proteins, passSettings);
				return null;
			}

			@Override
			protected void done() {
				try {

					appendStatus(inputStatus);
					startButton.setEnabled(true);
					cancelButton.setEnabled(false);		        	   
				} catch (Exception e) {

					e.printStackTrace();
				}
			}
		}

		if (hide)
		{
			processInput(proteins, passSettings);
			System.err.println(inputStatus);	
		}
		else
		{
			(new ProcessWorker()).execute();
		}
	}

	String inputStatus ="";

	void doCancel()
	{
		forceCancel = true;
	}
	public static boolean forceCancel = false;















	void startCheckProcess()
	{

		cancelButton.setEnabled(true);

		int delimiter = 0;
		if (setDelim.equals(headingsDelim[1]))
			delimiter = 1;
		if (setDelim.equals(headingsDelim[2]))
			delimiter = 2;

		int column = setColumn;
		AutoDetectSettings settings = new AutoDetectSettings();
		settings.peptideColumn = column;
		settings.tabDelimiter = (delimiter == 1);
		appendStatus("\nStart checking peptides...\n");

		settings = AutoDetect.autoDetect(inputFile, delimiter, column);


		final Database proteins = new Database(databaseFile);
		final AutoDetectSettings passSettings = settings;

		class ProcessWorker extends SwingWorker<String, Object> {
			@Override
			public String doInBackground() {
				checkInput(proteins, passSettings);

				return null;
			}

			protected void done() {
				try {
					cancelButton.setEnabled(false);

					updateStatusAsync("Finished Check\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


		}

		(new ProcessWorker()).execute();

	}


	int countCleavages( AutoDetectSettings s)
	{
		int max =0;

		try {
			BufferedReader b = new BufferedReader(new FileReader(inputFile));

			while (true)
			{
				if (forceCancel)
					break;

				String getline = b.readLine();
				if (getline == null)
					break;

				if (getline.trim().length() < 1)
					continue;




				String peptide = getPeptide(getline,s);

				if (peptide == null)
					continue;
				int cleavage = 0;
				char last = '0';
				for (int i=0; i < peptide.length(); i++)
				{
					char c = peptide.charAt(i);
					if (c=='K' || c=='R')
					{
						cleavage++;

					}
					if (c=='P')
						if (last=='K' || last=='R')
							cleavage --;
					last = c;

				}

				max = Math.max(max, cleavage);

			}
			b.close();

		} catch (IOException e) 
		{
			e.printStackTrace();
		}

		return max ;

	}

	void checkInput(Database d, AutoDetectSettings s)
	{
		foundBuffer = new HashSet<String>();

		inputStatus="";
		forceCancel = false;
		int count = 0;
		try {
			BufferedReader b = new BufferedReader(new FileReader(inputFile));


			while (true)
			{
				if (forceCancel)
					break;

				String getline = b.readLine();
				if (getline == null)
					break;

				if (getline.trim().length() < 1)
					continue;


				String line = findMissing(getline,s,d);


				if (line != null)
				{
					System.err.println(line + " not found");
					updateStatusAsync("Peptide "+line+" not found in database\n");

					count ++;
				}


			}
			b.close();

		} catch (IOException e) 
		{
			inputStatus= "\nError with reading / writing files.  Is the output file open in another program?\n";

		}
		inputStatus += ""+count+" peptides not found in fasta database\n";
		updateStatusAsync(inputStatus);
	}

	void updateStatusAsync(final String text)
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateStatus(text);
			}
		});
	}

	void processInput(Database d, AutoDetectSettings s)
	{
		
		Settings.MISSED_CONDITIONS = 100;
		if(Options.matchingPeptides)
			Settings.MISSED_CONDITIONS = 0;
		
		Settings.LOG_RATIO= false;
		if(Options.useLogRatio)
			Settings.LOG_RATIO= true;
		
		try
		{
			Settings.FDR = Double.parseDouble(Options.getFDR());
		}
		catch (NumberFormatException e) {}
		
		try
		{
			Settings.MIN_PEPTIDES = Integer.parseInt(Options.getMIN());
		}
		catch (NumberFormatException e) {}
		
		inHeader = true;

		forceCancel = false;
		try {

			BufferedWriter w = new BufferedWriter(new FileWriter(outputFile));
			BufferedReader b = new BufferedReader(new FileReader(inputFile));

			int numTryptic = 0;
			int numNeo = 0;

			int lineNo = 0;
			int inputLines = 0;


			while (true)
			{
				lineNo ++;
				if (forceCancel)
					break;

				String getline = b.readLine();
				inputLines++;
				if (getline == null)
					break;

				int length = getline.trim().length() ;
				if (length < 1)
					continue;


				Database.lastPrevious =' ';
				Boolean isTryptic = convertLine(getline,s,d);



				String line = getline+",";

				if (lineNo == s.headerRow)
				{
					line = line+"\"Is Tryptic\",\"previous\",\"next\"";
				}

				if (isTryptic != null)
				{
					if (isTryptic == true)
					{
						numTryptic++;
						line = line +"true"+","+Character.toString(Database.lastPrevious)+","+Character.toString(Database.lastNext);
					}
					if (isTryptic == false)
					{
						numNeo++;
						line = line +"false"+","+Character.toString(Database.lastPrevious)+","+Character.toString(Database.lastNext);
					}
				}					


				final int printLines = inputLines;

				if ((printLines%50)==0)
				{
					javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							updateStatus(printLines +" lines processed");
						}
					});

				}



				w.write(line+"\n");


			}
			w.close();
			b.close();

			inputStatus= "\nFinished writing "+numNeo+" neopeptides and "+numTryptic+" fully tryptic peptides\n";

		} catch (IOException e) 
		{
			inputStatus= "\nError with reading / writing files.  Is the output file open in another program?\n";

		}
		if (forceCancel)
			inputStatus = "CANCELLED before end of file\n";


		try
		{
			d.clear();
			Normalise.loadCSV(outputFile, s);

			int[] dataColumns = Normalise.getConditions(outputFile, s);
			if (dataColumns == null)
			{		
				inputStatus= "\nUnable to find the data in Progenesis file.  Is the csv file correct, and contains "+Options.getSearch()+"?\n";

			}
			else
			{
				for (int dataColumn : dataColumns)
				{
					
					
					Normalise.normalise(dataColumn);
					
				}
			}




		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!Normalise.display(outputTrypticFile))
		{
			inputStatus= "\nError with reading / writing files.  Is the processed output file open in another program?\n";
		}

	}

	boolean inHeader = true;
	Boolean convertLine(String getLine,AutoDetectSettings s, Database d)
	{

		String peptide = getPeptide(getLine,s);

		if (peptide == null)
			return null;
		// return getLine+",";

		if (inHeader)			
			if (!AutoDetect.isPeptide(peptide))
				return null; //getLine+",";


		inHeader = false;
		//remove it if it is tryptic

		if (!d.isTryptic(peptide))
			return false;
		//getLine+",false";

		return true; // getLine+",true";

	}





	Set<String> foundBuffer = new HashSet<String>();

	String getPeptide(String getLine,AutoDetectSettings s)
	{
		String delim="\t";
		String line= getLine;
		if (!s.tabDelimiter)
		{
			line = AutoDetect.convertToTabs(line);
		}
		String[] columns = line.split(delim);
		if (columns.length < s.peptideColumn)
			return null;

		String peptide = columns[s.peptideColumn-1].trim();
		int l = peptide.length();
		if(l < 1)
			return null;
		if (peptide.charAt(0)=='\'')
			peptide=peptide.substring(1);
		else if (peptide.charAt(0)=='\"')
			peptide=peptide.substring(1);
		l = peptide.length();
		if (peptide.charAt(l-1)=='\'')
			peptide=peptide.substring(0,l-1);
		else if (peptide.charAt(l-1)=='\"')
			peptide=peptide.substring(0,l-1);
		if(peptide.length() < 1)
			return null;

		return peptide;

	}

	String findMissing(String getLine,AutoDetectSettings s, Database d)
	{


		String peptide = getPeptide(getLine,s);

		if (peptide == null)
			return null;



		if (!AutoDetect.isPeptide(peptide))
			return null;

		if (foundBuffer.contains(peptide))
			return null;

		if (d.isPresent(peptide))
		{
			foundBuffer.add(peptide);
			return null;
		}
		foundBuffer.add(peptide);

		return peptide;
	}





	static void createAndShowGUI()
	{	 
		try {
			Locale.setDefault(new Locale("en", "US"));

			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;

				}	

			}
		 
			UIManager.getLookAndFeelDefaults().put("defaultFont",
					new Font("SansSerif", Font.PLAIN, 12*SCALE/256)); 

			UIManager.getLookAndFeelDefaults().put("ScrollBar.minimumThumbSize", new Dimension(30, 30));

			UIManager.put("control", new Color(252,250,248));
			UIManager.put("nimbusBlueGrey", new Color(251,217,161));
			UIManager.put("text", new Color(123,43,18));


		} catch (Exception e)
		{
			e.printStackTrace();

		}	

		//		AutoDetect.test();
		Instance = new MainWindow(); 

	}

	static boolean hide = false;
	static MainWindow Instance;
	static int SCALE = 256;
	public static void main(String[] args) 
	{
		System.err.println("Starting");

		if (args.length > 0)
		{
			settingsFile = new File(args[0]);
			hide = true;
		}

		int h = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int w = (int) java.awt.Toolkit.getDefaultToolkit().getScreenSize().getWidth();

		int size = Math.min(w,h);
		SCALE = 256;
		//		if (size > 900)
		//			SCALE = SCALE * 6/5;
		if (size > 1200)
			SCALE = SCALE * 5/4;
		if (size > 1800)
			SCALE = SCALE * 6/4;
		if (size > 2200)
			SCALE = SCALE * 7/4;


		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});



	}


}
