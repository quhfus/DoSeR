package experiments.collective.entdoccentric.dpo;

import java.util.Iterator;
import java.util.List;

public class Position {

	private Integer pageId;

	private List<Integer> offsets;

	private BoundingBox boundingBox;

	public Integer getPageId() {
		return pageId;
	}

	public void setPageId(Integer pageId) {
		this.pageId = pageId;
	}

	public List<Integer> getOffsets() {
		return offsets;
	}

	public void setOffsets(List<Integer> offsets) {
		this.offsets = offsets;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	@Override
	public String toString() {
		String position = String.valueOf(pageId);
		for (Iterator<Integer> iterator = offsets.iterator(); iterator
				.hasNext();) {
			Integer i = (Integer) iterator.next();
			position += i;
		}
		if (boundingBox != null) {
			position += boundingBox.getMaxx();
			position += boundingBox.getMaxy();
			position += boundingBox.getMinx();
			position += boundingBox.getMiny();
		}
		return position;
	}
}
