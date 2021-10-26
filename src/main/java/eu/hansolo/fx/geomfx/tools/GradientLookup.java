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

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class GradientLookup {
    private Map<Double, Stop> stops;


    // ******************** Constructors **************************************
    public GradientLookup () {
        this(new Stop[]{});
    }
    public GradientLookup(final Stop... stops) {
        this(Arrays.asList(stops));
    }
    public GradientLookup(final List<Stop> stops) {
        this.stops = new TreeMap<>();
        for (Stop stop : stops) { this.stops.put(stop.getOffset(), stop); }
        init();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (stops.isEmpty()) return;

        double minFraction = Collections.min(stops.keySet());
        double maxFraction = Collections.max(stops.keySet());

        if (Double.compare(minFraction, 0) > 0) { stops.put(0.0, new Stop(0.0, stops.get(minFraction).getColor())); }
        if (Double.compare(maxFraction, 1) < 0) { stops.put(1.0, new Stop(1.0, stops.get(maxFraction).getColor())); }
    }


    // ******************** Methods *******************************************
    public Color getColorAt(final double positionOfColor) {
        if (stops.isEmpty()) return Color.BLACK;
        final int    SIZE     = stops.size();
        final double POSITION = Helper.clamp(0.0, 1.0, positionOfColor);
        final Color COLOR;
        if (SIZE == 1) {
            final Map<Double, Color> ONE_ENTRY = (Map<Double, Color>) stops.entrySet().iterator().next();
            COLOR = stops.get(ONE_ENTRY.keySet().iterator().next()).getColor();
        } else {
            Stop lowerBound = stops.get(0.0);
            Stop upperBound = stops.get(1.0);
            int  counter    = 0;
            for (Double fraction : stops.keySet()) {
                if (counter != SIZE - 1 && Double.compare(fraction, POSITION) == 0) {
                    lowerBound = stops.get(fraction);
                } else if (Double.compare(fraction, POSITION) < 0) {
                    lowerBound = stops.get(fraction);
                } else if (Double.compare(fraction, POSITION) > 0) {
                    upperBound = stops.get(fraction);
                    break;
                }
                counter++;
            }
            COLOR = interpolateColor(lowerBound, upperBound, POSITION);
        }
        return COLOR;
    }

    public List<Stop> getStops() { return new ArrayList<>(stops.values()); }
    public void setStops(final Stop... stops) { setStops(Arrays.asList(stops)); }
    public void setStops(final List<Stop> stops) {
        this.stops.clear();
        for (Stop stop : stops) { this.stops.put(stop.getOffset(), stop); }
        init();
    }

    public Stop getStopAt(final double positionOfStop) {
        if (stops.isEmpty()) { throw new IllegalArgumentException("GradientStop stops should not be empty"); };

        final double POSITION = Helper.clamp(0.0, 1.0, positionOfStop);

        Stop stop = null;
        double distance = Math.abs(stops.get(Double.valueOf(0)).getOffset() - POSITION);
        for(Entry<Double, Stop> entry : stops.entrySet()) {
            double cdistance = Math.abs(entry.getKey() - POSITION);
            if (cdistance < distance) {
                stop = stops.get(entry.getKey());
                distance = cdistance;
            }
        }
        return stop;
    }

    public List<Stop> getStopsBetween(final double minOffset, final double maxOffset) {
        List<Stop> selectedStops = new ArrayList<>();
        for (Entry<Double, Stop> entry : stops.entrySet()) {
            if (entry.getValue().getOffset() >= minOffset && entry.getValue().getOffset() <= maxOffset) { selectedStops.add(entry.getValue()); }
        }
        return selectedStops;
    }

    private Color interpolateColor(final Stop lowerBound, final Stop upperBound, final double position) {
        final double POS  = (position - lowerBound.getOffset()) / (upperBound.getOffset() - lowerBound.getOffset());

        final double DELTA_RED     = (upperBound.getColor().getRed()     - lowerBound.getColor().getRed())     * POS;
        final double DELTA_GREEN   = (upperBound.getColor().getGreen()   - lowerBound.getColor().getGreen())   * POS;
        final double DELTA_BLUE    = (upperBound.getColor().getBlue()    - lowerBound.getColor().getBlue())    * POS;
        final double DELTA_OPACITY = (upperBound.getColor().getOpacity() - lowerBound.getColor().getOpacity()) * POS;

        final double red     = Helper.clamp(0.0, 1.0, (lowerBound.getColor().getRed()     + DELTA_RED));
        final double green   = Helper.clamp(0.0, 1.0, (lowerBound.getColor().getGreen()   + DELTA_GREEN));
        final double blue    = Helper.clamp(0.0, 1.0, (lowerBound.getColor().getBlue()    + DELTA_BLUE));
        final double opacity = Helper.clamp(0.0, 1.0, (lowerBound.getColor().getOpacity() + DELTA_OPACITY));

        return Color.color(red, green, blue, opacity);
    }
}
