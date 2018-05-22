package visualization;

import interfaces.tes.Atm;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class AtmGraphViz implements Writable{

	private Map<Integer, String> mapIdAtmIndex;
	private Atm<?> atm;
	private GraphViz gz;
	public AtmGraphViz(Atm<?> atm){
		this.gz = new GraphViz("digraph", "atm");
		this.atm = atm;
		init();
		arcs();
	}

	private void init() {
		this.mapIdAtmIndex = new HashMap<>();
		int index = 0;
		for(String id : atm.getAttractors().getAttractors().stream().map(x -> x.getId() + "").collect(Collectors.toList())){
			this.mapIdAtmIndex.put(index++, id);
			gz.addLine(id + GraphViz.LABEL_START + id + GraphViz.LABEL_END);
		}
	}

	private void arcs() {
		for (int i = 0; i < this.atm.getMatrix().length; i++) {
			for (int j = 0; j < this.atm.getMatrix().length; j++) {
				if (this.atm.getMatrix()[i][j] > 0.0) {
					gz.addLine(this.mapIdAtmIndex.get(i)
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

	@Override
	public void saveOnDisk(String path) {
		this.gz.generateDotFile(path).generateImg("jpg");
	}

}
