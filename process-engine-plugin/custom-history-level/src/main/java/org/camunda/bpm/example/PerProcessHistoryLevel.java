/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

public class PerProcessHistoryLevel implements HistoryLevel {

  protected Map<String, HistoryLevel> historyLevels = new HashMap<String, HistoryLevel>();
  protected Map<String, HistoryLevel> delegateHistoryLevelPerProcess = new HashMap<String, HistoryLevel>();

  public PerProcessHistoryLevel() {
    historyLevels.put(HISTORY_LEVEL_NONE.getName(), HISTORY_LEVEL_NONE);
    historyLevels.put(HISTORY_LEVEL_ACTIVITY.getName(), HISTORY_LEVEL_ACTIVITY);
    historyLevels.put(HISTORY_LEVEL_AUDIT.getName(), HISTORY_LEVEL_AUDIT);
    historyLevels.put(HISTORY_LEVEL_FULL.getName(), HISTORY_LEVEL_FULL);
  }

  public void addHistoryLevels(List<HistoryLevel> historyLevels) {
    for (HistoryLevel historyLevel : historyLevels) {
      this.historyLevels.put(historyLevel.getName(), historyLevel);
    }
  }

  public int getId() {
    return 12;
  }

  public String getName() {
    return "per-process";
  }

  public boolean isHistoryEventProduced(HistoryEventType historyEventType, Object entity) {
    if (entity == null) {
      return true;
    }
    else if (historyEventType == HistoryEventTypes.PROCESS_INSTANCE_START) {
      setDelegateHistoryLevel((ExecutionEntity) entity);
    }

    return isDelegateHistoryLevelEventProduced(historyEventType, entity);
  }

  protected void setDelegateHistoryLevel(ExecutionEntity execution) {
    Process process = (Process) execution.getBpmnModelInstance().getDefinitions().getUniqueChildElementByType(Process.class);
    ExtensionElements extensionElements = process.getExtensionElements();
    if (extensionElements != null) {
      CamundaProperties properties = (CamundaProperties) extensionElements.getUniqueChildElementByType(CamundaProperties.class);
      for (CamundaProperty camundaProperty : properties.getCamundaProperties()) {
        if (camundaProperty.getCamundaName().equals("history")) {
          String historyLevelName = camundaProperty.getCamundaValue();
          HistoryLevel historyLevel = historyLevels.get(historyLevelName);
          delegateHistoryLevelPerProcess.put(execution.getProcessInstanceId(), historyLevel);
        }
      }
    }
  }

  protected boolean isDelegateHistoryLevelEventProduced(HistoryEventType historyEventType, Object entity) {
    String processInstanceId = null;
    if (entity instanceof ExecutionEntity) {
      processInstanceId = ((ExecutionEntity) entity).getProcessInstanceId();
    }
    else if (entity instanceof VariableInstance) {
      processInstanceId = ((VariableInstance) entity).getProcessInstanceId();
    }
    HistoryLevel delegateHistoryLevel = delegateHistoryLevelPerProcess.get(processInstanceId);
    return delegateHistoryLevel != null && delegateHistoryLevel.isHistoryEventProduced(historyEventType, entity);
  }

}
