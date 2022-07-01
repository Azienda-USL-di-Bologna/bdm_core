package it.bologna.ausl.bdm.core;

import it.bologna.ausl.bdm.utilities.Bag;
import it.bologna.ausl.bdm.core.Bdm.BdmStatus;


/**
 *
 * @author andrea
 */
public class Result {

    private BdmStatus status;
    private Bag bag;
    private String message;

    public Result() {
    }

    public Result(BdmStatus status, Bag bag, String message) {
        this.status = status;
        this.bag = bag;
        this.message = message;
    }

    public BdmStatus getStatus() {
        return status;
    }

    public void setStatus(BdmStatus status) {
        this.status = status;
    }

    public Bag getBag() {
        return bag;
    }

    public void setBag(Bag bag) {
        this.bag = bag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
