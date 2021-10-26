/*
 * Copyright (c) 2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.fx.geomfx;

import eu.hansolo.fx.geomfx.Path.WindingRule;
import eu.hansolo.fx.geomfx.tools.Point;
import eu.hansolo.fx.geomfx.transform.Affine;
import eu.hansolo.fx.geomfx.transform.BaseTransform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.FillRule;

import java.util.Arrays;


public class BezierCurve extends Shape {
    private static final int BELOW     = -2;
    private static final int LOW_EDGE  = -1;
    private static final int INSIDE    = 0;
    private static final int HIGH_EDGE = 1;
    private static final int ABOVE     = 2;

    public double x1;
    public double y1;

    public double ctrlx1;
    public double ctrly1;
    public double ctrlx2;
    public double ctrly2;

    public double x2;
    public double y2;


    public BezierCurve() { }
    public BezierCurve(final double x1, final double y1, final double ctrlX1, final double ctrlY1, final double ctrlX2, final double ctrlY2, final double x2, final double y2) {
        setCurve(x1, y1, ctrlX1, ctrlY1, ctrlX2, ctrlY2, x2, y2);
    }


    public static int solveCubic(final double eqn[]) { return solveCubic(eqn, eqn); }
    public static int solveCubic(double eqn[], final double res[]) {
        double d = eqn[3];
        if (Double.compare(d, 0) == 0) { return QuadCurve.solveQuadratic(eqn, res); }
        double a     = eqn[2] / d;
        double b     = eqn[1] / d;
        double c     = eqn[0] / d;
        int    roots = 0;
        double Q     = (a * a - 3.0 * b) / 9.0;
        double R     = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0;
        double R2    = R * R;
        double Q3    = Q * Q * Q;
        a = a / 3.0;
        if (R2 < Q3) {
            double theta =  Math.acos(R / Math.sqrt(Q3));
            Q =  (-2.0 * Math.sqrt(Q));
            if (res == eqn) {
                eqn = new double[4];
                System.arraycopy(res, 0, eqn, 0, 4);
            }
            res[roots++] = (Q * Math.cos(theta / 3.0) - a);
            res[roots++] = (Q * Math.cos((theta + Math.PI * 2.0) / 3.0) - a);
            res[roots++] = (Q * Math.cos((theta - Math.PI * 2.0) / 3.0) - a);
            fixRoots(res, eqn);
        } else {
            boolean neg = (R < 0.0);
            double    S = Math.sqrt(R2 - Q3);
            if (neg) { R = -R; }
            double A = Math.pow(R + S, 1.0 / 3.0);
            if (!neg) { A = -A; }
            double B = (Double.compare(A, 0) == 0) ? 0.0 : (Q / A);
            res[roots++] = (A + B) - a;
        }
        return roots;
    }

    private static void fixRoots(final double res[], final double eqn[]) {
        final double EPSILON =  1E-5; // eek, Rich may have botched this
        for (int i = 0; i < 3; i++) {
            double t = res[i];
            if (Math.abs(t) < EPSILON) {
                res[i] = findZero(t, 0, eqn);
            } else if (Math.abs(t - 1) < EPSILON) {
                res[i] = findZero(t, 1, eqn);
            }
        }
    }

    private static double solveEqn(final double eqn[], int order, final double T) {
        double v = eqn[order];
        while (--order >= 0) { v = v * T + eqn[order]; }
        return v;
    }

    private static double findZero(double t, final double target, final double eqn[]) {
        double slopeqn[] = { eqn[1], 2 * eqn[2], 3 * eqn[3] };
        double slope;
        double origdelta = 0;
        double origt     = t;
        while (true) {
            slope = solveEqn(slopeqn, 2, t);
            if (Double.compare(slope, 0) == 0) { return t; }
            double y = solveEqn(eqn, 3, t);
            if (Double.compare(y, 0) == 0) { return t; }
            double delta = -(y / slope);
            if (Double.compare(origdelta, 0) == 0) { origdelta = delta; }
            if (t < target) {
                if (delta < 0) { return t; }
            } else if (t > target) {
                if (delta > 0) { return t; }
            } else {
                return (delta > 0 ? (target + Float.MIN_VALUE) : (target - Float.MIN_VALUE));
            }
            double newt = t + delta;
            if (t == newt) { return t; }
            if (delta * origdelta < 0) {
                int tag = (origt < t ? getTag(target, origt, t) : getTag(target, t, origt));
                if (tag != INSIDE) { return (origt + t) / 2; }
                t = target;
            } else {
                t = newt;
            }
        }
    }

    private static void fillEqn(final double eqn[], final double val, final double c1, final double cp1, final double cp2, final double c2) {
        eqn[0] = c1 - val;
        eqn[1] = (cp1 - c1) * 3.0;
        eqn[2] = (cp2 - cp1 - cp1 + c1) * 3.0;
        eqn[3] = c2 + (cp1 - cp2) * 3.0 - c1;
    }

    private static int evalCubic(double vals[], int num, boolean include0, boolean include1, double inflect[], double c1, double cp1, double cp2, double c2) {
        int j = 0;
        for (int i = 0; i < num; i++) {
            double t = vals[i];
            if ((include0 ? t >= 0 : t > 0) && (include1 ? t <= 1 : t < 1) && (inflect == null || inflect[1] + (2 * inflect[2] + 3 * inflect[3] * t) * t != 0)) {
                double u = 1 - t;
                vals[j++] = c1 * u * u * u + 3 * cp1 * t * u * u + 3 * cp2 * t * t * u + c2 * t * t * t;
            }
        }
        return j;
    }

    private static int getTag(final double coord, final double low, final double high) {
        if (coord <= low)  { return (coord < low ? BELOW : LOW_EDGE); }
        if (coord >= high) { return (coord > high ? ABOVE : HIGH_EDGE); }
        return INSIDE;
    }

    private static boolean inwards(int pttag, int opt1tag, int opt2tag) {
        switch (pttag) {
            case BELOW    :
            case ABOVE    :
            default       : return false;
            case LOW_EDGE : return (opt1tag >= INSIDE || opt2tag >= INSIDE);
            case INSIDE   : return true;
            case HIGH_EDGE: return (opt1tag <= INSIDE || opt2tag <= INSIDE);
        }
    }

    public RectBounds getBounds() {
        double left   = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
        double top    = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
        double right  = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
        double bottom = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
        return new RectBounds(left, top, right, bottom);
    }

    public Point eval(final double t) {
        Point result = new Point();
        eval(t, result);
        return result;
    }
    public void eval(final double td, final Point result) { result.set(calcX(td), calcY(td)); }

    public Point evalDt(final double T) {
        Point result = new Point();
        evalDt(T, result);
        return result;
    }
    public void evalDt(double td, final Point result) {
        double t = td;
        double u = 1 - t;
        double x = 3 * ((ctrlx1 - x1) * u * u + 2 * (ctrlx2 - ctrlx1) * u * t + (x2 - ctrlx2) * t * t);
        double y = 3 * ((ctrly1 - y1) * u * u + 2 * (ctrly2 - ctrly1) * u * t + (y2 - ctrly2) * t * t);
        result.set(x, y);
    }

    public void setCurve(final double[] coords, final int offset) {
        setCurve(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7]);
    }
    public void setCurve(final Point p1, final Point cp1, final Point cp2, final Point p2) {
        setCurve(p1.x, p1.y, cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y);
    }
    public void setCurve(final Point[] points, final int offset) {
        setCurve(points[offset + 0].x, points[offset + 0].y, points[offset + 1].x, points[offset + 1].y, points[offset + 2].x, points[offset + 2].y, points[offset + 3].x, points[offset + 3].y);
    }
    public void setCurve(final BezierCurve bezierCurve) { setCurve(bezierCurve.x1, bezierCurve.y1, bezierCurve.ctrlx1, bezierCurve.ctrly1, bezierCurve.ctrlx2, bezierCurve.ctrly2, bezierCurve.x2, bezierCurve.y2); }
    public void setCurve(final double x1, final double y1, final double ctrlX1, final double ctrlY1, final double ctrlX2, final double ctrlY2, final double x2, final double y2) {
        this.x1 = x1;
        this.y1 = y1;
        ctrlx1 = ctrlX1;
        ctrly1 = ctrlY1;
        ctrlx2 = ctrlX2;
        ctrly2 = ctrlY2;
        this.x2 = x2;
        this.y2 = y2;
    }

    public static double getFlatnessSq(final double coords[], final int offset) {
        return getFlatnessSq(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7]);
    }
    public double getFlatnessSq() {
        return getFlatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
    }
    public static double getFlatnessSq(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2) {
        return Math.max(Line.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1), Line.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2));
    }

    public static double getFlatness(final double coords[], final int offset) {
        return getFlatness(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3], coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7]);
    }
    public double getFlatness() { return getFlatness(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2); }
    public static double getFlatness(double x1, double y1, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, double x2, double y2) {
        return  Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2));
    }

    public void subdivide(final double t, final BezierCurve left, final BezierCurve right) {
        if ((left == null) && (right == null)) return;

        double npx = calcX(t);
        double npy = calcY(t);

        double x1  = this.x1;
        double y1  = this.y1;
        double c1x = this.ctrlx1;
        double c1y = this.ctrly1;
        double c2x = this.ctrlx2;
        double c2y = this.ctrly2;
        double x2  = this.x2;
        double y2  = this.y2;
        double u   = 1 - t;
        double hx  = u * c1x + t * c2x;
        double hy  = u * c1y + t * c2y;

        if (left != null) {
            double lx1  = x1;
            double ly1  = y1;
            double lc1x = u * x1 + t * c1x;
            double lc1y = u * y1 + t * c1y;
            double lc2x = u * lc1x + t * hx;
            double lc2y = u * lc1y + t * hy;
            double lx2  = npx;
            double ly2  = npy;
            left.setCurve(lx1, ly1, lc1x, lc1y, lc2x, lc2y, lx2, ly2);
        }

        if (right != null) {
            double rx1  = npx;
            double ry1  = npy;
            double rc2x = u * c2x + t * x2;
            double rc2y = u * c2y + t * y2;
            double rc1x = u * hx + t * rc2x;
            double rc1y = u * hy + t * rc2y;
            double rx2  = x2;
            double ry2  = y2;
            right.setCurve(rx1, ry1, rc1x, rc1y, rc2x, rc2y, rx2, ry2);
        }
    }
    public void subdivide(final BezierCurve left, final BezierCurve right) { subdivide(this, left, right); }
    public static void subdivide(final BezierCurve source, final BezierCurve left, final BezierCurve right) {
        double x1      = source.x1;
        double y1      = source.y1;
        double ctrlx1  = source.ctrlx1;
        double ctrly1  = source.ctrly1;
        double ctrlx2  = source.ctrlx2;
        double ctrly2  = source.ctrly2;
        double x2      = source.x2;
        double y2      = source.y2;
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1 = (x1 + ctrlx1) / 2.0;
        ctrly1 = (y1 + ctrly1) / 2.0;
        ctrlx2 = (x2 + ctrlx2) / 2.0;
        ctrly2 = (y2 + ctrly2) / 2.0;
        double ctrlx12 = (ctrlx1 + centerx) / 2.0;
        double ctrly12 = (ctrly1 + centery) / 2.0;
        double ctrlx21 = (ctrlx2 + centerx) / 2.0;
        double ctrly21 = (ctrly2 + centery) / 2.0;
        centerx = (ctrlx12 + ctrlx21) / 2.0;
        centery = (ctrly12 + ctrly21) / 2.0;
        if (left != null) { left.setCurve(x1, y1, ctrlx1, ctrly1, ctrlx12, ctrly12, centerx, centery); }
        if (right != null) { right.setCurve(centerx, centery, ctrlx21, ctrly21, ctrlx2, ctrly2, x2, y2); }
    }
    public static void subdivide(double src[], int srcoff, double left[], int leftoff, double right[], int rightoff) {
        double x1     = src[srcoff + 0];
        double y1     = src[srcoff + 1];
        double ctrlx1 = src[srcoff + 2];
        double ctrly1 = src[srcoff + 3];
        double ctrlx2 = src[srcoff + 4];
        double ctrly2 = src[srcoff + 5];
        double x2     = src[srcoff + 6];
        double y2     = src[srcoff + 7];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 6] = x2;
            right[rightoff + 7] = y2;
        }
        x1 = (x1 + ctrlx1) / 2.0;
        y1 = (y1 + ctrly1) / 2.0;
        x2 = (x2 + ctrlx2) / 2.0;
        y2 = (y2 + ctrly2) / 2.0;
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1  = (x1 + centerx) / 2.0;
        ctrly1  = (y1 + centery) / 2.0;
        ctrlx2  = (x2 + centerx) / 2.0;
        ctrly2  = (y2 + centery) / 2.0;
        centerx = (ctrlx1 + ctrlx2) / 2.0;
        centery = (ctrly1 + ctrly2) / 2.0;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx1;
            left[leftoff + 5] = ctrly1;
            left[leftoff + 6] = centerx;
            left[leftoff + 7] = centery;
        }
        if (right != null) {
            right[rightoff + 0] = centerx;
            right[rightoff + 1] = centery;
            right[rightoff + 2] = ctrlx2;
            right[rightoff + 3] = ctrly2;
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
    }

    public boolean intersects(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) { return false; }

        double x1    = this.x1;
        double y1    = this.y1;
        int    x1tag = getTag(x1, x, x + width);
        int    y1tag = getTag(y1, y, y + height);
        if (x1tag == INSIDE && y1tag == INSIDE) {
            return true;
        }
        double x2    = this.x2;
        double y2    = this.y2;
        int    x2tag = getTag(x2, x, x + width);
        int    y2tag = getTag(y2, y, y + height);
        if (x2tag == INSIDE && y2tag == INSIDE) {
            return true;
        }

        double ctrlx1    = this.ctrlx1;
        double ctrly1    = this.ctrly1;
        double ctrlx2    = this.ctrlx2;
        double ctrly2    = this.ctrly2;
        int    ctrlx1tag = getTag(ctrlx1, x, x + width);
        int    ctrly1tag = getTag(ctrly1, y, y + height);
        int    ctrlx2tag = getTag(ctrlx2, x, x + width);
        int    ctrly2tag = getTag(ctrly2, y, y + height);

        if (x1tag < INSIDE && x2tag < INSIDE && ctrlx1tag < INSIDE && ctrlx2tag < INSIDE) { return false; }
        if (y1tag < INSIDE && y2tag < INSIDE && ctrly1tag < INSIDE && ctrly2tag < INSIDE) { return false; }
        if (x1tag > INSIDE && x2tag > INSIDE && ctrlx1tag > INSIDE && ctrlx2tag > INSIDE) { return false; }
        if (y1tag > INSIDE && y2tag > INSIDE && ctrly1tag > INSIDE && ctrly2tag > INSIDE) { return false; }

        if (inwards(x1tag, x2tag, ctrlx1tag) && inwards(y1tag, y2tag, ctrly1tag)) { return true; }
        if (inwards(x2tag, x1tag, ctrlx2tag) && inwards(y2tag, y1tag, ctrly2tag)) { return true; }

        boolean xoverlap = (x1tag * x2tag <= 0);
        boolean yoverlap = (y1tag * y2tag <= 0);
        if (x1tag == INSIDE && x2tag == INSIDE && yoverlap) { return true; }
        if (y1tag == INSIDE && y2tag == INSIDE && xoverlap) { return true; }

        double[] eqn = new double[4];
        double[] res = new double[4];
        if (!yoverlap) {
            fillEqn(eqn, (y1tag < INSIDE ? y : y + height), y1, ctrly1, ctrly2, y2);
            int num = solveCubic(eqn, res);
            num = evalCubic(res, num, true, true, null, x1, ctrlx1, ctrlx2, x2);
            return (num == 2 && getTag(res[0], x, x + width) * getTag(res[1], x, x + width) <= 0);
        }

        if (!xoverlap) {
            fillEqn(eqn, (x1tag < INSIDE ? x : x + width), x1, ctrlx1, ctrlx2, x2);
            int num = solveCubic(eqn, res);
            num = evalCubic(res, num, true, true, null, y1, ctrly1, ctrly2, y2);
            return (num == 2 && getTag(res[0], y, y + height) * getTag(res[1], y, y + height) <= 0);
        }

        double dx = x2 - x1;
        double dy = y2 - y1;
        double k  = y2 * x1 - x2 * y1;
        int   c1tag, c2tag;

        c1tag = y1tag == INSIDE ? x1tag : getTag((k + dx * (y1tag < INSIDE ? y : y + height)) / dy, x, x + width);
        c2tag = y2tag == INSIDE ? x2tag : getTag((k + dx * (y2tag < INSIDE ? y : y + height)) / dy, x, x + width);

        if (c1tag * c2tag <= 0) { return true; }
        c1tag = ((c1tag * x1tag <= 0) ? y1tag : y2tag);

        fillEqn(eqn, (c2tag < INSIDE ? x : x + width), x1, ctrlx1, ctrlx2, x2);
        int num = solveCubic(eqn, res);
        num = evalCubic(res, num, true, true, null, y1, ctrly1, ctrly2, y2);

        int tags[] = new int[num + 1];
        for (int i = 0; i < num; i++) { tags[i] = getTag(res[i], y, y + height); }
        tags[num] = c1tag;
        Arrays.sort(tags);
        return ((num >= 1 && tags[0] * tags[1] <= 0) || (num >= 3 && tags[2] * tags[3] <= 0));
    }

    public boolean contains(Point point) { return contains(point.x, point.y); }
    public boolean contains(double x, double y) {
        if (!(Double.compare((x * 0.0 + y * 0.0), 0) == 0)) { return false; }
        int crossings = (Shape.pointCrossingsForLine(x, y, x1, y1, x2, y2) + Shape.pointCrossingsForCubic(x, y, x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2, 0));
        return ((crossings & 1) == 1);
    }
    public boolean contains(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) { return false; }

        if (!(contains(x, y) && contains(x + width, y) && contains(x + width, y + height) && contains(x, y + height))) { return false; }
        return !Shape.intersectsLine(x, y, width, height, x1, y1, x2, y2);
    }

    public PathIterator getPathIterator(BaseTransform transform) { return new BezierCurveIterator(this, transform); }
    public PathIterator getPathIterator(BaseTransform transform, double flatness) {
        return new FlatteningPathIterator(getPathIterator(transform), flatness);
    }

    private double calcX(final double T) {
        final double u = 1 - T;
        return (u * u * u * x1 + 3 * (T * u * u * ctrlx1 + T * T * u * ctrlx2) + T * T * T * x2);
    }
    private double calcY(final double T) {
        final double u = 1 - T;
        return (u * u * u * y1 + 3 * (T * u * u * ctrly1 + T * T * u * ctrly2) + T * T * T * y2);
    }

    @Override public BezierCurve copy() { return new BezierCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2); }

    @Override public boolean equals(Object obj) {
        if (obj == this) { return true; }
        if (obj instanceof BezierCurve) {
            BezierCurve curve = (BezierCurve) obj;
            return ((x1 == curve.x1) &&
                    (y1 == curve.y1) &&
                    (x2 == curve.x2) &&
                    (y2 == curve.y2) &&
                    (ctrlx1 == curve.ctrlx1) &&
                    (ctrly1 == curve.ctrly1) &&
                    (ctrlx2 == curve.ctrlx2) &&
                    (ctrly2 == curve.ctrly2));
        }
        return false;
    }

    @Override public void draw(final GraphicsContext ctx, final boolean doFill, final boolean doStroke) {
        PathIterator pi = getPathIterator(new Affine());

        ctx.setFillRule(WindingRule.WIND_EVEN_ODD == pi.getWindingRule() ? FillRule.EVEN_ODD : FillRule.NON_ZERO);
        ctx.beginPath();

        double[] seg = new double[6];
        int      segType;

        while(!pi.isDone()) {
            segType = pi.currentSegment(seg);
            switch (segType) {
                case PathIterator.MOVE_TO  : ctx.moveTo(seg[0], seg[1]); break;
                case PathIterator.LINE_TO  : ctx.lineTo(seg[0], seg[1]); break;
                case PathIterator.QUAD_TO  : ctx.quadraticCurveTo(seg[0], seg[1], seg[2], seg[3]);break;
                case PathIterator.BEZIER_TO: ctx.bezierCurveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5]);break;
                case PathIterator.CLOSE    : ctx.closePath();break;
                default                    : break;
            }
            pi.next();
        }

        if (doFill)   {
            ctx.setFill(getFill());
            ctx.fill();
        }
        if (doStroke) {
            ctx.save();
            ctx.setLineWidth(getLineWidth());
            ctx.setLineCap(getLineCap());
            ctx.setLineJoin(getLineJoin());
            ctx.setStroke(getStroke());
            ctx.stroke();
            ctx.restore();
        }
    }
}
