package it.bologna.ausl.bdm.processmanager;

import it.bologna.ausl.bdm.utilities.Bag;
import it.bologna.ausl.bdm.core.BdmProcess;
import it.bologna.ausl.bdm.utilities.Dumpable;
import it.bologna.ausl.bdm.exception.StorageException;
import it.bologna.ausl.bdm.utilities.DbConnectionPoolManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

/**
 *
 * @author andrea
 */
public class DbProcessStorageManager implements ProcessStorageManager {

    Logger log = LogManager.getLogger(DbProcessStorageManager.class);
    private DataSource ds = null;

    public DbProcessStorageManager(String dbUri) throws StorageException {
        try {
            ds = DbConnectionPoolManager.getConnection(dbUri);
        }
        catch (Exception ex) {
            throw new StorageException(ex);
        }
    }

    @Override
    public BdmProcess loadProcess(String id) throws StorageException {
        String q = "select id,json_process from bdm_process where id=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(q);) {
            ps.setString(1, id);
            String jsonProcess;
            try (ResultSet rs = ps.executeQuery()) {
                jsonProcess = null;
                while (rs.next()) {
                    jsonProcess = rs.getString("json_process");
                }
            }
            if (jsonProcess == null) {
                throw new StorageException("Process " + id + " not found");

            }
            return Dumpable.load(jsonProcess, BdmProcess.class);
        } catch (Exception ex) {
            log.error("Unable to load process", ex);
            throw new StorageException("Unable to load process", ex);
        }
    }

    @Override
    public void saveProcess(BdmProcess p) throws StorageException {
        String getTransactionIdQuery = "select (json_process->>?)::int from bdm.bdm_process where id = ? for update";
        String insertProcessQuery = "insert into bdm_process (id,json_process,status) values (?,?::jsonb,?)";
        String updateProcessQuery = "update bdm_process set json_process=?::jsonb, status=? where id = ?";
        
        try (Connection c = ds.getConnection();
                PreparedStatement psg = c.prepareStatement(getTransactionIdQuery); 
                PreparedStatement psi = c.prepareStatement(insertProcessQuery);
                PreparedStatement psu = c.prepareStatement(updateProcessQuery);
            ) {
            try { // questo try serve per fare il rollback della connessione in caso di errore
                c.setAutoCommit(false);

                String idProcess = p.getProcessId();
                String statusProcess = p.getStatus().toString();
                
                psg.setString(1, BdmProcess.TRANSACTION_ID_FIELD);
                psg.setString(2, idProcess);
                ResultSet res = psg.executeQuery();
                if (res != null && res.next()) {
                    int savedTransactionId = res.getInt(1);
                    if (res.wasNull() || p.getTransactionId() == null || savedTransactionId != p.getTransactionId())
                        throw new ConcurrentModificationException("transactionId doesn't match");
                    else {
                        //update
                        p.setTransactionId(++savedTransactionId);
                        psu.setString(1, p.dump());
                        psu.setString(2, statusProcess);
                        psu.setString(3, idProcess);
                        int updatedRows = psu.executeUpdate();
                        if (updatedRows != 1) 
                            throw new StorageException("process to update not found");
                    }
                }
                else {
                    //insert
                    psi.setString(1, idProcess);
                    psi.setString(2, p.dump());
                    psi.setString(3, statusProcess);
                    int updatedRows = psi.executeUpdate();
                    if (updatedRows != 1) 
                            throw new StorageException("no rows inserted");
                }
                c.commit();
            }
            catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            log.error("Error saving process", ex);
            throw new StorageException("Error saving process", ex);

        }
    }

    @Override
    public void deleteProcess(String id) throws StorageException {

        String q = "delete from bdm_process where id = ?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(q)) {

            ps.setString(1, id);

            ps.executeUpdate();

        } catch (Exception ex) {
            log.error("Error saving process", ex);
            throw new StorageException("Error saving process", ex);

        }
    }

    @Override
    public List<String> getProcessList() throws StorageException {
        String q = "select id from bdm_process order by 1";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(q);) {

            ArrayList<String> res = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    res.add(rs.getString("id"));
                }
            }

            return res;
        } catch (Exception ex) {
            log.error("Unable to list porcesses", ex);
            throw new StorageException("Unable to list porcesses", ex);
        }
    }

    @Override
    public List<String> getProcessList(Bag queryParams) throws StorageException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
