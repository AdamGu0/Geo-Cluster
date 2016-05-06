package DBscan;

public class Tuple {

    public double xValue;
    public double yValue;
    public boolean isGPS;

    public boolean isCorePoint = false;     //it is used for DBScan
    public boolean isVisited = false;     //if it is visited by DBScan
    public int clusterID = 0;         // 0 represents the tuple belongs to none cluster

    public Tuple(boolean isGPS, double xValue, double yValue) {
        this.isGPS = isGPS;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public boolean isInRange(I range) {
        boolean flag;
        if (xValue <= range.xMax && xValue >= range.xMin
                && yValue <= range.yMax && yValue >= range.yMin) {
            flag = true;
        } else {
            flag = false;
        }
        return flag;
    }

    public double distanceTo(Tuple anotherTuple) {
        //if (isGPS) return geoDistanceTo(anotherTuple);
        return Math.sqrt(Math.pow(this.xValue - anotherTuple.xValue, 2) + Math.pow(this.yValue - anotherTuple.yValue, 2));
    }
    
    public double geoDistanceTo(Tuple t) {
        double R = 6371; // Radius of the Earth in km
        double dLat = (this.yValue - t.yValue) * Math.PI / 180;
        double dLon = (this.xValue - t.xValue) * Math.PI / 180;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(this.yValue * Math.PI / 180) * Math.cos(t.yValue * Math.PI / 180)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    //return the value of the added area if inserting to one range
    public double areaAddedIfInsertion(I range) {
        double previousArea = (range.xMax - range.xMin) * (range.yMax - range.yMin);

        double newXMin = (xValue < range.xMin) ? xValue : range.xMin;
        double newXMax = (xValue > range.xMax) ? xValue : range.xMax;
        double newYMin = (yValue < range.yMin) ? yValue : range.yMin;
        double newYMax = (yValue > range.yMax) ? yValue : range.yMax;

        double newArea = (newYMax - newYMin) * (newXMax - newXMin);

        return newArea - previousArea;

    }
}
