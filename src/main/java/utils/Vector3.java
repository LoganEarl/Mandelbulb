package utils;

import processing.core.PApplet;

import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class Vector3 extends Vector3f {
    public Vector3(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector3(float[] v) {
        super(v);
    }

    public Vector3(Vector3f v1) {
        super(v1);
    }

    public Vector3(Vector3d v1) {
        super(v1);
    }

    public Vector3(Tuple3f t1) {
        super(t1);
    }

    public Vector3(Tuple3d t1) {
        super(t1);
    }

    public Vector3() {
    }

    public Vector3 rotateCC(Vector3 axis, float theta) {
        return this.rotate(axis, -1 * theta);
    }

    public Vector3 rotate(Vector3 axis, float theta) {
        float x, y, z;
        float u, v, w;
        x = this.getX();
        y = this.getY();
        z = this.getZ();
        u = axis.getX();
        v = axis.getY();
        w = axis.getZ();
        float c = u * x + v * y + w * z;
        float xPrime = u * c * (1f - PApplet.cos(theta))
                + x * PApplet.cos(theta)
                + (-w * y + v * z) * PApplet.sin(theta);
        float yPrime = v * c * (1f - PApplet.cos(theta))
                + y * PApplet.cos(theta)
                + (w * x - u * z) * PApplet.sin(theta);
        float zPrime = w * c * (1f - PApplet.cos(theta))
                + z * PApplet.cos(theta)
                + (-v * x + u * y) * PApplet.sin(theta);
        this.set(xPrime, yPrime, zPrime);
        return this;
    }

    public Vector3 addV(Vector3 other) {
        super.add(other);
        return this;
    }

    public Vector3 subV(Vector3 v1) {
        super.sub(v1);
        return this;
    }

    public Vector3 scaleV(float s) {
        super.scale(s);
        return this;
    }

    public Vector3 negateV() {
        super.negate();
        return this;
    }

    public Vector3 crossV(Vector3 v1, Vector3 v2) {
        super.cross(v1, v2);
        return this;
    }

    public Vector3 normalizeV() {
        super.normalize();
        return this;
    }

    public Vector3 setV(float x, float y, float z) {
        super.set(x, y, z);
        return this;
    }

    public Vector3 setV(Vector3 v1) {
        super.set(v1);
        return this;
    }

    public Vector3 clone() {
        return new Vector3(this);
    }


}
