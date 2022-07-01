package it.bologna.ausl.bdm.processmanager;

import it.bologna.ausl.bdm.core.BdmProcess;

/**
 *
 * @author gdm
 */
public class ProcessesUtilities {
    
    /**
     * torna la versione del processo identificato dal nome passato
     * @param processName il nome del processo
     * @return la versione del processo identificato dal nome passato
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InstantiationException
     * @throws java.lang.IllegalAccessException
     */
    public static String getProcessVersion(String processName) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        BdmProcess p = (BdmProcess) Class.forName("it.bologna.ausl.bdm.workflows.processes." + processName).newInstance();
        return p.getProcessVersion();
    }
}
