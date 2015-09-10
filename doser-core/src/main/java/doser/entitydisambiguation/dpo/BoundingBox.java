package doser.entitydisambiguation.dpo;

public class BoundingBox {

	private String maxx;

	private String maxy;

	private String minx;

	private String miny;

	public String getMaxx() {
		return this.maxx;
	}

	public String getMaxy() {
		return this.maxy;
	}

	public String getMinx() {
		return this.minx;
	}

	public String getMiny() {
		return this.miny;
	}

	public void setMaxx(final String maxx) {
		this.maxx = maxx;
	}

	public void setMaxy(final String maxy) {
		this.maxy = maxy;
	}

	public void setMinx(final String minx) {
		this.minx = minx;
	}

	public void setMiny(final String miny) {
		this.miny = miny;
	}
}
