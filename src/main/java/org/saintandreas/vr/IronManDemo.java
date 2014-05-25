package org.saintandreas.vr;

import static javax.measure.unit.SI.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.ContextAttribs;
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
import org.saintandreas.math.Vector3f;
import org.saintandreas.math.Vector4f;
import org.saintandreas.resources.BasicResource;
import org.saintandreas.scene.RootNode;
import org.saintandreas.scene.SceneNode;
import org.saintandreas.scene.ShaderNode;
import org.saintandreas.worldwind.WorldWindUtils;

import com.google.common.collect.Lists;


public class IronManDemo extends RiftApp {
//  private static final LatLon HOME = LatLon.fromDegrees(51.4682715,0.0107339);
  private static final LatLon HOME = LatLon.fromDegrees(47.5391123, -122.2775141);
  private static final int RESOLUTION = 512;
  private static final long MAX_FLIGHT_AGE = 1000 * 15;
  private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
  private static final double MAX_DISTANCE = 30000;

  private RootNode root = new RootNode();
  private Matrix4f camera = new Matrix4f();
  private double currentElevation = 0;
  private long flightUpdateTime = 0;
  private IndexedGeometry terrainGeometry;
  private List<Flight> flights = Lists.newArrayList();
  private final List<Vector3f> flightPositions = Lists.newArrayList();
  private Measure<Float, Length> radius = Measure.valueOf(100.0f, KILOMETER);
  private List<Path2D> water = Lists.newArrayList();
  
  private static final String SHAPE_FILES[] = new String[] {
  // "F:/Downloads/wash/temp.shp",
  // "C:/Users/bdavis/Git/ShapeFileReader/testdata/freeworld/10m-coastline/10m_coastline.shp"
//  "F:/Downloads/water10/temp.shp", 
  "F:/Downloads/coast/COAST.shp",
  };

  
  protected void updateTerrainGeometry(LatLon center) {
//    System.out.println(SHAPES.size());
//    List<Path2D> neededShapes = new ArrayList<>();
//    Sector s = Sector.boundingSector(WorldWindUtils.GLOBE, HOME, Measure.valueOf(km, SI.KILOMETER).doubleValue(SI.METER));
//    List<LatLon> lv = WorldWindUtils.getLatLongs(s, res);
//    for (AbstractPolyPlainShape shape : SHAPES) {
//      Sector c = Sector.fromDegrees(shape.getBoxMaxX(), shape.getBoxMaxY(), shape.getBoxMinX(), shape.getBoxMinY());
//      if (c.intersects(s)) {
//        Path2D pg = new Path2D.Double();
//        PointData [] pds = shape.getPoints();
//        pg.moveTo(pds[0].getX(), pds[0].getY());
//        for (int i = 1; i < pds.length; ++i) {
//          pg.lineTo(pds[i].getX(), pds[i].getY());
//        }
//        pg.lineTo(pds[0].getX(), pds[0].getY());
//        neededShapes.add(pg);
//      }
//    }
    
    if (null != terrainGeometry) {
      terrainGeometry.destroy();
      terrainGeometry = null;
    }
    Sector s = Sector.boundingSector(WorldWindUtils.GLOBE, center, radius.doubleValue(METER));
    List<LatLon> lv = WorldWindUtils.getLatLongs(s, RESOLUTION);
    List<Vector3f> vs1 = WorldWindUtils.fetchElevations(center, RESOLUTION, radius);
    currentElevation = WorldWindUtils.fetchElevation(center);
    List<Vector4f> vs2 = new ArrayList<>();
    List<Integer> is = new ArrayList<>();
    Statics.forEach(RESOLUTION, (x, y) -> {
      int offset = RESOLUTION * y + x;
      Vector3f v = vs1.get(offset);
      LatLon ll = lv.get(offset);
      boolean sea = v.z <= 0;
      for (Path2D w : water) {
        if (w.contains(new Point2D.Double(ll.longitude.degrees, ll.latitude.degrees))) {
          sea = true;
          break;
        }
      }
      vs2.add(new Vector4f(-v.x, v.z, v.y, 1.0f));
      double r = radius.doubleValue(METER) * Math.sqrt(2);
      double vr = v.length();
      double f = 1 - ((vr / r) * 0.9);
      f = Math.pow(f, 2);
      Vector3f color = sea ? Vector3f.UNIT_Z : Vector3f.UNIT_Y;
      vs2.add(new Vector4f(color.mult((float)f)));
      vs2.add(new Vector4f((0 == x % 2) ? -1 : 1, (0 == y % 2) ? -1 : 1, 0, 0));
      if ((y < (RESOLUTION - 1)) && (x < (RESOLUTION - 1))) {
        is.add(offset + RESOLUTION);
        is.add(offset + RESOLUTION + 1);
        is.add(offset + 1);
        is.add(offset);
      }
    });
    IndexedGeometry.Builder builder = new IndexedGeometry.Builder(is, vs2);
    builder.withDrawType(GL_QUADS).withAttribute(Attribute.POSITION).withAttribute(Attribute.COLOR).withAttribute(4);
    terrainGeometry = builder.build();
  }

  protected SceneNode getTerrainNode() {
    updateTerrainGeometry(HOME);
    Program program = new Program(new BasicResource("shaders/Terrain.vs"), new BasicResource("shaders/Terrain.fs"));
    return new SceneNode().addChild( //
        new ShaderNode(program, () -> {
          MatrixStack.bindAll(program);
        }).addChild( //
        new SceneNode(() -> {
          terrainGeometry.bindVertexArray();
        }, () -> {
          terrainGeometry.draw();
        }, () -> {
          VertexArray.unbind();
        })));
  }

  protected SceneNode getAircraftNode() {
    Program program = new Program(ExampleResource.SHADERS_SIMPLE_VS, ExampleResource.SHADERS_COLORED_FS);
    program.link();
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
      planeTexture.bind();
      glEnable(GL_POINT_SPRITE);
      glTexEnvi(GL_POINT_SPRITE, GL_COORD_REPLACE, GL_TRUE);
      program.use();
      program.setUniform("Color", new Vector4f(1, 1, 1, 1));
      geometry.bindVertexArray();
      for (Vector3f position : flightPositions) {
        double distance = position.length();
        double size = 1 - (distance / MAX_DISTANCE);
        size *= 10;
        size += 1;
        glPointSize((float)size);
        MatrixStack.MODELVIEW.withPush((mv) -> {
          mv.translate(position);
          MatrixStack.bindAll(program);
          geometry.draw();
        });
      }
      VertexArray.unbind();
      Program.clear();
      MatrixStack.MODELVIEW.withPush((mv) -> {
        MatrixStack.bindAllGl();
        for (Vector3f position : flightPositions) {
          glBegin(GL_LINES);
            glVertex3f(position.x, position.y, position.z);
            glVertex3f(position.x, 0, position.z);
          glEnd();
        }
      });
    });
  }

  public static void vertex(Vector3f v) {
    glVertex3f(v.x, v.y, v.z);
  }
  
  protected SceneNode getAircraftDetailNode() {
    return new SceneNode(()->{
      MatrixStack mv = MatrixStack.MODELVIEW;
      Vector3f eyeDirection;
      Vector3f eyePosition = camera.toTranslationVector();
      {
        eyeDirection = mv.getRotation().inverse().mult(Vector3f.UNIT_Z.mult(-1));
      }
      Program.clear();
      mv.withPush(() -> {
        mv.bindGl();
        glPointSize(20);
        glDisable(GL_DEPTH_TEST);

        glBegin(GL_LINES);
        vertex(eyePosition);
        vertex(eyePosition.add(eyeDirection));
        for (Vector3f position : flightPositions) {
            vertex(eyePosition);
            vertex(position);
        }
        glEnd();

//        glBegin(GL_POINTS);
//          glColor3f(1, 1, 1);
//          glVertex3f(eyePosition.x, eyePosition.y, eyePosition.z);
//          glColor3f(0, 0, 1);
//          glVertex3f(eyePosition.x, eyePosition.y, eyePosition.z + 1);
//          glVertex3f(eyePosition.x, eyePosition.y, eyePosition.z - 1);
//          glColor3f(0, 1, 0);
//          glVertex3f(eyePosition.x, eyePosition.y - 1, eyePosition.z);
//          glVertex3f(eyePosition.x, eyePosition.y + 1, eyePosition.z);
//          glColor3f(1, 0, 0);
//          glVertex3f(eyePosition.x - 1, eyePosition.y, eyePosition.z);
//          glVertex3f(eyePosition.x + 1, eyePosition.y, eyePosition.z);
//          glColor3f(1, 1, 1);
//          glColor3f(1, 0, 0);
//          glVertex3f(0, 0, 0);
//        glEnd();
      });
      MatrixStack.bindAllGl();
    });
  }

  @Override
  protected void setupContext() {
    pixelFormat = pixelFormat.withSamples(4).withDepthBits(16);
    contextAttributes = new ContextAttribs(4, 4)
    .withForwardCompatible(true).withProfileCompatibility(true)
    .withDebug(true);
  }

  @Override
  protected void initGl() {
    super.initGl();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_LINE_SMOOTH);
    glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

    root.addChild(getTerrainNode());
    root.addChild(getAircraftNode());
    root.addChild(getAircraftDetailNode());
  }

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

        case Keyboard.KEY_PERIOD:
          updateTerrainGeometry(HOME);
          break;

//        case Keyboard.KEY_UP:
//          radius = Measure.valueOf(radius.floatValue(KILOMETER) * 1.5f, KILOMETER);
//          updateTerrainGeometry(HOME);
//          break;
//
//        case Keyboard.KEY_DOWN:
//          radius = Measure.valueOf(radius.floatValue(KILOMETER) / 1.5f, KILOMETER);
//          updateTerrainGeometry(HOME);
//          break;
        }
      }
    }

    List<Flight> flights = this.flights;
    flightPositions.clear();
    for (Flight flight : flights) {
      double distance = WorldWindUtils.distance(flight.location, HOME);
      if (distance < MAX_DISTANCE) {
        Vector3f position = WorldWindUtils.relative(flight.location, HOME, flight.altitude.doubleValue(SI.METER));
        Vector3f offset = flight.getOffset();
        Vector3f pos = new Vector3f(-position.x + offset.x, position.z + offset.y, position.y + offset.z);
        flightPositions.add(pos);
      }
    }
    

    Vector3f eye = new Vector3f(0, (float) currentElevation * radius.floatValue(KILOMETER) / 2.0f, 0);
    MatrixStack.MODELVIEW.lookat(eye, eye.add(Vector3f.UNIT_Z.mult(-1)), Vector3f.UNIT_Y);
    camera = MatrixStack.MODELVIEW.getTransform().invert();
//    MatrixStack.MODELVIEW.identity().translate(new Vector3f(0, (float) -currentElevation * radius.floatValue(KILOMETER) / 2.0f, 0));
//    MatrixStack.MODELVIEW.getTransform().invert();
    MatrixStack.bindAllGl();
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

  public IronManDemo() throws FileNotFoundException, IOException {
//    ValidationPreferences vp = new ValidationPreferences();
//    vp.setMaxNumberOfPointsPerShape(1000000);
//    ShapeFileReader r = new ShapeFileReader(new FileInputStream(new File(SHAPE_FILES[0])), vp);
//    AbstractShape s;
//    while ((s = r.next()) != null) {
//      switch (s.getShapeType()) {
//      case POLYGON:
//        Path2D.Double path = new Path2D.Double();
//        PolygonShape aPolygon = (PolygonShape) s;
//        boolean started = false;
//        for (PointData p : aPolygon.getPoints()) {
//          if (!started) {
//            path.moveTo(p.getX(), p.getY());
//            started = true;
//          } else {
//            path.lineTo(p.getX(), p.getY());
//          }
//        }
//        path.closePath();
//        water.add(path);
//        break;
//      default:
//        System.out.println("Read other type of shape.");
//      }
//    }
  }
  public static void main(String[] args) throws IOException {
    // XYZ test =
    // HOME.getCoordinateReferenceSystem().getConverterTo(XYZ.CRS).convert(HOME);
    // getElevations(HOME, Measure.valueOf(15, SI.KILOMETER));
    new IronManDemo().run();
  }
}
