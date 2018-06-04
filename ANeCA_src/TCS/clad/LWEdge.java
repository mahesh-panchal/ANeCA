// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces 
// Source File Name:   LWEdge.java

package TCS.clad;

import java.util.ArrayList;

// Referenced classes of package clad:
//            Path, TaxaItem

public class LWEdge
{

    public LWEdge(TaxaItem myDestt, TaxaItem mySourcet, int myType, int label)
    {
        differences = new ArrayList();
        source = mySourcet;
        dest = myDestt;
        type = myType;
        this.label = String.valueOf(label);
        third = new ArrayList();
        inPath = false;
        path = null;
    }

    public String toString()
    {
        return label;
    }

    public Path path;
    public static final int INTINT = 0;
    public static final int HAPINT = 1;
    public static final int HAPHAP = 2;
    public ArrayList third;
    public boolean inPath;
    public int type;
    public TaxaItem source;
    public TaxaItem dest;
    public String label;
    public String sites;
    public double confidence;
    public ArrayList differences;
}
