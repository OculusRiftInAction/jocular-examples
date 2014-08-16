package org.saintandreas.vr.demo;

import java.util.List;


public class HeadPosePosition {

  double vec[] = new double[3];

  public HeadPosePosition(double x, double y, double z) {
    vec[0] = x;
    vec[1] = y;
    vec[2] = z;
  }
  
  public static HeadPosePosition average(List<HeadPosePosition> positions) {
    HeadPosePosition total = new HeadPosePosition(0, 0, 0);
    for (HeadPosePosition p : positions) {
      total.add(p);
    }
    return new HeadPosePosition(
        total.vec[0] / positions.size(),
        total.vec[1] / positions.size(),
        total.vec[2] / positions.size());
  }

  public double distanceTo(HeadPosePosition B) {
    double x = vec[0] - B.vec[0];
    double y = vec[1] - B.vec[1];
    double z = vec[2] - B.vec[2];
    return (double) Math.sqrt(x * x + y * y + z * z);
  }

  private void add(HeadPosePosition B) {
    for (int i = 0; i < 3; i++) {
      vec[i] += B.vec[i];
    }
  }
}
