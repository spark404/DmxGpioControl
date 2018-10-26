package nl.sonicity.raspi.dmx.artnet;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ArtNetNodeConfig {
    private String networkInterface;
    private int universe;
    private int subnet;
    private int network;
}
