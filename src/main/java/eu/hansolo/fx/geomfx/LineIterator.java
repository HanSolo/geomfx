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
import eu.hansolo.fx.geomfx.transform.BaseTransform;

import java.util.NoSuchElementException;


class LineIterator implements PathIterator {
    Line          line;
    BaseTransform transform;
    int           index;


    LineIterator(final Line line, final BaseTransform transform) {
        this.line      = line;
        this.transform = transform;
    }


    public boolean isDone() { return (index > 1); }

    public void next() { ++index; }

    public WindingRule getWindingRule() { return WindingRule.WIND_NON_ZERO; }

    public int currentSegment(final double[] coords) {
        if (isDone()) { throw new NoSuchElementException("line iterator out of bounds"); }
        int type;
        if (index == 0) {
            coords[0] = line.x1;
            coords[1] = line.y1;
            type = MOVE_TO;
        } else {
            coords[0] = line.x2;
            coords[1] = line.y2;
            type = LINE_TO;
        }
        if (transform != null) { transform.transform(coords, 0, coords, 0, 1); }
        return type;
    }
}