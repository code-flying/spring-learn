/**  
 * <p>Title: GPApplicationContext.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年5月24日  
 */
package com.mylearn.spring.framework.context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mylearn.spring.framework.annotation.GPAutowired;
import com.mylearn.spring.framework.annotation.GPController;
import com.mylearn.spring.framework.annotation.GPService;
import com.mylearn.spring.framework.beans.GPBeanWrapper;
import com.mylearn.spring.framework.beans.config.GPBeanDefinition;
import com.mylearn.spring.framework.beans.support.GPBeanDefinitionReader;

/**
 * @author dingding
 *
 */
public class GPApplicationContext {

	private GPBeanDefinitionReader reader;

	private Map<String, GPBeanDefinition> beanDefinitionMap = new HashMap<String, GPBeanDefinition>();

	private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new HashMap<String, GPBeanWrapper>();

	private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();

	public GPApplicationContext(String... configLocations) {

		// 1. 加载配置文件
		reader = new GPBeanDefinitionReader(configLocations);

		// 2. 解析配置文件，封装成BeanDefinition
		List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

		// 3. 缓存BeanDefinition
		try {
			doRegistBeanDefinition(beanDefinitions);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 4. DI
		doAutowired();
	}

	private void doAutowired() {
		for (Map.Entry<String, GPBeanDefinition> beanDefiniEntry : this.beanDefinitionMap
				.entrySet()) {
			String beanName = beanDefiniEntry.getKey();
			getBean(beanName);
		}
	}

	public Object getBean(String beanName) {
		GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
		Object instance = instantiateBean(beanName, beanDefinition);

		GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);

		factoryBeanObjectCache.put(beanName, beanWrapper);

		populateBean(beanName, beanDefinition, beanWrapper);

		return beanWrapper.getWrapperInstance();
	}

	private void populateBean(String beanName, GPBeanDefinition beanDefinition,
			GPBeanWrapper beanWrapper) {

		try {
			Object instance = beanWrapper.getWrapperInstance();

			Class<?> clazz = beanWrapper.getWrapperdClass();

			if (!(clazz.isAnnotationPresent(GPController.class) || clazz
					.isAnnotationPresent(GPService.class))) {
				return;
			}

			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(GPAutowired.class)) {
					continue;
				}
				GPAutowired autowired = field.getAnnotation(GPAutowired.class);

				String autowiredBeanName = autowired.value().trim();
				if ("".equals(autowiredBeanName)) {
					autowiredBeanName = field.getType().getName();
				}

				field.setAccessible(true);

				if (this.factoryBeanObjectCache.get(autowiredBeanName) == null) {
					continue;
				}

				field.set(instance,
						this.factoryBeanInstanceCache.get(autowiredBeanName)
								.getWrapperInstance());

			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	private Object instantiateBean(String beanName,
			GPBeanDefinition beanDefinition) {
		String className = beanDefinition.getBeanClassName();
		Object instance = null;

		try {
			Class<?> clazz = Class.forName(className);
			instance = clazz.newInstance();
			this.factoryBeanObjectCache.put(beanName, instance);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	private void doRegistBeanDefinition(List<GPBeanDefinition> beanDefinitions)
			throws Exception {
		for (GPBeanDefinition beanDefinition : beanDefinitions) {
			if (this.beanDefinitionMap.containsKey(beanDefinition
					.getFactoryBeanName())) {
				throw new Exception("The "
						+ beanDefinition.getFactoryBeanName() + "is exists");
			} else {
				beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),
						beanDefinition);
				beanDefinitionMap.put(beanDefinition.getBeanClassName(),
						beanDefinition);
			}
		}

	}

}
