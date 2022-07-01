package it.bologna.ausl.bdm.processmanager;

import it.bologna.ausl.bdm.utilities.Bag;
import it.bologna.ausl.bdm.core.BdmProcess;
import it.bologna.ausl.bdm.exception.StorageException;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 *
 * @author andrea
 */
public interface ProcessStorageManager {

    public BdmProcess loadProcess(String id) throws StorageException;

    public void saveProcess(BdmProcess p) throws StorageException;

    public void deleteProcess(String id) throws StorageException;

    public List<String> getProcessList() throws StorageException;

    public List<String> getProcessList(Bag queryParams) throws StorageException;

}
