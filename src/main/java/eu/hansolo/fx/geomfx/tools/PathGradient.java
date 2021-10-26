package eu.hansolo.fx.geomfx.tools;

import com.google.common.graph.Graph;
import eu.hansolo.fx.geomfx.Path;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class PathGradient {

    public static void strokePathWithGradient(final GraphicsContext ctx, final Path path, final GradientLookup gradientLookup) {
        //Map<Point, Double> samples = samplePath(path, 0.1);
        Map<Point, Double> samples        = samplePath(path, 0.1);
        Map<Point, Double> samplesPattern = samplePath(path, 10);
        //draw(ctx, samplesPattern, gradientLookup, path.getLineWidth(), path.getLineCap());
        draw(ctx, samples, samplesPattern, gradientLookup, path.getLineWidth(), path.getLineCap());
    }

    private static Map<Point, Double> samplePath(final Path path, final double precision) {
        PathTool           pathTool         = new PathTool(path);
        double             length           = pathTool.getLengthOfPath();
        Map<Point, Double> pointFractionMap = new LinkedHashMap<>();
        for (double i = 0 ; i < length ; i += precision) {
            Point p = pathTool.getPointAtLength(i);
            pointFractionMap.put(p, (i / length));
        }
        pointFractionMap.put(pathTool.getSegmentPointAtLength(length), 1.0);
        return pointFractionMap;
    }

    private static void draw1(final GraphicsContext ctx, final Map<Point, Double> samples, final GradientLookup gradientLookup, final double lineWidth, final StrokeLineCap lineCap) {
        ctx.save();
        ctx.setLineCap(lineCap);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        AtomicReference<Point> lp = new AtomicReference<>(samples.keySet().iterator().next());
        samples.keySet().forEach(p -> {
            ctx.setLineWidth(lineWidth);
            ctx.setStroke(gradientLookup.getColorAt(samples.get(p)));
            ctx.strokeLine(lp.get().getX(), lp.get().getY(), p.getX(), p.getY());
            lp.set(p);
        });
        ctx.restore();
    }

    private static void draw2(final GraphicsContext ctx, final Map<Point, Double> samples, final GradientLookup gradientLookup, final double lineWidth, final StrokeLineCap lineCap) {
        ctx.save();
        ctx.setLineCap(lineCap);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        AtomicReference<Point> lastPoint = new AtomicReference<>(samples.keySet().iterator().next());
        samples.keySet().forEach(p -> {
            ctx.setLineWidth(lineWidth);
            ctx.setFill(gradientLookup.getColorAt(samples.get(p)));
            double[] patternCenter = Helper.getPointBetweenP1AndP2(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY());
            double   alpha         = Helper.getAngleFromXY(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY(), 180);
            ctx.save();
            ctx.translate(patternCenter[0], patternCenter[1]);
            ctx.rotate(alpha);
            ctx.translate(-patternCenter[0], -patternCenter[1]);

            ctx.beginPath();
            ctx.moveTo(patternCenter[0] - 2.5, patternCenter[1] - 2.5);
            ctx.lineTo(patternCenter[0], patternCenter[1] - 2.5);
            ctx.lineTo(patternCenter[0] + 2.5, patternCenter[1]);
            ctx.lineTo(patternCenter[0], patternCenter[1] + 2.5);
            ctx.lineTo(patternCenter[0] - 2.5, patternCenter[1] + 2.5);
            ctx.lineTo(patternCenter[0], patternCenter[1]);
            ctx.lineTo(patternCenter[0] - 2.5, patternCenter[1] - 2.5);
            ctx.closePath();
            ctx.fill();
            ctx.restore();

            lastPoint.set(p);
        });
        ctx.restore();
    }

    private static void draw3(final GraphicsContext ctx, final Map<Point, Double> samples, final GradientLookup gradientLookup, final double lineWidth, final StrokeLineCap lineCap) {
        ctx.save();
        ctx.setLineCap(lineCap);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        AtomicReference<Point> lastPoint = new AtomicReference<>(samples.keySet().iterator().next());
        samples.keySet().forEach(p -> {
            ctx.setLineWidth(lineWidth);
            ctx.setFill(gradientLookup.getColorAt(samples.get(p)));
            double[] patternCenter = Helper.getPointBetweenP1AndP2(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY());
            double   alpha         = Helper.getAngleFromXY(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY(), 180);
            ctx.save();
            ctx.translate(patternCenter[0], patternCenter[1]);
            ctx.rotate(alpha);

            ctx.beginPath();
            ctx.appendSVGPath("M-9,-4l0,-1l18,0l-0,4.996l-3.998,-3.996l-5.002,0l4.002,4l-4.002,4l5.002,0l3.998,-3.996l-0,4.996l-18,0l0,-1l4.99,0l3.998,-3.996l0,-0.008l-3.998,-3.996l-4.99,0Zm3.99,4l-3.99,3.988l0,-7.976l3.99,3.988Z");
            ctx.closePath();
            ctx.fill();

            ctx.translate(-patternCenter[0], -patternCenter[1]);
            ctx.restore();

            lastPoint.set(p);
        });
        ctx.restore();
    }

    private static void draw4(final GraphicsContext ctx, final Map<Point, Double> samples, final GradientLookup gradientLookup, final double lineWidth, final StrokeLineCap lineCap) {
        ctx.save();
        ctx.setLineCap(lineCap);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        AtomicReference<Point> lastPoint = new AtomicReference<>(samples.keySet().iterator().next());
        samples.keySet().forEach(p -> {
            ctx.setLineWidth(lineWidth);
            ctx.setFill(gradientLookup.getColorAt(samples.get(p)));
            double[] patternCenter = Helper.getPointBetweenP1AndP2(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY());
            double   alpha         = Helper.getAngleFromXY(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY(), 270);
            ctx.save();
            ctx.translate(patternCenter[0], patternCenter[1]);
            ctx.rotate(alpha);

            ctx.beginPath();
            ctx.appendSVGPath("M0,-4.995l1.125,3.451l3.642,0l-2.946,2.134l1.125,3.451l-2.946,-2.133l-2.946,2.133l1.125,-3.451l-2.946,-2.134l3.642,0l1.125,-3.451Z");
            ctx.closePath();
            ctx.fill();

            ctx.translate(-patternCenter[0], -patternCenter[1]);
            ctx.restore();

            lastPoint.set(p);
        });
        ctx.restore();
    }

    private static void draw(final GraphicsContext ctx, final Map<Point, Double> samples, final Map<Point, Double> samplesPattern, final GradientLookup gradientLookup, final double lineWidth, final StrokeLineCap lineCap) {
        ctx.save();
        ctx.setLineCap(lineCap);
        ctx.setLineJoin(StrokeLineJoin.ROUND);
        AtomicReference<Point> lastPoint = new AtomicReference<>(samples.keySet().iterator().next());
        ctx.setStroke(Color.BLACK);
        samples.keySet().forEach(p -> {
            ctx.setLineWidth(lineWidth);
            ctx.strokeLine(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY());
            lastPoint.set(p);
        });

        lastPoint.set(samplesPattern.keySet().iterator().next());
        samplesPattern.keySet().forEach(p -> {
            ctx.setFill(gradientLookup.getColorAt(samplesPattern.get(p)));
            double[] patternCenter = Helper.getPointBetweenP1AndP2(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY());
            double   alpha         = Helper.getAngleFromXY(lastPoint.get().getX(), lastPoint.get().getY(), p.getX(), p.getY(), 180);
            ctx.save();
            ctx.translate(patternCenter[0], patternCenter[1]);
            ctx.rotate(alpha);

            ctx.beginPath();
            ctx.appendSVGPath("M0,-2.112c0.843,-1.408 2.529,-1.408 3.371,-0.704c0.843,0.704 0.843,2.112 0,3.52c-0.59,1.056 -2.107,2.112 -3.371,2.816c-1.264,-0.704 -2.781,-1.76 -3.371,-2.816c-0.843,-1.408 -0.843,-2.816 -0,-3.52c0.842,-0.704 2.528,-0.704 3.371,0.704Z");
            ctx.closePath();
            ctx.fill();

            ctx.translate(-patternCenter[0], -patternCenter[1]);
            ctx.restore();

            lastPoint.set(p);
        });
        ctx.restore();
    }
}
