package eu.hansolo.fx.geomfx.tools;

import eu.hansolo.fx.geomfx.Path;
import eu.hansolo.fx.geomfx.Path.WindingRule;
import eu.hansolo.fx.geomfx.transform.Affine;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.VLineTo;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.List;


public class ShapeConverter {
    public static final Path convert(final java.awt.geom.Path2D swingPath) {
        return convert(swingPath, Color.BLACK, Color.BLACK, 1, StrokeLineCap.BUTT, StrokeLineJoin.BEVEL, "");
    }
    public static final Path convert(final java.awt.geom.Path2D swingPath, final Paint fill, final Paint stroke, final double lineWidth, final StrokeLineCap lineCap, final StrokeLineJoin lineJoin, final String text) {
        PathIterator pathIterator = swingPath.getPathIterator(new AffineTransform());
        int          windingRule  = swingPath.getWindingRule();
        Path         path         = new Path();
        path.setWindingRule(windingRule == Path2D.WIND_EVEN_ODD ? WindingRule.WIND_EVEN_ODD : WindingRule.WIND_NON_ZERO);
        double[]     seg          = new double[6];
        int          segType;
        while(!pathIterator.isDone()) {
            segType = pathIterator.currentSegment(seg);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(seg[0], seg[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(seg[0], seg[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadraticCurveTo(seg[0], seg[1], seg[2], seg[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.bezierCurveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                default: break;
            }
            pathIterator.next();
        }
        return path;
    }

    public static final java.awt.geom.Path2D convertToSwingPath(final Path path) {
        eu.hansolo.fx.geomfx.PathIterator pathIterator = path.getPathIterator(new Affine());
        WindingRule                       windingRule  = path.getWindingRule();
        java.awt.geom.Path2D                      swingPath    = new java.awt.geom.GeneralPath();
        swingPath.setWindingRule(windingRule == WindingRule.WIND_EVEN_ODD ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
        double[]     seg          = new double[6];
        int          segType;
        while(!pathIterator.isDone()) {
            segType = pathIterator.currentSegment(seg);
            switch (segType) {
                case PathIterator.SEG_MOVETO:
                    swingPath.moveTo(seg[0], seg[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    swingPath.lineTo(seg[0], seg[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    swingPath.quadTo(seg[0], seg[1], seg[2], seg[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    swingPath.curveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    swingPath.closePath();
                    break;
                default: break;
            }
            pathIterator.next();
        }
        return swingPath;
    }

    public static final Path convert(final javafx.scene.shape.Path fxPath) {
        return convert(fxPath, "");
    }
    public static final Path convert(final javafx.scene.shape.Path fxPath, final String text) {
        List<PathElement> elements = fxPath.getElements();
        FillRule          fillRule = fxPath.getFillRule();
        Path              path     = new Path();
        path.setWindingRule(fillRule == FillRule.EVEN_ODD ? WindingRule.WIND_EVEN_ODD : WindingRule.WIND_NON_ZERO);
        double            lastX    = 0;
        double            lastY    = 0;
        for (PathElement element : elements) {
            if (element instanceof MoveTo) {
                MoveTo moveTo = (MoveTo) element;
                path.moveTo(moveTo.getX(), moveTo.getY());
                lastX = moveTo.getX();
                lastY = moveTo.getY();
            } else if (element instanceof LineTo) {
                LineTo lineTo = (LineTo) element;
                path.lineTo(lineTo.getX(), lineTo.getY());
                lastX = lineTo.getX();
                lastY = lineTo.getY();
            } else if (element instanceof HLineTo) {
                HLineTo hLineTo = (HLineTo) element;
                path.lineTo(hLineTo.getX(), lastY);
                lastX = hLineTo.getX();
            } else if (element instanceof VLineTo) {
                VLineTo vLineTo = (VLineTo) element;
                path.lineTo(lastX, vLineTo.getY());
                lastY = vLineTo.getY();
            } else if (element instanceof QuadCurveTo) {
                QuadCurveTo quadCurveTo = (QuadCurveTo) element;
                path.quadraticCurveTo(quadCurveTo.getControlX(), quadCurveTo.getControlY(), quadCurveTo.getX(), quadCurveTo.getY());
                lastX = quadCurveTo.getX();
                lastY = quadCurveTo.getY();
            } else if (element instanceof CubicCurveTo) {
                CubicCurveTo cubicCurveTo = (CubicCurveTo) element;
                path.bezierCurveTo(cubicCurveTo.getControlX1(), cubicCurveTo.getControlY1(), cubicCurveTo.getControlX2(), cubicCurveTo.getControlY2(), cubicCurveTo.getX(), cubicCurveTo.getY());
                lastX = cubicCurveTo.getX();
                lastY = cubicCurveTo.getY();
            } else if (element instanceof ClosePath) {
                path.closePath();
            }
        }
        path.setFill(fxPath.getFill());
        path.setStroke(fxPath.getStroke());
        path.setLineWidth(fxPath.getStrokeWidth());
        path.setLineCap(fxPath.getStrokeLineCap());
        path.setLineJoin(fxPath.getStrokeLineJoin());
        return path;
    }

    public static final javafx.scene.shape.Path convertToFxPath(final Path path) {
        eu.hansolo.fx.geomfx.PathIterator pathIterator = path.getPathIterator(new Affine());
        WindingRule                       windingRule  = path.getWindingRule();
        javafx.scene.shape.Path                   fxPath       = new javafx.scene.shape.Path();
        fxPath.setFillRule(windingRule == WindingRule.WIND_EVEN_ODD ? FillRule.EVEN_ODD: FillRule.NON_ZERO);
        double[]     seg          = new double[6];
        int          segType;
        while(!pathIterator.isDone()) {
            segType = pathIterator.currentSegment(seg);
            switch (segType) {
                case PathIterator.SEG_MOVETO : fxPath.getElements().add(new MoveTo(seg[0], seg[1])); break;
                case PathIterator.SEG_LINETO : fxPath.getElements().add(new LineTo(seg[0], seg[1])); break;
                case PathIterator.SEG_QUADTO : fxPath.getElements().add(new QuadCurveTo(seg[0], seg[1], seg[2], seg[3])); break;
                case PathIterator.SEG_CUBICTO: fxPath.getElements().add(new CubicCurveTo(seg[0], seg[1], seg[2], seg[3], seg[4], seg[5])); break;
                case PathIterator.SEG_CLOSE  : fxPath.getElements().add(new ClosePath()); break;
                default: break;
            }
            pathIterator.next();
        }
        fxPath.setFill(path.getFill());
        fxPath.setStroke(path.getStroke());
        fxPath.setStrokeWidth(path.getLineWidth());
        fxPath.setStrokeLineCap(path.getLineCap());
        fxPath.setStrokeLineJoin(path.getLineJoin());

        return fxPath;
    }
}
