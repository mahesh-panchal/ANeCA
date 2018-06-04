// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) braces 
// Source File Name:   Mapper.java

package TCS.clad;

import java.io.PrintStream;
import java.util.*;

// Referenced classes of package clad:
//            Path, LWEdge, TaxaItem, PathCompare, 
//            PathCompare2

public class Mapper
{

    public static Path[] getPaths(ArrayList edges, boolean gapmode)
    {
        Path paths[] = new Path[0];
        ArrayList pathlist = new ArrayList();
        Iterator i = edges.iterator();
        LWEdge edge;
        for(LWEdge prev = null; i.hasNext(); prev = edge)
        {
            edge = (LWEdge)i.next();
            ArrayList pathedges = new ArrayList();
            TaxaItem source = edge.source;
            TaxaItem dest = edge.dest;
            if(source == null || dest == null)
            {
                System.err.println("source or dest == null");
            }
            int numNbors = source.nbor.size();
            Path path = new Path(null, null, -1);
            if(numNbors > 2 || !source.isIntermediate)
            {
                if(source.isIntermediate)
                {
                    path.dest = source;
                } else
                {
                    path.source = source;
                }
            } else
            {
                System.err.println("there is a problem, source is not the end, try dest?");
            }
            for(prev = edge; dest.nbor.size() <= 2 && dest.isIntermediate; prev = edge)
            {
                edge.path = path;
                pathedges.add(edge);
                edge.source.paths.add(path);
                edge.dest.paths.add(path);
                edge = (LWEdge)i.next();
                source = edge.source;
                dest = edge.dest;
            }

            int type = -1;
            edge.path = path;
            pathedges.add(edge);
            edge.source.paths.add(path);
            edge.dest.paths.add(path);
            if(!dest.isIntermediate)
            {
                if(path.source == null)
                {
                    type = 1;
                    path.source = dest;
                } else
                {
                    type = 2;
                    path.dest = dest;
                    computeDifferences(path, gapmode);
                    path.resolved = true;
                }
            } else
            if(path.source == null)
            {
                path.source = path.dest;
                path.dest = dest;
                type = 0;
            } else
            {
                path.dest = dest;
                type = 1;
            }
            path.edges = pathedges;
            path.type = type;
            pathlist.add(path);
        }

        paths = (Path[])pathlist.toArray(paths);
        return paths;
    }

    private static void computeDifferences(Path path, boolean gapmode)
    {
        TaxaItem source = path.source;
        TaxaItem dest = path.dest;
        for(int siteNum = 0; siteNum < source.characters.length; siteNum++)
        {
            if(source.characters[siteNum] != '?' && dest.characters[siteNum] != '?' && source.characters[siteNum] != dest.characters[siteNum] && (gapmode || source.characters[siteNum] != '-' && dest.characters[siteNum] != '-'))
            {
                path.differences.add(new Integer(siteNum));
            }
        }

    }

    private static TaxaItem[] isExternalPath(Path path)
    {
        if(path.source.resolved)
        {
            return (new TaxaItem[] {
                path.dest, path.source
            });
        }
        if(path.dest.resolved)
        {
            return (new TaxaItem[] {
                path.source, path.dest
            });
        } else
        {
            return null;
        }
    }

    public static void map(ArrayList myEdges, int maxParsimonyDistance, boolean gapmode)
    {
        Path paths[] = getPaths(myEdges, gapmode);
        PathCompare pc = new PathCompare();
        Arrays.sort(paths, pc);
        Arrays.sort(paths, new PathCompare2());
        int numUnresolvedPaths = 0;
        boolean changed = false;
        int resolverCount = 2;
        boolean secondtry = false;
        int lastNumUnresolvedPaths = 0;
        do
        {
            do
            {
                do
                {
                    numUnresolvedPaths = 0;
                    changed = false;
                    for(int path = 0; path < paths.length; path++)
                    {
                        if(paths[path].resolved)
                        {
                            continue;
                        }
                        numUnresolvedPaths++;
                        if(paths[path].source.resolved && paths[path].dest.resolved)
                        {
                            computeDifferences(paths[path], gapmode);
                            secondtry = false;
                            paths[path].resolved = true;
                            numUnresolvedPaths--;
                            changed = true;
                            continue;
                        }
                        TaxaItem ends[] = isExternalPath(paths[path]);
                        if(ends == null)
                        {
                            continue;
                        }
                        Hashtable seqs = new Hashtable();
                        HashSet pathset = new HashSet();
                        pathset.add(paths[path]);
                        HashSet resolvednodeset = new HashSet();
                        HashSet siteset = null;
                        ArrayList endPaths = new ArrayList();
                        getResolvedNodes(ends[0], pathset, resolvednodeset, endPaths);
                        TaxaItem node = null;
                        Iterator it = resolvednodeset.iterator();
                        boolean first = true;
                        while(it.hasNext()) 
                        {
                            node = (TaxaItem)it.next();
                            Path temppath = new Path(node, ends[1], -1);
                            computeDifferences(temppath, gapmode);
                            if(first)
                            {
                                siteset = new HashSet(temppath.differences);
                                first = false;
                            } else
                            {
                                siteset.retainAll(temppath.differences);
                            }
                            for(Iterator siteIt = siteset.iterator(); siteIt.hasNext();)
                            {
                                Integer site = (Integer)siteIt.next();
                                Character ch = new Character(node.characters[site.intValue()]);
                                if(!seqs.containsKey(ch))
                                {
                                    seqs.put(ch, new Integer[node.characters.length]);
                                }
                                Integer counts[] = (Integer[])seqs.get(ch);
                                counts[site.intValue()] = counts[site.intValue()] != null ? new Integer(counts[site.intValue()].intValue() + 1) : new Integer(1);
                            }

                        }
                        if(siteset.size() == paths[path].edges.size())
                        {
                            boolean resolved = checkCons(siteset, seqs, paths[path], ends, resolvednodeset.size(), secondtry);
                            if(resolved)
                            {
                                secondtry = false;
                                numUnresolvedPaths--;
                                changed = true;
                            }
                            continue;
                        }
                        if(siteset.size() < paths[path].edges.size() && secondtry)
                        {
                            TaxaItem closestNode = null;
                            closestNode = getNearestResolvedNode(paths[path], ends[0]);
                            if(closestNode != null)
                            {
                                Path pigidy = new Path(ends[1], closestNode, -1);
                                computeDifferences(pigidy, gapmode);
                                HashSet hs = new HashSet(pigidy.differences);
                                hs.removeAll(siteset);
                                if(hs.size() + siteset.size() >= paths[path].edges.size())
                                {
                                    Iterator sites = hs.iterator();
                                    for(; siteset.size() < paths[path].edges.size(); siteset.add(sites.next())) { }
                                }
                                boolean resolved = checkCons(siteset, seqs, paths[path], ends, resolvednodeset.size(), secondtry);
                                if(resolved)
                                {
                                    secondtry = false;
                                    numUnresolvedPaths--;
                                    changed = true;
                                }
                                continue;
                            }
                        }
                        Iterator eachpath = endPaths.iterator();
                        boolean first3 = true;
                        HashSet siteset2 = null;
                        HashSet allOtherSites = new HashSet();
                        while(eachpath.hasNext()) 
                        {
                            Path curpath = (Path)eachpath.next();
                            TaxaItem ends2[] = isExternalPath(curpath);
                            if(ends2 != null)
                            {
                                HashSet resolvednodeset2 = new HashSet(resolvednodeset);
                                resolvednodeset2.remove(ends2[1]);
                                resolvednodeset2.add(ends[1]);
                                TaxaItem node2 = null;
                                for(Iterator it2 = resolvednodeset2.iterator(); it2.hasNext();)
                                {
                                    node2 = (TaxaItem)it2.next();
                                    Path temppath = new Path(node2, ends2[1], -1);
                                    computeDifferences(temppath, gapmode);
                                    if(node2 != ends[1] && ends2[1] != ends[1])
                                    {
                                        allOtherSites.addAll(temppath.differences);
                                    }
                                    if(first3)
                                    {
                                        siteset2 = new HashSet(temppath.differences);
                                        first3 = false;
                                    } else
                                    {
                                        siteset2.retainAll(temppath.differences);
                                    }
                                }

                            } else
                            {
                                System.err.println("ends2 == null?  This is bad!");
                            }
                        }
                        HashSet diff = new HashSet(siteset);
                        diff.removeAll(siteset2);
                        if(diff.size() == paths[path].edges.size())
                        {
                            boolean resolved = checkCons(diff, seqs, paths[path], ends, resolvednodeset.size(), secondtry);
                            if(resolved)
                            {
                                secondtry = false;
                                numUnresolvedPaths--;
                                changed = true;
                                break;
                            }
                        } else
                        {
                            diff = new HashSet(siteset);
                            diff.removeAll(allOtherSites);
                            if(diff.size() == paths[path].edges.size())
                            {
                                boolean resolved = checkCons(diff, seqs, paths[path], ends, resolvednodeset.size(), secondtry);
                                if(resolved)
                                {
                                    secondtry = false;
                                    numUnresolvedPaths--;
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        if(!secondtry || siteset.size() < paths[path].edges.size() + 1 || siteset.size() != resolverCount)
                        {
                            continue;
                        }
                        secondtry = false;
                        paths[path].resolved = true;
                        ends[0].isAmbiguous = true;
                        numUnresolvedPaths--;
                        changed = true;
                        paths[path].differences = new ArrayList(siteset);
                        TaxaItem resolvedEnd = null;
                        resolvedEnd = getNearestResolvedNode(paths[path], ends[0]);
                        if(resolvedEnd == null)
                        {
                            continue;
                        }
                        Hashtable cons = new Hashtable();
                        Iterator i = siteset.iterator();
                        for(int sitenum = 0; sitenum < paths[path].edges.size(); sitenum++)
                        {
                            Integer site = (Integer)i.next();
                            if(ends[1].characters[site.intValue()] != resolvedEnd.characters[site.intValue()])
                            {
                                Character c = new Character(resolvedEnd.characters[site.intValue()]);
                                cons.put(site, c);
                            } else
                            {
                                System.out.println("site " + site + "was not different!");
                            }
                        }

                        assignSequence(ends, paths[path], cons);
                        break;
                    }

                } while(changed);
                if(numUnresolvedPaths > 0 && lastNumUnresolvedPaths != numUnresolvedPaths)
                {
                    secondtry = true;
                    resolverCount = 2;
                    lastNumUnresolvedPaths = numUnresolvedPaths;
                } else
                if(lastNumUnresolvedPaths == numUnresolvedPaths)
                {
                    secondtry = false;
                }
            } while(secondtry);
            resolverCount++;
            secondtry = true;
        } while(resolverCount <= maxParsimonyDistance && numUnresolvedPaths != 0);
        System.out.println("there are " + numUnresolvedPaths + " unresolved paths!");
    }

    private static TaxaItem getNearestResolvedNode(Path path, TaxaItem end)
    {
        Path cpath = null;
        TaxaItem resolvedEnd = null;
        for(int pathnum = 0; pathnum < end.paths.size(); pathnum++)
        {
            cpath = (Path)end.paths.get(pathnum);
            if(cpath != path)
            {
                if(cpath.source.resolved)
                {
                    if(resolvedEnd == null)
                    {
                        resolvedEnd = cpath.source;
                    } else
                    if(!resolvedEnd.isIntermediate)
                    {
                        resolvedEnd = cpath.source;
                    }
                } else
                if(cpath.dest.resolved)
                {
                    if(resolvedEnd == null)
                    {
                        resolvedEnd = cpath.dest;
                    } else
                    if(!resolvedEnd.isIntermediate)
                    {
                        resolvedEnd = cpath.dest;
                    }
                }
            }
        }

        return resolvedEnd;
    }

    private static boolean checkCons(HashSet sites, Hashtable seqs, Path path, TaxaItem ends[], int resolvedNodeSetSize, boolean secondtry)
    {
        boolean result = false;
        Iterator eachsite = sites.iterator();
        Hashtable cons = new Hashtable();
        while(eachsite.hasNext()) 
        {
            Integer site = (Integer)eachsite.next();
            for(Iterator eachChar = seqs.keySet().iterator(); eachChar.hasNext();)
            {
                Character ch = (Character)eachChar.next();
                Integer counts[] = (Integer[])seqs.get(ch);
                if(counts[site.intValue()] != null && counts[site.intValue()].intValue() == resolvedNodeSetSize)
                {
                    cons.put(site, ch);
                }
            }

        }
        if(cons.size() == sites.size())
        {
            path.resolved = true;
            result = true;
            path.differences = new ArrayList(cons.keySet());
            assignSequence(ends, path, cons);
        } else
        if(secondtry)
        {
            Path cpath = null;
            TaxaItem resolvedEnd = getNearestResolvedNode(path, ends[0]);
            if(resolvedEnd != null)
            {
                HashSet remain = new HashSet(sites);
                remain.removeAll(cons.keySet());
                for(Iterator i = remain.iterator(); i.hasNext();)
                {
                    Integer site = (Integer)i.next();
                    if(ends[1].characters[site.intValue()] != resolvedEnd.characters[site.intValue()])
                    {
                        Character c = new Character(resolvedEnd.characters[site.intValue()]);
                        cons.put(site, c);
                    } else
                    {
                        System.out.println("site " + site + "was not different doh!");
                    }
                }

                if(cons.size() == sites.size())
                {
                    path.resolved = true;
                    ends[0].isAmbiguous = true;
                    path.isAmbiguous = true;
                    result = true;
                    path.differences = new ArrayList(cons.keySet());
                    assignSequence(ends, path, cons);
                }
            }
        }
        return result;
    }

    private static void assignSequence(TaxaItem myEnds[], Path myPath, Hashtable cons)
    {
        myEnds[0].characters = (char[])myEnds[1].characters.clone();
        myEnds[0].resolved = true;
        if(myEnds[1].isAmbiguous)
        {
            myEnds[0].isAmbiguous = true;
        }
        for(Iterator it = cons.keySet().iterator(); it.hasNext();)
        {
            Integer site = (Integer)it.next();
            myEnds[0].characters[site.intValue()] = ((Character)cons.get(site)).charValue();
        }

    }

    private static void getResolvedNodes(TaxaItem loosend, HashSet myPathset, HashSet myResolvednodeset, ArrayList endPaths)
    {
        for(Iterator it = loosend.paths.iterator(); it.hasNext();)
        {
            Path path = (Path)it.next();
            if(!myPathset.contains(path))
            {
                myPathset.add(path);
                if(path.source.resolved)
                {
                    myResolvednodeset.add(path.source);
                    endPaths.add(path);
                } else
                {
                    getResolvedNodes(path.source, myPathset, myResolvednodeset, endPaths);
                }
                if(path.dest.resolved)
                {
                    myResolvednodeset.add(path.dest);
                    endPaths.add(path);
                } else
                {
                    getResolvedNodes(path.dest, myPathset, myResolvednodeset, endPaths);
                }
            }
        }

    }

    public Mapper()
    {
    }
}
