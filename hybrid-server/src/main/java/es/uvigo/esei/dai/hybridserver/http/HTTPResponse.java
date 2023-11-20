/**
 *  HybridServer
 *  Copyright (C) 2023 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.uvigo.esei.dai.hybridserver.http;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {

  private HTTPResponseStatus status;
  private String version;
  private String content;
  private final Map<String, String> parameters;

  public HTTPResponse() {
    this.status = null;
    this.version = null;
    this.content = null;
    this.parameters = new LinkedHashMap<>();
  }

  public HTTPResponseStatus getStatus() {
    return this.status;
  }

  public void setStatus(HTTPResponseStatus status) {
    this.status = status;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
    parameters.put("Content-Length", Integer.toString(content.length()));
  }

  public Map<String, String> getParameters() {
    return this.parameters;
  }

  public String putParameter(String name, String value) {
    return this.parameters.put(name, value);
  }

  public boolean containsParameter(String name) {
    return this.parameters.containsKey(name);
  }

  public String removeParameter(String name) {
    return this.parameters.remove(name);
  }

  public void clearParameters() {
    this.parameters.clear();
  }

  public List<String> listParameters() {
    return (List<String>) this.parameters.values();
  }

  public void print(Writer writer) throws IOException {

    writer.write(this.getVersion() + " "
        + this.getStatus().getCode() + " "
        + this.getStatus().getStatus()
        + "\r\n");

    for (String key : parameters.keySet()) {
      writer.write(key + ": " + parameters.get(key) + "\r\n");
    }

    writer.write("\r\n");

    if (this.content != null) {
      writer.write(this.content);
    }

    writer.flush();
  }

  @Override
  public String toString() {
    try (final StringWriter writer = new StringWriter()) {
      this.print(writer);
      return writer.toString();
    } catch (IOException e) {
      throw new RuntimeException("Unexpected I/O exception", e);
    }
  }
}
