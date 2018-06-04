// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces 
// Source File Name:   PathCompare2.java

package TCS.clad;

import java.util.Comparator;

// Referenced classes of package clad:
//            Path

public class PathCompare2
    implements Comparator
{

    public int compare(Object o1, Object o2)
        throws ClassCastException
    {
        Path p1 = (Path)o1;
        Path p2 = (Path)o2;
        return p2.type - p1.type;
    }

    public PathCompare2()
    {
    }
}
