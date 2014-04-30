package org.saintandreas.vr;

import static javax.measure.unit.SI.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.geography.coordinates.LatLong;
import org.lwjgl.input.Keyboard;
import org.saintandreas.ExampleResource;
import org.saintandreas.Statics;
import org.saintandreas.flightradar.Flight;
import org.saintandreas.gl.Geometry;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.buffers.VertexBuffer;
import org.saintandreas.gl.shaders.Attribute;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.math.Matrix4f;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;
import org.saintandreas.math.Vector4f;
import org.saintandreas.scene.RootNode;
import org.saintandreas.scene.SceneNode;
import org.saintandreas.scene.ShaderNode;
import org.saintandreas.vr.oculus.RiftApp;
import org.saintandreas.worldwind.WorldWindUtils;

import com.google.common.collect.Lists;
import com.oculusvr.capi.EyeRenderDesc;

public class IronManDemo extends RiftApp {
  private static final LatLon HOME = LatLon.fromDegrees(47.5391123, -122.2775141);
  private RootNode root = new RootNode();
  private double currentElevation = 0;
  private List<Flight> flights = Lists.newArrayList();
  private long flightUpdateTime = 0;
  private static final long MAX_FLIGHT_AGE = 1000 * 60;

  public static float distFrom(LatLong al, LatLong bl) {
    double earthRadius = 3958.75;
    double dLat = al.latitudeValue(RADIAN) - bl.latitudeValue(RADIAN);
    double dLng = al.longitudeValue(RADIAN) - bl.longitudeValue(RADIAN);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(al.latitudeValue(RADIAN))
        * Math.cos(bl.latitudeValue(RADIAN)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    double dist = earthRadius * c;
    int meterConversion = 1609;
    return (float) (dist * meterConversion);
  }

  public IronManDemo() throws IOException {
    // String flightsJson =
    // Resources.toString(Resources.getResource("flights.json"),
    // Charsets.UTF_8);
    // flights = Flight.parseFlights(flightsJson);
  }

  IndexedGeometry terrainGeometry;

  private static final int RESOLUTION = 250;
  private Measure<Length> radius = Measure.valueOf(20, KILOMETER);

  protected void updateTerrainGeometry(LatLon center) {
    if (null != terrainGeometry) {
      terrainGeometry.destroy();
      terrainGeometry = null;
    }
    Sector s = Sector.boundingSector(WorldWindUtils.GLOBE, center, radius.doubleValue(METER));
    List<LatLon> lv = WorldWindUtils.getLatLongs(s, RESOLUTION);
    List<Vector3f> vs1 = WorldWindUtils.fetchElevations(center, RESOLUTION, radius);
    currentElevation = WorldWindUtils.fetchElevation(center);
    List<Vector4f> vs2 = new ArrayList<>();
    List<Short> is = new ArrayList<>();
    Statics.forEach(RESOLUTION, (x, y) -> {
      int offset = RESOLUTION * y + x;
      Vector3f v = vs1.get(offset);
      LatLon ll = lv.get(offset);
      boolean sea = v.z <= 0;
      vs2.add(new Vector4f(-v.x, v.z, v.y, 1.0f));
      double r = radius.doubleValue(METER) * Math.sqrt(2);
      double vr = v.length();
      double f = 1 - ((vr / r) * 0.9);
      f = Math.pow(f, 2);
      Vector3f color = sea ? Vector3f.UNIT_Z : Vector3f.UNIT_Y;
      
      vs2.add(new Vector4f(color.mult((float)f)));
      is.add((short) (offset));
    });
    IndexedGeometry.Builder builder = new IndexedGeometry.Builder(is, vs2);
    builder.withDrawType(GL_POINTS).withAttribute(Attribute.POSITION).withAttribute(Attribute.COLOR);
    terrainGeometry = builder.build();
  }

  protected SceneNode getTerrainNode() {
    updateTerrainGeometry(HOME);
    Program program = new Program(ExampleResource.SHADERS_COLORED_VS, ExampleResource.SHADERS_COLORED_FS);
    return new SceneNode().addChild( //
        new ShaderNode(program, () -> {
          MatrixStack.bindAll(program);
        }).addChild( //
        new SceneNode(() -> {
          terrainGeometry.bindVertexArray();
        }, () -> {
          glPointSize(2.5f);
          terrainGeometry.draw();
        }, () -> {
          VertexArray.unbind();
        })));
  }

  protected SceneNode getAircraftNode() {
    Program program = new Program(ExampleResource.SHADERS_SIMPLE_VS, ExampleResource.SHADERS_COLORED_FS);
    Texture planeTexture;
    try {
      planeTexture = Texture.loadImage(new File("F:/downloads/1397548707_plane.png").toURI().toURL());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    VertexBuffer vb = OpenGL.toVertexBuffer(Lists.newArrayList(new Vector4f(0, 0, 0, 1)));
    Geometry geometry = new Geometry.Builder(vb, 1).withDrawType(GL_POINTS).withAttribute(Attribute.POSITION).build();
    return new SceneNode(()->{
    Program.clear();
  },()->{
    planeTexture.bind();
    glEnable(GL_POINT_SPRITE);
    glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
    double maxDistance = 150000;
    MatrixStack.MODELVIEW.withPush((mv) -> {
//      mv.translate(new Vector3f(0, (float) -currentElevation * 100, 0));

    List<Flight> flights = this.flights;
      for (Flight flight : flights) {
        double distance = WorldWindUtils.distance(flight.location, HOME);
        if (distance < maxDistance) {
          double size = 1 - (distance / maxDistance);
          size *= 10;
          size += 1;
          glPointSize((float)size);
            Vector3f position = WorldWindUtils.relative(flight.location, HOME, flight.altitude.doubleValue(SI.METER));
            Vector3f offset = flight.getOffset();
            glUseProgram(0);
            MatrixStack.bindAllGl();
            glBegin(GL_POINTS);
              glVertex3f(-position.x + offset.x, position.z + offset.y, position.y + offset.z);
            glEnd();
            glBegin(GL_LINES);
              glVertex3f(-position.x + offset.x, position.z + offset.y, position.y + offset.z);
              glVertex3f(-position.x + offset.x, 0, position.y + offset.z);
            glEnd();
        }
      }
    });
  }, ()->{});
  }

  @Override
  protected void initGl() {
    super.initGl();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_LINE_SMOOTH);
    glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

    root.addChild(getTerrainNode());
    root.addChild(getAircraftNode());
  }

  private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
  @Override
  protected void update() {
    if (MAX_FLIGHT_AGE < (System.currentTimeMillis() - flightUpdateTime)) {
      flightUpdateTime = System.currentTimeMillis();
      EXECUTOR.submit(()->{
        flights = Flight.getFlights();
      });
    }
    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        switch (Keyboard.getEventKey()) {
        case Keyboard.KEY_R:
          hmd.resetSensor();
          break;

        case Keyboard.KEY_UP:
          radius = Measure.valueOf(radius.floatValue(KILOMETER) * 1.5f, KILOMETER);
          updateTerrainGeometry(HOME);
          break;

        case Keyboard.KEY_DOWN:
          radius = Measure.valueOf(radius.floatValue(KILOMETER) / 1.5f, KILOMETER);
          updateTerrainGeometry(HOME);
          break;
        }
      }
    }

    for (int eye = 0; eye < 2; ++eye) {
      EyeRenderDesc erd = eyeRenderDescs[eye];
      // projections[eye] = new
      // Matrix4f(OVR.ovrMatrix4f_Projection(erd.Desc.Fov, 0.1f, 1000000f,
      // (byte) 1).M).transpose();
    }

    Quaternion q = hmd.getSensorState(OVR.ovr_GetTimeInSeconds()).Predicted.Pose.Orientation.toQuaternion();
    Matrix4f m = new Matrix4f();// .rotate(q);
    MatrixStack.MODELVIEW.identity().preMultiply(m.invert())
        .translate(new Vector3f(0, (float) -currentElevation * 60, 0));
  }

  @Override
  public void renderScene() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glClearColor(0.05f, 0.05f, 0.05f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glPointSize(1.5f);
    root.render();
  }

  private static final String SHAPE_FILES[] = new String[] {
  // "F:/Downloads/wash/temp.shp",
  // "C:/Users/bdavis/Git/ShapeFileReader/testdata/freeworld/10m-coastline/10m_coastline.shp"
  "F:/Downloads/water10/temp.shp", };

  public static void main(String[] args) throws IOException {
    // XYZ test =
    // HOME.getCoordinateReferenceSystem().getConverterTo(XYZ.CRS).convert(HOME);
    // getElevations(HOME, Measure.valueOf(15, SI.KILOMETER));
    new IronManDemo().run();
  }
}
