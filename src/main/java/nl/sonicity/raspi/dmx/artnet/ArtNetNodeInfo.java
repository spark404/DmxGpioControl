package nl.sonicity.raspi.dmx.artnet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArtNetNodeInfo {
    String shortName;
    String lastSeen;
}
