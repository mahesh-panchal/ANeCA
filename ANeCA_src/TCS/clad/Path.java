// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces 
// Source File Name:   Path.java

package TCS.clad;

import java.util.*;

// Referenced classes of package clad:
//            TaxaItem

public class Path
{

    public Path(TaxaItem mySourcet, TaxaItem myDestt, int myType)
    {
        source = mySourcet;
        dest = myDestt;
        type = myType;
        resolved = false;
        edges = new ArrayList();
        differences = new ArrayList();
        isAmbiguous = false;
    }

    public String toString()
    {
        return "type=" + type + " source=" + source + " dest=" + dest + " resolved=" + resolved + " length=" + edges.size() + "(" + getLabel() + ")";
    }

    public String getLabel()
    {
        StringBuffer s = new StringBuffer();
        for(Iterator it = differences.iterator(); it.hasNext();)
        {
            s.append(it.next());
            if(it.hasNext())
            {
                s.append(",");
            }
        }

        return s.toString();
    }

    public static final int INTINT = 0;
    public static final int HAPINT = 1;
    public static final int HAPHAP = 2;
    public ArrayList edges;
    public boolean isAmbiguous;
    public int type;
    public TaxaItem source;
    public TaxaItem dest;
    public String label;
    public String sites;
    public boolean resolved;
    public ArrayList differences;
}
