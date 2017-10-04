package network;

import interfaces.network.Row;

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

}
