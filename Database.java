package uk.ac.liv.iib.pgb.neo;

import java.io.*;
import java.util.*;

// the 'Database' of proteins  / peptides created from the fasta file 
public class Database {

	//the protein list is just stored as a single string
	String database;

	// a cache for getting previous / next residues of a given peptide without doing the search again
	Map<String,CharPair> cache = new TreeMap<String,CharPair>();

	//any more than this, and we will have to search instead of using dictionary lookup
	final int MAX_CLEAVAGE = 7;

	public void clear()
	{
		trypticDatabase = null;
		cache = null;
		database=null;
		System.gc();
	}
	
	int maxCleavages = 3;
	
	//get the previous residue for a given peptide string 
	public CharPair getPrevious(String neo)
	{
		
		//check if result has been cached
		CharPair pair = cache.get(neo);

		if (pair != null)
			return pair;

		
		pair = new CharPair();
		pair.start = 'L';
		pair.end = 'L';

		// KMP search = new KMP(neo);
		// int start = search.search(database);
		char c= ':';

		int start = database.indexOf(neo);

		// alternative code for Knuth-Morris-Pratt search
		// theoretically quicker with long strings
		// but has not shown to be quicker in practice with example data, so has been removed
		
		//		 KMP search = new KMP(neo);
		//		 int start = search.search(database);

		
		// Found a match in protein database
		if (start > 1)
		{
			c = database.charAt(start - 1);
			char endc = neo.charAt(neo.length() - 1);

			boolean trypticEnd = (endc=='K') || (endc == 'R');
			char nextc = database.charAt(start +neo.length());

			// a protein may start with an M, which gets cut off
			//hence is still tryptic
			if ((c == 'M') && database.charAt(start - 2) == ':')
				c = ':';

			//if this peptide ss not tryptic, look for another one that is
			//as peptide may be present in more than one protein
			while ((c != 'K' && c!= 'R' && c != ':') || (!trypticEnd  && nextc != ':') )
			{
				int index = start + 1;
				int next = database.indexOf(neo, index);
				//		 int next = search.search(database, index);

				if (next < 0)
					break;

				c = database.charAt(next - 1);
				if ((c == 'M') && database.charAt(next - 2) == ':')
					c = ':';

				start = next;
				nextc = database.charAt(start +neo.length());

			}

			pair.end = database.charAt(start +neo.length()) ;
			pair.start = c;

		}

		cache.put(neo,pair);
		return pair;

	}

	//check if peptide is contained within fasta file
	public boolean isPresent(String neo)
	{
		if (database == null)
			readFile();
		return (database.indexOf(neo) > -1);
	}
	
	public static char lastPrevious;
	public static char lastNext;
	
	boolean isTryptic(String neo)
	{
		//if using 'fast' dictionary-based search
		if (USE_DICT)
		{
			if (trypticDatabase == null)
				setDictionary();					
			return trypticDatabase.contains(neo);
		}


		CharPair pair = getPrevious(neo);
		char last = neo.charAt(neo.length()-1);

		char prev = pair.start;
		char next = pair.end;

		lastPrevious = prev;
		lastNext = next;
		if (next == ':')
			lastNext = ' ';
		if (prev == ':')
			lastPrevious = ' ';
		//if it doesn't end with K or R, then it is probably not tryptic
		//but it could be the last in the protein
		if ((last!= 'K') && (last!='R') )
			if (next != ':')
			{
				return false;			
			}

		//It must end in K,R, or be the final peptide - so we check the start

		//If the previous was not either K or R, an it's not the start of a protein
		//then it's not tryptic either
		if ((prev!= 'K') && (prev!='R') && (prev != ':') )
		{
			return false;
		}

		return true;

	}

	File inputFile;

	Set<String> trypticDatabase = null;

	
	// Builds a dictionary of all tryptic peptides
	// stored in trypticDatabase
	void setDictionary()
	{
		// System.err.println("Building Dictionary");
		trypticDatabase = new TreeSet<String>();

		File input = inputFile;
		int pepNo = 0;
		char last = ':';

		int total = 0;
		try 
		{
			BufferedReader b = new BufferedReader(new FileReader(input));
			StringBuilder peptide = new StringBuilder(1000);

			int cleavages = maxCleavages;
			String previous[] = new String[cleavages+1];

			for (int i=0; i < cleavages; i++)
				previous[i] = "";

			// parse the fasta file
			while (true)
			{

				if (MainWindow.forceCancel)
				{
					break;
				}

				String line = b.readLine();
				if ((line == null) || (line.startsWith(">")))
				{


					//got to the end of the file / protein
					//so clean up any hanging characters and treat as a peptide
					
					String pep = peptide.toString();

					for (int i=0; i < cleavages; i++)
					{
						trypticDatabase.add(pep);
						total++;
						pep = previous[i] + pep;
						previous[i]="";

					}

					if (line == null)
						break;

					peptide = new StringBuilder(10000);
					pepNo = 0;
					last = ':';

				}
				else
				{


					for (int i=0; i < line.length(); i++)
					{
						char c = line.charAt(i);
						// look for a tryptic cleavage
						if ((c != 'P')
								&& (last == 'K' || last == 'R'))
						{
							String pep = peptide.toString();

							for (int j=0; j < cleavages; j++)
							{
								total++;
								trypticDatabase.add(pep);
								if (pepNo == j && pep.charAt(0)=='M')
								{
									total++;
									trypticDatabase.add(pep.substring(1));

								}

								pep = previous[j] + pep;

							}



							for (int j=0; j < cleavages-1; j++)
							{
								previous[(cleavages-j)-1] = previous[(cleavages -j)-2];
							}
							previous[0] = peptide.toString();;

							peptide = new StringBuilder(10000);

							pepNo++;

						}


						peptide.append(c);
						last = c;


					}

				}


			}
			
			b.close();
 			System.err.println(total+" peptides found in protein database");

		} catch (IOException e) 
		{
			e.printStackTrace();
		}


	}
	boolean USE_DICT = true;


	// turn the input file into a database string
	// a ':' is used in between proteins
	// this is just to prevent searches returning a value across proteins
	
	void readFile()
	{
		File input = inputFile;

		cache = new TreeMap<String,CharPair>();


		StringBuilder buffer = new StringBuilder((int) input.length());
		buffer.append(":");

		try {
			BufferedReader b = new BufferedReader(new FileReader(input));

			StringBuilder protein = new StringBuilder(8000);

			while (true)
			{
				String line = b.readLine();
				if (line == null)
				{
					//end of file
					buffer.append(":");
					buffer.append(protein);
					break;
				}
				if (line.startsWith(">"))
				{
					//new protein found
					if (protein.length() > 0)
					{
						buffer.append(":");
						buffer.append(protein);
						protein = new StringBuilder();
					}
				}
				else
				{
					protein.append(line.trim());
				}


			}
			b.close();

		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		buffer.append(":");
		database = buffer.toString();


	}

	
	public Database(File input, int cleavages)
	{
		maxCleavages = cleavages;
		inputFile= input;

		if ((cleavages > MAX_CLEAVAGE) || (Options.DBSearch) )
		{
			USE_DICT = false;
			readFile();
		}	

	}

	public Database(File input)
	{

		inputFile= input;

	}
}
