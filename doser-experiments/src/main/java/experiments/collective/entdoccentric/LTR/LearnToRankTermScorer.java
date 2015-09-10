package experiments.collective.entdoccentric.LTR;

import java.io.IOException;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;

/** Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class LearnToRankTermScorer extends Scorer {
  private final DocsEnum docsEnum;
  private final Similarity.ExactSimScorer docScorer;
  private final int docFreq;
  
  /**
   * Construct a <code>TermScorer</code>.
   * 
   * @param weight
   *          The weight of the <code>Term</code> in the query.
   * @param td
   *          An iterator over the documents matching the <code>Term</code>.
   * @param docScorer
   *          The </code>Similarity.ExactSimScorer</code> implementation 
   *          to be used for score computations.
   * @param docFreq
   *          per-segment docFreq of this term
   */
  LearnToRankTermScorer(Weight weight, DocsEnum td, Similarity.ExactSimScorer docScorer, int docFreq) {
    super(weight);
    this.docScorer = docScorer;
    this.docsEnum = td;
    this.docFreq = docFreq;
  }

  @Override
  public int docID() {
    return docsEnum.docID();
  }

  @Override
  public int freq() throws IOException {
    return docsEnum.freq();
  }

  /**
   * Advances to the next document matching the query. <br>
   * 
   * @return the document matching the query or NO_MORE_DOCS if there are no more documents.
   */
  @Override
  public int nextDoc() throws IOException {
    return docsEnum.nextDoc();
  }
  
  @Override
  public float score() throws IOException {
    assert docID() != NO_MORE_DOCS;
    return docScorer.score(docsEnum.docID(), docsEnum.freq());  
  }

  /**
   * Advances to the first match beyond the current whose document number is
   * greater than or equal to a given target. <br>
   * The implementation uses {@link DocsEnum#advance(int)}.
   * 
   * @param target
   *          The target document number.
   * @return the matching document or NO_MORE_DOCS if none exist.
   */
  @Override
  public int advance(int target) throws IOException {
    return docsEnum.advance(target);
  }

  /** Returns a string representation of this <code>TermScorer</code>. */
  @Override
  public String toString() { return "scorer(" + weight + ")"; }

  // TODO: benchmark if the specialized conjunction really benefits
  // from this, or if instead its from sorting by docFreq, or both

  DocsEnum getDocsEnum() {
    return docsEnum;
  }
  
  // TODO: generalize something like this for scorers?
  // even this is just an estimation...
  
  int getDocFreq() {
    return docFreq;
  }
}