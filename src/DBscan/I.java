package DBscan;

public class I {
	
	public double xMin;
	public double xMax;
	public double yMin;
	public double yMax;
	
	public I(){}
	
	public I(double xMin, double xMax, double yMin, double yMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}
	
	public void setRange(double xMin, double xMax, double yMin, double yMax)
	{
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}
	
	public boolean overlappedWith(I anotherRange)
	{
		boolean flag = false;
		
		if (anotherRange.xMax < this.xMin || anotherRange.xMin > this.xMax
			|| anotherRange.yMax < this.yMin || anotherRange.yMin > this.yMax)
			flag = false;
		else flag = true;
		
		return flag;
	}
	
	public void printRange()
	{
		System.out.println("xMin: "+xMin+" xMax: "+xMax+" yMin: "+yMin+" yMax: "+ yMax);
	}

}
