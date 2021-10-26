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

package eu.hansolo.fx.geomfx.tools;


import eu.hansolo.fx.geomfx.transform.Affine;


public class Helper {

    public static final int clamp(final int MIN, final int MAX, final int VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final long clamp(final long MIN, final long MAX, final long VALUE) {
        if (VALUE < MIN) return MIN;
        if (VALUE > MAX) return MAX;
        return VALUE;
    }
    public static final double clamp(final double MIN, final double MAX, final double VALUE) {
        if (Double.compare(VALUE, MIN) < 0) return MIN;
        if (Double.compare(VALUE, MAX) > 0) return MAX;
        return VALUE;
    }

    public static final double getAngleFromXY(final double x, final double y, final double centerX, final double centerY) {
        return getAngleFromXY(x, y, centerX, centerY, 90.0);
    }
    public static final double getAngleFromXY(final double x, final double y, final double centerX, final double centerY, final double angleOffset) {
        // For ANGLE_OFFSET =  0 -> Angle of 0 is at 3 o'clock
        // For ANGLE_OFFSET = 90  ->Angle of 0 is at 12 o'clock
        double deltaX      = x - centerX;
        double deltaY      = y - centerY;
        double radius      = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        double nx          = deltaX / radius;
        double ny          = deltaY / radius;
        double theta       = Math.atan2(ny, nx);
        theta              = Double.compare(theta, 0.0) >= 0 ? Math.toDegrees(theta) : Math.toDegrees((theta)) + 360.0;
        double angle       = (theta + angleOffset) % 360;
        return angle;
    }

    public static final Point rotatePointAroundRotationCenter(final Point point, final Point rotationCenter, final double angleDeg) {
        Point  result   = new Point();
        Affine rotation = new Affine();
        double rad      = Math.toRadians(angleDeg);
        rotation.rotate(rad, rotationCenter.x, rotationCenter.y);
        rotation.transform(point, result);
        return result;
    }
    public static final double[] rotatePointAroundRotationCenter(final double x, final double y, final double rX, final double rY, final double angleDeg) {
        final double rad = Math.toRadians(angleDeg);
        final double sin = Math.sin(rad);
        final double cos = Math.cos(rad);
        final double nX  = rX + (x - rX) * cos - (y - rY) * sin;
        final double nY  = rY + (x - rX) * sin + (y - rY) * cos;
        return new double[] { nX, nY };
    }

    public static final Point getPointBetweenP1AndP2(final Point p1, final Point p2) {
        double[] xy = getPointBetweenP1AndP2(p1.x, p1.y, p2.x, p2.y);
        return new Point(xy[0], xy[1]);
    }
    public static final double[] getPointBetweenP1AndP2(final double p1X, final double p1Y, final double p2X, final double p2Y) {
        return new double[] { (p1X + p2X) * 0.5, (p1Y + p2Y) * 0.5 };
    }

}
