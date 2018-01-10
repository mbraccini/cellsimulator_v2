package visualization;

import interfaces.tes.Atm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class AtmGraphViz extends GraphViz {
	
	private Map<Integer, String> mapIdAtmIndex; 
	private Atm<?> atm;
	public AtmGraphViz(Atm<?> atm, String filenames){
		super("digraph", "atm", filenames);
		this.atm = atm;
		init();
		arcs();
	}

	private void init() {
		this.mapIdAtmIndex = new HashMap<>();
		int index = 0;
		for(String id : atm.getAttractors().stream().map(x -> x.getId() + "").collect(Collectors.toList())){
			this.mapIdAtmIndex.put(index++, id);
			addLine(id + GraphViz.LABEL_START + id + GraphViz.LABEL_END);
		}
	}

	private void arcs() {
		for (int i = 0; i < this.atm.getMatrix().length; i++) {
			for (int j = 0; j < this.atm.getMatrix().length; j++) {
				if (this.atm.getMatrix()[i][j].compareTo(BigDecimal.ZERO) > 0.0) {
					addLine(this.mapIdAtmIndex.get(i)
								+ GraphViz.ARC_START
								+ this.mapIdAtmIndex.get(j)
								+ " "
								+ GraphViz.LABEL_START
								+ this.atm.getMatrix()[i][j]
								+ GraphViz.LABEL_END);
				}
			}
		}

	}
	
}
