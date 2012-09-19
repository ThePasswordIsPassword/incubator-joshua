package joshua.decoder.hypergraph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

import joshua.decoder.ff.state_maintenance.DPState;
import joshua.decoder.ff.state_maintenance.StateComputer;

/**
 * this class implement (1) HyperGraph-related data structures (Item and Hyper-edges)
 * 
 * Note: to seed the kbest extraction, each deduction should have the best_cost properly set. We do
 * not require any list being sorted
 * 
 * @author Zhifei Li, <zhifei.work@gmail.com>
 */
public class HyperGraph {

  // pointer to goal HGNode
  public HGNode goalNode = null;

  public int numNodes = -1;
  public int numEdges = -1;
  public int sentID = -1;
  public int sentLen = -1;

  static final Logger logger = Logger.getLogger(HyperGraph.class.getName());

  public HyperGraph(HGNode goalNode, int numNodes, int numEdges, int sentID, int sentLen) {
    this.goalNode = goalNode;
    this.numNodes = numNodes;
    this.numEdges = numEdges;
    this.sentID = sentID;
    this.sentLen = sentLen;
  }

  public void dump(String fileName) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(fileName));

      HashMap<HGNode,Integer> printedNodes = new HashMap<HGNode,Integer>();
      
      dump(out, this.goalNode, printedNodes);
      out.close();
      
    } catch (IOException e) {
      System.err.println("* Can't dump hypergraph to file '" + fileName + "'");
      e.printStackTrace();
    }
  }

  private void dump(BufferedWriter out, HGNode node, HashMap<HGNode, Integer> printedNodes) {
    if (printedNodes.containsKey(node))
      return;
    
    printedNodes.put(node, 1);
    for (HyperEdge edge : node.getHyperEdges()) {
      if (edge.getTailNodes() != null)
        for (HGNode tailNode : edge.getTailNodes()) {
          dump(out, tailNode, printedNodes);
      }
    }

    try {
//      System.err.println("DUMP: " + Integer.toHexString(node.hashCode()) + " ||| " + node);
      out.write(node.toString());
      out.newLine();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public double bestLogP() {
    return this.goalNode.bestHyperedge.bestDerivationLogP;
  }

  // === merge two hypergraphs at root node
  public static HyperGraph mergeTwoHyperGraphs(HyperGraph hg1, HyperGraph hg2) {
    List<HyperGraph> hgs = new ArrayList<HyperGraph>();
    hgs.add(hg1);
    hgs.add(hg2);
    return mergeHyperGraphs(hgs);
  }

  public static HyperGraph mergeHyperGraphs(List<HyperGraph> hgs) {
    // Use the first hg to get i, j, lhs, sentID, sentLen.
    HyperGraph hg1 = hgs.get(0);
    int sentID = hg1.sentID;
    int sentLen = hg1.sentLen;

    // Create new goal node.
    int goalI = hg1.goalNode.i;
    int goalJ = hg1.goalNode.j;
    int goalLHS = hg1.goalNode.lhs;
    TreeMap<StateComputer, DPState> goalDPStates = null;
    double goalEstTotalLogP = -1;
    HGNode newGoalNode = new HGNode(goalI, goalJ, goalLHS, goalDPStates, null, goalEstTotalLogP);;

    // Attach all edges under old goal nodes into the new goal node.
    int numNodes = 0;
    int numEdges = 0;
    for (HyperGraph hg : hgs) {
      // Sanity check if the hgs belongs to the same source input.
      if (hg.sentID != sentID || hg.sentLen != sentLen || hg.goalNode.i != goalI
          || hg.goalNode.j != goalJ || hg.goalNode.lhs != goalLHS) {
        logger.severe("hg belongs to different source sentences, must be wrong");
        System.exit(1);
      }

      newGoalNode.addHyperedgesInNode(hg.goalNode.hyperedges);
      numNodes += hg.numNodes;
      numEdges += hg.numEdges;
    }
    numNodes = numNodes - hgs.size() + 1;

    return new HyperGraph(newGoalNode, numNodes, numEdges, sentID, sentLen);
  }

  // ####### template to explore hypergraph #########################
  /*
   * private HashSet<HGNode> processHGNodesTbl = new HashSet<HGNode>();
   * 
   * private void operationHG(HyperGraph hg){ processHGNodesTbl.clear(); operationNode(hg.goalNode);
   * }
   * 
   * private void operationNode(HGNode it){ if(processHGNodesTbl.contains(it)) return;
   * processHGNodesTbl.add(it);
   * 
   * //=== recursive call on each edge for(HyperEdge dt : it.hyperedges){ operationEdge(dt); }
   * 
   * //=== node-specific operation }
   * 
   * private void operationEdge(HyperEdge dt){
   * 
   * //=== recursive call on each ant node if(dt.getAntNodes()!=null) for(HGNode ant_it :
   * dt.getAntNodes()) operationNode(ant_it);
   * 
   * //=== edge-specific operation Rule rl = dt.getRule(); if(rl!=null){
   * 
   * } }
   */
  // ############ end ##############################
}
