package uk.ac.liv.iib.pgb.neo;

public class Settings 
{
	public static Boolean	allowMultipleProteins = false;
	public static Boolean	sumAllPeptides = false;
	public static int MIN_PEPTIDES = 3;
	
	//how many conditions are allowed to be missing for a peptide to not count
	//as part of the protein quant
	public static int MISSED_CONDITIONS = 0;
	public static Boolean MISS_CHARGE_STATES = true;

	public static Boolean	LOG_RATIO = false;
	
	public static double FDR = 0.05;
}
