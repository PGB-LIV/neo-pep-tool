package uk.ac.liv.iib.pgb.neo;

public class Utils 
{

	// trim function to also remove quotes correctly
	public static String trim(String input)
	{
		String output = input.trim();
		int length = output.length();
		if (length < 1)
			return output;

		char first = output.charAt(0);
		char last = output.charAt(length-1);
		if ((first == '\'' || first =='\"')
				&& (last == '\'' || last =='\"') )
		{
			return output.substring(1,length-1);
		}

		if (first == '\'' || first =='\"')
		{
			return output.substring(1);
		}
		if (last == '\'' || last =='\"')
		{
			return output.substring(0,length-1);
		}

		return output;
	}
}
