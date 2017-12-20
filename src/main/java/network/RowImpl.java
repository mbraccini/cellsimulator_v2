package network;

import interfaces.network.Row;

import java.util.Objects;

public class RowImpl<K,V> implements Row<K,V> {

	protected K input;
	protected V output;
	
	public RowImpl(K input, V output){
		this.input = input;
		this.output = output;
	}
	
	@Override
	public K getInput() {
		return input;
	}

	@Override
	public V getOutput() {
		return output;
	}

	@Override
	public void setInput(K value){
		this.input = value;
	}
	
	@Override
	public void setOutput(V value){
		this.output = value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RowImpl<?, ?> row = (RowImpl<?, ?>) o;
		return Objects.equals(input, row.input) &&
				Objects.equals(output, row.output);
	}

	@Override
	public int hashCode() {

		return Objects.hash(input, output);
	}
}
