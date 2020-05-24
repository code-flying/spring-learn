package com.gupaoedu.mvcframework.v2.servlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPRequestParam;
import com.gupaoedu.mvcframework.annotation.GPService;

/**  
 * <p>Title: GPDispatherServlet.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年4月4日  
 */

/**
 * @author dingding
 *
 */
public class GPDispatherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Properties contextConfig = new Properties();

	// IoC容器，key默认是类名首字母小写，value就是对应的实例对象
	private Map<String, Object> ioc = new HashMap<String, Object>();

	// 享元模式，缓存
	private List<String> classNames = new ArrayList<String>();

	private Map<String, Method> handlerMapping = new HashMap<String, Method>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			this.doPost(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
			resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			doDispatcher(req,resp);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
		
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!!!");
            return;
        }	
        
        @SuppressWarnings("unchecked")
		Map<String,String[]> params = req.getParameterMap();

        Method method = this.handlerMapping.get(url);
        
      //获取形参列表
        Class<?> [] parameterTypes = method.getParameterTypes();
        Object [] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramterType = parameterTypes[i];
            if(paramterType == HttpServletRequest.class){
                paramValues[i] = req;
            }else if(paramterType == HttpServletResponse.class){
                paramValues[i] = resp;
            }else if(paramterType == String.class){
                //通过运行时的状态去拿到你
                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length ; j ++) {
                    for(Annotation a : pa[i]){
                        if(a instanceof GPRequestParam){
                            String paramName = ((GPRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                               String value = Arrays.toString(params.get(paramName))
                                       .replaceAll("\\[|\\]","")
                                       .replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }

            }
        }


        //暂时硬编码
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //赋值实参列表
        method.invoke(ioc.get(beanName),paramValues);
        
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 1. 加载配置文件
		doLoadConfig(config.getInitParameter("contextConfigLocation"));

		// 2. 扫描相关的类
		doScanner(contextConfig.getProperty("scanPackage"));

		// 3. 初始化IOC容器，将扫描的相关类实例化，保存到IOC容器中
		doInstance();

		// 4. 完成依赖注入
		doAutowired();

		// 5. 初始化HandlerMapping()
		doInitHandlerMapping();

		System.out.println("Gp Spring framework is init.");

	}

	private void doInitHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry : ioc.entrySet()) {
			Class<?> clazz = entry.getValue().getClass();
			
			if(!clazz.isAnnotationPresent(GPController.class)){
				continue;
			}
			
			String baseUrl = "";
			if(clazz.isAnnotationPresent(GPRequestMapping.class)){
				GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
				baseUrl = requestMapping.value();
			}
			
			//只获取public的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(GPRequestMapping.class)){continue;}
                //提取每个方法上面配置的url
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);

                // //demo//query
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped : " + url + "," + method);
            }
			
		}

	}

	private void doAutowired() {
		if (ioc.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry : ioc.entrySet()) {

			for (Field field : entry.getValue().getClass().getDeclaredFields()) {
				if (!field.isAnnotationPresent(GPAutowired.class)) {
					continue;
				}

				GPAutowired autowired = field.getAnnotation(GPAutowired.class);
				String beanName = autowired.value().trim();
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}

				field.setAccessible(true);

				try {
					field.set(entry.getValue(), ioc.get(beanName));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}

	}

	private void doInstance() {
		if (classNames.isEmpty()) {
			return;
		}

		try {
			for (String className : classNames) {
				Class<?> clazz = Class.forName(className);
				if (clazz.isAnnotationPresent(GPController.class)) {
					String beanName = toLowerFirstCase(clazz.getSimpleName());
					Object instance = clazz.newInstance();
					ioc.put(beanName, instance);
				} else if (clazz.isAnnotationPresent(GPService.class)) {
					// 自定义命名类
					String beanName = clazz.getAnnotation(GPService.class)
							.value();
					if ("".equals(beanName.trim())) {
						beanName = toLowerFirstCase(clazz.getSimpleName());
					}

					Object instance = clazz.newInstance();
					ioc.put(beanName, instance);

					// 如果是接口
					for (Class<?> i : clazz.getInterfaces()) {
						if (ioc.containsKey(i.getName())) {
							throw new Exception("The " + i.getName()
									+ " is exists!!");
						}
						ioc.put(i.getName(), instance);
					}
				} else {
					continue;
				}

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

	private void doScanner(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        
		File classPath = new File(url.getFile());

		for (File file : classPath.listFiles()) {
			if (file.isDirectory()) {
				doScanner(scanPackage + "." + file.getName());
			} else {
				if (!file.getName().endsWith(".class")) {
					continue;
				}
				// 全类名
				String className = (scanPackage + "." + file.getName().replace(
						".class", ""));
				classNames.add(className);
			}
		}
	}

	/**
	 * 加载配置文件
	 */
	private void doLoadConfig(String contextConfigLocation) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(
				contextConfigLocation);
		if (is != null) {
			try {
				contextConfig.load(is);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
