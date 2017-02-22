package uk.ac.liv.iib.pgb.neo;



import java.io.*;
import java.util.*;

 
import org.apache.commons.math3.stat.inference.*;

class DataLine
{
	static int curIndex = 0;
	String protein;
	String modification;
	String sample;
	String peptide;
	String previous;
	String next;
	double quant;
	int index;
	int charge;

	static class Peptide
	{
		String key;
		double value;
		double score;
		int charge;
		static int curIndex =0;
		int index;

		String peptide;
		public Peptide(String k, int c, double v, double s, String pep)
		{
			key = k;
			value = v;
			score = s; 
			charge = c;
			index = curIndex++;
			peptide = pep;
		}

	}

	Map<String,Peptide> peptides = new TreeMap<String,Peptide>();



	public static final boolean  separate_mods = true;

	//represents either a protein or peptide quant
	public String proteinKey()
	{

		String keyString = protein+sample; //+ modification; 
		if (separate_mods)
			keyString = protein+sample+ modification; 
		return keyString;
	}

	public String peptideKey()
	{

		String keyString = peptide; //+ modification; 
		if (separate_mods)
			keyString = peptide+ modification; 
		return keyString;
	}
	public String toString()
	{
		return protein+" , "+modification+" , "+sample+" , "+quant ;

	}
	public DataLine(String prot, String mod, String s, String p, int c)
	{
		peptide = p;
		protein = prot;
		modification = mod;
		sample = s;
		quant = 0;
		charge = c;
		index = curIndex++;
	}

	public int 	getNumPeptides()
	{

		//	getValue();


		countPeptides();
		return numPeptides;
		//		return peptides.getSize();

	}
	public static String stripMod(String mod)
	{
		String newString =mod.replaceAll("\\[.*?\\]", "");
		return newString;
	}
	public DataLine(String prot, String mod, String s, String p, String pr, String ne, int c)
	{
		peptide = p;
		protein = prot;
		modification = stripMod(mod);
		sample = s;
		quant = 0;
		previous = pr;
		next = ne;
		charge = c;
		index = curIndex++;

	}
	public DataLine(String prot, String mod, String s)
	{
		protein = prot;
		modification = mod;
		modification = stripMod(mod);
		sample = s;
		quant = 0;
		numPeptides = 0;
		index = curIndex++;

	}

	int numPeptides = 0;

	//need to deal with a) multiple ids
	//and b) multiple charge states

	public void addQuant(String peptideID, int c, double q, double score, String pepName)
	{

		peptides.put(peptideID, new Peptide(peptideID,c,q,score,pepName));
	}

	int numPepCharge = 0;
	public double countPeptides()
	{

		numPeptides = 0;
		for (Peptide p : peptides.values())
		{
			boolean best = true;

			for (Peptide p2 : peptides.values())
				if (p != p2)
				{
					//for each key, only get one
					//i.e. ignore multiple charge states
					if ((p.key.equals(p2.key))  && (p.index > p2.index) )
						best = false;
				}

			if (best)
			{


				numPeptides++;
				//	 	System.err.println(" counting "+	numPeptides+"  "+p.key+" : "+p.charge+" for "+protein +" , "+peptide);	

			}

		}
		return numPeptides;
	}


	public double getValue()
	{

		numPepCharge = 0;
		double q = 0;
		for (Peptide p : peptides.values())
		{
			boolean best = true;

			for (Peptide p2 : peptides.values())
				if (p != p2)
				{
					if (p.key.equals(p2.key))
						if (p.charge == p2.charge)
							if ((p2.score > p.score) || ((p2.score == p.score) && (p.index > p2.index)) )
								best = false;
				}

			if (best)
			{
				q += p.value;

				numPepCharge++;
			}

		}
		return q;
	}
	public double getValue(DataLine pep)
	{
		//protein quant (for source peptide)
		Map<String,StringSet> peptideConditions = Normalise.peptideConditions;

		String peptideConditionsKey = pep.peptide;

		//find which conditions the target peptide appears in

		//if (Settings.MISS_CHARGE_STATES)
		//	peptideConditionsKey +=":"+pep.charge;
		StringSet pepConds = peptideConditions.get(peptideConditionsKey);	

		numPepCharge = 0;
		double q = 0;

		//go through all of the peptides associated with this protein
		//where there is more than one, choose the best or first
		for (Peptide p : peptides.values())
		{
			boolean best = true;

			for (Peptide p2 : peptides.values())
				if (p != p2)
				{
					if (p.key.equals(p2.key))
						if (p.charge == p2.charge)
							if ((p2.score > p.score) || ((p2.score == p.score) && (p.index > p2.index)) )
								best = false;
				}

			if (best)
			{
				String peptideConditionsKey2 = p.peptide;
				if (Settings.MISS_CHARGE_STATES)
					peptideConditionsKey2 +=":"+p.charge;

				// System.err.println(peptideConditionsKey2);

				//see how many conditions this peptide appears in
				// (possibly including its charge state)

				//compare to which conditions the target neopeptide appears in

				StringSet pConds = peptideConditions.get(peptideConditionsKey2);
				int count = 0;

				for (String s : pConds.strings)
				{
					if (pepConds.strings.contains(s))
						count++;
				}

				if (count >= pepConds.strings.size() - Settings.MISSED_CONDITIONS)
				{
					q += p.value;


					numPepCharge++;
				}
			}

		}
		return q;
	}

}



//Used to store a set of proteins for each peptide
//peptide keys include the protein value, to allow them to be
//treated separately

class StringSet
{

	Set<String> strings;

	public StringSet()
	{
		strings = new TreeSet<String>();
	}
	public StringSet(String pt)
	{
		strings = new TreeSet<String>();
		add(pt);

	}

	public void add(String pt)
	{
		strings.add(pt);
	}

}

public class Normalise {
	static Map<String,StringSet> peptideProteins = new TreeMap<String,StringSet>(); 


	private static final boolean  separate_mods = DataLine.separate_mods;

	
	static String[] header;
	static List<String> buffer;
	static List<Row> csv;
	public static void loadCSV(	 File input, AutoDetectSettings a)
	{
		System.err.println("load CSV");

		reset();

		csv = new LinkedList<Row>();
		int lineNo = 0;

		buffer = new LinkedList<String>();
		try {
			BufferedReader b = new BufferedReader(new FileReader(input));

			while (true)
			{
				String line = b.readLine();
				if (line == null)
					break;
				//				buffer.add(line);



				if (!a.tabDelimiter)
					line = AutoDetect.convertToTabs(line);

				lineNo ++;
				String[] cellsText = line.split("\t");


				if (lineNo ==a.headerRow)
					header = cellsText;

				Row row = new Row(cellsText.length);



				if (lineNo >= a.headerSize)
				{
					for (String cell : cellsText)
						row.add(new Cell(cell));

					csv.add(row);
				}






			}

			b.close();

		} catch (IOException e) 
		{
			return;
		}

		System.err.println(csv.size()+" Rows loaded");


	}

	public static int distance(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		// i == 0
		int [] costs = new int [b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			// j == 0; nw = lev(i - 1, j)
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}


	static int getColumn(String search, String[] header)
	{
		if (header == null)
			return -1;



		int best = 0;
		int bestVal = Integer.MAX_VALUE;

		for (int i=0; i < header.length; i++)
		{
			String compare = Utils.trim(header[i]);

			if (compare.equals(search))
				return i;
			int testVal = distance(compare,search);
			if (testVal < bestVal)
			{
				bestVal = testVal;
				best = i;
			}


		}		


		return best;

	}

	static int getColumn(String search)
	{


		return getColumn(search,header);

	}


	static Map<String,DataLine> quant = new TreeMap<String,DataLine>(); 
	static Map<String,DataLine> quantPeps = new TreeMap<String,DataLine>(); 
	static  List<String> columns = new LinkedList<String>();

	static Map<String,StringSet> peptideConditions = new TreeMap<String,StringSet>(); 

	public static void reset()
	{
		quant = new TreeMap<String,DataLine>(); 
		quantPeps = new TreeMap<String,DataLine>(); 
		columns = new LinkedList<String>();



	}


	//	static Map<String,DataLine> quant = new TreeMap<String,DataLine>(); 

	// public static void normalise(String dataColumnText)
	public static void normalise(int dataColumn)
	{
		//stores the last value per peptide
		//if we found a better peptide, may need to remove it from protein quant
 
		String dataColumnText = Utils.trim(header[dataColumn]);
		// System.err.println("normalise "+dataColumnText);
		columns.add(dataColumnText);

		if (csv == null)	
			return;

		int proteinColumn = getColumn("Accession");
		int descColumn = getColumn("Description");
		int useColumn = getColumn("Use in quantitation");
		int modsColumn = getColumn("Modifications");
		int trypticColumn = getColumn("Is Tryptic");
		//	int anovaColumn = getColumn("Anova");
		int chargeColumn = getColumn("Charge");
		int peptideColumn = getColumn("Sequence");
		int scoreColumn = getColumn("Score");
		int prevColumn = getColumn("previous");
		int nextColumn = getColumn("next");

		//for each protein
		//sum up all of the data 
		for (Row row : csv)
		{

			String peptideString = ""+row.get(peptideColumn);
			String previous = ""+row.get(prevColumn);
			String next = ""+row.get(nextColumn);
			//		System.err.println(previous);

			Cell tryptic = row.get(trypticColumn);
			boolean isTryptic = tryptic.isTrue();
			Cell use = row.get(useColumn);
			//			Cell anova = row.get(anovaColumn);

			//			if (!anova.hasValue())
			//				continue;
			//			if (anova.toDouble() > 0.05)
			// continue;
			Cell scoreCell = row.get(scoreColumn);

			if (Options.removeUnused)
				if (!use.isTrue())
					continue;


			//a key for the protein, but also used in output, hence the extra column
			String proteinString = ""+row.get(proteinColumn)+"\",\""+row.get(descColumn);

			//get just the modification
			String modString = DataLine.stripMod(""+row.get(modsColumn));

			//get the numerical value from the data cell (method is called for each column separately)
			Cell data = row.get(dataColumn);

			if (!data.hasValue()) // could also threshold?
				continue;

			Cell chargeCell = row.get(chargeColumn);
			int charge = (int) chargeCell.toDouble();

			double value = data.toDouble();
			if (value <= 0.000000001)
				continue;

			String peptideConditionKey = peptideString;


			if (!peptideConditions.containsKey(peptideConditionKey))
				peptideConditions.put(peptideConditionKey, new StringSet());
			peptideConditions.get(peptideConditionKey).add(""+dataColumn);

			if (Settings.MISS_CHARGE_STATES)
			{
				peptideConditionKey += ":" + charge;
				if (!peptideConditions.containsKey(peptideConditionKey))
					peptideConditions.put(peptideConditionKey, new StringSet());
				peptideConditions.get(peptideConditionKey).add(""+dataColumn);
			}


			if (!peptideProteins.containsKey(peptideString))
			{
				peptideProteins.put(peptideString, new StringSet());

			}
			peptideProteins.get(peptideString).add(proteinString);

			//includes charge state
			//to allow merging of charge states
			String pepKeyString = peptideString +dataColumnText;
			if (separate_mods)
				pepKeyString = peptideString + modString +dataColumnText ;


			double score = scoreCell.toDouble();




			//may need to deal with multiple identifications
			if (isTryptic)
			{



				//if it's a tryptic peptide, add it to the 'quant' for the protein
				DataLine d = new DataLine(proteinString, modString,dataColumnText,peptideString, charge);
				String keyString = d.proteinKey();

				if (!quant.containsKey(keyString))
					quant.put(keyString, d);

				quant.get(keyString).addQuant(pepKeyString, charge, value, score,peptideString);



			}
			else
			{



				//if it's not tryptic, put it into just the peptide quant table
				String keyString = peptideString +dataColumnText + proteinString;


				if (separate_mods)
					keyString = peptideString + modString +dataColumnText + proteinString;

				//Keystring keeps all peptides separate includes proteinstring for output only
				if (!quantPeps.containsKey(keyString))
					quantPeps.put(keyString, new DataLine(proteinString, modString,dataColumnText, peptideString, previous, next,charge));


				quantPeps.get(keyString).addQuant(keyString, charge, value,-1, peptideString);


				//TODO - support this properly
				//needs to be able to count how many are non-tryptic
				if (Settings.sumAllPeptides)
				{
					DataLine d = new DataLine(proteinString, modString,dataColumnText);
					String prokeyString = d.proteinKey();

					if (!quant.containsKey(prokeyString))
						quant.put(prokeyString, d);

					quant.get(prokeyString).addQuant(pepKeyString, charge, value, score,  peptideString);


				}

			}








		}


	}

	static int condition0Size;

	static double mean(List<Double> input)
	{
		double count = 0;
		for (Double i: input)
			count += i;

		return count / input.size();

	}


	static double variance(List<Double> input)
	{
		double count = 0;
		double mean = mean(input);

		for (Double i: input)
			count += (i-mean) * (i-mean);

		return count / input.size();

	}




	public static void outputLine(Map<Double,String> out, String peptide , 		Map<String,Map<String,Double>> buffer ) throws IOException
	{

		StringBuffer line = new StringBuffer(1000);

		line.append("\""+peptide+"\"," );

		Map<String,Double> data = buffer.get(peptide);

		List<Double> set0 = new LinkedList<Double>();
		List<Double> set1 = new LinkedList<Double>();
		for (int i=0; i < columns.size(); i++)
		{
			int cNum = 0;
			if (i >= condition0Size)
				cNum = 1;

			String column = columns.get(i);

			if (data.containsKey(column))
			{
				if (cNum == 0)
					set0.add(data.get(column));
				else
					set1.add(data.get(column));
			}
		}
		//now we have two conditions separated

		double pVal = Double.MAX_VALUE;
	 
		if ((set0.size() > 1)  && (set1.size() > 1) ) 
		{
			double[] sample1 = new double[set0.size()];
			for (int i=0; i < set0.size(); i++)
				sample1[i] = set0.get(i);

			double[] sample2 = new double[set1.size()];
			for (int i=0; i < set1.size(); i++)
				sample2[i] = set1.get(i); 

			TTest t = new TTest();

			pVal = t.tTest(sample1,sample2);
	 

		}

		for (String column : columns)
		{
			String output = " , ";
			if (data.containsKey(column))
				output =  ""+ data.get(column)+",";

			line.append(output);

		}


		//		line.append("\n");

		out.put(pVal,line.toString());

	}

	public static boolean display(File outputFile)
	{

		System.err.println("Display");

		try
		{
			BufferedWriter w = new BufferedWriter(new FileWriter(outputFile));

			//buffer stores the normalised value for every unique neopeptide
			//(i.e. to tabulate the output data)
			//the map it contains is the value for each condition
			Map<String,Map<String,Double>> buffer = new TreeMap<String,Map<String,Double>>();

			//proteins stores which protein each neopeptide came from (for output)
			//if a neopeptide is reported for different proteins, each are treated separately
			//but may be ignored

			Map<String,String> proteins = new TreeMap<String,String>();


			//go through every neopeptide we have seen
			for (String key : quantPeps.keySet())
			{
				DataLine peptideData = quantPeps.get(key);

				if (!Settings.allowMultipleProteins)
					if (peptideProteins.get(peptideData.peptide).strings.size() > 1)
					{
						continue;
					}


				String proteinKey = peptideData.proteinKey();

				//check to see if the protein quant is vaguely reliable
				DataLine proteinData = quant.get(proteinKey);
				if (proteinData == null)
				{
					continue;
				}



				//	System.err.println(proteinData.getNumPeptides());
				if (proteinData.getNumPeptides() < Settings.MIN_PEPTIDES)
					continue;



				double protQuant =  proteinData.getValue(peptideData);

				double pepQuant = peptideData.getValue() ;
				
				
					double ratio = (pepQuant / protQuant);
				
				//	System.err.println(ratio +" , "+Math.log(ratio));
					if (Settings.LOG_RATIO)
						ratio = Math.log(ratio);
				//should only sum up the peptides that appear in a minimum number of conditions


				if ((protQuant > 0) && (pepQuant > 0) )
				{
					String peptide = peptideData.peptideKey() +"\",\""+proteinData.protein+"\",\""+peptideData.previous+"\",\""+peptideData.next;
					if (separate_mods)
						peptide = peptideData.peptideKey() +"\",\""+proteinData.protein+" "+proteinData.modification+"\",\""+peptideData.previous+"\",\""+peptideData.next;


					if (!buffer.containsKey(peptide))
					{

						buffer.put(peptide,new TreeMap<String,Double>());
						proteins.put(peptide, proteinData.protein );
					}
					Map<String,Double> proteinMap = buffer.get(peptide);
					String sample = proteinData.sample;


					proteinMap.put(sample, ratio);

				}
			}	


			System.err.println("writing");




			w.write("peptide,accession,protein,previous,next,");
			for (String column : columns)
			{
				w.write("\""+column +"\",");

			}

			w.write("\"p-value\",\"significant?\",\"adjusted p-value\"");


			Map<Double,String> outputBuffer = new TreeMap<Double,String>();


			w.write("\n");
			for (String peptide :  buffer.keySet())
			{

				outputLine(outputBuffer,peptide,buffer);

			}


			double FDR = Settings.FDR;
			int count = 1;
			int size = outputBuffer.size();

			int significant = 0;

			Map<Double,Double> adjustedKeys  = new TreeMap<Double,Double>(Collections.reverseOrder());
			Map<Double,Double> adjusted  = new TreeMap<Double,Double>();

			for (Double value : outputBuffer.keySet()) 
			{ 
				if (value == Double.MAX_VALUE)
						size --;

			}

			for (Double key : outputBuffer.keySet()) 
			{ 
				if (key < FDR * count / size)
				{
					significant = count;
				}

				double a = key *  size / count;
				adjusted.put(key, a);
				adjustedKeys.put(key, a);
				count++;
			}


			double last =-1;
			for (Double key : adjustedKeys.keySet()) 
			{ 
				if (last >= 0)
				{
					if (adjusted.get(key) > adjusted.get(last))
						adjusted.put(key,adjusted.get(last));

				}

				last = key;


			}


			count = 1;
			for (Double key : outputBuffer.keySet()) 
			{ 

				boolean isSignificant = false;
				 
				if (count <= significant)
				{
					isSignificant = true;
					//do correction
				}
				count++;
				String value = outputBuffer.get(key);

				Double adj = adjusted.get(key);

				if (key == Double.MAX_VALUE)
					value = value +" , , ";
				else
					value = value + key+" , "+isSignificant+", "+adj;



				// do something
				w.write(value+"\n");
			}


			w.close();
		} catch (Exception e) {e.printStackTrace();
		return false;
		}
		return true;
	}


	static class DataName
	{
		public Integer columnNo;
		public String name;

	}

	//search the progenesis csv file for the conditions
	public static int[] getConditions(File input, AutoDetectSettings a)
	{

		int lineNo = 0;

		int getColumn = -1;
		int getRow = 0;
		int headerLine = 0;
		int lastColumn = -1;

		int condition1Pos = -1;
		int condition0Pos = -1;


		String rawAbundance = Options.getSearch();
		try 
		{
			BufferedReader b = new BufferedReader(new FileReader(input));

			List<DataName>data = new LinkedList<DataName>();


			while (true)
			{
				String line = b.readLine();
				if (line == null)
					break;

				if (!a.tabDelimiter)
					line = AutoDetect.convertToTabs(line);

				lineNo ++;
				String[] cellsText = line.split("\t");

				for  (int i=0; i < cellsText.length; i++)
					cellsText[i] = Utils.trim(cellsText[i]);


				if (getRow == 0)
				{
					int column = getColumn(rawAbundance,cellsText);

					if (distance(rawAbundance, cellsText[column]) < 4)
					{

						for  (int i=column+1; i < cellsText.length; i++)
						{
							lastColumn = i;
							if (cellsText[i].length() > 0)
							{
								lastColumn = i-1;
								break;
							}

						}
						getColumn = column;
						getRow = lineNo +1;
						headerLine = 1;
					}

				}
				if ((getRow == lineNo) && (headerLine == 1) )
				{
					for (int i=getColumn; i <= lastColumn; i++)
					{
						if (cellsText[i].length() > 0)
						{
							DataName d = new DataName();
							d.columnNo = i;
							d.name = cellsText[i];

							if (condition0Pos < 0)
								condition0Pos = i;
							else
								if (condition1Pos < 0)
									condition1Pos = i;

							condition0Size = condition1Pos - condition0Pos;


							data.add(d);
						}

					}

					getRow = lineNo+1;
					if (data.size() > 0)
					{
						int numColumns=lastColumn - getColumn+1;
						int[] output = new int[numColumns];
						int j =0;
						for (int i=getColumn; i <= lastColumn; i++)
						{
							output[j++] = i;  
						}
						b.close();
						return output;
					}


				}


			}

			b.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;

	}

}
