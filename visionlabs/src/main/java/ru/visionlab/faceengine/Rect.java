/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package ru.visionlab.faceengine;

public class Rect {

    private transient boolean swigCMemOwn;

    private transient long swigCPtr;

    public Rect(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    public Rect() {
        this(FaceEngineJNI.new_Rect__SWIG_0(), true);
    }

    public Rect(Point topLeft, int width, int height) {
        this(FaceEngineJNI.new_Rect__SWIG_1(Point.getCPtr(topLeft), topLeft, width, height), true);
    }

    public Rect(Point topLeft, Point bottomRight) {
        this(FaceEngineJNI
                .new_Rect__SWIG_2(Point.getCPtr(topLeft), topLeft, Point.getCPtr(bottomRight),
                        bottomRight), true);
    }

    public static long getCPtr(Rect obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    protected void finalize() throws Throwable {
        delete();
        super.finalize();
    }

    private synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                FaceEngineJNI.delete_Rect(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    public Point getTopLeft() {
        long cPtr = FaceEngineJNI.Rect_topLeft_get(swigCPtr, this);
        return (cPtr == 0) ? null : new Point(cPtr, false);
    }

    public void setTopLeft(Point value) {
        FaceEngineJNI.Rect_topLeft_set(swigCPtr, this, Point.getCPtr(value), value);
    }

    public Point getBottomRight() {
        long cPtr = FaceEngineJNI.Rect_bottomRight_get(swigCPtr, this);
        return (cPtr == 0) ? null : new Point(cPtr, false);
    }

    public void setBottomRight(Point value) {
        FaceEngineJNI.Rect_bottomRight_set(swigCPtr, this, Point.getCPtr(value), value);
    }

    public int getTop() {
        return FaceEngineJNI.Rect_getTop(swigCPtr, this);
    }

    public int getLeft() {
        return FaceEngineJNI.Rect_getLeft(swigCPtr, this);
    }

    public int getRight() {
        return FaceEngineJNI.Rect_getRight(swigCPtr, this);
    }

    public int getBottom() {
        return FaceEngineJNI.Rect_getBottom(swigCPtr, this);
    }

    public int getWidth() {
        return FaceEngineJNI.Rect_getWidth(swigCPtr, this);
    }

    public int getHeight() {
        return FaceEngineJNI.Rect_getHeight(swigCPtr, this);
    }

    public int getArea() {
        return FaceEngineJNI.Rect_getArea(swigCPtr, this);
    }

    public boolean isValid() {
        return FaceEngineJNI.Rect_isValid(swigCPtr, this);
    }

}