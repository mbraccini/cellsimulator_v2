package interfaces.network;

import exceptions.RowNotFoundException;

import java.util.List;

public interface Table<K,V> { //OK
	
	/* Immutable */
		
	Integer getVariablesNumber(); // number of variables 
	
	List<Row<K,V>> getRows(); // with respect to a lexicographical order with leftmost more important
	
	Row<K,V> getRow(Integer index); // with respect to a lexicographical order with leftmost more important
	
	Row<K,V> getRowByInput(K input) throws RowNotFoundException; // e.g. "010"

	<S,P> Table<S,P> newInstance(int variablesNumber, List<Row<S,P>> rowsToCopy);


		/* Mutable */
	
	/* If we want to change the table, we must recreate it*/
}
