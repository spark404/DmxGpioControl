package nl.sonicity.raspi.dmx.artnet;

import java.util.HashMap;
import java.util.Map;

public enum ArtNetOpCodes {
    OpPoll(0x2000),
    OpPollReply(0x2100),
    OpDmx(0x5000),
    OpNzs(0x5100);

    private final static Map<Integer, ArtNetOpCodes> intToType = new HashMap<>();

    static {
        for (ArtNetOpCodes artNetOpCodes : ArtNetOpCodes.values()) {
            intToType.put(artNetOpCodes.getOpCode(), artNetOpCodes);
        }
    }

    private final int opCode;

    ArtNetOpCodes(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static ArtNetOpCodes fromInt(Integer integer) {
        return intToType.get(integer);
    }
}
