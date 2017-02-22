package uk.ac.liv.iib.pgb.neo;
import javax.swing.*;

import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;

//The user interface for the user options

public class Options extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8259354657688385850L;
	public static boolean DBSearch = false;
	public static JCheckBox doDBSearch;

	public static boolean removeUnused = false;
	public static JCheckBox removeUnusedCB;

	public static boolean sameFolder = true;
	public static JCheckBox sameFolderCB;


	public static boolean sameDriveLetter = true;
	public static JCheckBox sameDriveLetterCB;


	public static boolean matchingPeptides = true;
	public static JCheckBox matchingPeptidesCB;
	
	
	public static boolean useLogRatio = false;
	public static JCheckBox useLogRatioCB;
	
	public void setSameDriveLetter(boolean isOn)
	{
		sameDriveLetterCB.setSelected(isOn);
		sameDriveLetter = isOn;
	}

	public void setSameFolder(boolean isOn)
	{
		sameFolderCB.setSelected(isOn);
		sameFolder = isOn;
	}

	
	public void setMatching(boolean isOn)
	{
		matchingPeptidesCB.setSelected(isOn);
		matchingPeptides = isOn;
	}
	
	public void setLogRatio(boolean isOn)
	{
		useLogRatioCB.setSelected(isOn);
		useLogRatio = isOn;
	}
	public void setSearch(boolean isOn)
	{
		doDBSearch.setSelected(isOn);
		DBSearch = isOn;
	}
	public void setUnused(boolean isOn)
	{
		removeUnusedCB.setSelected(isOn);
	}

	static JTextField searchFor = new JTextField("Raw Abundance");
	static JTextField FDRRate = new JTextField("0.05");
	static JTextField MINPeptides = new JTextField("3");
	
	public static String getSearch()
	{
		return searchFor.getText();
	}
	public static String getMIN()
	{
		return MINPeptides.getText();
	}
	public static void setMIN(String text)
	{
		MINPeptides.setText(text) ;
	}
	public static void setSearchString(String text)
	{
		searchFor.setText(text) ;
	}
	
	public static String getFDR()
	{
		return FDRRate.getText();
	}
	public static void setFDR(String text)
	{
		FDRRate.setText(text) ;
	}
	
	
	
	public Options()
	{
		super();
		
		JPanel quantOptions = new JPanel();
		JPanel fileOptions = new JPanel();
		
		quantOptions.setLayout(new FlowLayout(FlowLayout.LEADING));
		fileOptions.setLayout(new FlowLayout(FlowLayout.LEADING));
		
		// setLayout(new FlowLayout(FlowLayout.LEFT));
		  setLayout(new GridLayout(0,1));
 
		doDBSearch = new JCheckBox("Do Slow Search ");
		fileOptions.add(doDBSearch);

		doDBSearch.setToolTipText("<html>This must be enabled to compute the previous & next residue.<br> The resulting search will be much slower for large files.</html>");
		sameFolderCB = new JCheckBox("Automatic file names");
		sameFolderCB.setSelected(sameFolder);
		sameFolderCB.setToolTipText("<html>Always sets the output files automatically.</html>");
		fileOptions.add(sameFolderCB);


		sameDriveLetterCB = new JCheckBox("Change drive letter automatically");
		sameDriveLetterCB.setSelected(sameDriveLetter);
		sameDriveLetterCB.setToolTipText("<html>Always sets the drive letter to the current drive.</html>");
		fileOptions.add(sameDriveLetterCB);


		quantOptions.add(new JLabel("Data Field: "));

		quantOptions.add(searchFor);
		searchFor.setToolTipText("<html>This field marks which of the various Progenesis outputs to look for.</html>");

		
		removeUnusedCB = new JCheckBox("Filter on 'Use in Quantitation'");
		searchFor.setToolTipText("<html>Enable to discard neopeptides not marked as 'Use in Quantitation'.</html>");
		fileOptions.add(removeUnusedCB);

		useLogRatioCB = new JCheckBox("Log transform ratios");
		useLogRatioCB.setSelected(useLogRatio);
		useLogRatioCB.setToolTipText("<html>Use log of neopeptide / protein ratio.</html>");
		quantOptions.add(useLogRatioCB);
	
		
		matchingPeptidesCB = new JCheckBox("Quantify on matching peptides only ");
		matchingPeptidesCB.setSelected(matchingPeptides);
		matchingPeptidesCB.setToolTipText("<html>Only quantify proteins based on the peptides that are present in all relevant samples.</html>");
		quantOptions.add(matchingPeptidesCB);
	
		quantOptions.add(new JLabel("| Min. Peptides: "));

		quantOptions.add(MINPeptides);
		MINPeptides.setToolTipText("<html>The minimum number of peptides to quantify a protein.</html>");

		quantOptions.add(new JLabel("| FDR : "));

		quantOptions.add(FDRRate);
		searchFor.setToolTipText("<html>The FDR rate used to determine if differential expression p-value is significant.</html>");

		
		setBorder(new TitledBorder("Options"));
		doDBSearch.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						DBSearch =  doDBSearch.isSelected();
					}
				}
				);

		removeUnusedCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						removeUnused =  removeUnusedCB.isSelected();
					}
				}
				);

		sameFolderCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						sameFolder =  sameFolderCB.isSelected();
					}
				}
				);

		
		
		useLogRatioCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						useLogRatio =  useLogRatioCB.isSelected();
					}
				}
				);
		

		matchingPeptidesCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						matchingPeptides =  matchingPeptidesCB.isSelected();
					}
				}
				);
		

		sameDriveLetterCB.addActionListener(
				new ActionListener() 
				{
					public void actionPerformed(ActionEvent e)
					{
						sameDriveLetter =  sameDriveLetterCB.isSelected();
					}
				}
				);
		
		JScrollPane fo = new JScrollPane(fileOptions);
		fo.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
		
		
		JScrollPane qo = new JScrollPane(quantOptions);
		qo.setBorder(null);
		fo.setBorder(null);
		qo.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 8));
		qo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		add(fo);
		add(qo);
		
		
	}
}
