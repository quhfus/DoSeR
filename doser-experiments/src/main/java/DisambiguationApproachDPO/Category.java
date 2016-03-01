package DisambiguationApproachDPO;

public class Category implements Comparable<Category>{

	private String url;
	
	private String label;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int compareTo(Category o) {
		if(o.getUrl().length() > url.length()) {
			return -1;
		} else if(o.getUrl().length() < url.length()) {
			return 1;
		} else {
			return 0;
		}
	}
}
