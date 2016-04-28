package joshua.decoder.hypergraph;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Stack;

import joshua.decoder.ff.tm.Rule;
import joshua.decoder.hypergraph.KBestExtractor.DerivationState;
import joshua.decoder.hypergraph.KBestExtractor.DerivationVisitor;

/**
 * This class enables extraction of word-level alignments from hypotheses.
 * It implements two interfaces, WalkerFunction and DerivationVisitor.
 * The former is for using the Viterbi walk function, the latter is for
 * k-best extraction.
 * Intermediate WordAlignmentStates are placed on a stack and/or merged down
 * if possible.
 * @author fhieber
 */
public class WordAlignmentExtractor implements WalkerFunction, DerivationVisitor {
  
  private final Stack<WordAlignmentState> stack = new Stack<WordAlignmentState>();

  /**
   * Merges a state with the top of the stack if applicable or places it on top of the stack.
   */
  private void merge(final WordAlignmentState state) {
    // if alignment state has no NTs left AND stack is not empty
    // and parent state on stack still needs something to substitute
    if (!stack.isEmpty()
        && state.isComplete()
        && !stack.peek().isComplete()) {
      final WordAlignmentState parentState = stack.pop();
      parentState.substituteIn(state);
      merge(parentState);
    } else {
      stack.add(state);
    }
  }
  
  /**
   * Common entry point for WalkerFunction and DerivationVisitor.
   */
  private void extract(final Rule rule, final int spanStart) {
    if (rule != null) {
      merge (new WordAlignmentState(rule, spanStart));
    }
  }
  
  /**
   * entry for Viterbi walker. Calls word alignment extraction
   * for best hyperedge from given node.
   */
  @Override
  public void apply(HGNode node) {
    extract(node.bestHyperedge.getRule(), node.i);
  }
  
  /**
   * Visiting a node during k-best extraction is the same as
   * apply() for Viterbi extraction but using the edge from
   * the Derivation state.
   */
  @Override
  public void before(final DerivationState state, final int level) {
    extract(state.edge.getRule(), state.parentNode.i);
  }

  /**
   * Nothing to do after visiting a node.
   */
  @Override
  public void after(final DerivationState state, final int level) {}
  
  /**
   * Final word alignment without sentence markers
   * or empty list if stack is empty.
   */
  public List<List<Integer>> getFinalWordAlignments() {
    if (stack.isEmpty()) {
      return emptyList();
    }
    
    if (stack.size() != 1) {
      throw new RuntimeException(
          String.format(
              "Stack of WordAlignmentExtractor should contain only a single (last) element, but was size %d", stack.size()));
    }
    
    return stack.peek().toFinalList();
  }
  
  /**
   * Returns a String representation of the (final) word alignment
   * state on top of the stack.
   * Empty string for empty stack.
   */
  @Override
  public String toString() {
    if (stack.isEmpty()) {
      return "";
    }
    
    if (stack.size() != 1) {
      throw new RuntimeException(
          String.format(
              "Stack of WordAlignmentExtractor should contain only a single (last) element, but was size %d", stack.size()));
    }
    
    return stack.peek().toFinalString();
  }
}