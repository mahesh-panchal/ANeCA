public class PopulationData implements Comparable{
	
	// Need to see if we can incorporate the distances to

	private double latitude; // y 
	private double longitude; //  x 
	private double popradius = 0;
	
	private int padLimit = 20; // characters long
	
	public PopulationData(double lat, double lon){
		latitude = lat;
		longitude = lon;
	}

	public PopulationData(double lat, double lon, double rad){
		latitude = lat;
		longitude = lon;
		popradius = rad;
	}
	
	public double getLatitude(){
		return latitude;
	}
	
	public void setLatitude(double lat){
		latitude = lat;
	}
	
	public double getLongitude(){
		return longitude;
	}
	
	public void setLongitude(double lon){
		longitude = lon;
	}
	
	public double getPopulationRadius(){
		return popradius;
	}
	
	public void setPopulationRadius(double rad){
		popradius = rad;
	}
	
	public boolean equals(Object o){
		if(o instanceof PopulationData){
			PopulationData pd = (PopulationData) o;
			return ((pd.latitude == latitude) && (pd.longitude == longitude) && (pd.popradius == popradius));
		}
		return false;
	}
	
	public int compareTo(Object o){
		// allows sort by first comparing x, then y, then radius, then if sampled
		PopulationData pd = (PopulationData) o;
		if(pd.longitude == longitude){
			if(pd.latitude == latitude){
				if(pd.popradius == popradius){
					return 0;
				} else if(pd.popradius > popradius){
					return -1;
				} else {
					return 1;
				}
			} else if(pd.latitude > latitude){
				return -1;
			} else {
				return 1;
			}
		} else if(longitude > pd.longitude){
			return 1;
		} else {
			return -1;
		}
	}
	
	public String toString(){
		/* The hopefully formatted way - someone do it better please*/
		StringBuffer str = new StringBuffer();
		str.append(" Lat: ").append(latitude);
		int len = Double.toString(latitude).length();
		for(int i = 0; i < (padLimit - len); i++){
			str.append(" ");
		}
		str.append(" Lon: ").append(longitude);
		len = Double.toString(longitude).length();
		for(int i = 0; i < (padLimit - len); i++){
			str.append(" ");
		}
		str.append(" Rad: ");
		len = Double.toString(popradius).length();
		str.append(popradius);
		return str.append("\n").toString();
		/* The old way
		StringBuffer str = new StringBuffer();
		str.append(" Lat: ").append(latitude);
		str.append(" Lon: ").append(longitude);
		str.append(" Rad: ").append(popradius);
		return str.append("\n").toString();*/
	}
	
	public PopulationData copy(){
		return new PopulationData(latitude,longitude,popradius);
	}
	
}
