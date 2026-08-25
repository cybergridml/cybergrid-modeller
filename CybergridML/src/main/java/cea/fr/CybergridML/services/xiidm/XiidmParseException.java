package cea.fr.CybergridML.services.xiidm;

/** Thrown when XIIDM parsing fails. */
public class XiidmParseException extends Exception {

    public XiidmParseException(String message) {
        super(message);
    }

    public XiidmParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
