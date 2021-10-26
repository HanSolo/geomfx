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

import eu.hansolo.fx.geomfx.transform.BaseTransform;
import javafx.scene.canvas.GraphicsContext;


public class Ellipse extends RectangularShape {
    public double x;
    public double y;
    public double width;
    public double height;


    public Ellipse() { }
    public Ellipse(final double X, final double Y, final double WIDTH, final double HEIGHT) {
        set(X, Y, WIDTH, HEIGHT);
    }
    /*public Ellipse(double centerX, double centerY, double radiusX, double radiusY) {
        setFrame(centerX - radiusX, centerY - radiusY, radiusX * 2, radiusY * 2);
    }*/


    @Override public double getX() { return x; }
    @Override public double getY() { return y; }

    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return height; }

    public double getCenterX() { return (x + width) / 2; }
    public double getCenterY() { return (y + height) / 2; }

    public double getRadiusX() { return width / 2; }
    public double getRadiusY() { return height / 2; }

    @Override public boolean isEmpty() { return (Double.compare(width, 0) <= 0 || Double.compare(height, 0) <= 0); }

    public void set(final double X, final double Y, final double WIDTH, final double HEIGHT) {
        x      = X;
        y      = Y;
        width  = WIDTH;
        height = HEIGHT;
    }

    public RectBounds getBounds() { return new RectBounds(x, y, x + width, y + height); }

    public boolean intersects(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) { return false; }

        double ellw = this.width;

        if (ellw <= 0) { return false; }

        double normx0 = (x - this.x) / ellw - 0.5;
        double normx1 = normx0 + width / ellw;
        double ellh   = this.height;

        if (ellh <= 0) { return false; }

        double normy0 = (y - this.y) / ellh - 0.5;
        double normy1 = normy0 + height / ellh;
        double nearx, neary;
        if (normx0 > 0) {
            nearx = normx0;
        } else if (normx1 < 0) {
            nearx = normx1;
        } else {
            nearx = 0;
        }
        if (normy0 > 0) {
            neary = normy0;
        } else if (normy1 < 0) {
            neary = normy1;
        } else {
            neary = 0;
        }
        return (nearx * nearx + neary * neary) < 0.25;
    }

    public boolean contains(double x, double y) {
        double ellw = this.width;

        if (ellw <= 0) { return false; }

        double normx = (x - this.x) / ellw - 0.5;
        double ellh = this.height;

        if (ellh <= 0) { return false; }

        double normy = (y - this.y) / ellh - 0.5;

        return (normx * normx + normy * normy) < 0.25;
    }
    public boolean contains(double x, double y, double width, double height) {
        return (contains(x, y) &&
                contains(x + width, y) &&
                contains(x, y + height) &&
                contains(x + width, y + height));
    }

    public PathIterator getPathIterator(BaseTransform transform) { return new EllipseIterator(this, transform); }

    @Override public Ellipse copy() { return new Ellipse(x, y, width, height); }

    @Override public boolean equals(final Object OBJECT) {
        if (OBJECT == this) { return true; }
        if (OBJECT instanceof Ellipse) {
            Ellipse e2d = (Ellipse) OBJECT;
            return ((x == e2d.x) && (y == e2d.y) && (width == e2d.width) && (height == e2d.height));
        }
        return false;
    }

    @Override public void draw(final GraphicsContext ctx, final boolean doFill, final boolean doStroke) {
        if (doFill)   {
            ctx.setFill(getFill());
            ctx.fillOval(getX(), getY(), getWidth(), getHeight());
        }
        if (doStroke) {
            ctx.save();
            ctx.setLineWidth(getLineWidth());
            ctx.setLineCap(getLineCap());
            ctx.setLineJoin(getLineJoin());
            ctx.setStroke(getStroke());
            ctx.strokeOval(getX(), getY(), getWidth(), getHeight());
            ctx.restore();
        }
    }
}