public class Plane extends PlaneBase {

    public Plane(String planeNumber, String time) {
        super(planeNumber, time);
    }

    public int getIntTime() {
        StringBuilder sb = new StringBuilder(this.getTime());
        sb.deleteCharAt(2);
        String resultString = sb.toString();
        return Integer.parseInt(resultString);
    }

    @Override
    public int compareTo(PlaneBase o) {
        Plane plane = (Plane) o;
        if (this.getIntTime() > plane.getIntTime()) {
            return 1;
        } else if (this.getIntTime() < plane.getIntTime()) {
            return -1;
        } else {
            return this.getPlaneNumber().compareTo(plane.getPlaneNumber());
        }
    }
}
