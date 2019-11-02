package utility;

import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.qual.*;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSBMLvisualizer extends JFrame {

private static final long serialVersionUID = 6864318867423022411L;

        public JSBMLvisualizer(SBase tree) {
         super("SBML Structure Visualization");
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         getContentPane().add(new JScrollPane(new JTree(tree)));
        pack();
        setAlwaysOnTop(true);
        setLocationRelativeTo(null);
        setVisible(true);
 }


public static void main(String[] args) throws Exception {
           UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    SBMLDocument Doc = SBMLReader.read(new File("29555_v1.sbml"));
    new JSBMLvisualizer(Doc);

    //doc.getModel().getListOfSpecies()..stream().forEach(System.out::println);
    //System.out.println(doc.getModel().getListOfFunctionDefinitions());
    //System.out.println(doc.getModel().children().nextElement());


    Model model = Doc.getModel();



    List<FunctionDefinition> fun =  model.getListOfFunctionDefinitions();
    for (FunctionDefinition ff:fun
         ) {
            System.out.println(ff);
    }
    String[] ids = Doc.metaIds().toArray(new String[0]);
    System.out.println(ids);
    for (String id:ids) {
        SBase el = Doc.getElementByMetaId(id);
        if (el instanceof QualitativeSpecies){
            QualitativeSpecies a = (QualitativeSpecies) el;
            System.out.println(a.getName());

        }

    }

    Map<String, String> idFunction = new HashMap<>();
    Pattern pattern = Pattern.compile(".*\\|\\|.*|.*!.*|.*&&.*|.*==.*");
    BFS(model,0, pattern, idFunction);
    System.out.println(idFunction);
    System.out.println(idFunction.size());


/*
    System.out.println(model.getSBaseCount());

    */

}

private static void BFS (TreeNode n, int level, Pattern pattern, Map<String,String>  idFunction) {
    Matcher matcher;
    List<TreeNode> children = new ArrayList<>();
    for (int i = 0; i < n.getChildCount(); i++) {
        TreeNode nodo = n.getChildAt(i);
        if (nodo instanceof QualitativeSpecies) {
            QualitativeSpecies nodoQS = (QualitativeSpecies) nodo;
            System.out.println("QualitativeSpecies: " + nodoQS.getName() + ", " + nodoQS.getId());
        }
        if (nodo instanceof Transition) {
            Transition nodoTRN = (Transition) nodo;
            String id = nodoTRN.getId();
            //System.out.println("Transition ID: " + id);
            retrieveFunction(id, nodoTRN, idFunction, pattern);
        }
        /*if (nodo instanceof FunctionTerm) {
            FunctionTerm nodoFUN = (FunctionTerm) nodo;
            System.out.println("FunctionTerm: " + nodoFUN);
            System.out.println("FunctionTerm: " + nodoFUN.getId());
            System.out.println("FunctionTerm: " + nodoFUN.getNotes());
            System.out.println("FunctionTerm: " + nodoFUN.getNumChildren());
            System.out.println("FunctionTerm: " + nodoFUN.getMetaId());
            System.out.println("FunctionTerm: " + nodoFUN.getParent().getParent());

            for (int j = 0; j < nodoFUN.getChildCount(); j++) {
                TreeNode nodo2 = nodoFUN.getChildAt(j);
                matcher = pattern.matcher(nodo2.toString());
                System.out.println("STRINGA: " + nodo2.toString());
                if (matcher.matches())
                    System.out.println("MATCH " + nodo2);
                else
                    System.out.println("NOT " + nodo2);

            }
        }*/

        children.add(nodo);
    }


    for (TreeNode child : children){
        BFS(child, ++level,  pattern, idFunction);
    }
}

private static void retrieveFunction(String id, TreeNode n, Map<String,String> idFunction, Pattern pattern)
{
    if (n instanceof FunctionTerm) {
        for (int j = 0; j < n.getChildCount(); j++) {
            TreeNode nodo2 = n.getChildAt(j);
            Matcher matcher = pattern.matcher(nodo2.toString());
            if (matcher.matches()) {
                idFunction.put(id, nodo2.toString());
                return;
            }
        }
    } else {
        for (int i = 0; i < n.getChildCount(); i++) {
            retrieveFunction(id, n.getChildAt(i), idFunction, pattern);
        }
    }
}


}

