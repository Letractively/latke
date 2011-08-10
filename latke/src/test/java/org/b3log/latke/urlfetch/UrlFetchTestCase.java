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
package org.b3log.latke.urlfetch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.urlfetch.local.LocalURLFetchService;

public class UrlFetchTestCase {

	public void testGetFetch() throws IOException {

		HTTPRequest request = new HTTPRequest();
		request.setRequestMethod(HTTPRequestMethod.GET);
		request.setURL(new URL("http://www.baidu.com"));

		LocalURLFetchService fetchService = new LocalURLFetchService();
		HTTPResponse httpResponse = fetchService.fetch(request);

		printHttpREsponse(httpResponse);
	}

	public void testPostFetch() throws IOException {

		HTTPRequest request = new HTTPRequest();
		request.setRequestMethod(HTTPRequestMethod.POST);
		request.setURL(new URL("https://passport.baidu.com/api/?login"));

	 
		String content =URLEncoder.encode("username=yaoliceng&password=09101112", "UTF-8");
		request.setPayload(content.getBytes());

		LocalURLFetchService fetchService = new LocalURLFetchService();
		HTTPResponse httpResponse = fetchService.fetch(request);

		printHttpREsponse(httpResponse);
	}

	private void printHttpREsponse(HTTPResponse httpResponse) throws IOException {

		System.out.println("responseCode == " + httpResponse.getResponseCode());
		;
		System.out.println("finalUrl == " + httpResponse.getFinalURL());

		for (HTTPHeader httpHeader : httpResponse.getHeaders()) {
			System.out.println(httpHeader.getName() + " == " + httpHeader.getValue());
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
				httpResponse.getContent())));

		String lines;
		while ((lines = reader.readLine()) != null) {
			System.out.println(lines);
		}

	}

	public static void main(String[] args) throws IOException {

		UrlFetchTestCase fetchTestCase = new UrlFetchTestCase();
		//		fetchTestCase.testGetFetch();
		fetchTestCase.testPostFetch();
	}

}
