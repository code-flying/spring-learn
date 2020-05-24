/**  
 * <p>Title: GPBeanDefinition.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年5月24日  
 */
package com.mylearn.spring.framework.beans.config;

/**
 * @author dingding
 *
 */
public class GPBeanDefinition {

	private String factoryBeanName;
	private String beanClassName;

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getBeanClassName() {
		return beanClassName;
	}

	public void setBeanClassName(String beanClassName) {
		this.beanClassName = beanClassName;
	}

}
