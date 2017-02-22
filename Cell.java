package uk.ac.liv.iib.pgb.neo;

public class Cell
{
	//A cell in a data table
	private String base;
	private String lower;
	private double value;
	private boolean hasValue;
	
	public boolean hasValue()
	{
		return hasValue;
	}
	public String toString()
	{
		if (base == null)
			return ""+value;
		return base;
	}
	public double toDouble()
	{
		return value;
	}
	
	public boolean isTrue()
	{
		if (hasValue)
			return (value != 0);
		
		return (lower.equals("true"));
		
	}
	public Cell(String init)
	{
		value =  Double.NaN;
		
		init = Utils.trim(init);
		
		base = init;
		lower = base.toLowerCase();
		
		Double val = null;
		
		
		try
		{
			val = Double.parseDouble(init);
		}
		catch (Exception e)
		{
			
		}
		
		if (val != null)
		{
			hasValue = true;
			value = val;
			base = null;
		}
		
	}
	
	
}