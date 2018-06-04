
import java.util.*;

public class ANeCAdata extends ArrayList{
	
	public ANeCAdata(){
	}
	
	public CladeGroupList findCrossValidatedInferences(){
		// Make grouping based on inference
		// Make grouping based on concordence. 
		// - If clade is concordant with at least one other in group
		// - then regarded as concordant as a whole.
		// Then do time check.
		CladeGroupList cgl = new CladeGroupList();
		for(Iterator i = iterator(); i.hasNext();){
			// over all loci
			ANeCAlocus anLoc = (ANeCAlocus) i.next();
			//anLoc.consolidateData();
			for(Iterator k = anLoc.iterator(); k.hasNext();){
				FinalisedCladeData fcd = (FinalisedCladeData)k.next();
				cgl.sortCladesIntoGroups(fcd); 
			}
		}
		cgl.separateByGeographicConcordence();
		return cgl;
	}
}
