/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.locationtech.jts.android.geom;


import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Path;
import org.mapsforge.core.graphics.Style;

/**
 * A {@link Path} based {@link DrawableShape shape}.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 *
 */
public class PathShape implements DrawableShape {

    private Path path;

    public PathShape( Path path ) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public void draw(Canvas canvas, Paint paint ) {
        paint.setStyle(Style.STROKE);
        canvas.drawPath(path, paint);
    }

    @Override
    public void fill( Canvas canvas, Paint paint ) {
        paint.setStyle(Style.FILL);
        canvas.drawPath(path, paint);
    }

}