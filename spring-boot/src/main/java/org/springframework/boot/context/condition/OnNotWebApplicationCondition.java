/*
 * Copyright 2012-2013 the original author or authors.
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

package org.springframework.boot.context.condition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link Condition} that checks for a web application context and returns false if one is
 * found.
 * 
 * @author Dave Syer
 * @see ConditionalOnNotWebApplication
 */
class OnNotWebApplicationCondition implements Condition {

	private static Log logger = LogFactory.getLog(OnNotWebApplicationCondition.class);

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

		String checking = ConditionLogUtils.getPrefix(logger, metadata);

		if (!ClassUtils.isPresent(
				"org.springframework.web.context.support.GenericWebApplicationContext",
				null)) {
			if (logger.isDebugEnabled()) {
				logger.debug(checking + "Web application classes not found");
			}
			return true;
		}
		boolean result = !StringUtils.arrayToCommaDelimitedString(
				context.getBeanFactory().getRegisteredScopeNames()).contains("session");
		if (logger.isDebugEnabled()) {
			logger.debug(checking + "Web application context found: " + !result);
		}

		return result;

	}

}
