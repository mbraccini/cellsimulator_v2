package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import exceptions.RowNotFoundException;
import interfaces.network.Row;
import interfaces.network.Table;

public abstract class AbstractTable<K,V> implements Table<K,V> {

	protected int variablesNumber;
	protected int rowsNumber;
	protected List<Row<K,V>> rows;
	protected Map<K, Row<K,V>> mapRows;



	public AbstractTable(int variablesNumber){
		this.variablesNumber = variablesNumber;
		
		this.rowsNumber = new Double(Math.pow(2, variablesNumber)).intValue(); //2^(variablesNumber)
		
		this.rows = new ArrayList<Row<K,V>>(this.rowsNumber);
		this.mapRows = new HashMap<>();
	}
	
	protected abstract void configure();
	
	
	@Override
	public Integer getVariablesNumber() {
		return this.variablesNumber;
	}

	@Override
	public List<Row<K,V>> getRows() {
		return this.rows;
	}

	@Override
	public Row<K,V> getRow(Integer index) {
		return this.rows.get(index);
	}
	
	@Override
	public Row<K,V> getRowByInput(K input) throws RowNotFoundException {
		Row<K,V> row = this.mapRows.get(input);
		if (row != null) {
			return row;
		}
		throw new RowNotFoundException();
	}

	@Override
	public String toString() {

		String s = "(inputOrder=Xn,...,X0) \n" + this.rows.stream().map( r-> {return r.getInput()
				+ " | "+
				r.getOutput().toString();})
				.collect(Collectors.joining(" \n", "----------\n", "\n----------"));
		return s;
	}
	

}
