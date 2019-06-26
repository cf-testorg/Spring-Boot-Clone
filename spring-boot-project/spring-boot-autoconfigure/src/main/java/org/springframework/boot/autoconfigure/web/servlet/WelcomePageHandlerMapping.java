/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.springframework.boot.autoconfigure.web.servlet;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

/**
 * An {@link AbstractUrlHandlerMapping} for an application's welcome page. Supports both
 * static and templated files. If both a static and templated index page is available, the
 * static page is preferred.
 *
 * @author Andy Wilkinson
 */
final class WelcomePageHandlerMapping extends AbstractUrlHandlerMapping {

	private static final Log logger = LogFactory.getLog(WelcomePageHandlerMapping.class);

	WelcomePageHandlerMapping(Optional<Resource> welcomePage, String staticPathPattern) {
		if (welcomePage.isPresent() && "/**".equals(staticPathPattern)) {
			logger.info("Adding welcome page: " + welcomePage.get());
			ParameterizableViewController controller = new ParameterizableViewController();
			controller.setViewName("forward:index.html");
			setRootHandler(controller);
			setOrder(0);
		}
	}

	@Override
	public Object getHandlerInternal(HttpServletRequest request) throws Exception {
		for (MediaType mediaType : getAcceptedMediaTypes(request)) {
			if (mediaType.includes(MediaType.TEXT_HTML)) {
				return super.getHandlerInternal(request);
			}
		}
		return null;
	}

	private List<MediaType> getAcceptedMediaTypes(HttpServletRequest request) {
		String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
		return MediaType.parseMediaTypes(
				StringUtils.hasText(acceptHeader) ? acceptHeader : "*/*");
	}

}
