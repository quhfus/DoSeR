package doser.gerbilwrapper;

import java.util.List;

/**
 * Optional class for position storage of a surface form in the original
 * document
 * 
 * @author quh
 * 
 */
public class Position {

	private BoundingBox boundingBox;

	private List<Integer> offsets;

	private Integer pageId;

	public BoundingBox getBoundingBox() {
		return this.boundingBox;
	}

	public List<Integer> getOffsets() {
		return this.offsets;
	}

	public Integer getPageId() {
		return this.pageId;
	}

	public void setBoundingBox(final BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setOffsets(final List<Integer> offsets) {
		this.offsets = offsets;
	}

	public void setPageId(final Integer pageId) {
		this.pageId = pageId;
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer(
				String.valueOf(this.pageId));
		for (final Integer integer : this.offsets) {
			final Integer off = integer;
			buffer.append(off);
		}
		if (this.boundingBox != null) {
			buffer.append(this.boundingBox.getMaxx());
			buffer.append(this.boundingBox.getMaxy());
			buffer.append(this.boundingBox.getMinx());
			buffer.append(this.boundingBox.getMiny());
		}
		return buffer.toString();
	}
}
