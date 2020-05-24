/**  
 * <p>Title: GPDispatcherServlet.java</p>  
 * <p>Description: </p>   
 * @author dingding  
 * @date 2020年5月24日  
 */
package com.mylearn.spring.framework.webmvc.servlet;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mylearn.spring.framework.annotation.GPController;
import com.mylearn.spring.framework.annotation.GPRequestMapping;
import com.mylearn.spring.framework.annotation.GPRequestParam;
import com.mylearn.spring.framework.context.GPApplicationContext;

/**
 * @author dingding
 *
 */
public class GPDispatcherServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// ioc 容器
	private Map<String, Object> ioc = new HashMap<String, Object>();

	// url 映射
	private Map<String, Method> handlerMapping = new HashMap<String, Method>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			doDispatch(req, resp);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void doDispatch(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {

		String url = req.getRequestURI();
		String contextPath = req.getContextPath();
		url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

		if (!this.handlerMapping.containsKey(url)) {
			resp.getWriter().write("404 Not Found!!!");
			return;
		}

		@SuppressWarnings("unchecked")
		Map<String, String[]> params = req.getParameterMap();

		Method method = this.handlerMapping.get(url);

		// 获取形参列表
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] paramValues = new Object[parameterTypes.length];

		for (int i = 0; i < parameterTypes.length; i++) {
			Class<?> paramterType = parameterTypes[i];
			if (paramterType == HttpServletRequest.class) {
				paramValues[i] = req;
			} else if (paramterType == HttpServletResponse.class) {
				paramValues[i] = resp;
			} else if (paramterType == String.class) {
				// 通过运行时的状态去拿到你
				Annotation[][] pa = method.getParameterAnnotations();
				for (int j = 0; j < pa.length; j++) {
					for (Annotation a : pa[i]) {
						if (a instanceof GPRequestParam) {
							String paramName = ((GPRequestParam) a).value();
							if (!"".equals(paramName.trim())) {
								String value = Arrays
										.toString(params.get(paramName))
										.replaceAll("\\[|\\]", "")
										.replaceAll("\\s+", ",");
								paramValues[i] = value;
							}
						}
					}
				}

			}
		}

		// 暂时硬编码
		String beanName = toLowerFirstCase(method.getDeclaringClass()
				.getSimpleName());
		// 赋值实参列表
		method.invoke(ioc.get(beanName), paramValues);

	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		new GPApplicationContext(
				config.getInitParameter("contextConfigLocation"));

		doInitHandlerMapping();
	}

	private void doInitHandlerMapping() {
		if (ioc.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry : ioc.entrySet()) {

			Class<?> clazz = entry.getValue().getClass();
			if (!clazz.isAnnotationPresent(GPController.class)) {
				continue;
			}

			String baseUrl = "";
			if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
				GPRequestMapping requestMapping = clazz
						.getAnnotation(GPRequestMapping.class);
				baseUrl = requestMapping.value();
			}

			for (Method method : clazz.getMethods()) {
				if (!method.isAnnotationPresent(GPRequestMapping.class)) {
					continue;
				}

				GPRequestMapping requestMapping = method
						.getAnnotation(GPRequestMapping.class);

				String url = ("/" + baseUrl + "/" + requestMapping.value())
						.replaceAll("/+", "/");
				handlerMapping.put(url, method);
				System.out.println("Mapped : " + url + "," + method);

			}

		}

	}

	private String toLowerFirstCase(String simpleName) {
		char[] chars = simpleName.toCharArray();
		chars[0] += 32;
		return String.valueOf(chars);
	}

}
