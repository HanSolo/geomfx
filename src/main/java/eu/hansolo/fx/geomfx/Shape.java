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

import eu.hansolo.fx.geomfx.tools.Helper;
import eu.hansolo.fx.geomfx.tools.IllegalPathStateException;
import eu.hansolo.fx.geomfx.tools.Point;
import eu.hansolo.fx.geomfx.transform.BaseTransform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;


public abstract class Shape {
    public static final int            RECT_INTERSECTS = 0x80000000;
    public static final int            OUT_LEFT        = 1;
    public static final int            OUT_TOP         = 2;
    public static final int            OUT_RIGHT       = 4;
    public static final int            OUT_BOTTOM      = 8;

    protected           Paint          fill            = Color.BLACK;
    protected           Paint          stroke          = Color.BLACK;
    protected           double         lineWidth       = 1.0;
    protected           StrokeLineJoin lineJoin        = StrokeLineJoin.BEVEL;
    protected           StrokeLineCap  lineCap         = StrokeLineCap.BUTT;


    public abstract RectBounds getBounds();

    public boolean contains(final Point point) { return contains(point.x, point.y); }
    public abstract boolean contains(final double x, final double y);
    public boolean contains(final RectBounds bounds) {
        double x = bounds.getMinX();
        double y = bounds.getMinY();
        double w = bounds.getMaxX() - x;
        double h = bounds.getMaxY() - y;
        return contains(x, y, w, h);
    }
    public abstract boolean contains(final double x, final double y, final double width, final double height);

    public abstract boolean intersects(final double x, final double y, final double width, final double height);
    public boolean intersects(final RectBounds bounds) {
        double x = bounds.getMinX();
        double y = bounds.getMinY();
        double w = bounds.getMaxX() - x;
        double h = bounds.getMaxY() - y;
        return intersects(x, y, w, h);
    }

    public abstract PathIterator getPathIterator(final BaseTransform transform);
    public abstract PathIterator getPathIterator(final BaseTransform transform, final double flatness);

    public abstract Shape copy();

    public static int pointCrossingsForPath(final PathIterator pathIterator, final double pointX, final double pointY) {
        if (pathIterator.isDone()) { return 0; }
        double coords[] = new double[6];
        if (pathIterator.currentSegment(coords) != PathIterator.MOVE_TO) {
            throw new IllegalPathStateException("missing initial moveto in path definition");
        }
        pathIterator.next();
        double movx = coords[0];
        double movy = coords[1];
        double curx = movx;
        double cury = movy;
        double endx;
        double endy;
        int crossings = 0;
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.MOVE_TO:
                    if (cury != movy) { crossings += pointCrossingsForLine(pointX, pointY, curx, cury, movx, movy); }
                    movx = curx = coords[0];
                    movy = cury = coords[1];
                    break;
                case PathIterator.LINE_TO:
                    endx = coords[0];
                    endy = coords[1];
                    crossings += pointCrossingsForLine(pointX, pointY, curx, cury, endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.QUAD_TO:
                    endx = coords[2];
                    endy = coords[3];
                    crossings += pointCrossingsForQuad(pointX, pointY, curx, cury, coords[0], coords[1], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.BEZIER_TO:
                    endx = coords[4];
                    endy = coords[5];
                    crossings += pointCrossingsForCubic(pointX, pointY, curx, cury, coords[0], coords[1], coords[2], coords[3], endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.CLOSE:
                    if (cury != movy) { crossings += pointCrossingsForLine(pointX, pointY, curx, cury, movx, movy); }
                    curx = movx;
                    cury = movy;
                    break;
            }
            pathIterator.next();
        }
        if (cury != movy) { crossings += pointCrossingsForLine(pointX, pointY, curx, cury, movx, movy); }
        return crossings;
    }

    public static int pointCrossingsForLine(final double pointX, final double pointY,
                                            final double x0, final double y0,
                                            final double x1, final double y1) {
        if (pointY <  y0 && pointY <  y1) { return 0; }
        if (pointY >= y0 && pointY >= y1) { return 0; }
        if (pointX >= x0 && pointX >= x1) { return 0; }
        if (pointX <  x0 && pointX <  x1) { return (y0 < y1) ? 1 : -1; }
        double xIntercept = x0 + (pointY - y0) * (x1 - x0) / (y1 - y0);
        if (pointX >= xIntercept) { return 0; }
        return (y0 < y1) ? 1 : -1;
    }

    public static int pointCrossingsForQuad(final double pointX, final double pointY,
                                            final double x0, final double y0,
                                            double xc, double yc,
                                            final double x1, final double y1, final int level) {
        if (pointY <  y0 && pointY <  yc && pointY <  y1) { return 0; }
        if (pointY >= y0 && pointY >= yc && pointY >= y1) { return 0; }
        if (pointX >= x0 && pointX >= xc && pointX >= x1) { return 0; }
        if (pointX <  x0 && pointX <  xc && pointX <  x1) {
            if (pointY >= y0) {
                if (pointY < y1) { return 1; }
            } else {
                if (pointY >= y1) { return -1; }
            }
            return 0;
        }
        if (level > 52) return pointCrossingsForLine(pointX, pointY, x0, y0, x1, y1);
        double x0c = (x0 + xc) / 2;
        double y0c = (y0 + yc) / 2;
        double xc1 = (xc + x1) / 2;
        double yc1 = (yc + y1) / 2;
        xc = (x0c + xc1) / 2;
        yc = (y0c + yc1) / 2;
        if (Double.isNaN(xc) || Double.isNaN(yc)) { return 0; }
        return (pointCrossingsForQuad(pointX, pointY, x0, y0, x0c, y0c, xc, yc, level+1) +
                pointCrossingsForQuad(pointX, pointY, xc, yc, xc1, yc1, x1, y1, level+1));
    }

    public static int pointCrossingsForCubic(final double pointX, final double pointY,
                                             final double x0, final double y0,
                                             double xc0, double yc0,
                                             double xc1, double yc1,
                                             final double x1, final double y1, final int level)
    {
        if (pointY <  y0 && pointY <  yc0 && pointY <  yc1 && pointY <  y1) { return 0; }
        if (pointY >= y0 && pointY >= yc0 && pointY >= yc1 && pointY >= y1) { return 0; }
        if (pointX >= x0 && pointX >= xc0 && pointX >= xc1 && pointX >= x1) { return 0; }
        if (pointX <  x0 && pointX <  xc0 && pointX <  xc1 && pointX <  x1) {
            if (pointY >= y0) {
                if (pointY < y1) { return 1; }
            } else {
                if (pointY >= y1) { return -1; }
            }
            return 0;
        }
        if (level > 52) { return pointCrossingsForLine(pointX, pointY, x0, y0, x1, y1); }
        double xmid = (xc0 + xc1) / 2;
        double ymid = (yc0 + yc1) / 2;
        xc0 = (x0 + xc0) / 2;
        yc0 = (y0 + yc0) / 2;
        xc1 = (xc1 + x1) / 2;
        yc1 = (yc1 + y1) / 2;
        double xc0m = (xc0 + xmid) / 2;
        double yc0m = (yc0 + ymid) / 2;
        double xmc1 = (xmid + xc1) / 2;
        double ymc1 = (ymid + yc1) / 2;
        xmid = (xc0m + xmc1) / 2;
        ymid = (yc0m + ymc1) / 2;
        if (Double.isNaN(xmid) || Double.isNaN(ymid)) { return 0; }
        return (pointCrossingsForCubic(pointX, pointY, x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, level+1) +
                pointCrossingsForCubic(pointX, pointY, xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, level+1));
    }


    public static int rectCrossingsForPath(final PathIterator pathIterator,
                                           final double rectXMin, final double rectYMin,
                                           final double rectXMax, final double rectYMax) {
        if (rectXMax <= rectXMin || rectYMax <= rectYMin) { return 0; }
        if (pathIterator.isDone()) { return 0; }
        double coords[] = new double[6];
        if (pathIterator.currentSegment(coords) != PathIterator.MOVE_TO) {
            throw new IllegalPathStateException("missing initial moveto in path definition");
        }
        pathIterator.next();
        double curx, cury, movx, movy, endx, endy;
        curx = movx = coords[0];
        cury = movy = coords[1];
        int crossings = 0;
        while (crossings != RECT_INTERSECTS && !pathIterator.isDone()) {
            switch (pathIterator.currentSegment(coords)) {
                case PathIterator.MOVE_TO:
                    if (curx != movx || cury != movy) {
                        crossings = rectCrossingsForLine(crossings,
                                                         rectXMin, rectYMin,
                                                         rectXMax, rectYMax,
                                                         curx, cury,
                                                         movx, movy);
                    }
                    movx = curx = coords[0];
                    movy = cury = coords[1];
                    break;
                case PathIterator.LINE_TO:
                    endx = coords[0];
                    endy = coords[1];
                    crossings = rectCrossingsForLine(crossings,
                                                     rectXMin, rectYMin,
                                                     rectXMax, rectYMax,
                                                     curx, cury,
                                                     endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.QUAD_TO:
                    endx = coords[2];
                    endy = coords[3];
                    crossings = rectCrossingsForQuad(crossings,
                                                     rectXMin, rectYMin,
                                                     rectXMax, rectYMax,
                                                     curx, cury,
                                                     coords[0], coords[1],
                                                     endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.BEZIER_TO:
                    endx = coords[4];
                    endy = coords[5];
                    crossings = rectCrossingsForCubic(crossings,
                                                      rectXMin, rectYMin,
                                                      rectXMax, rectYMax,
                                                      curx, cury,
                                                      coords[0], coords[1],
                                                      coords[2], coords[3],
                                                      endx, endy, 0);
                    curx = endx;
                    cury = endy;
                    break;
                case PathIterator.CLOSE:
                    if (curx != movx || cury != movy) {
                        crossings = rectCrossingsForLine(crossings,
                                                         rectXMin, rectYMin,
                                                         rectXMax, rectYMax,
                                                         curx, cury,
                                                         movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    // Count should always be a multiple of 2 here.
                    // assert((crossings & 1) != 0);
                    break;
            }
            pathIterator.next();
        }
        if (crossings != RECT_INTERSECTS && (curx != movx || cury != movy)) {
            crossings = rectCrossingsForLine(crossings,
                                             rectXMin, rectYMin,
                                             rectXMax, rectYMax,
                                             curx, cury,
                                             movx, movy);
        }
        // Count should always be a multiple of 2 here.
        // assert((crossings & 1) != 0);
        return crossings;
    }


    public static int rectCrossingsForLine(int crossings,
                                           final double rectXMin, final double rectYMin,
                                           final double rectXMax, final double rectYMax,
                                           final double x0, final double y0,
                                           final double x1, final double y1) {
        if (y0 >= rectYMax && y1 >= rectYMax) { return crossings; }
        if (y0 <= rectYMin && y1 <= rectYMin) { return crossings; }
        if (x0 <= rectXMin && x1 <= rectXMin) { return crossings; }
        if (x0 >= rectXMax && x1 >= rectXMax) {
            if (y0 < y1) {
                if (y0 <= rectYMin) { crossings++; }
                if (y1 >= rectYMax) { crossings++; }
            } else if (y1 < y0) {
                if (y1 <= rectYMin) { crossings--; }
                if (y0 >= rectYMax) { crossings--; }
            }
            return crossings;
        }
        // Remaining case:
        // Both x and y ranges overlap by a non-empty amount
        // First do trivial INTERSECTS rejection of the cases
        // where one of the endpoints is inside the rectangle.
        if ((x0 > rectXMin && x0 < rectXMax && y0 > rectYMin && y0 < rectYMax) ||
            (x1 > rectXMin && x1 < rectXMax && y1 > rectYMin && y1 < rectYMax))
        {
            return RECT_INTERSECTS;
        }
        // Otherwise calculate the y intercepts and see where
        // they fall with respect to the rectangle
        double xi0 = x0;
        if (y0 < rectYMin) {
            xi0 += ((rectYMin - y0) * (x1 - x0) / (y1 - y0));
        } else if (y0 > rectYMax) {
            xi0 += ((rectYMax - y0) * (x1 - x0) / (y1 - y0));
        }
        double xi1 = x1;
        if (y1 < rectYMin) {
            xi1 += ((rectYMin - y1) * (x0 - x1) / (y0 - y1));
        } else if (y1 > rectYMax) {
            xi1 += ((rectYMax - y1) * (x0 - x1) / (y0 - y1));
        }
        if (xi0 <= rectXMin && xi1 <= rectXMin) return crossings;
        if (xi0 >= rectXMax && xi1 >= rectXMax) {
            if (y0 < y1) {
                // y-increasing line segment...
                // We know that Y0 < RECT_Y_MAX and Y1 > RECT_Y_MIN
                if (y0 <= rectYMin) crossings++;
                if (y1 >= rectYMax) crossings++;
            } else if (y1 < y0) {
                // y-decreasing line segment...
                // We know that Y1 < RECT_Y_MAX and Y0 > RECT_Y_MIN
                if (y1 <= rectYMin) crossings--;
                if (y0 >= rectYMax) crossings--;
            }
            return crossings;
        }
        return RECT_INTERSECTS;
    }


    public static int rectCrossingsForQuad(int crossings,
                                           double rxmin, double rymin,
                                           double rxmax, double rymax,
                                           double x0, double y0,
                                           double xc, double yc,
                                           double x1, double y1,
                                           int level)
    {
        if (y0 >= rymax && yc >= rymax && y1 >= rymax) { return crossings; }
        if (y0 <= rymin && yc <= rymin && y1 <= rymin) { return crossings; }
        if (x0 <= rxmin && xc <= rxmin && x1 <= rxmin) { return crossings; }
        if (x0 >= rxmax && xc >= rxmax && x1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin && y1 >  rymin) { crossings++; }
                if (y0 <  rymax && y1 >= rymax) { crossings++; }
            } else if (y1 < y0) {
                if (y1 <= rymin && y0 >  rymin) { crossings--; }
                if (y1 <  rymax && y0 >= rymax) { crossings--; }
            }
            return crossings;
        }
        if ((x0 < rxmax && x0 > rxmin && y0 < rymax && y0 > rymin) || (x1 < rxmax && x1 > rxmin && y1 < rymax && y1 > rymin)) {
            return RECT_INTERSECTS;
        }
        if (level > 52) { return rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x1, y1); }
        double x0c = (x0 + xc) / 2;
        double y0c = (y0 + yc) / 2;
        double xc1 = (xc + x1) / 2;
        double yc1 = (yc + y1) / 2;
        xc = (x0c + xc1) / 2;
        yc = (y0c + yc1) / 2;
        if (Double.isNaN(xc) || Double.isNaN(yc)) { return 0; }
        crossings = rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x0c, y0c, xc, yc, level+1);
        if (crossings != RECT_INTERSECTS) {
            crossings = rectCrossingsForQuad(crossings, rxmin, rymin, rxmax, rymax, xc, yc, xc1, yc1, x1, y1, level+1);
        }
        return crossings;
    }


    public static int rectCrossingsForCubic(int crossings,
                                            double rxmin, double rymin,
                                            double rxmax, double rymax,
                                            double x0,  double y0,
                                            double xc0, double yc0,
                                            double xc1, double yc1,
                                            double x1,  double y1,
                                            int level)
    {
        if (y0 >= rymax && yc0 >= rymax && yc1 >= rymax && y1 >= rymax) {
            return crossings;
        }
        if (y0 <= rymin && yc0 <= rymin && yc1 <= rymin && y1 <= rymin) {
            return crossings;
        }
        if (x0 <= rxmin && xc0 <= rxmin && xc1 <= rxmin && x1 <= rxmin) {
            return crossings;
        }
        if (x0 >= rxmax && xc0 >= rxmax && xc1 >= rxmax && x1 >= rxmax) {
            if (y0 < y1) {
                if (y0 <= rymin && y1 >  rymin) crossings++;
                if (y0 <  rymax && y1 >= rymax) crossings++;
            } else if (y1 < y0) {
                if (y1 <= rymin && y0 >  rymin) crossings--;
                if (y1 <  rymax && y0 >= rymax) crossings--;
            }
            return crossings;
        }
        if ((x0 > rxmin && x0 < rxmax && y0 > rymin && y0 < rymax) || (x1 > rxmin && x1 < rxmax && y1 > rymin && y1 < rymax)) {
            return RECT_INTERSECTS;
        }
        if (level > 52) { return rectCrossingsForLine(crossings, rxmin, rymin, rxmax, rymax, x0, y0, x1, y1); }
        double xmid = (xc0 + xc1) / 2;
        double ymid = (yc0 + yc1) / 2;
        xc0 = (x0 + xc0) / 2;
        yc0 = (y0 + yc0) / 2;
        xc1 = (xc1 + x1) / 2;
        yc1 = (yc1 + y1) / 2;
        double xc0m = (xc0 + xmid) / 2;
        double yc0m = (yc0 + ymid) / 2;
        double xmc1 = (xmid + xc1) / 2;
        double ymc1 = (ymid + yc1) / 2;
        xmid = (xc0m + xmc1) / 2;
        ymid = (yc0m + ymc1) / 2;
        if (Double.isNaN(xmid) || Double.isNaN(ymid)) { return 0; }
        crossings = rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, x0, y0, xc0, yc0, xc0m, yc0m, xmid, ymid, level+1);
        if (crossings != RECT_INTERSECTS) {
            crossings = rectCrossingsForCubic(crossings, rxmin, rymin, rxmax, rymax, xmid, ymid, xmc1, ymc1, xc1, yc1, x1, y1, level+1);
        }
        return crossings;
    }

    static boolean intersectsLine(double rx1, double ry1, double rwidth, double rheight, double x1, double y1, double x2, double y2) {
        int out1, out2;
        if ((out2 = outcode(rx1, ry1, rwidth, rheight, x2, y2)) == 0) { return true; }
        while ((out1 = outcode(rx1, ry1, rwidth, rheight, x1, y1)) != 0) {
            if ((out1 & out2) != 0) { return false; }
            if ((out1 & (OUT_LEFT | OUT_RIGHT)) != 0) {
                if ((out1 & OUT_RIGHT) != 0) { rx1 += rwidth; }
                y1 = y1 + (rx1 - x1) * (y2 - y1) / (x2 - x1);
                x1 = rx1;
            } else {
                if ((out1 & OUT_BOTTOM) != 0) { ry1 += rheight; }
                x1 = x1 + (ry1 - y1) * (x2 - x1) / (y2 - y1);
                y1 = ry1;
            }
        }
        return true;
    }

    static int outcode(final double rectX, final double rectY, final double rectWidth, final double rectHeight, final double x, final double y) {
        int out = 0;
        if (rectWidth <= 0) {
            out |= OUT_LEFT | OUT_RIGHT;
        } else if (x < rectX) {
            out |= OUT_LEFT;
        } else if (x > rectX + rectWidth) {
            out |= OUT_RIGHT;
        }
        if (rectHeight <= 0) {
            out |= OUT_TOP | OUT_BOTTOM;
        } else if (y < rectY) {
            out |= OUT_TOP;
        } else if (y > rectY + rectHeight) {
            out |= OUT_BOTTOM;
        }
        return out;
    }

    public static void accumulate(double bbox[], Shape s, BaseTransform tx) {
        PathIterator pi = s.getPathIterator(tx);
        double coords[] = new double[6];
        double mx = 0.0, my = 0.0, x0 = 0.0, y0 = 0.0, x1, y1;
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.MOVE_TO:
                    mx = coords[0];
                    my = coords[1];
                case PathIterator.LINE_TO:
                    x0 = coords[0];
                    y0 = coords[1];
                    if (bbox[0] > x0) bbox[0] = x0;
                    if (bbox[1] > y0) bbox[1] = y0;
                    if (bbox[2] < x0) bbox[2] = x0;
                    if (bbox[3] < y0) bbox[3] = y0;
                    break;
                case PathIterator.QUAD_TO:
                    x1 = coords[2];
                    y1 = coords[3];
                    if (bbox[0] > x1) bbox[0] = x1;
                    if (bbox[1] > y1) bbox[1] = y1;
                    if (bbox[2] < x1) bbox[2] = x1;
                    if (bbox[3] < y1) bbox[3] = y1;
                    if (bbox[0] > coords[0] || bbox[2] < coords[0]) {
                        accumulateQuad(bbox, 0, x0, coords[0], x1);
                    }
                    if (bbox[1] > coords[1] || bbox[3] < coords[1]) {
                        accumulateQuad(bbox, 1, y0, coords[1], y1);
                    }
                    x0 = x1;
                    y0 = y1;
                    break;
                case PathIterator.BEZIER_TO:
                    x1 = coords[4];
                    y1 = coords[5];
                    if (bbox[0] > x1) bbox[0] = x1;
                    if (bbox[1] > y1) bbox[1] = y1;
                    if (bbox[2] < x1) bbox[2] = x1;
                    if (bbox[3] < y1) bbox[3] = y1;
                    if (bbox[0] > coords[0] || bbox[2] < coords[0] || bbox[0] > coords[2] || bbox[2] < coords[2]) {
                        accumulateCubic(bbox, 0, x0, coords[0], coords[2], x1);
                    }
                    if (bbox[1] > coords[1] || bbox[3] < coords[1] || bbox[1] > coords[3] || bbox[3] < coords[3]) {
                        accumulateCubic(bbox, 1, y0, coords[1], coords[3], y1);
                    }
                    x0 = x1;
                    y0 = y1;
                    break;
                case PathIterator.CLOSE:
                    x0 = mx;
                    y0 = my;
                    break;
            }
            pi.next();
        }
    }

    public static void accumulateQuad(double bbox[], int off, double v0, double vc, double v1) {
        double num = v0 - vc;
        double den = v1 - vc + num;
        if (den != 0.0) {
            double t = num / den;
            if (t > 0 && t < 1) {
                double u = 1.0 - t;
                double v = v0 * u * u + 2 * vc * t * u + v1 * t * t;
                if (bbox[off] > v)     { bbox[off] = v;   }
                if (bbox[off + 2] < v) { bbox[off+2] = v; }
            }
        }
    }

    public static void accumulateCubic(double bbox[], int off, double v0, double vc0, double vc1, double v1) {
        double c = vc0 - v0;
        double b = 2.0 * ((vc1 - vc0) - c);
        double a = (v1 - vc1) - b - c;
        if (Double.compare(a, 0) == 0) {
            if (Double.compare(b, 0) == 0) { return; }
            accumulateCubic(bbox, off, -c/b, v0, vc0, vc1, v1);
        } else {
            double d = b * b - 4.0 * a * c;
            if (d < 0.0) { return; }
            d = Math.sqrt(d);
            if (b < 0.0) { d = -d; }
            double q = (b + d) / -2.0;
            accumulateCubic(bbox, off, q/a, v0, vc0, vc1, v1);
            if (q != 0.0) { accumulateCubic(bbox, off, c/q, v0, vc0, vc1, v1); }
        }
    }

    public static void accumulateCubic(double bbox[], int off, double t, double v0, double vc0, double vc1, double v1) {
        if (t > 0 && t < 1) {
            double u = 1.0 - t;
            double v = v0 * u * u * u + 3 * vc0 * t * u * u + 3 * vc1 * t * t * u + v1 * t * t * t;
            if (bbox[off] > v)     { bbox[off] = v;   }
            if (bbox[off + 2] < v) { bbox[off+2] = v; }
        }
    }


    public Paint getFill() { return fill; }
    public void setFill(final Paint fill) { this.fill = fill;}

    public Paint getStroke() { return stroke; }
    public void setStroke(final Paint stroke) { this.stroke = stroke; }

    public double getLineWidth() { return lineWidth; }
    public void setLineWidth(final double lineWidth) { this.lineWidth = Helper.clamp(0, Double.MAX_VALUE, lineWidth); }

    public StrokeLineJoin getLineJoin() { return lineJoin; }
    public void setLineJoin(final StrokeLineJoin lineJoin) { this.lineJoin = lineJoin; }

    public StrokeLineCap getLineCap() { return lineCap; }
    public void setLineCap(final StrokeLineCap lineCap) { this.lineCap = lineCap; }


    // Method to draw the shape on the GraphicsContext
    public void draw(final GraphicsContext ctx) {
        draw(ctx, true, true);
    }
    public abstract void draw(final GraphicsContext ctx, final boolean doFill, final boolean doStroke);
}