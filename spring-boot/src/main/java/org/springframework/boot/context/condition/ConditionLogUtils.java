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
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.MethodMetadata;

/**
 * General utilities for constructing {@code @Conditional} log messages.
 * 
 * @author Dave Syer
 */
public abstract class ConditionLogUtils {

	public static String getPrefix(Log logger, AnnotatedTypeMetadata metadata) {
		String prefix = "";
		if (logger.isDebugEnabled()) {
			prefix = metadata instanceof ClassMetadata ? "Processing "
					+ ((ClassMetadata) metadata).getClassName() + ". "
					: (metadata instanceof MethodMetadata ? "Processing "
							+ getMethodName((MethodMetadata) metadata) + ". " : "");
		}
		return prefix;
	}

	private static String getMethodName(MethodMetadata metadata) {
		return metadata.getDeclaringClassName() + "#" + metadata.getMethodName();
	}

}
