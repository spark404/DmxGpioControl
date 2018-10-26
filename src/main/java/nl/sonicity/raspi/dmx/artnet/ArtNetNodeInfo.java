package nl.sonicity.raspi.dmx.artnet;

public class ArtNetNodeInfo {
    String shortName;
    String lastSeen;

    public ArtNetNodeInfo() {
    }

    public ArtNetNodeInfo(String shortName, String lastSeen) {
        this.shortName = shortName;
        this.lastSeen = lastSeen;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(String lastSeen) {
        this.lastSeen = lastSeen;
    }
}
