package org.saintandreas.vr.demo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.google.common.collect.Lists;
import com.oculusvr.capi.OvrVector3f;

public class HeadMotionStatsDemo extends JFrame {

  private static final DecimalFormat FORMAT = new DecimalFormat("#.####");
  private static final double INERT = -10.5;
  private static final double MOVING = -4.5;

  private final List<HeadPosePosition> deciSecondAverages = new ArrayList<HeadPosePosition>();
  private final List<HeadPosePosition> currentData = new ArrayList<HeadPosePosition>();
  private final List<Double> standardDeviationPerDeciSecond = Lists.newLinkedList();

  private long currentDeciSecond = System.currentTimeMillis() / 100;

  public HeadMotionStatsDemo() {
    setBounds(100, 100, 1100, 600);
    getContentPane().setBackground(Color.WHITE);
    addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    });
  }
  
  public void run() {
    setVisible(true);
    new RiftDemo() {
      @Override
      protected void finishFrame() {
        addRiftPosition(poses[0].Position);
      }
    }.run();
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);

    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    Rectangle r = getContentPane().getBounds();
    
    g.setFont(new Font("TimesRoman", Font.PLAIN, 12));
    for (Double d : standardDeviationPerDeciSecond) {
      min = Math.min(min, d);
      max = Math.max(max, d);
    }
    int x = 100;
    int bottom = r.height - 20;
    int height = bottom - 60;

    g.clearRect(100, bottom - height, r.width - 110, height);
    for (Double d : standardDeviationPerDeciSecond) {
      g.setColor(Color.RED);
      g.drawLine(x, bottom, x, bottom - (int) (height * (Math.min(d, INERT) - min) / (max - min)));
      if (d >= INERT) {
        g.setColor(Color.GREEN);
        g.drawLine(
            x, bottom - (int) (height * (INERT - min) / (max - min)), 
            x, bottom - (int) (height * (Math.min(d, MOVING) - min) / (max - min)));
      }
      if (d >= MOVING) {
        g.setColor(Color.YELLOW);
        g.drawLine(
            x, bottom - (int) (height * (MOVING - min) / (max - min)), 
            x, bottom - (int) (height * (d - min) / (max - min)));
      }
      x++;
    }
    g.setColor(Color.BLACK);
    for (double t = min; t < max + 0.001; t += (max - min) / 5.0) {
      double d = (t - min) / (max - min);
      int y = bottom - (int) (d * height);
      g.drawString(FORMAT.format(t), 15, y + 5);
      g.drawLine(65, y, 100, y);
    }
  }

  public void addRiftPosition(OvrVector3f v) {
    long now = System.currentTimeMillis() / 100;
    if (now != currentDeciSecond) {
      if (!currentData.isEmpty()) {
        deciSecondAverages.add(HeadPosePosition.average(currentData));

        if (deciSecondAverages.size() >= 10) {
          List<Double> deciSecondDifferences = Lists.newArrayList();
          HeadPosePosition previousDeciSecond = null;
          double averageDist = 0;
          double standardDeviation = 0;

          for (HeadPosePosition currentDeciSecond : deciSecondAverages) {
            if (previousDeciSecond != null) {
              double dist = currentDeciSecond.distanceTo(previousDeciSecond);
              deciSecondDifferences.add(dist);
              averageDist += dist;
            }
            previousDeciSecond = currentDeciSecond;
          }
          averageDist /= deciSecondDifferences.size();
          for (Double dist : deciSecondDifferences) {
            standardDeviation += (dist - averageDist) * (dist - averageDist);
          }
          standardDeviation /= deciSecondDifferences.size();
          standardDeviation = Math.sqrt(standardDeviation);
          standardDeviationPerDeciSecond.add(Math.log(standardDeviation));
          deciSecondAverages.remove(0);
          if (standardDeviationPerDeciSecond.size() > getContentPane().getBounds().width - 110) {
            standardDeviationPerDeciSecond.remove(0);
          }
          repaint();
        }
      }
      currentData.clear();
      currentDeciSecond = now;
    }

    currentData.add(toM3d(v));
  }

  private static HeadPosePosition toM3d(OvrVector3f v) {
    return new HeadPosePosition(v.x, v.y, v.z);
  }

  /**
   * Release the kraken.
   */
  public static void main(String[] args) {
    new HeadMotionStatsDemo().run();
  }
}
