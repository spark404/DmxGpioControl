package nl.sonicity.raspi.dmx.artnet;

public class DmxHandlerInfo {
    private String name;
    private int universe;
    private int address;
    private int width;

    public DmxHandlerInfo(String name, int universe, int address, int width) {
        this.name = name;
        this.universe = universe;
        this.address = address;
        this.width = width;
    }

    public String getName() {
        return name;
    }

    public int getUniverse() {
        return universe;
    }

    public int getAddress() {
        return address;
    }

    public int getWidth() {
        return width;
    }
}
