package cea.fr.CybergridML.services.xiidm;

/**
 * Raised when an uploaded file cannot be read as an XIIDM network.
 */
public class XiidmParseException extends Exception {

    public XiidmParseException(String message) {
        super(message);
    }

    public XiidmParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
