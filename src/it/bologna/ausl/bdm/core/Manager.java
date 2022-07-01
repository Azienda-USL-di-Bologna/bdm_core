package it.bologna.ausl.bdm.core;

import it.bologna.ausl.bdm.utilities.Dumpable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author gdm
 */
public class Manager {
    
    private Bdm.BdmStatus executeProcess(String processId) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("c:/job.json");
        BdmProcess process = Dumpable.loadFromInputStream(fis, BdmProcess.class);
        return null;
        
    }
}
