/**  
 * <p>Title: GPBeanWrapper.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年5月24日  
 */
package com.mylearn.spring.framework.beans;

/**
 * @author dingding
 *
 */
public class GPBeanWrapper {
	private Object wrapperInstance;
    private Class<?> wrappedClass;

	/**
	 * @param wrapperInstance
	 * @param wrapperdClass
	 */
	public GPBeanWrapper(Object instance) {
        this.wrapperInstance = instance;
        this.wrappedClass = instance.getClass();
    }

	public Object getWrapperInstance() {
		return wrapperInstance;
	}

	public Class<?> getWrapperdClass() {
		return wrappedClass;
	}

}
