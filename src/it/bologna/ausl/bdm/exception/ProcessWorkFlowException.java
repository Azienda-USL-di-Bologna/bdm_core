package it.bologna.ausl.bdm.exception;

/**
 *
 * @author gdm
 */
public class ProcessWorkFlowException extends BdmExeption {

    public ProcessWorkFlowException(String message) {
        super(message);
    }

    public ProcessWorkFlowException(Throwable cause) {
        super(cause);
    }

    public ProcessWorkFlowException(String message, Throwable cause) {
        super(message, cause);
    }
}
