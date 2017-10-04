package interfaces.network;

public interface Node<K,V> { //OK
	
	/* Immutable */
	
	String getName(); 	/* e.g. "gene_0" */

	Table<K,V> getFunction();
		
	Integer getId(); //? serve per avere un ordinamento per esempio usando le Map
	
	
}
