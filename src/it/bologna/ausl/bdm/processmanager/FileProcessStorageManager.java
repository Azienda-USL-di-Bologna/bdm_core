package it.bologna.ausl.bdm.processmanager;

import it.bologna.ausl.bdm.utilities.Bag;
import it.bologna.ausl.bdm.core.BdmProcess;
import it.bologna.ausl.bdm.utilities.Dumpable;
import it.bologna.ausl.bdm.exception.StorageException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrea
 */
public class FileProcessStorageManager implements ProcessStorageManager {

    private final String root;
    Logger log = LogManager.getLogger(FileProcessStorageManager.class);

    public FileProcessStorageManager(String root) {
        this.root = root;
        File r = new File(root);
        if (!r.exists()) {
            r.mkdirs();
        }
    }

    @Override
    public BdmProcess loadProcess(String id) throws StorageException {
        try (FileInputStream in = new FileInputStream(new File(root, id))) {
            return (BdmProcess) Dumpable.loadFromInputStream(in, BdmProcess.class);

        } catch (IOException ex) {
            log.error(ex);
            throw new StorageException("Error loading Process", ex);

        } catch (ClassNotFoundException ex) {
            log.error(ex);
            return null;
        }

    }

    @Override
    public void saveProcess(BdmProcess p) throws StorageException {
        try (FileInputStream in = new FileInputStream(new File(root, p.getProcessId()))) {
                BdmProcess savedProcess = Dumpable.loadFromInputStream(in, BdmProcess.class);
                if (savedProcess.getTransactionId() == null || p.getTransactionId() == null || !savedProcess.getTransactionId().equals(p.getTransactionId())) {
                    throw new ConcurrentModificationException("transactionId doesn't match");
                }
                p.setTransactionId(p.getTransactionId() + 1);
        }
        catch (IOException ex) {
            // se da questa eccezione il processo è nuovo e va creato
        }
        catch (ConcurrentModificationException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new StorageException("Error reading Process transactionId", ex);
        }
        
        try (FileOutputStream out = new FileOutputStream(new File(root, p.getProcessId()))) {
            
            try (FileInputStream in = new FileInputStream(new File(root, p.getProcessId()))) {
                BdmProcess savedProcess = Dumpable.loadFromInputStream(in, BdmProcess.class);
                if (savedProcess.getTransactionId() == null || p.getTransactionId() == null || !savedProcess.getTransactionId().equals(p.getTransactionId())) {
                    throw new ConcurrentModificationException("transactionId doesn't match");
                }
                p.setTransactionId(p.getTransactionId() + 1);
            }
            catch (IOException ex) {
                // se da questa eccezione il processo è nuovo e va creato
            }
            p.dumpToOutputStream(out);
        }
        catch (Exception ex) {
            log.error(ex);
            throw new StorageException("Error saving Process", ex);
        }
    }

    @Override
    public void deleteProcess(String id) throws StorageException {

        try {
            Files.delete(Paths.get(root, id));
        } catch (IOException ex) {
            log.error(ex);
            throw new StorageException("Error deleting file : " + Paths.get(root, id).toString(), ex);

        }

    }

    @Override
    public List<String> getProcessList() throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getProcessList(Bag queryParams) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
