package comp557.a4;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class Mesh extends Intersectable {

	/** Static map storing all meshes by name */
	public static Map<String, Mesh> meshMap = new HashMap<String, Mesh>();

	/** Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";

	public Box bounding = null;

	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}

	public void createBounding() {
		Point3d min = new Point3d();
		Point3d max = new Point3d();
		for (int i = 0; i < soup.vertexList.size(); ++i) {
			if (soup.vertexList.get(i).p.x < min.x)
				min.x = soup.vertexList.get(i).p.x;
			if (soup.vertexList.get(i).p.y < min.y)
				min.y = soup.vertexList.get(i).p.y;
			if (soup.vertexList.get(i).p.z < min.z)
				min.z = soup.vertexList.get(i).p.z;
			if (soup.vertexList.get(i).p.x > max.x)
				max.x = soup.vertexList.get(i).p.x;
			if (soup.vertexList.get(i).p.y > max.y)
				max.y = soup.vertexList.get(i).p.y;
			if (soup.vertexList.get(i).p.z > max.z)
				max.z = soup.vertexList.get(i).p.z;
		}
		bounding = new Box();
		bounding.material = material;
		bounding.max = max;
		bounding.min = min;
		bounding.createBounding();
	}

	@Override
	public void intersect(Ray ray, IntersectResult result, boolean shadow) {

		IntersectResult boundingResult = new IntersectResult();
		bounding.intersect(ray, boundingResult, shadow);
		if (boundingResult.t != Double.POSITIVE_INFINITY) {
			double a, b, c, d, e, f, j, k, l, M, beta, gamma, t;

			for (int[] faceVertex : soup.faceList) {
				Point3d A = soup.vertexList.get(faceVertex[0]).p;
				Point3d B = soup.vertexList.get(faceVertex[1]).p;
				Point3d C = soup.vertexList.get(faceVertex[2]).p;

				a = A.x - B.x;
				b = A.y - B.y;
				c = A.z - B.z;
				d = A.x - C.x;
				e = A.y - C.y;
				f = A.z - C.z;
				j = A.x - ray.eyePoint.x;
				k = A.y - ray.eyePoint.y;
				l = A.z - ray.eyePoint.z;

				M = a * ((e * ray.viewDirection.z) - (ray.viewDirection.y * f))
						+ b * ((ray.viewDirection.x * f) - (d * ray.viewDirection.z))
						+ c * ((d * ray.viewDirection.y) - (e * ray.viewDirection.x));

				t = -(f * (a * k - j * b) + e * (j * c - a * l) + d * (b * l - k * c)) / M;

				if (t > -0.0001 && t < result.t) {
					gamma = (ray.viewDirection.z * (a * k - j * b) + ray.viewDirection.y * (j * c - a * l)
							+ ray.viewDirection.x * (b * l - k * c)) / M;

					if (gamma > 0 & gamma < 1) {
						beta = (j * (e * ray.viewDirection.z - ray.viewDirection.y * f)
								+ k * (ray.viewDirection.x * f - d * ray.viewDirection.z)
								+ l * (d * ray.viewDirection.y - e * ray.viewDirection.x)) / M;

						if (beta > 0 && beta < 1 - gamma) {
							Point3d p = new Point3d(ray.viewDirection);
							p.scale(t);
							p.add(ray.eyePoint);
							result.p = p;
							result.material = this.material;
							result.t = t;

							Vector3d ba = new Vector3d();
							Vector3d ca = new Vector3d();
							Vector3d n = new Vector3d();
							ba.sub(B, A);
							ca.sub(C, A);
							n.cross(ba, ca);
							n.normalize();
							result.n = n;
						}
					}
				}
			}
		}
	}

}
