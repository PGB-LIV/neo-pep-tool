package uk.ac.liv.iib.pgb.neo;

import java.io.*;
import java.util.*;

public class AutoDetect {

	static final int maxTest = 1000;
	
	
	// to check if the data is using tabs as seperators rather than commas
	private static boolean useTabs(List<String> buffer)
	{
		int numCommas = 0;
		int numTabs = 0;
		for (String line : buffer)
		{
			char[] chars = line.toCharArray();
			for (char c: chars)
			{
				if (c==',')
					numCommas ++;
				if (c=='\t')
					numTabs ++;

			}
			
		}
		return (numTabs > numCommas);
		
	}
	
	//converts a line from comma delimted to tab delimited

	public static String convertToTabs(String line)
	{
		boolean inQuotes = false;
		List<String> newLine = new LinkedList<String>();
		
		int startFrom = 0;
		for (int i=0; i < line.length(); i++)
		{
			char c = line.charAt(i);
			if ((c=='\'') || (c=='\"') )
				inQuotes = !inQuotes;
			
			if ((c==',') && (! inQuotes))
			{
				
				String partial = "";
				if (i > 0)
					partial = line.substring(startFrom,i );
				startFrom = i+1;
				newLine.add(partial);
			}
			
		}
		if (startFrom < line.length())
		{
			String partial = line.substring(startFrom,line.length() );
			newLine.add(partial);
		}
		StringBuilder sb = new StringBuilder();
		for(String partial : newLine)
		{
			sb.append(partial);
			sb.append("\t");

		}
		return sb.toString();		
	}
	
	//Convert a buffer to tabs from comma-delimited
	public static List<String> convertToTabs(List<String> buffer)
	{
		List<String> newBuffer = new LinkedList<String>();
		
		for (String line : buffer)
		{

			newBuffer.add(convertToTabs(line));
			
		}
		
		return newBuffer;
	}

	static String aminoAcids="ARNDCEQGHILKMFPSTWYVUO\'\"";
	static int[] isAmino = null;

	//builds a lookup table to quickly check if a given character is actually an amino acid 
	static void setAmino()
	{
		isAmino = new int[32768];

		for (int i=0; i < aminoAcids.length(); i++)
		{
			char c = aminoAcids.charAt(i);
			isAmino[c] = 1;
			
		}

	}

	
	// Check if a string is a valid peptide (using the isAmino lookup table)
	public static boolean isPeptide(String peptide)
	{
		if (isAmino == null)
			setAmino();
		peptide = peptide.trim();
		for (int i=0; i < peptide.length(); i++)
		{
			char c = peptide.charAt(i);
			if (isAmino[c] == 0)
				return false;
		}
		return true;
	}
	
	//look for a column that is made up of peptides (with some leeway for header rows etc.)
	private static int guessPeptideColumn(List<String> buffer)
	{
		
		int maxColumns = 32768;
		int[] sums = new int[maxColumns];
		int maxCol = 0;
		for (String line : buffer)
		{
			String[] columns = line.split("\t");
			int col = 0;
			for (String word : columns)
			{
				word = word.trim();
				for (int i=0; i < word.length(); i++)
				{
				
					char c = word.charAt(i);
					if (c > 255)
						continue;
					if (isAmino[c] > 0)
					{
						sums[col] ++;
					}
					else
						sums[col] -=10;
					
				}
				
				col++;
			}
			if (col > maxCol)
				maxCol = col;
		}
		int best = 0;
		
		for  (int i=1; i < maxCol; i++)
		{
			if (sums[i] > sums[best])
				best = i;
		}
		
		if (sums[best] == 0)
			return 0;
		
		return best+1;
		
	}

	
	
	// Try to autodetect the settings from the input file
	// delimiter = 0 is autodetect, 1 is commas, 2 is tabs

	
	
	public static AutoDetectSettings autoDetect(File input, int delimiter, int column)
	{
		
		AutoDetectSettings settings = new AutoDetectSettings();
		settings.peptideColumn = 0;
		settings.tabDelimiter = true;

		setAmino();
		List<String> buffer = new LinkedList<String>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(input));
			int lines = 0;
			while (lines++ < maxTest)
			{
				String line = b.readLine();
				if (line == null)
					break;
				buffer.add(line);
				
			}
			
			b.close();
			
		} catch (IOException e) 
		{
			return settings;
		}
		
		if (delimiter == 0)
		{
			delimiter = 1;
			if (!useTabs(buffer))
				delimiter = 2;
		}
		
		if (delimiter == 2)
			buffer = convertToTabs(buffer);
		
		settings.tabDelimiter = (delimiter ==1);
		
		settings.peptideColumn = column;
		if (column == 0)
		{
			settings.peptideColumn = guessPeptideColumn(buffer);
		}
		//read first 1000

		//Now look for header row
		// valid numbers in the data indicate it is not a header row 
		
		
		int headerMax = 0;
		int headerRow = 0;
		int maxWidth = 10;
	 
			int lines = 0;
			for(String line : buffer)
			{
				lines++;
				
				String[] cells = line.split("\t");
				
				maxWidth = Math.max(maxWidth,cells.length);
				
				int length = line.length();
			
				int numNum = 0;
				for (String cell : cells)
				{
					try
					{
						cell = Utils.trim(cell);
						
						Double.parseDouble(cell);
						numNum++;
					}
					catch (Exception e)
					{
						
					}
					
				}
				

				// If there are 'lots' of numbers, then it can't be a header row
				if (numNum > maxWidth / 3)
					break;
				
				if (length > headerMax)
				{
					headerMax = length;
					headerRow = lines;
				}
			
				
			}
			
 
		settings.headerSize = lines;
		settings.headerRow = headerRow;
		
		return settings;
		
	}
	
}
