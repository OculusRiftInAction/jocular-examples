package org.saintandreas.vr;

import org.saintandreas.math.Matrix4f;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;

import com.oculusvr.capi.Posef;
import com.oculusvr.capi.Quatf;

public class RiftUtils {

  public static Vector3f toVector3f(com.oculusvr.capi.Vector3f v) {
    return new Vector3f(v.x, v.y, v.z);
  }

  public static Quaternion toQuaternion(Quatf q) {
    return new Quaternion(q.x, q.y, q.z, q.w);
  }

  public static Matrix4f toMatrix4f(Posef p) {
    return new Matrix4f().rotate(toQuaternion(p.Orientation)).mult(new Matrix4f().translate(toVector3f(p.Position)));
  }

  public static Matrix4f toMatrix4f(com.oculusvr.capi.Matrix4f m) {
    return new org.saintandreas.math.Matrix4f(m.M).transpose();
  }

}
