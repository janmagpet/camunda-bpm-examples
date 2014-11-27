/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.cycle.example;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;

/**
 * An example connector implementation which persists BPMN files to a
 * simple memory based map.
 *
 */
public class ExampleConnector extends Connector {

  protected ExampleConnectorNode rootNode;
  protected ExampleConnectorNode folder;

  protected Map<String, ExampleConnectorNode> nodes = new HashMap<String, ExampleConnectorNode>();

  public void init(ConnectorConfiguration config) {
    super.init(config);
    rootNode = new ExampleConnectorNode("/", getId(), ConnectorNodeType.FOLDER);
    folder = new ExampleConnectorNode("/aFolder", getId(), ConnectorNodeType.FOLDER);
  }

  public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message) {
    ExampleConnectorNode newNode = new ExampleConnectorNode(label, getId());
    System.out.println("created new node with id "+label);
    nodes.put(label, newNode);
    return newNode;
  }

  public void deleteNode(ConnectorNode node, String arg1) {
    nodes.remove(node.getLabel());
  }

  public List<ConnectorNode> getChildren(ConnectorNode arg0) {
    if(arg0.getId().equals(folder.getId())) {
      return new ArrayList<ConnectorNode>(nodes.values());
    }
    else if(arg0.getId().equals(rootNode.getId())) {
      return Collections.<ConnectorNode>singletonList(folder);
    }
    else {
      return Collections.emptyList();
    }
  }

  public InputStream getContent(ConnectorNode arg0) {
    ExampleConnectorNode exampleConnectorNode = nodes.get(arg0.getId());
    ByteArrayInputStream inputStream = null;
    if(exampleConnectorNode == null) {
      inputStream = new ByteArrayInputStream(new byte[0]);
    }
    else {
      byte[] content = exampleConnectorNode.getContent();
      if(content == null) {
        content = new byte[0];
      }
      inputStream = new ByteArrayInputStream(content);
    }
    return inputStream;
  }

  public ContentInformation getContentInformation(ConnectorNode arg0) {
    ExampleConnectorNode node = nodes.get(arg0.getId());
    if(node == null) {
      return ContentInformation.notFound();
    }
    else {
      return new ContentInformation(true, node.getLastModified());
    }
  }

  public ConnectorNode getNode(String arg0) {
    return nodes.get(arg0);
  }

  public ConnectorNode getRoot() {
    return rootNode;
  }

  public boolean isSupportsCommitMessage() {
    return false;
  }

  public boolean needsLogin() {
    return false;
  }

  public ContentInformation updateContent(ConnectorNode arg0, InputStream arg1, String arg2) throws Exception {
    ExampleConnectorNode exampleConnectorNode = nodes.get(arg0.getId());
    if(exampleConnectorNode == null) {
      throw new RuntimeException("Node with id "+arg0.getId()+" not found.");
    }
    else {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      while((bytesRead = arg1.read(buffer, 0, buffer.length))> 0) {
        byteArrayOutputStream.write(buffer, 0, bytesRead);
      }
      exampleConnectorNode.setContent(byteArrayOutputStream.toByteArray());
      return getContentInformation(exampleConnectorNode);
    }
  }

}
