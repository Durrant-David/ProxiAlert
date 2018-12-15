package edu.byui.team06.proxialert.database.model;

public class Fence {
    private int id;
    private double lat;
    private double lng;
    private float radius;
    private long duration;
    private int dwell;

    public Fence(){

    }

    public Fence(int id, double lat, double lng, float radius, long duration, int dwell) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.duration = duration;
        this.dwell = dwell;
    }

    public Fence(ProxiDB proxiDB) {
        this.id = proxiDB.getId();
        this.lat = Double.valueOf(proxiDB.getLat());
        this.lng = Double.valueOf(proxiDB.getLong());
        this.radius = convertToMeters(Float.valueOf(proxiDB.getRadius()), proxiDB.getUnits());
    }

    private float convertToMeters(float value, String unit) {

        float returnVal;

        switch (unit) {
            case "Miles":
                returnVal = value * 5280;
                break;
            case "Meters":
                returnVal = value;
                break;
            case "Feet":
                returnVal = value * 0.3048f;
                break;
            case "Km":
                returnVal = value * 1000;
                break;
            default:
                return 100;
        }

        if(returnVal < 100) {
            return 100;
        } else {
            return returnVal;
        }
    }

    public String getStringId(){
        return String.valueOf(id);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getDwell() {
        return dwell;
    }

    public void setDwell(int dwell) {
        this.dwell = dwell;
    }
}

