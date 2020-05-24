/**  
* <p>Title: MyAction.java</p>  
* <p>Description: </p>   
* @author dingding  
* @date 2020年5月24日  
*/  
package com.mylearn.spring.framework;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mylearn.spring.framework.annotation.GPAutowired;
import com.mylearn.spring.framework.annotation.GPController;
import com.mylearn.spring.framework.annotation.GPRequestMapping;
import com.mylearn.spring.framework.annotation.GPRequestParam;
import com.mylearn.spring.framework.service.IModifyService;
import com.mylearn.spring.framework.service.IQueryService;

/**
 * @author dingding
 *
 */
@GPController
@GPRequestMapping("/web")
public class MyAction {


	@GPAutowired IQueryService queryService;
	@GPAutowired IModifyService modifyService;

	@GPRequestMapping("/query.json")
	public void query(HttpServletRequest request, HttpServletResponse response,
								@GPRequestParam("name") String name){
		String result = queryService.query(name);
		out(response,result);
	}
	
	@GPRequestMapping("/add*.json")
	public void add(HttpServletRequest request,HttpServletResponse response,
			   @GPRequestParam("name") String name,@GPRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		out(response,result);
	}
	
	@GPRequestMapping("/remove.json")
	public void remove(HttpServletRequest request,HttpServletResponse response,
		   @GPRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		out(response,result);
	}
	
	@GPRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,
			@GPRequestParam("id") Integer id,
			@GPRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		out(response,result);
	}
	
	
	
	private void out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	


}
