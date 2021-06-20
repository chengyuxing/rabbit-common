package tests.entity;

public class Location {
    private String name;
    private String address;
    private double x;
    private double y;
    private boolean arrived;

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Location(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public Location(String address, double x, double y, boolean arrived) {
        this.address = address;
        this.x = x;
        this.y = y;
        this.arrived = arrived;
    }
}
