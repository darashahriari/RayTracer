package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {

	/** Radius of the sphere. */
	public double radius = 1;

	/** Location of the sphere center. */
	public Point3d center = new Point3d(0, 0, 0);

	/**
	 * Default constructor
	 */
	public Sphere() {
		super();
	}

	/**
	 * Creates a sphere with the request radius and center.
	 * 
	 * @param radius
	 * @param center
	 * @param material
	 */
	public Sphere(double radius, Point3d center, Material material) {
		super();
		this.radius = radius;
		this.center = center;
		this.material = material;
	}

	@Override
	public void intersect(Ray ray, IntersectResult result, boolean shadow) {
		double a = ray.viewDirection.dot(ray.viewDirection);

		Vector3d ec = new Vector3d(ray.eyePoint);
		ec.sub(center);

		double b = ray.viewDirection.dot(ec) * 2;
		double c = ec.dot(ec) - Math.pow(radius, 2);

		double discriminant = Math.pow(b, 2) - (4 * a * c);

		double t = Double.POSITIVE_INFINITY;
		if (discriminant < 0) {
			return;
		} else {
			double t1 = (-b + Math.sqrt(discriminant)) / (2 * a);
			double t2 = (-b - Math.sqrt(discriminant)) / (2 * a);
			if (t1 < t2 && t1 > 0) {
				t = t1;
			} else if (t1 > t2 && t2 > 0) {
				t = t2;
			}
		}

		// Set intersect result
		Point3d intersection = new Point3d();
		ray.getPoint(t, intersection);
		result.p = intersection;
		result.material = this.material;
		result.t = t;

		// Normal at point p = (p-c)/r
		Vector3d normal = new Vector3d(intersection);
		normal.sub(center);
		normal.scale(1 / this.radius);
		result.n = normal;
		result.center = new Point3d(center);
	}

	public void update(Ray ray, IntersectResult result, double t) {

	}
}
