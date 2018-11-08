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

package org.springframework.zero.autoconfigure.orm.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.springframework.zero.autoconfigure.AutoConfigurationUtils;
import org.springframework.zero.autoconfigure.EnableAutoConfiguration;
import org.springframework.zero.autoconfigure.jdbc.EmbeddedDatabaseConfiguration;
import org.springframework.zero.context.condition.ConditionalOnBean;
import org.springframework.zero.context.condition.ConditionalOnClass;

/**
 * Base {@link EnableAutoConfiguration Auto-configuration} for JPA.
 * 
 * @author Phillip Webb
 */
@ConditionalOnClass({ LocalContainerEntityManagerFactoryBean.class,
		EnableTransactionManagement.class, EntityManager.class })
@ConditionalOnBean(DataSource.class)
public abstract class JpaAutoConfiguration implements BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(entityManagerFactory().getObject());
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setJpaVendorAdapter(jpaVendorAdapter());
		entityManagerFactoryBean.setDataSource(getDataSource());
		entityManagerFactoryBean.setPackagesToScan(getPackagesToScan());
		configure(entityManagerFactoryBean);
		return entityManagerFactoryBean;
	}

	/**
	 * Determines if the {@code dataSource} being used by Spring was created from
	 * {@link EmbeddedDatabaseConfiguration}.
	 * @return true if the data source was auto-configured.
	 */
	protected boolean isAutoConfiguredDataSource() {
		try {
			BeanDefinition beanDefinition = this.beanFactory
					.getBeanDefinition("dataSource");
			return EmbeddedDatabaseConfiguration.class.getName().equals(
					beanDefinition.getFactoryBeanName());
		}
		catch (NoSuchBeanDefinitionException ex) {
			return false;
		}
	}

	@Bean
	public abstract JpaVendorAdapter jpaVendorAdapter();

	protected DataSource getDataSource() {
		try {
			return this.beanFactory.getBean("dataSource", DataSource.class);
		}
		catch (RuntimeException ex) {
			return this.beanFactory.getBean(DataSource.class);
		}
	}

	protected String[] getPackagesToScan() {
		List<String> basePackages = AutoConfigurationUtils
				.getBasePackages(this.beanFactory);
		Assert.notEmpty(basePackages,
				"Unable to find JPA packages to scan, please define "
						+ "a @ComponentScan annotation or disable JpaAutoConfiguration");
		return basePackages.toArray(new String[basePackages.size()]);
	}

	protected void configure(
			LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}
}
