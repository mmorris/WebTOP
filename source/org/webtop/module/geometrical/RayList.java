/*
* (C) Mississippi State University 2009
*
* The WebTOP employs two licenses for its source code, based on the intended use. The core license for WebTOP applications is 
* a Creative Commons GNU General Public License as described in http://*creativecommons.org/licenses/GPL/2.0/. WebTOP libraries 
* and wapplets are licensed under the Creative Commons GNU Lesser General Public License as described in 
* http://creativecommons.org/licenses/*LGPL/2.1/. Additionally, WebTOP uses the same licenses as the licenses used by Xj3D in 
* all of its implementations of Xj3D. Those terms are available at http://www.xj3d.org/licenses/license.html.
*/

package org.webtop.module.geometrical;

public class RayList implements Cloneable {
    public float x, y, xv, yv, /*x0,y0,xv0,yv0,*/ z;
    public int p;
    public float color[] = new float[3];
    public RayList next;

    public RayList() {}

    public RayList(float X, float Y, float Z, float XV, float YV, int P, float[] RGB) {
        /*x0=*/x = X;
        /*y0=*/y = Y;
        /*z0=*/z = Z;
        /*xv0=*/xv = XV;
        /*yv0=*/yv = YV;
        p = P;
        color = RGB;
    }

    public RayList hook(float X, float Y, float Z, float XV, float YV, int P, float[] RGB) {
        return next = new RayList(X, Y, Z, XV, YV, P, RGB);
    }

    public RayList hook(RayList r) {
        return next = r;
    }

    //Removes next ray from the list; returns one after it (if any).
    //Unhooking when there is nothing to unhook just returns null.
    public RayList unhook() {
        if (next != null) {
            return next = next.next;
        }
        return null;
    }

    //Removes this ray from the list.	 Slower than unhooking the next.
    //Thus, best used only on first ray.
    //Returns null if this is a tail ray which thus cannot unhook itself.
    //Otherwise, returns this ray.
    /**
     * @deprecated
     */
    public RayList unhookSelf() {
        if (next != null) {
            x = next.x;
            y = next.y;
            xv = next.xv;
            yv = next.xv;
            z = next.z;
            p = next.p;
            unhook();
            return this;
        } else {
            return null;
        }
    }

    public void propagate(float Z) {
        x += xv * (Z - z);
        y += yv * (Z - z);
        z = Z;
    }

    public void propagateAll(float Z) {
        for (RayList walker = this; walker != null; walker = walker.next) {
            float dz = Z - walker.z;
            walker.x += walker.xv * dz;
            walker.y += walker.yv * dz;
            walker.z = Z;
        }
    }

    /*public void reset() {
     x=x0;y=y0;
     xv=xv0;yv=yv0;
     z=0;
      }*/
    //If 'in' is false, keeps rays that MISS a circle of diameter d at position p
    //Returns count of rays pruned.  Note that the first ray is never pruned.
    //There may be some benefit to having specialized versions of this that make
    //assumptions like rays already at p or in==true.
    public int prune(float p, float d, boolean in) {
        d *= d / 4; //r^2
        int misses = 0;
        for (RayList walker = next, previous = this; walker != null; ) {
            final float x = walker.x + walker.xv * (p - walker.z),
                            y = walker.y + walker.yv * (p - walker.z);
            if ((x * x + y * y < d) == in) {
                //DebugPrinter.println("Keeping "+walker+" (projected to r="+Math.sqrt(x*x+y*y)+"; {"+x+','+y+','+p+"})");
                previous = walker;
                walker = walker.next;
            } else {
                ++misses;
                walker = previous.unhook();
            }
        }
        return misses;
    }

    public static RayList virtualClone(RayList src) {
        RayList nu = src.copy(), first = nu, walker;
        for (walker = src.next; walker != null; walker = walker.next) {
            RayList copy = walker.copy();
            copy.color = new float[] {1, 1, 0};
            nu = nu.hook(copy);
        }
        return first;
    }

    public static RayList clone(RayList src) {
        RayList nu = src.copy(), first = nu, walker;
        for (walker = src.next; walker != null; walker = walker.next) {
            nu = nu.hook(walker.copy());
        }
        return first;
    }

    private RayList copy() {
        try {
            return (RayList)super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        } //Won't happen; we're Cloneable
    }

    public String toString() {
        return getClass().getName() + "[x=" + x + ",y=" + y + ",xv=" + xv + ",yv=" + yv + ",z=" + z +
                ",p=" + p + (next == null ? " (tail)]" : "]");
    }

    //This is slow!	 Don't call except for debugging.	 Returns number of rays after this one.
    public int getLength() {
        int i = 0;
        for (RayList walker = next; walker != null; walker = walker.next) {
            ++i;
        }
        return i;
    }
}
