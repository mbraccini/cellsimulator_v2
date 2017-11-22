package visualization;

import interfaces.tes.DifferentiationNode;
import interfaces.tes.DifferentiationTree;
import utility.GenericUtility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class DifferentiationTreeGraphViz<T> extends GraphViz {


	protected Map<DifferentiationNode<T>, String> mapLvlLabel = new HashMap<>();
	protected List<DifferentiationNode<T>> rootLevel;
	protected DifferentiationTree<T> tree;

	public DifferentiationTreeGraphViz(DifferentiationTree<T> tree, String filenames){
		super("graph", "diffTree", filenames);
		this.tree = tree;
		this.rootLevel = tree.getRootLevel();
		//this.rootLevel.add(this.rootLevel.get(0)); //prova senza senso per vedere se aveva problemi con più radici, non ne ha!
		init();
		rank();
	}


	private void init(){
		int count = 0;
		for (Iterator<DifferentiationNode<T>> i = GenericUtility.safeClient(rootLevel).iterator(); i.hasNext();) {
			DifferentiationNode<T> DifferentiationNode = i.next();
			String rootId = "R_" + count;
			mapLvlLabel.put(DifferentiationNode, rootId); //add to map
			addLine(rootId + GraphViz.LABEL_START + nodeInfo(DifferentiationNode) + GraphViz.LABEL_END);
			configure(DifferentiationNode, rootId, null);

			count++;
		}
	}
	private void configure(DifferentiationNode<T> DifferentiationNodeFiglio, String childId, String fatherId) {
		if (fatherId != null) {
			addLine(fatherId + GraphViz.EDGE_START + childId + GraphViz.LABEL_START + this.tree.getThresholds().get(DifferentiationNodeFiglio.getLevel()) + GraphViz.LABEL_END);
		}
		int count = 0;
		for (Iterator<DifferentiationNode<T>> i = GenericUtility.safeClient(DifferentiationNodeFiglio.getChildren()).iterator(); i.hasNext();) {
			DifferentiationNode<T> DifferentiationNode = i.next();
			if (mapLvlLabel.get(DifferentiationNode) == null) {
				String childIdCreation = childId + "_C_" + DifferentiationNode.getLevel() + "_" + count;
				mapLvlLabel.put(DifferentiationNode, childIdCreation);
			}
			addLine(mapLvlLabel.get(DifferentiationNode) + GraphViz.LABEL_START + nodeInfo(DifferentiationNode) + GraphViz.LABEL_END);
			configure(DifferentiationNode, mapLvlLabel.get(DifferentiationNode), childId);
			count++;
		}

	}

	private void rank() {
		/*if (DifferentiationNode.getLevel() != 0){
		gz.addLine("{ rank=same;" + rootId + "; " +  mapLvlLabel.get(DifferentiationNode.getLevel()) + "}");
		}*/

		List<Integer> levels = mapLvlLabel.entrySet().stream()
														.mapToInt(x -> x.getKey().getLevel())
														.distinct()
														.boxed()
														.collect(Collectors.toList());

		for (Integer lvl : levels) {
			addLine(mapLvlLabel.entrySet().stream()
									.filter(x -> x.getKey().getLevel().intValue() == lvl)
									.map(x -> x.getValue())
									.collect(Collectors.joining("; ", "{ rank=same;", "}"))
					  );
		}

	}



	protected String nodeInfo(DifferentiationNode<T> DifferentiationNode){
		return 	"\""
				+ "Lvl:" + DifferentiationNode.getLevel()
				+ "\"";
	}



	



}
