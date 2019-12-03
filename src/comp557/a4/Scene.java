package comp557.a4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {

	/** List of surfaces in the scene */
	public List<Intersectable> surfaceList = new ArrayList<Intersectable>();

	/** All scene lights */
	public Map<String, Light> lights = new HashMap<String, Light>();

	/** Contains information about how to render the scene */
	public Render render;

	/** The ambient light colour */
	public Color3f ambient = new Color3f();

	/**
	 * Default constructor.
	 */
	public Scene() {
		this.render = new Render();
	}

	public double[] offsetCalc(int n) {
		double[] offset = null;
		double random = 0;
		if (render.jitter) {
			random = Math.random() * 0;
		}
		if (n == 0)
			offset = new double[] { -.75 - random, .25 + random };
		if (n == 1)
			offset = new double[] { -.25 - random, .25 + random };
		if (n == 2)
			offset = new double[] { -.25 - random, .75 + random };
		if (n == 3)
			offset = new double[] { -.75 - random, .75 + random };
		if (n > 3)
			offset = new double[] { -.5 - random, .5 + random };
		return offset;
	}

	/**
	 * renders the scene
	 */
	public void render(boolean showPanel) {

		Camera cam = render.camera;
		int w = cam.imageSize.width;
		int h = cam.imageSize.height;

		render.init(w, h, showPanel);
		for (int j = 0; j < h && !render.isDone(); j++) {
			for (int i = 0; i < w && !render.isDone(); i++) {
				Ray[] rays = new Ray[render.samples];
				IntersectResult[] results = new IntersectResult[render.samples];
				Color3f[] colors = new Color3f[render.samples];

				for (int n = 0; n < render.samples; n++) {
					Ray ray = new Ray();
					generateRay(i, j, offsetCalc(n), cam, ray);
					rays[n] = ray;
					IntersectResult result = new IntersectResult();
					results[n] = result;
				}

				SceneNode root = new SceneNode();
				for (int n = 0; n < render.samples; n++) {
					for (Intersectable surface : surfaceList) {
						IntersectResult tempResult = new IntersectResult();
						Ray tmpRay = new Ray(rays[n].eyePoint, rays[n].viewDirection);
						surface.intersect(tmpRay, tempResult, false);
						root.children.add(surface);
						if (results[n].t > Math.abs(tempResult.t) && Math.abs(tempResult.t) > 1e-9) {
							results[n] = new IntersectResult(tempResult);
						}
					}
					Color3f color = new Color3f();
					if (results[n].t != Double.POSITIVE_INFINITY) {
						color = lighting(rays[n], results[n], root, true);
						colors[n] = color;
					} else {
						Color3f c = new Color3f(render.bgcolor);
						colors[n] = c;
					}

					if (n == render.samples - 1) {
						Color3f c = new Color3f();
						for (int h1 = 0; h1 < colors.length; h1++) {
							c.x += colors[h1].x;
							c.y += colors[h1].y;
							c.z += colors[h1].z;
						}
						c.x = c.x / render.samples;
						c.y = c.y / render.samples;
						c.z = c.z / render.samples;
						int r = (int) Math.min(255 * c.x, 255);
						int g = (int) Math.min(255 * c.y, 255);
						int b = (int) Math.min(255 * c.z, 255);
						int a = 255;
						int argb = (a << 24 | r << 16 | g << 8 | b);
						// update the render image
						render.setPixel(i, j, argb);
					}
				}
			}
		}

		// save the final render image
		render.save();

		// wait for render viewer to close
		render.waitDone();

	}

	public Color3f lighting(Ray ray, IntersectResult result, SceneNode root, boolean first) {
		Color3f color = new Color3f(0, 0, 0);
		Light light = new Light();
		Vector3d l = new Vector3d();
		Vector3d hlf = new Vector3d();
		Vector3d v = new Vector3d();

		for (Light lighta : lights.values()) {

			double x = 0;
			double y = 0;
			double z = 0;
			if (lighta.type.equals("area")) {
				for (Light[] m : lighta.area) {
					for (Light b : m) {
						Color3f tempColor = new Color3f(0, 0, 0);
						light = b;
						double sign1 = 0;
						double sign2 = 0;
						double sign3 = 0;
						if (Math.random() > .5) {
							sign1 = -.1;
						} else {
							sign1 = .1;
						}
						if (Math.random() > .5) {
							sign2 = -.1;
						} else {
							sign2 = .1;
						}
						if (Math.random() > .5) {
							sign3 = -.1;
						} else {
							sign3 = .1;
						}
						l.sub(new Point3d(light.from.x + Math.random() * sign1, light.from.y + Math.random() * sign2,
								light.from.z + Math.random() * sign3), result.p);
						l.normalize();

						v.sub(ray.eyePoint, result.p);
						v.normalize();

						Vector3d n = new Vector3d(result.n.x, result.n.y, result.n.z);
						n.normalize();

						hlf.x = l.x + v.x;
						hlf.y = l.y + v.y;
						hlf.z = l.z + v.z;
						hlf.normalize();

						float p = result.material.shinyness;

						IntersectResult shadowResult = new IntersectResult();
						Ray shadowRay = new Ray(result.p, l);
						Point3d point = new Point3d();
						shadowRay.getPoint(.001, point);
						shadowRay.set(point, l);
						double shadowed = inShadow(result, light, shadowResult, root, shadowRay);

						if (result.n.dot(l) >= 0 && shadowed == 1) {
							tempColor.x += (float) (result.material.diffuse.x * light.color.x * light.power * n.dot(l));
							tempColor.y += (float) (result.material.diffuse.y * light.color.y * light.power * n.dot(l));
							tempColor.z += (float) (result.material.diffuse.z * light.color.z * light.power * n.dot(l));
						}
						if (result.n.dot(hlf) >= 0 && shadowed == 1 && !result.material.glazed) {
							tempColor.x += (float) (result.material.specular.x * light.color.x * light.power
									* Math.pow(n.dot(hlf), p));
							tempColor.y += (float) (result.material.specular.y * light.color.y * light.power
									* Math.pow(n.dot(hlf), p));
							tempColor.z += (float) (result.material.specular.z * light.color.z * light.power
									* Math.pow(n.dot(hlf), p));
						}
						if (shadowed == 1 && first && result.material.glazed) {

							Color3f reflectedColor = new Color3f();
							IntersectResult reflectResult = new IntersectResult();

							Vector3d norm = new Vector3d(n);
							norm.scale((v.dot(n) * 2));
							Vector3d r = new Vector3d(norm);
							r.x -= v.x;
							r.y -= v.y;
							r.z -= v.z;
							r.normalize();

							Ray reflectRay = new Ray(result.p, r);
							reflectedColor = reflection(result, light, reflectResult, reflectRay, root);
							if (reflectedColor != null) {
								tempColor.x += reflectedColor.x;
								tempColor.y += reflectedColor.y;
								tempColor.z += reflectedColor.z;
							}
						}
						tempColor.x += (result.material.diffuse.x * ambient.x);
						tempColor.y += (result.material.diffuse.y * ambient.y);
						tempColor.z += (result.material.diffuse.z * ambient.z);

						x += tempColor.x;
						y += tempColor.y;
						z += tempColor.z;
					}
				}
				color.x += (float) (x / (lighta.size * lighta.size));
				color.y += (float) (y / (lighta.size * lighta.size));
				color.z += (float) (z / (lighta.size * lighta.size));
			}

			else {
				light = lighta;
				l.sub(light.from, result.p);
				l.normalize();

				v.sub(ray.eyePoint, result.p);
				v.normalize();

				Vector3d n = new Vector3d(result.n.x, result.n.y, result.n.z);
				n.normalize();

				hlf.x = l.x + v.x;
				hlf.y = l.y + v.y;
				hlf.z = l.z + v.z;
				hlf.normalize();

				float p = result.material.shinyness;

				IntersectResult shadowResult = new IntersectResult();
				Ray shadowRay = new Ray(result.p, l);
				Point3d point = new Point3d();
				shadowRay.getPoint(.001, point);
				shadowRay.set(point, l);
				double shadowed = inShadow(result, light, shadowResult, root, shadowRay);

				if (result.n.dot(l) >= 0 && shadowed == 1) {
					color.x += (float) (result.material.diffuse.x * light.color.x * light.power * n.dot(l));
					color.y += (float) (result.material.diffuse.y * light.color.y * light.power * n.dot(l));
					color.z += (float) (result.material.diffuse.z * light.color.z * light.power * n.dot(l));
				}
				if (result.n.dot(hlf) >= 0 && shadowed == 1 && !result.material.glazed) {
					color.x += (float) (result.material.specular.x * light.color.x * light.power
							* Math.pow(n.dot(hlf), p));
					color.y += (float) (result.material.specular.y * light.color.y * light.power
							* Math.pow(n.dot(hlf), p));
					color.z += (float) (result.material.specular.z * light.color.z * light.power
							* Math.pow(n.dot(hlf), p));
				}
				if (shadowed == 1 && first && result.material.glazed) {

					Color3f reflectedColor = new Color3f();
					IntersectResult reflectResult = new IntersectResult();

					Vector3d norm = new Vector3d(n);
					norm.scale((v.dot(n) * 2));
					Vector3d r = new Vector3d(norm);
					r.x -= v.x;
					r.y -= v.y;
					r.z -= v.z;
					r.normalize();

					Ray reflectRay = new Ray(result.p, r);
					reflectedColor = reflection(result, light, reflectResult, reflectRay, root);
					if (reflectedColor != null) {
						color.x += reflectedColor.x;
						color.y += reflectedColor.y;
						color.z += reflectedColor.z;
					}
				}
				color.x += (result.material.diffuse.x * ambient.x);
				color.y += (result.material.diffuse.y * ambient.y);
				color.z += (result.material.diffuse.z * ambient.z);
			}

		}

		return color;

	}

	/**
	 * Generate a ray through pixel (i,j).
	 * 
	 * @param i
	 *            The pixel row.
	 * @param j
	 *            The pixel column.
	 * @param offset
	 *            The offset from the center of the pixel, in the range [-0.5,+0.5]
	 *            for each coordinate.
	 * @param cam
	 *            The camera.
	 * @param ray
	 *            Contains the generated ray.
	 */
	public static void generateRay(final int width, final int height, final double[] offset, final Camera cam,
			Ray ray) {
		double distance = (cam.imageSize.getHeight() / 2) / Math.tan(cam.fovy / 2);
		double newH = (cam.imageSize.getHeight() / 2) - height - offset[1];
		double newW = -(cam.imageSize.getWidth() / 2) + width + offset[0];

		Point3d eye = new Point3d(cam.from.x, cam.from.y, cam.from.z);
		Vector3d up = new Vector3d(cam.up);
		Vector3d u = new Vector3d();
		Vector3d w = new Vector3d();
		Vector3d v = new Vector3d();

		w.sub(cam.to, eye);
		w.normalize();
		w.scale(1);

		u.cross(up, w);
		u.normalize();

		v.cross(w, u);
		v.normalize();

		u.scale(-newW);
		v.scale(newH);
		w.scale(distance);

		Point3d pixel = new Point3d(v.x + u.x + w.x, v.y + u.y + w.y, v.z + u.z + w.z);
		Vector3d direction = new Vector3d();

		direction.sub(pixel, eye);
		direction.normalize();
		ray.set(eye, direction);
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result
	 *            Intersection result from raytracing.
	 * @param light
	 *            The light to check for visibility.
	 * @param root
	 *            The scene node.
	 * @param shadowResult
	 *            Contains the result of a shadow ray test.
	 * @param shadowRay
	 *            Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise.
	 */
	public double inShadow(final IntersectResult result, final Light light, IntersectResult shadowResult,
			SceneNode root, Ray shadowRay) {
		IntersectResult res = new IntersectResult();
		for (Intersectable surface : surfaceList) {
			surface.intersect(shadowRay, shadowResult, true);
			if (shadowResult.t < res.t) {
				res = new IntersectResult(shadowResult);
			}
		}
		if (res.t != Double.POSITIVE_INFINITY) {
			return 0;
		}
		return 1;
	}

	public Color3f reflection(final IntersectResult result, final Light light, IntersectResult reflectResult,
			Ray reflectRay, SceneNode root) {
		IntersectResult res = new IntersectResult();
		for (Intersectable surface : surfaceList) {
			surface.intersect(reflectRay, reflectResult, true);
			if (reflectResult.t < res.t) {
				res = new IntersectResult(reflectResult);
			}
		}
		if (res.t != Double.POSITIVE_INFINITY) {
			Color3f reflectedColor = reflectedLighting(reflectRay, res, root, true, light);
			reflectedColor.scale((float) .7);
			return reflectedColor;
		}
		return render.bgcolor;
	}

	public Color3f reflectedLighting(Ray ray, IntersectResult result, SceneNode root, boolean first, Light light) {
		int reflectCount = 0;
		Color3f color = new Color3f(0, 0, 0);
		Vector3d l = new Vector3d();
		Vector3d hlf = new Vector3d();
		Vector3d v = new Vector3d();

		l.sub(light.from, result.p);
		l.normalize();

		v.sub(ray.eyePoint, result.p);
		v.normalize();

		Vector3d n = new Vector3d(result.n.x, result.n.y, result.n.z);
		n.normalize();

		hlf.x = l.x + v.x;
		hlf.y = l.y + v.y;
		hlf.z = l.z + v.z;
		hlf.normalize();

		float p = result.material.shinyness;

		IntersectResult shadowResult = new IntersectResult();
		Ray shadowRay = new Ray(result.p, l);
		Point3d point = new Point3d();
		shadowRay.getPoint(.001, point);
		shadowRay.set(point, l);
		double shadowed = inShadow(result, light, shadowResult, root, shadowRay);

		if (result.n.dot(l) >= 0 && shadowed == 1) {
			color.x += (float) (result.material.diffuse.x * light.color.x * light.power * n.dot(l));
			color.y += (float) (result.material.diffuse.y * light.color.y * light.power * n.dot(l));
			color.z += (float) (result.material.diffuse.z * light.color.z * light.power * n.dot(l));
		}
		if (result.n.dot(hlf) >= 0 && shadowed == 1 && !result.material.glazed) {
			color.x += (float) (result.material.specular.x * light.color.x * light.power * Math.pow(n.dot(hlf), p));
			color.y += (float) (result.material.specular.y * light.color.y * light.power * Math.pow(n.dot(hlf), p));
			color.z += (float) (result.material.specular.z * light.color.z * light.power * Math.pow(n.dot(hlf), p));
		}
		if (shadowed == 1 && first && result.material.glazed) {

			Color3f reflectedColor = new Color3f();
			IntersectResult reflectResult = new IntersectResult();

			Vector3d norm = new Vector3d(n);
			norm.scale((v.dot(n) * 2));
			Vector3d r = new Vector3d(norm);
			r.x -= v.x;
			r.y -= v.y;
			r.z -= v.z;
			r.normalize();

			Ray reflectRay = new Ray(result.p, r);
			reflectedColor = reflection(result, light, reflectResult, reflectRay, root);
			if (reflectedColor != null && reflectCount < 2) {
				reflectCount++;
				color.x += reflectedColor.x;
				color.y += reflectedColor.y;
				color.z += reflectedColor.z;
			}
		}
		color.x += (result.material.diffuse.x * ambient.x);
		color.y += (result.material.diffuse.y * ambient.y);
		color.z += (result.material.diffuse.z * ambient.z);

		return color;
	}
}
