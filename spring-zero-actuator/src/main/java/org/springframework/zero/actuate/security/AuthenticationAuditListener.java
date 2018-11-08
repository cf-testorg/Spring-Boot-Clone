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

package org.springframework.zero.actuate.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.web.authentication.switchuser.AuthenticationSwitchUserEvent;
import org.springframework.zero.actuate.audit.AuditEvent;
import org.springframework.zero.actuate.audit.listener.AuditApplicationEvent;

/**
 * {@link ApplicationListener} expose Spring Security {@link AbstractAuthenticationEvent
 * authentication events} as {@link AuditEvent}s.
 * 
 * @author Dave Syer
 */
public class AuthenticationAuditListener implements
		ApplicationListener<AbstractAuthenticationEvent>, ApplicationEventPublisherAware {

	private ApplicationEventPublisher publisher;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		if (event instanceof AbstractAuthenticationFailureEvent) {
			onAuthenticationFailureEvent((AbstractAuthenticationFailureEvent) event);
		} else if (event instanceof AuthenticationSwitchUserEvent) {
			onAuthenticationSwitchUserEvent((AuthenticationSwitchUserEvent) event);
		} else {
			onAuthenticationEvent(event);
		}
	}

	private void onAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("type", event.getException().getClass().getName());
		data.put("message", event.getException().getMessage());
		publish(new AuditEvent(event.getAuthentication().getName(),
				"AUTHENTICATION_FAILURE", data));
	}

	private void onAuthenticationSwitchUserEvent(AuthenticationSwitchUserEvent event) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (event.getAuthentication().getDetails() != null) {
			data.put("details", event.getAuthentication().getDetails());
		}
		data.put("target", event.getTargetUser().getUsername());
		publish(new AuditEvent(event.getAuthentication().getName(),
				"AUTHENTICATION_SWITCH", data));
	}

	private void onAuthenticationEvent(AbstractAuthenticationEvent event) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (event.getAuthentication().getDetails() != null) {
			data.put("details", event.getAuthentication().getDetails());
		}
		publish(new AuditEvent(event.getAuthentication().getName(),
				"AUTHENTICATION_SUCCESS", data));
	}

	private void publish(AuditEvent event) {
		if (this.publisher != null) {
			this.publisher.publishEvent(new AuditApplicationEvent(event));
		}
	}

}
