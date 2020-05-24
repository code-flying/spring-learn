/**  
 * <p>Title: GPBeanDefinitionReader.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年5月24日  
 */
package com.mylearn.spring.framework.beans.support;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.mylearn.spring.framework.beans.config.GPBeanDefinition;

/**
 * @author dingding
 *
 */
public class GPBeanDefinitionReader {

	// 保存扫描的类
	private List<String> regitryBeanClasses = new ArrayList<>();
	private Properties contextConfig = new Properties();

	public GPBeanDefinitionReader(String... configLocations) {
		doLoadConfig(configLocations[0]);

		// 扫描配置文件中的配置的相关的类
		doScanner(contextConfig.getProperty("scanPackage"));
	}

	public List<GPBeanDefinition> loadBeanDefinitions() {
		List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
		try {
			for (String className : regitryBeanClasses) {
				Class<?> beanClass = Class.forName(className);

				result.add(doCreateBeanDefinition(
						toLowerFirstCase(beanClass.getSimpleName()),
						beanClass.getName()));

				for (Class<?> clazz : beanClass.getInterfaces()) {
					result.add(doCreateBeanDefinition(clazz.getName(),
							beanClass.getName()));
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	private GPBeanDefinition doCreateBeanDefinition(String beanName,
			String beanClassName) {
		GPBeanDefinition beanDefinition = new GPBeanDefinition();
		beanDefinition.setFactoryBeanName(beanName);
		beanDefinition.setBeanClassName(beanClassName);
		return beanDefinition;
	}

	private void doScanner(String scanPackage) {
		URL url = this.getClass().getClassLoader()
				.getResource("/" + scanPackage.replaceAll("\\.", "/"));

		File classPath = new File(url.getFile());
		for (File file : classPath.listFiles()) {
			if (file.isDirectory()) {
				doScanner(scanPackage + "." + file.getName());
			} else {
				if (file.getName().endsWith(".class")) {
					continue;
				} else {
					String className = scanPackage + "."
							+ file.getName().replace(".class", "");
					regitryBeanClasses.add(className);
				}
			}
		}

	}

	private void doLoadConfig(String contextConfigLocation) {
		InputStream inputStream = null;
		try {
			inputStream = this.getClass().getClassLoader()
					.getResourceAsStream(contextConfigLocation.replaceAll("classpath:", ""));
			contextConfig.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}
}
