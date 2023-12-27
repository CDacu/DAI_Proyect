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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPRequest {

    private final HTTPRequestMethod method;
    private final String resourceChain;
    private final String resourceName;
    private final String httpVersion;
    private final Map<String, String> headerParameters;
    private String[] resourcePath;
    private Map<String, String> resourceParameters;
    private String content;

    public HTTPRequest(Reader reader) throws IOException, HTTPParseException {

        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = "";

        // Method resourceChain HttpVersion
        try {
            line = bufferedReader.readLine();
            String[] lineSplit = line.split(" ");

            this.method = HTTPRequestMethod.valueOf(lineSplit[0]);

            this.resourceChain = lineSplit[1];

            String[] resourceSplit = lineSplit[1].split("\\?");
            this.resourceName = resourceSplit[0].replaceFirst("/", "");

            if ((this.resourcePath = resourceName.split("/")).length == 1 && this.resourcePath[0].isBlank()) {
                this.resourcePath = new String[0];
            }

            resourceParameters = new LinkedHashMap<>();
            if (resourceSplit.length > 1) {
                String[] resourceSplitParameters = resourceSplit[1].split("&");
                String[] temporal;

                for (String resource : resourceSplitParameters) {
                    temporal = resource.split("=");
                    resourceParameters.put(temporal[0], temporal[1]);
                }
            }

            this.httpVersion = lineSplit[2];

        } catch (IOException e) {
            throw new IOException("An error ocurred while reading the HTTPResquest Header");

        } catch (Exception e) {
            throw new HTTPParseException("An error ocurred while parsing the HTTPResquest Header");
        }

        //Header parameters
        headerParameters = new LinkedHashMap<>();
        try {
            while (bufferedReader.ready() && !(line = bufferedReader.readLine()).isEmpty()) {
                String[] temporal = line.split(": ");
                this.headerParameters.put(temporal[0], temporal[1]);
            }
        } catch (IOException e) {
            throw new IOException("An error ocurred while reading the HTTPResquest Header Parameters");

        } catch (Exception e) {
            throw new HTTPParseException("An error ocurred while parsing the HTTPResquest Header Parameters");
        }

        //Content + PostEncode
        if (headerParameters.containsKey(HTTPHeaders.CONTENT_LENGTH.getHeader())
                && headerParameters.get(HTTPHeaders.CONTENT_TYPE.getHeader()) != null) {
            try {
                char[] readBuffer = new char[this.getContentLength()];
                bufferedReader.read(readBuffer, 0, readBuffer.length);

                this.content = String.valueOf(readBuffer);

                if (this.method.equals(HTTPRequestMethod.POST)) {
                    this.resourceParameters = new LinkedHashMap<>();
                    String[] contentSplit = this.content.split("&");
                    String[] temporal;
                    for (String contentLine : contentSplit) {
                        temporal = contentLine.split("=");
                        this.resourceParameters.put(temporal[0], URLDecoder.decode(temporal[1], StandardCharsets.UTF_8));
                    }

                    if (headerParameters.get(HTTPHeaders.CONTENT_TYPE.getHeader()).equals(MIME.FORM.getMime())) {
                        this.content = URLDecoder.decode(this.content, StandardCharsets.UTF_8);
                    }
                }
            } catch (IOException e) {
                throw new IOException("An error ocurred while reading the HTTPResquest Content");

            } catch (Exception e) {
                throw new HTTPParseException("An error ocurred while parsing the HTTPResquest Content");
            }
        }
    }

    public HTTPRequestMethod getMethod() {
        return method;
    }

    public String getResourceChain() {
        return resourceChain;
    }

    public String[] getResourcePath() {
        return resourcePath;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Map<String, String> getResourceParameters() {
        return resourceParameters;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaderParameters() {
        return headerParameters;
    }

    public String getContent() {
        return content;
    }

    public int getContentLength() {
        if (headerParameters.containsKey(HTTPHeaders.CONTENT_LENGTH.getHeader())) {
            return Integer.parseInt(headerParameters.get(HTTPHeaders.CONTENT_LENGTH.getHeader()));
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append(this.getMethod().name()).append(' ')
                .append(this.getResourceChain()).append(' ').append(this.getHttpVersion()).append("\r\n");

        for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
            sb.append(param.getKey()).append(": ").append(param.getValue()).append("\r\n");
        }

        if (this.getContentLength() > 0) {
            sb.append("\r\n").append(this.getContent());
        }

        return sb.toString();
    }
}
