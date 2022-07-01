package it.bologna.ausl.bdm.exception;

/**
 *
 * @author gdm
 */
public class BdmExeption extends Exception {

    public BdmExeption(String message) {
        super(message);
    }

    public BdmExeption(Throwable cause) {
        super(cause);
    }

    public BdmExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
