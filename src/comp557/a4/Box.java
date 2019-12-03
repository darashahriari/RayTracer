package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see
 * max) corneray.
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	public Sphere bounding = null;

	/**
	 * Default constructoray. Creates a 2x2x2 box centered at (0,0,0)
	 */
	public Box() {
		super();
		this.max = new Point3d(1, 1, 1);
		this.min = new Point3d(-1, -1, -1);
	}

	public void createBounding() {
		Point3d center = new Point3d((max.x + min.x) / 2, (max.y + min.y) / 2, (max.z + min.z) / 2);
		Vector3d diameter = new Vector3d();
		diameter.sub(min, max);
		double radius = diameter.length() / 2;
		this.bounding = new Sphere(radius, center, material);
	}

	@Override
	public void intersect(Ray ray, IntersectResult result, boolean shadow) {

		IntersectResult boundingResult = new IntersectResult();
		bounding.intersect(ray, boundingResult, shadow);
		if (boundingResult.t != Double.POSITIVE_INFINITY || shadow) {
			double txmin, txmax, tymin, tymax, tzmin, tzmax;
			if (ray.viewDirection.x >= 0) {
				txmin = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
				txmax = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
			} else {
				txmin = (max.x - ray.eyePoint.x) / ray.viewDirection.x;
				txmax = (min.x - ray.eyePoint.x) / ray.viewDirection.x;
			}
			if (ray.viewDirection.y >= 0) {
				tymin = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
				tymax = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
			} else {
				tymin = (max.y - ray.eyePoint.y) / ray.viewDirection.y;
				tymax = (min.y - ray.eyePoint.y) / ray.viewDirection.y;
			}
			if ((txmin > tymax) || (tymin > txmax)) {
				return;
			}
			if (tymin > txmin) {
				txmin = tymin;
			}
			if (tymax < txmax) {
				txmax = tymax;
			}

			if (ray.viewDirection.z >= 0) {
				tzmin = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
				tzmax = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
			} else {
				tzmin = (max.z - ray.eyePoint.z) / ray.viewDirection.z;
				tzmax = (min.z - ray.eyePoint.z) / ray.viewDirection.z;
			}
			if ((txmin > tzmax) || (tzmin > txmax)) {
				return;
			}
			if (tzmin > txmin) {
				txmin = tzmin;
			}
			if (tzmax < txmax) {
				txmax = tzmax;
			}

			if (txmin <= 0) {
				return;
			}

			Point3d intersection = new Point3d();
			ray.getPoint(txmin, intersection);
			result.p = intersection;
			result.material = this.material;
			result.t = txmin;

			Vector3d n = new Vector3d(intersection);

			if (Math.abs(n.x - min.x) < 0.00001) {
				n.set(-1, 0, 0);
			} else if (Math.abs(n.x - max.x) < 0.00001) {
				n.set(1, 0, 0);
			} else if (Math.abs(n.y - min.y) < 0.00001) {
				n.set(0, -1, 0);
			} else if (Math.abs(n.y - max.y) < 0.00001) {
				n.set(0, 1, 0);
			} else if (Math.abs(n.z - min.z) < 0.00001) {
				n.set(0, 0, -1);
			} else if (Math.abs(n.z - max.z) < 0.00001) {
				n.set(0, 0, 1);
			}
			result.n = n;

		}
		result.center = new Point3d((max.x + min.x) / 2, (max.y + min.y) / 2, (max.z + min.z) / 2);
	}
}
