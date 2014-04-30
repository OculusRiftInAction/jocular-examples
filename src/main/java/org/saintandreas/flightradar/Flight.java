package org.saintandreas.flightradar;

import gov.nasa.worldwind.geom.LatLon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.joda.time.DateTime;
import org.saintandreas.HttpUtil;
import org.saintandreas.json.Json;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;

import com.fasterxml.jackson.databind.JsonNode;

public class Flight {
  public final String registration;
  public final LatLon location;
  public final Measure<Angle> bearing;
  public final Measure<Length> altitude;
  public final Measure<Velocity> velocity;
  public final String squawk;
  public final String aircraft;
  public final String registration2;
  public final DateTime time;
  public final Measure<Velocity> verticalVelocity;

  public Flight(JsonNode node) {
    assert (node.isArray());
    // 0 reg "40040A",
    // 1 lat 47.3787,
    // 2 lon -122.3118,
    // 3 bearing 359,
    registration = node.get(0).asText();
    location = LatLon.fromDegrees(node.get(1).asDouble(), node.get(2).asDouble());
    bearing = Measure.valueOf(node.get(3).asInt(), NonSI.DEGREE_ANGLE);
    // 4 altitude 1250,
    // 5 speed 142,
    // 15 vertical speed -768,
    altitude = Measure.valueOf(node.get(4).asInt(), NonSI.FOOT);
    velocity = Measure.valueOf(node.get(5).asInt(), NonSI.KNOT);
    verticalVelocity = Measure.valueOf(node.get(15).asInt(), NonSI.FOOT_PER_SECOND);
    // 6 squawk: "1373",
    squawk = node.get(6).asText();
    // 8 aircraft "B744",
    aircraft = node.get(8).asText();
    // 9 reg "G-BNLK",
    registration2 = node.get(9).asText();
    // 10 time 1397433169,
    time = new DateTime(node.get(10).asLong() * 1000);
    // 7 radar: "F-KSEA1",
    // 11 departure airport "LHR",
    // 12 arrival airport "SEA",
    // 13 flight number "BA49",
    // 14 ??? 0,
    // 16 flight number "BAW49",
    // 17 ??? 0
  }

  public static List<Flight> parseFlights(String json) throws IOException {
    List<Flight> results = new ArrayList<>();
    JsonNode node = Json.MAPPER.readTree(json);
    for (JsonNode flight : node) {
      if (!flight.isArray()) {
        continue;
      }
      results.add(new Flight(flight));
    }
    return results;
  }
  
  public Vector3f getOffset() {
    return getOffset(System.currentTimeMillis());
  }

  public Vector3f getOffset(long time) {
    Vector3f vector = Vector3f.UNIT_Z.mult(-1);
    vector = Quaternion.fromAngles(0, -this.bearing.floatValue(SI.RADIAN), 0).mult(vector);
    float interval = time - this.time.getMillis();
    interval /= 1000.0f;
    float mps = this.velocity.floatValue(SI.METERS_PER_SECOND);
    vector = vector.scale(mps * interval);
    vector = vector.add(Vector3f.UNIT_Y.mult(this.verticalVelocity.floatValue(SI.METERS_PER_SECOND))); 
    return vector;
  }
  private static final String URL = "http://db8.flightradar24.com/zones/na_nw_all.js?callback=test&_=";
  public static List<Flight> getFlights(long time) {
    try {
      String data = HttpUtil.getHttpResponse(URL + time);
      data = data.replaceAll("^pd_callback\\(", "").replaceAll("\\);$", "");
      return parseFlights(data);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static List<Flight> getFlights() {
    return getFlights(System.currentTimeMillis());
  }
}
