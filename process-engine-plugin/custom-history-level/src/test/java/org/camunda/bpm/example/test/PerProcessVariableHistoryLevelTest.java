package org.camunda.bpm.example.test;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test Bpmn Parse listener as process engine plugin and
 * parse extension properties on bpmn element
 *
 * @author kristin.polenz
 *
 */
public class PerProcessVariableHistoryLevelTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-per-process.cfg.xml");

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void getEngineServices() {
    runtimeService = processEngineRule.getRuntimeService();
    historyService = processEngineRule.getHistoryService();
  }

  @Test
  @Deployment(resources = { "process-history-none.bpmn" })
  public void testProcessHistoryNone() {
    runtimeService.startProcessInstanceByKey("process-history-none");

    // assert that no history was written
    assertEquals(0, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(0, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "process-history-activity.bpmn" })
  public void testProcessHistoryActivity() {
    runtimeService.startProcessInstanceByKey("process-history-activity");

    // assert that only activity history was written
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(3, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(0, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "process-history-full.bpmn" })
  public void testProcessHistoryFull() {
    runtimeService.startProcessInstanceByKey("process-history-full");

    // assert that full history was written
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(3, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(4, historyService.createHistoricVariableInstanceQuery().count());
  }

  @Test
  @Deployment(resources = { "process-history-custom-variable.bpmn" })
  public void testProcessHistoryCustomVariable() {
    runtimeService.startProcessInstanceByKey("process-history-custom-variable");

    // assert that full history was written
    assertEquals(1, historyService.createHistoricProcessInstanceQuery().count());
    assertEquals(3, historyService.createHistoricActivityInstanceQuery().count());
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());
  }

}
