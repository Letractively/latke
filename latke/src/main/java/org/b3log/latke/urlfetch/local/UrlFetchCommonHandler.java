/*
 * Copyright (c) 2009, 2010, 2011, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.b3log.latke.urlfetch.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.b3log.latke.service.ServiceException;
import org.b3log.latke.urlfetch.HTTPHeader;
import org.b3log.latke.urlfetch.HTTPRequest;
import org.b3log.latke.urlfetch.HTTPResponse;

/**
 * 
 * @author <a href="mailto:wmainlove@gmail.com">Love Yao</a>
 * 
 *
 */
public class UrlFetchCommonHandler {

	protected HTTPResponse doFetch(HTTPRequest request) throws IOException, ServiceException {

		HttpURLConnection httpURLConnection = prepareConnection(request);
		configConnection(httpURLConnection, request);
		httpURLConnection.connect();
		HTTPResponse httpResponse = resultConnection(httpURLConnection);
		// httpURLConnection.disconnect();

		return httpResponse;
	}

	protected HttpURLConnection prepareConnection(HTTPRequest request) throws IOException, ServiceException {

		if(request.getURL()==null){
			throw new ServiceException("URL for URLFetch should not be null");
		}
		
		HttpURLConnection connection = (HttpURLConnection) request.getURL().openConnection();
		connection.setRequestMethod(request.getRequestMethod().toString());
		
		for (HTTPHeader httpHeader : request.getHeaders()) {
			// XXX set or add
			connection.setRequestProperty(httpHeader.getName(), httpHeader.getValue());
		}
		
		
		Properties prop = System.getProperties();
		prop.setProperty("http.proxyHost", "10.1.2.188");
		prop.setProperty("http.proxyPort", "80");
		prop.setProperty("https.proxyHost", "10.1.2.188");
		prop.setProperty("https.proxyPort", "80");

		return connection;
	}

	protected void configConnection(HttpURLConnection httpURLConnection, HTTPRequest request)
			throws IOException {

	};

	protected HTTPResponse resultConnection(HttpURLConnection httpURLConnection) throws IOException {

		HTTPResponse httpResponse = new HTTPResponse();

		httpResponse.setResponseCode(httpURLConnection.getResponseCode());
		// httpResponse.setFinalURL(httpURLConnection.getURL());
		httpResponse.setContent(InputStreamToByte(httpURLConnection.getInputStream()));

		fillHttpResponseHeader(httpResponse, httpURLConnection.getHeaderFields());

		return httpResponse;
	}

	protected void fillHttpResponseHeader(HTTPResponse httpResponse,
			Map<String, List<String>> headerFields) {

		for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
			httpResponse.addHeader(new HTTPHeader(entry.getKey(), entry.getValue().toString()));
		}

	}

	// XXX need to move to 'util'
	private byte[] InputStreamToByte(InputStream is) throws IOException {
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
		int ch;
		while ((ch = is.read()) != -1) {
			bytestream.write(ch);
		}
		byte imgdata[] = bytestream.toByteArray();
		bytestream.close();
		return imgdata;
	}

}
