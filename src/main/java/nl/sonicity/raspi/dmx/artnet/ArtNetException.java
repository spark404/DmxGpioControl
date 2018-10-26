package nl.sonicity.raspi.dmx.artnet;

public class ArtNetException extends Exception {
    public ArtNetException() {
        super();
    }

    public ArtNetException(String message) {
        super(message);
    }

    public ArtNetException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtNetException(Throwable cause) {
        super(cause);
    }

    protected ArtNetException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
