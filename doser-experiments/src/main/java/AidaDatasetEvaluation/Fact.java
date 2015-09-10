package AidaDatasetEvaluation;


import java.util.List;

/**
 * TODO description POJO for json service input
 * 
 * { "type": "fact", "confidence": 0.83, "subject":{ "text":"SVM",
 * "normalized":"Support Vector Machine",
 * "uri":"http://en.dbpedia.org/page/Support_Vector_Machine", "confidence":
 * 0.83, "positions": [ ... ] }, "predicate":{ "text":"is a",
 * "uri":"http://somerdfvocabulary.org/isA", "confidence": 0.83, "positions": [
 * ... ] }, "object":{ "text":"supervised classifier",
 * "normalized":"Supervised Classification",
 * "uri":"http://en.dbpedia.org/page/Supervised_Classification", "confidence":
 * 0.83, "positions": [ ... ] } }
 */

public class Fact {

	public class SubjectPredObject {

		private double confidence;
		private String normalized;
		private List<Position> positions;
		private String text;
		private String uri;

		public double getConfidence() {
			return this.confidence;
		}

		public String getNormalized() {
			return this.normalized;
		}

		public List<Position> getPositions() {
			return this.positions;
		}

		public String getText() {
			return this.text;
		}

		public String getUri() {
			return this.uri;
		}

		public void setConfidence(final double confidence) {
			this.confidence = confidence;
		}

		public void setNormalized(final String normalized) {
			this.normalized = normalized;
		}

		public void setPositions(final List<Position> positions) {
			this.positions = positions;
		}

		public void setText(final String text) {
			this.text = text;
		}

		public void setUri(final String uri) {
			this.uri = uri;
		}
	}

	private double confidence;
	private SubjectPredObject object;
	private SubjectPredObject predicate;
	private SubjectPredObject subject;
	private String type;

	public double getConfidence() {
		return this.confidence;
	}

	public SubjectPredObject getObject() {
		return this.object;
	}

	public SubjectPredObject getPredicate() {
		return this.predicate;
	}

	public SubjectPredObject getSubject() {
		return this.subject;
	}

	public String getType() {
		return this.type;
	}

	public void setConfidence(final double confidence) {
		this.confidence = confidence;
	}

	public void setObject(final SubjectPredObject object) {
		this.object = object;
	}

	public void setPredicate(final SubjectPredObject predicate) {
		this.predicate = predicate;
	}

	public void setSubject(final SubjectPredObject subject) {
		this.subject = subject;
	}

	public void setType(final String type) {
		this.type = type;
	}
}
