package visualization;

import interfaces.attractor.ImmutableAttractor;
import interfaces.state.State;
import interfaces.tes.DifferentiationNode;
import interfaces.tes.DifferentiationTree;
import interfaces.tes.Tes;
import utility.GenericUtility;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class DifferentiationTesTreeGraphViz<T extends State> extends DifferentiationTreeGraphViz<Tes<T>> {

    public DifferentiationTesTreeGraphViz(DifferentiationTree<Tes<T>> tree, String filenames) {
        super(tree, filenames);
        dashedEdges();
    }

    private void dashedEdges() {
        for (Iterator<DifferentiationNode<Tes<T>>> i = GenericUtility.safeClient(rootLevel).iterator(); i.hasNext();) {
            DifferentiationNode<Tes<T>> rootNode = i.next();
            if (rootNode.getLevel() == 0) {
                continue;
            }

            int levelDecrease = 1;
            while (rootNode.getLevel().intValue() - levelDecrease >= 0) {
                int levelInExam  = rootNode.getLevel().intValue() - levelDecrease;
                List<DifferentiationNode<Tes<T>>> previousLevel = mapLvlLabel.entrySet().stream()
                        .map(x -> x.getKey())
                        .filter(x -> x.getLevel().intValue() == levelInExam)
                        .collect(Collectors.toList());
                //////////////////////////////////////////////////////////////////
                for (ImmutableAttractor<T> attractor : rootNode.getWrappedElement().getTesAttractors()) {

                    List<DifferentiationNode<Tes<T>>> nodesFound = previousLevel.stream()
                            .filter(x -> x.getWrappedElement().getTesAttractors().contains(attractor))
                            .collect(Collectors.toList());
                    if (nodesFound.size() != 0) {
                        for (DifferentiationNode<Tes<T>> father : nodesFound) {
                            if (father.equals(rootNode)) { //per evitare self-loop
                                continue;
                            } else {
                                addLine(mapLvlLabel.get(father) + GraphViz.EDGE_START + mapLvlLabel.get(rootNode)
                                        + "[style = \"dashed\"]");
                            }
                        }
                    }
                }
                //////////////////////////////////////////////////////////////////
                levelDecrease++;
            }

        }
    }

    @Override
    protected String nodeInfo(DifferentiationNode<Tes<T>> DifferentiationNode) {
        return 	"\""
                + (DifferentiationNode.getWrappedElement().getName().isPresent() ? "TES:" + DifferentiationNode.getWrappedElement().getName().get() : "")
                + GraphViz.NEW_LINE_DOT_ESCAPE
                + "Lvl:" + DifferentiationNode.getLevel()
                + GraphViz.NEW_LINE_DOT_ESCAPE
                + DifferentiationNode.getWrappedElement().getTesAttractors().stream().map(x -> x.getId() + "").collect(Collectors.joining(", ", "Att[", "]"))
                + " #" + DifferentiationNode.getWrappedElement().getTesAttractors().size()
                + "\"";
    }


}
