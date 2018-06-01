package network;

import java.util.*;
import java.util.stream.Collectors;

import exceptions.RowNotFoundException;
import interfaces.network.Row;
import interfaces.network.Table;

public abstract class AbstractTable<K,V> implements Table<K,V> {

	protected int variablesNumber;
	protected List<Row<K,V>> rows;
	protected Map<K, Row<K,V>> mapRows;

	public AbstractTable(int variablesNumber){
		this.variablesNumber = variablesNumber;

		this.rows = new ArrayList<Row<K,V>>();
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

	@Override
	public final boolean equals(Object o) {

		if (this == o) return true;

		if (!(o instanceof AbstractTable))
			return false;

		AbstractTable<?, ?> that = (AbstractTable<?, ?>) o;
		return variablesNumber == that.variablesNumber &&
				Objects.equals(rows, that.rows) &&
				Objects.equals(mapRows, that.mapRows);
	}

	@Override
	public int hashCode() {

		return Objects.hash(variablesNumber, rows, mapRows);
	}

	@Override
	public <S,P> Table<S,P> newInstance(int variablesNumber, List<Row<S,P>> rowsToCopy){
		return new ConfigurableGenericTable<>(variablesNumber,rowsToCopy);
	}

}
