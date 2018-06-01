package visualization;

import interfaces.attractor.Attractors;
import interfaces.attractor.ImmutableAttractor;
import interfaces.tes.Atm;


public class AtmGraphViz implements Writable{

	private final Attractors<?> attractors;
	private Atm<?> atm;
	private GraphViz gz;
	public AtmGraphViz(Atm<?> atm){
		this.gz = new GraphViz("digraph", "atm");
		this.atm = atm;
		this.attractors = atm.getAttractors();
		init();
		arcs();
	}

	private void init() {
		int max = attractors.getAttractors().stream().mapToInt(x -> x.getBasinSize().get()).max().getAsInt();
		//int min = attractors.getAttractors().stream().mapToInt(x -> x.getBasinSize().get()).min().getAsInt();
		for(ImmutableAttractor<?> att: attractors.getAttractors()){
			double radius = (((double) att.getBasinSize().get()) / max) * 0.8 + 0.2;
			gz.addLine(att.getId()
						+ GraphViz.LABEL_START
							+ att.getId()
							+ ", shape=circle, fixedsize=true, width=" + radius
						+ GraphViz.LABEL_END);
		}
	}

	private void arcs() {
		double max = 1.0;
		for (int i = 0; i < this.atm.getMatrix().length; i++) {
			for (int j = 0; j < this.atm.getMatrix().length; j++) {
				if (this.atm.getMatrix()[i][j] > 0.0) {
					gz.addLine(attractors.getAttractors().get(i).getId()
								+ GraphViz.ARC_START
								+ attractors.getAttractors().get(j).getId()
								+ GraphViz.LABEL_START
								+ "\" " + this.atm.getMatrix()[i][j] + " \""
								+ ", penwidth = " + ((this.atm.getMatrix()[i][j] / max) * 4.0 + 0.1)
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
