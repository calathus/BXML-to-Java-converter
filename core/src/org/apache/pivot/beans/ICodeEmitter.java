/*
 * author: calathus
 * create date: 12/24/2010
 */
package org.apache.pivot.beans;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.pivot.collections.Sequence;

public interface ICodeEmitter {
	
	void code_start();
	
	void code_end();

	void code_new(Class<?> type, Object obj);
	
	void code_set_element(Object obj, String name, Object value);
	
	void code_add_element(Sequence<?> sequence, Object value);
	
	void code_set_attr(Object obj, String name, Class<?> propertyClass, Object value);
	
	void code_static_set_attr(Method setterMethod, Object object, Object value);
	
	void code_decl_default_value(Object obj, String defaultPropertyName, Object defaultPropertyValue);
	
	void code_bind(Field field, Object object, Object value);
	
	void start_element();
	
	void end_element();
}
