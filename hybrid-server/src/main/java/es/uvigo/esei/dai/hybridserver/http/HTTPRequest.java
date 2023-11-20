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
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.BufferedReader;

public class HTTPRequest {

	private HTTPRequestMethod method;
	private String resourceName;
	private String resourceChain;
	private String[] resourcePath;
	private Map<String, String> resourceParam;
	private Map<String, String> headerParam;
	private String httpVer;
	private int contLength;
	private String content;

	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {

		String reqLine = "";
		String[] resTemp = null;
		String[] reqTemp = null;
		String mthdTemp;
		String[] resParamTmp;
		String[] paramValueTmp;
		String[] headerParamTmp;
		char[] buffer = new char [4096];
		int chrRead = 0;
		boolean keepGoing = true;

		BufferedReader bufRead = new BufferedReader(reader);

		try {
			reqLine = bufRead.readLine();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		// Stripping out the method field.
		
		reqTemp = reqLine.split(" ");

		if (reqTemp.length != 3) {
			throw new HTTPParseException();
		}
		
		mthdTemp = reqTemp[0]; // First field before the blank space. => Method

		switch (mthdTemp) {

		case "HEAD":
			this.method = HTTPRequestMethod.HEAD;
			break;

		case "GET":
			this.method = HTTPRequestMethod.GET;
			break;

		case "POST":
			this.method = HTTPRequestMethod.POST;
			break;

		case "PUT":
			this.method = HTTPRequestMethod.PUT;
			break;

		case "DELETE":
			this.method = HTTPRequestMethod.DELETE;
			break;

		default:
			throw new HTTPParseException();
		}

		this.httpVer = reqTemp[2];

		// Stripping out the resource field
		this.resourceChain = reqTemp[1];
		
		resTemp = reqTemp[1].split("\\?");

		//TODO : Comprobar que el ResourceName sea html (a futuro xsl, xls, xslt (I think))
		//TODO : Comprobar validez ResourcePath
		this.resourceName = resTemp[0].replaceFirst("/", "");

		if ((this.resourcePath = resourceName.split("/")).length == 1 && this.resourcePath[0].isBlank()) {
			this.resourcePath = new String[0];
		}

		resourceParam = new LinkedHashMap<>();

		if (resTemp.length > 1) {

			// Only grab the parameters, each value of the array looks like: paramN=valueN
			resParamTmp = resTemp[1].split("&");

            for (String s : resParamTmp) {
                paramValueTmp = s.split("="); // paramValueTmp is a size=2 array, pVT[0] is the key and
                // pVT[1] is the value
                resourceParam.put(paramValueTmp[0], paramValueTmp[1]);
            }
		}

		headerParam = new LinkedHashMap<>();

		reqLine = bufRead.readLine();
		


		while ( keepGoing ) {

			headerParamTmp = reqLine.split(": ");

			if (headerParamTmp.length != 2 && !headerParamTmp[0].isBlank() && this.contLength == 0) {
				throw new HTTPParseException();
			} else {

				if (headerParamTmp.length == 2) {

					if (headerParamTmp[0].equalsIgnoreCase(HTTPHeaders.CONTENT_LENGTH.getHeader())) {

						this.contLength = Integer.parseInt(headerParamTmp[1]);
					}

					headerParam.put(headerParamTmp[0], headerParamTmp[1]);
				} else {
					
					//The only scenario where we'll get a blank its if we reach EOF or before the content
					if ( headerParamTmp[0].isBlank() ) {
						
						//If theres content, it will read it all before setting the flag to continue to false						
						if ( this.contLength > 0 ) {
							
							this.content = "";
							
							while( chrRead != this.contLength ){
								
								if( this.contLength > 4096 ) {
									chrRead += bufRead.read(buffer, chrRead, 4096);
								}else {
									chrRead += bufRead.read(buffer, chrRead, (this.contLength - chrRead));
								}
								
								reqLine = String.valueOf(buffer).trim();
								
								this.content += java.net.URLDecoder.decode(reqLine, StandardCharsets.UTF_8);
								resParamTmp = this.content.split("&");

                                for (String s : resParamTmp) {
                                    paramValueTmp = s.split("=");
                                    resourceParam.put(paramValueTmp[0], paramValueTmp[1]);
                                }
							}
						}
						
						//after reading all the content if there was any, or if there was no content it means its the end
						//so either way we'll set the flag to false
						
						keepGoing = false;
					} else {
						throw new HTTPParseException();
					}
				}
			}

			if ( keepGoing ) {
				reqLine = bufRead.readLine();
			}
		}
	}

	public HTTPRequestMethod getMethod() {
		return this.method;
	}

	public String getResourceChain() {
		return this.resourceChain;
	}

	public String[] getResourcePath() {
		return this.resourcePath;
	}

	public String getResourceName() {
		return this.resourceName;
	}

	public Map<String, String> getResourceParameters() {

		return this.resourceParam;
	}

	public String getHttpVersion() {
		return this.httpVer;
	}

	public Map<String, String> getHeaderParameters() {
		return this.headerParam;
	}

	public String getContent() {

		return this.content;
	}

	public int getContentLength() {
		return this.contLength;
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
