package doser.webclassify.dpo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DocumentStatistic<K, V> {

	private List<ParagraphStatistic> paragraphs;
	private List<Map.Entry<K, V>> documentStatistic;

	public DocumentStatistic() {
		super();
		this.paragraphs = new LinkedList<ParagraphStatistic>();
	}

	public List<ParagraphStatistic> getParagraphs() {
		return paragraphs;
	}

	public void setParagraphs(List<ParagraphStatistic> paragraphs) {
		this.paragraphs = paragraphs;
	}

	public void addStatistic(String ID, String headline, String content,
			String topic, List<Map.Entry<K, V>> list) {
		this.paragraphs.add(new ParagraphStatistic(ID, headline, content,
				topic, list));
	}

	public List<Map.Entry<K, V>> getDocumentStatistic() {
		return documentStatistic;
	}

	public void setDocumentStatistic(List<Map.Entry<K, V>> documentStatistic) {
		this.documentStatistic = documentStatistic;
	}

	public class ParagraphStatistic {

		private String headline;

		private String content;

		private String id;

		private String topic;

		private List<Map.Entry<K, V>> statistic;

		public ParagraphStatistic(String id, String headline, String content,
				String topic, List<Map.Entry<K, V>> statistic) {
			super();
			this.id = id;
			this.headline = headline;
			this.content = content;
			this.statistic = statistic;
			this.topic = topic;
		}

		public String getHeadline() {
			return headline;
		}

		public void setHeadline(String headline) {
			this.headline = headline;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getTopic() {
			return topic;
		}

		public void setTopic(String topic) {
			this.topic = topic;
		}

		public List<Map.Entry<K, V>> getStatistic() {
			return statistic;
		}

		public void setStatistic(List<Map.Entry<K, V>> statistic) {
			this.statistic = statistic;
		}
	}
}
