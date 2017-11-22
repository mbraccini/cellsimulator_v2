package visualization;

import java.io.IOException;

import utility.Files;

public abstract class GraphViz {
	
	public static final String NEW_LINE_DOT_ESCAPE = "\\n";
	public static final String LABEL_START = "[label = ";
	public static final String LABEL_END = "];";
	public static final String EDGE_START = " -- ";
	public static final String EDGE_END = ";";
	public static final String ARC_START = " -> ";
	public static final String ARC_END = ";";


	public static final String Linux = "/usr/bin/dot";
	public static final String Windows = "c:/Program Files (x86)/Graphviz 2.28/bin/dot.exe";
	public static final String MacOSX = "/usr/local/bin/dot";

	private String graph = "", dotPath, graphType, graphName, filenames;

	public GraphViz(String graphType, String graphName, String filenames) {
		String os = System.getProperty("os.name").replaceAll("\\s", "");
		switch (os) {
			case "Linux" 	: this.dotPath = Linux; break;
			case "Windows" 	: this.dotPath = Windows; break;
			case "MacOSX" 	: this.dotPath = MacOSX; break;
		}
		this.graphType = graphType;
		this.graphName = graphName;
		this.filenames = filenames;
	}

	protected String starting(){
		return graphType + " " + graphName +" { " 		+ Files.NEW_LINE
						+ "forcelabels=true; " 			+ Files.NEW_LINE
						+ "node [fontname=\"Arial\"];" 	+ Files.NEW_LINE;
	}

	protected void addLine(String line) {
		graph += line + Files.NEW_LINE;
	}

	protected String ending() {
		return "}"; //end
	}

	private boolean dotFileAlreadyGenerated = false;
	public GraphViz generateDotFile() {
		if (!dotFileAlreadyGenerated) {
			StringBuilder sb = new StringBuilder();
			graph = sb.append(starting()).append(graph).append(ending()).toString();
		}
		String dotFilename = filenames + ".gv";
		Files.writeStringToFile2(dotFilename, graph);
		dotFileAlreadyGenerated = true;
		return this;
	}

	public void generateImg(String type) {
		if (!dotFileAlreadyGenerated) return;

		String imgFilename = filenames + "." + type;

		try {
			Runtime rt = Runtime.getRuntime();
	        String[] args = {dotPath, "-T" + type, filenames + ".gv", "-o", imgFilename};
	        Process p = rt.exec(args);
	        p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
