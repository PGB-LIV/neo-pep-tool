package uk.ac.liv.iib.pgb.neo;

public class AutoDetectSettings {

	//The settings associated with an input file
	//Where the peptide column us
	public int peptideColumn;
	//Where the header row is
	public int headerRow;
	//How much of the file are header rows (and hence not data)
	public int headerSize;
	//is the data tab delimited (if not, it will be comma)
	public boolean tabDelimiter;
	
}
