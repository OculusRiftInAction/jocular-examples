package org.saintandreas.worldwind;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.BilinearInterpolator;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.saintandreas.Statics;
import org.saintandreas.math.Vector3f;

public class WorldWindUtils {
  public static final Model MODEL;
  public static final Globe GLOBE;

  static {
    GLOBE = (Globe) WorldWind.createConfigurationComponent(AVKey.GLOBE_CLASS_NAME);
    MODEL = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
    MODEL.setGlobe(GLOBE);
  }

  public static BilinearInterpolator getInterpolator(Sector s) {
    double xmin = s.getMinLongitude().degrees;
    double xmax = s.getMaxLongitude().degrees;
    double ymin = s.getMinLatitude().degrees;
    double ymax = s.getMaxLatitude().degrees;
    BilinearInterpolator bi = new BilinearInterpolator(new Vec4(xmin, ymin, 0, 0), new Vec4(xmax, ymin, 0, 0),
        new Vec4(xmax, ymax, 0, 0), new Vec4(xmin, ymax, 0, 0));
    return bi;
  }

  public static double[] fetchElevations(Sector s, double resolution, List<LatLon> lv) {
    double elevations[] = new double[lv.size()];
    double gotResolution = GLOBE.getElevations(s, lv, resolution, elevations);
    while (gotResolution > 1e10) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
      gotResolution = GLOBE.getElevations(s, lv, resolution, elevations);
    }
    return elevations;
  }

  public static List<LatLon> getLatLongs(Sector s, int res) {
    BilinearInterpolator bi = getInterpolator(s);
    List<LatLon> lv = new ArrayList<>();
    Statics.forEach(res, (x, y) -> {
      float xi = (float) x / (float) (res - 1);
      float yi = (float) y / (float) (res - 1);
      Vec4 v = bi.interpolateAsPoint(xi, yi);
      LatLon loc = LatLon.fromDegrees(v.y, v.x);
      LatLon.linearDistance(s.getCentroid(), loc);
      lv.add(loc);
    });
    return lv;
  }

  public static double fetchElevation(LatLon center) {
    return GLOBE.getElevation(center.latitude, center.longitude);
  }

  public static List<Vector3f> fetchElevations(LatLon center, int res, Measure<Length> radius) {
    Sector s = Sector.boundingSector(GLOBE, center, radius.doubleValue(SI.METER));
    List<LatLon> lv = getLatLongs(s, res);
    double resolutionRadians = s.getDeltaLat().radians / res;
    double elevations[] = fetchElevations(s, resolutionRadians, lv);
    List<Vector3f> results = new ArrayList<Vector3f>();
    for (int i = 0; i < lv.size(); ++i) {
      LatLon ll = lv.get(i);
      double el = elevations[i];
      results.add(relative(ll, center, el));
    }
    return results;
  }

  public static Vector3f relative(LatLon origin, LatLon point) {
    return relative(origin, point, 0);
  }


  public static Vector3f relative(LatLon origin, LatLon point, double elevation) {
    Angle ad = LatLon.greatCircleDistance(origin, point);
    double r = ad.radians * GLOBE.getRadius();
    Angle a = LatLon.greatCircleAzimuth(origin, point);
    double x = Math.sin(a.radians) * r;
    double y = Math.cos(a.radians) * r;
    return new Vector3f((float)x, (float)y, (float)elevation);
  }

  public static double distance(Angle a) {
    return a.radians * GLOBE.getRadius();
  }

  public static double distance(LatLon a, LatLon b) {
    return LatLon.linearDistance(a, b).radians * GLOBE.getRadius();
  }
}
