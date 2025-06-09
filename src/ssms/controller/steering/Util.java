/*
 * Copyright (C) 2021 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.controller.steering;

import org.lwjgl.util.vector.Vector2f;

/**
 *
 * @author Malte Schulze
 */
public class Util {
    static public float getFacingFromHeading(Vector2f v) {
        return (float)Math.atan2(v.y, v.x) * 57.295784F;
        //return com.fs.starfarer.prototype.Utils.\u00D300000(v);
    }
    
    static public Vector2f getHeadingFromFacing(float f) {
        Vector2f vector2f = new Vector2f();
        //either with or without
        f = f * 0.017453292F;
        vector2f.x = (float)Math.cos(f);
        vector2f.y = (float)Math.sin(f);
        return vector2f;
        //return com.fs.starfarer.prototype.Utils.\u00D200000(f);
    }
}
