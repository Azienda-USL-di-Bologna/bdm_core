package it.bologna.ausl.bdm.processmanager;

import it.bologna.ausl.bdm.core.Bdm;
import it.bologna.ausl.bdm.core.BdmProcess;
import it.bologna.ausl.bdm.core.Step;
import it.bologna.ausl.bdm.core.Task;
import it.bologna.ausl.bdm.exception.BdmExeption;
import it.bologna.ausl.bdm.exception.IllegalStepStateException;
import it.bologna.ausl.bdm.exception.ProcessWorkFlowException;
import it.bologna.ausl.bdm.exception.StorageException;
import it.bologna.ausl.bdm.utilities.Bag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrea
 */
public class BdmProcessManager {

    Logger log = LogManager.getLogger(BdmProcessManager.class);

    public static final String ADDING_PROCESS_TYPE = "process_type";
    public static final String ADDING_PROCESS_PARAMS = "process_params";
    private final ProcessStorageManager psm;

    public BdmProcessManager(ProcessStorageManager psm) {
        this.psm = psm;
    }

    public String addProcess(BdmProcess p) {
        try {
            psm.saveProcess(p);
            return p.getProcessId();
        } catch (StorageException ex) {
            log.error(ex);
        }
        return null;
    }

    public BdmProcess addProcess(Bag parameters) throws BdmExeption {
        try {
            String processType = (String) parameters.get(ADDING_PROCESS_TYPE);
            Bag processParameters = (Bag) parameters.get(ADDING_PROCESS_PARAMS);

            BdmProcess p = (BdmProcess) Class.forName("it.bologna.ausl.bdm.workflows.processes." + processType).newInstance();
            p.init(processParameters);
            addProcess(p);
            return p;
        } catch (Exception ex) {
            throw new BdmExeption("unable to instantiate " + (String) parameters.get(ADDING_PROCESS_TYPE), ex);
        }

    }

    public BdmProcess getProcess(String id) {
        try {
            return psm.loadProcess(id);
        } catch (StorageException ex) {
            log.error(ex);
            return null;
        }
    }

    public Boolean abortProcess(String id) throws StorageException {
        Objects.requireNonNull(id);
        BdmProcess process;
        try {
            process = psm.loadProcess(id);
        } catch (StorageException ex) {
            log.error(ex);
            return false;
        }
        process.setStatus(Bdm.BdmStatus.ABORTED);
        try {
            psm.saveProcess(process);
        } catch (StorageException ex) {
            log.error(ex);
            return false;
        }
        return true;
    }
    
    public Boolean deleteProcess(String id) {

        try {
            psm.deleteProcess(id);
        } catch (StorageException ex) {
            log.error(ex);
            return false;
        }
        return true;
    }

    public Bdm.BdmStatus stepOnProcess(String id, Bag parameters) throws IllegalStepStateException, ProcessWorkFlowException, StorageException {
        BdmProcess p;
        try {
            p = psm.loadProcess(id);
        } catch (StorageException ex) {
            log.error(ex);
            return null;
        }
        Bdm.BdmStatus status = p.stepOn(parameters);
        psm.saveProcess(p);
        return status;
    }

    public String stepToStep(String processId, String stepId, Bag parameters) throws IllegalStepStateException, ProcessWorkFlowException, StorageException {
        BdmProcess p;
        try {
            p = psm.loadProcess(processId);
        } catch (StorageException ex) {
            log.error(ex);
            return null;
        }
        Bdm.BdmStatus status = p.stepTo(stepId, parameters);
        psm.saveProcess(p);
        return status.toString();

    }

    public String addTask(String taskType, Bag taskParameters, String processId, String stepId) throws StorageException, BdmExeption {
        Objects.requireNonNull(taskType);
        Objects.requireNonNull(processId);
        Objects.requireNonNull(stepId);

        BdmProcess p = psm.loadProcess(processId);
        Step s = p.getStep(stepId);
        try {
            Task t = (Task) Class.forName("it.bologna.ausl.bdm.workflows.tasks." + taskType).newInstance();
            t.init(taskParameters);
            s.addTask(t);
            psm.saveProcess(p);
            return t.getTaskId();
        } catch (Exception ex) {
            throw new BdmExeption("Unable to instatiate task", ex);
        }
    }
    
    public void setContext(String processId, Bag context) throws StorageException, BdmExeption {
        Objects.requireNonNull(processId);

        BdmProcess p = psm.loadProcess(processId);
        p.setContext(context);
        psm.saveProcess(p);
    }
    
    public void addInContext(String processId, Bag values) throws StorageException {
        Objects.requireNonNull(processId);
        Objects.requireNonNull(values);

        BdmProcess p = psm.loadProcess(processId);
        Bag currentContext = p.getContext();
        
        Map<String, Object> parameters = values.getParameters();
        Set<String> keys = parameters.keySet();
        for (String key: keys) {
            currentContext.put(key, values.get(key));
        }
        p.setContext(currentContext);
        psm.saveProcess(p);
    }
    
    public void setStepLogic(String processId, String stepId, Step.StepLogic stepLogic) throws StorageException {
        Objects.requireNonNull(processId);
        Objects.requireNonNull(stepId);
        Objects.requireNonNull(stepLogic);

        BdmProcess process = psm.loadProcess(processId);
        Step step = process.getStep(stepId);
        step.setStepLogic(stepLogic);
        psm.saveProcess(process);
    }

    public void removeTask(String taskId, String processId, String stepId) throws StorageException, BdmExeption {
        Objects.nonNull(taskId);
        Objects.nonNull(processId);
        Objects.nonNull(stepId);

        BdmProcess p = psm.loadProcess(processId);
        Step s = null;
        if (stepId != null) {
            s = p.getStep(stepId);
        }
        List<Step> stepList;
        if (s != null) {
            stepList = Arrays.asList(s);

        } else {
            stepList = p.getStepList();
        }
        boolean found = false;
        for (Step stmp : stepList) {
            for (Task t : stmp.getTaskList()) {
                if (t.getTaskId().equals(taskId)) {
                    stmp.getTaskList().remove(t);
                    found = true;
                    break;
                }
                if (found) {
                    break;
                }
            }

        }
        psm.saveProcess(p);
    }

    public String addStep(String processId, String stepDescription, Step.StepLogic stepLogic, String stepType, List<Step.StepLogic> allowedStepLogic) throws StorageException, BdmExeption {
        Objects.nonNull(stepType);
        Objects.nonNull(processId);
        Objects.nonNull(stepLogic);
        Objects.nonNull(stepType);
        Objects.nonNull(stepDescription);
        BdmProcess p = psm.loadProcess(processId);
        Step s = new Step(stepType, stepDescription, stepLogic, allowedStepLogic);
        p.getStepList().add(s);
        psm.saveProcess(p);
        return s.getStepId();
    }

    public void removeStep(String stepId, String processId) throws StorageException {
        Objects.nonNull(stepId);
        Objects.nonNull(processId);

        BdmProcess p = psm.loadProcess(processId);
        p.getStepList().remove(p.getStep(stepId));
        psm.saveProcess(p);
    }

    public List<String> getProcessIdList() throws StorageException {
        return psm.getProcessList();

    }

}
