package doser.word2vec;

import java.util.ArrayList;
import java.util.List;

public class Doc2VecJsonFormat {

	private List<Data> data;

	public Doc2VecJsonFormat() {
		super();
		this.data = new ArrayList<Data>();
	}

	public List<Data> getData() {
		return data;
	}

	public void setData(List<Data> data) {
		this.data = data;
	}

	public void addData(Data doc) {
		this.data.add(doc);
	}
	
}
