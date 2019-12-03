package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials. If both are defined, a 1x1 tile checker
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {

	/**
	 * The second material, if non-null is used to produce a checker board pattern.
	 */
	Material material2 = null;

	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d(0, 1, 0);

	/**
	 * Default constructor
	 */
	public Plane() {
		super();
	}

	@Override
	public void intersect(Ray ray, IntersectResult result, boolean shadow) {

		double denom = n.dot(ray.viewDirection);

		if (denom < -.0000001) {
			Vector3d p = new Vector3d();
			p.sub(new Point3d(0, 0, 0), ray.eyePoint);

			double t = p.dot(n) / denom;

			result.t = Math.abs(t);
			result.n.normalize();
			ray.getPoint(t, result.p);
			result.n = new Vector3d(0, 1, 0);
			if (material2 != null) {
				if (result.p.x < 0) {
					if (Math.abs(result.p.x) % 2 < 1) {
						if (result.p.z < 0) {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material;
							} else {
								result.material = material2;
							}
						} else {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material2;
							} else {
								result.material = material;
							}
						}
					} else {
						if (result.p.z < 0) {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material2;
							} else {
								result.material = material;
							}
						} else {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material;
							} else {
								result.material = material2;
							}
						}
					}
				} else {
					if (Math.abs(result.p.x) % 2 < 1) {
						if (result.p.z < 0) {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material2;
							} else {
								result.material = material;
							}
						} else {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material;
							} else {
								result.material = material2;
							}
						}
					} else {
						if (result.p.z < 0) {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material;
							} else {
								result.material = material2;
							}
						} else {
							if (Math.abs(result.p.z) % 2 < 1) {
								result.material = material2;
							} else {
								result.material = material;
							}
						}
					}
				}
			} else {
				result.material = material;
			}
		}

	}

}
