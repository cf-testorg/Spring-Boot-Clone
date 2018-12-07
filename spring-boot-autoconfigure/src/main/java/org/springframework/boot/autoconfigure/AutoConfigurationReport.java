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

package org.springframework.boot.autoconfigure;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.Condition;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Records auto-configuration details for reporting and logging.
 * 
 * @author Greg Turnquist
 * @author Dave Syer
 * @author Phillip Webb
 */
public class AutoConfigurationReport {

	private static final String BEAN_NAME = "autoConfigurationReport";

	private final SortedMap<String, ConditionAndOutcomes> outcomes = new TreeMap<String, ConditionAndOutcomes>();

	private AutoConfigurationReport parent;

	/**
	 * Private constructor.
	 * @see #get(ConfigurableListableBeanFactory)
	 */
	private AutoConfigurationReport() {
	}

	/**
	 * Record the occurrence of condition evaluation.
	 * @param source the source of the condition (class or method name)
	 * @param condition the condition evaluated
	 * @param outcome the condition outcome
	 */
	public void recordConditionEvaluation(String source, Condition condition,
			ConditionOutcome outcome) {
		Assert.notNull(source, "Source must not be null");
		Assert.notNull(condition, "Condition must not be null");
		Assert.notNull(outcome, "Outcome must not be null");
		if (!this.outcomes.containsKey(source)) {
			this.outcomes.put(source, new ConditionAndOutcomes());
		}
		this.outcomes.get(source).add(condition, outcome);
	}

	/**
	 * Returns condition outcomes from this report, grouped by the source.
	 */
	public Map<String, ConditionAndOutcomes> getConditionAndOutcomesBySource() {
		return Collections.unmodifiableMap(this.outcomes);
	}

	/**
	 * The parent report (from a parent BeanFactory if there is one).
	 * @return the parent report (or null if there isn't one)
	 */
	public AutoConfigurationReport getParent() {
		return this.parent;
	}

	/**
	 * Obtain a {@link AutoConfigurationReport} for the specified bean factory.
	 * @param beanFactory the bean factory
	 * @return an existing or new {@link AutoConfigurationReport}
	 */
	public static AutoConfigurationReport get(ConfigurableListableBeanFactory beanFactory) {
		synchronized (beanFactory) {
			AutoConfigurationReport report;
			if (beanFactory.containsSingleton(BEAN_NAME)) {
				report = beanFactory.getBean(BEAN_NAME, AutoConfigurationReport.class);
			}
			else {
				report = new AutoConfigurationReport();
				beanFactory.registerSingleton(BEAN_NAME, report);
			}
			locateParent(beanFactory.getParentBeanFactory(), report);
			return report;
		}
	}

	private static void locateParent(BeanFactory beanFactory,
			AutoConfigurationReport report) {
		if (beanFactory != null && report.parent == null
				&& beanFactory.containsBean(BEAN_NAME)) {
			report.parent = beanFactory.getBean(BEAN_NAME, AutoConfigurationReport.class);
		}
	}

	/**
	 * Provides access to a number of {@link ConditionAndOutcome} items.
	 */
	public static class ConditionAndOutcomes implements Iterable<ConditionAndOutcome> {

		private final Set<ConditionAndOutcome> outcomes = new LinkedHashSet<ConditionAndOutcome>();

		public void add(Condition condition, ConditionOutcome outcome) {
			this.outcomes.add(new ConditionAndOutcome(condition, outcome));
		}

		/**
		 * Return {@code true} if all outcomes match.
		 */
		public boolean isFullMatch() {
			for (ConditionAndOutcome conditionAndOutcomes : this) {
				if (!conditionAndOutcomes.getOutcome().isMatch()) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Iterator<ConditionAndOutcome> iterator() {
			return Collections.unmodifiableSet(this.outcomes).iterator();
		}

	}

	/**
	 * Provides access to a single {@link Condition} and {@link ConditionOutcome}.
	 */
	public static class ConditionAndOutcome {

		private final Condition condition;

		private final ConditionOutcome outcome;

		public ConditionAndOutcome(Condition condition, ConditionOutcome outcome) {
			this.condition = condition;
			this.outcome = outcome;
		}

		public Condition getCondition() {
			return this.condition;
		}

		public ConditionOutcome getOutcome() {
			return this.outcome;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ConditionAndOutcome other = (ConditionAndOutcome) obj;
			return (ObjectUtils.nullSafeEquals(this.condition.getClass(),
					other.condition.getClass()) && ObjectUtils.nullSafeEquals(
					this.outcome, other.outcome));
		}

		@Override
		public int hashCode() {
			return this.condition.getClass().hashCode() * 31 + this.outcome.hashCode();
		}
	}

}
