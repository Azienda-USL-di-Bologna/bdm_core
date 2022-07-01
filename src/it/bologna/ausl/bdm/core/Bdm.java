/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.bdm.core;

import it.bologna.ausl.bdm.utilities.Bag;
import com.fasterxml.jackson.core.JsonProcessingException;
import it.bologna.ausl.bdm.utilities.Dumpable;
import java.io.IOException;
import java.util.ArrayList;
import org.joda.time.DateTime;

/**
 *
 * @author andrea
 */
public class Bdm {

    public static enum BdmStatus {

        NOT_STARTED,
        RUNNING,
        FINISHED,
        ABORTED,
        ERROR
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws JsonProcessingException, IOException, ClassNotFoundException {
        // TODO code application logic here

        Bag p = new Bag();
        DateTime dt = DateTime.now();
        String[] ss = {"a", "b", "c"};
        p.put("ciccio", "pasticcio");
        p.put("numero", 42);
        p.put("ss", ss);
        p.put("data", dt);
        String j = p.dump();

        System.out.println(j);
        Bag p2 = Dumpable.load(j, Bag.class);
        System.out.println(p2.dump());
        System.out.println(((ArrayList<String>) p2.get("ss")).get(0));
        System.out.println(((ArrayList<String>) p2.get("ss")).get(0));

    }

}
