package comp557.a4;

import javax.vecmath.Color4f;
import javax.vecmath.Point3d;

public class Light {

	/** Light name */
	public String name = "";

	/** Light colour, default is white */
	public Color4f color = new Color4f(1, 1, 1, 1);

	/** Light position, default is the origin */
	public Point3d from = new Point3d(0, 0, 0);

	/** Light intensity, I, combined with colour is used in shading */
	public double power = 1.0;

	/** Type of light, default is a point light */
	public String type = "point";

	public Light[][] area;

	public int size = 0;

	/**
	 * Default constructor
	 */
	public Light() {
		// do nothing
	}

	public void testType() {
		if (type.equals("area")) {
			area = new Light[size][size];
			for (int i = 0; i < size; i++) {
				for (int j = 0; j < size; j++) {
					Light l = new Light();
					l.power = this.power;
					l.color = this.color;
					l.from = new Point3d(from.x + (j * .5), from.y + (i * .5), from.z);
					area[i][j] = l;
				}
			}
		}
	}
}
