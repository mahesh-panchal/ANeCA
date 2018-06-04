package ANeCA;

import javax.swing.JMenuItem;
import java.awt.event.*;

class NCPAActionListener implements ActionListener{

	public void actionPerformed(ActionEvent e){
		String id = ((JMenuItem) e.getSource()).getText();
		if(id.equals(NCPA.TCS_MENU_LABEL)){
			Mediator.newTCSAnalysis();
		} else if(id.equals(NCPA.NEST_MENU_LABEL)){
			Mediator.createGeoDisFile();
		} else if(id.equals(NCPA.GEODIS_MENU_LABEL)){
			Mediator.startGeodis();
		} else if(id.equals(NCPA.INFERENCE_MENU_LABEL)){
			Mediator.runInferenceKey();
		} else if(id.equals("Exit NCPA")){
			System.exit(0);
		} else {
			System.out.println("Hey unknown Action here!");
		}
	}
}	
