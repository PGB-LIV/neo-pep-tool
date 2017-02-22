package uk.ac.liv.iib.pgb.neo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//Represents a 'row' of data in the input file
public class Row
{
	List<Cell> data;	
	public Row()
	{
		data = new LinkedList<Cell>();
	}
	public Row(int size)
	{
		data = new ArrayList<Cell>(size);
	}

	public void add(Cell c)
	{
		data.add(c);
	}
	
	public List<Cell> getData()
	{
		return data;
	}
	public Cell get(int i)
	{
		return data.get(i);
	}
	
	
}