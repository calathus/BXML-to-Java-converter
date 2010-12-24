/*
 * author: calathus
 * create date: 12/24/2010
 */

package org.apache.pivot.wtk.converter;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.ICodeEmitter;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;

public class CodeEmitter implements ICodeEmitter {
	private final CodeEmitterContext ctx;
	private final String packageName;
	private final String targetClassName;
	private final PrintWriter wr;
	private final File bxmlfile;
	
	private int call_depth = 0;
	private int indent_level = 2;
	
	public CodeEmitter(File bxmlfile, String packageName, String targetClassName, PrintWriter wr) {
		this.ctx = new CodeEmitterContext(packageName);
		this.packageName = packageName;
		this.targetClassName = targetClassName;
		this.wr = wr;
		this.bxmlfile = bxmlfile;
	}
	
	public CodeEmitter(File bxmlfile, String packageName, String targetClassName, OutputStream out) {
		this(bxmlfile, packageName, targetClassName, new PrintWriter(out));
	}
	
	//
	public void code_start() {
		if (call_depth == 0) {
			String bxmlFileNameExp = "\""+bxmlfile.getName().replace("\"", "\\\"")+"\"";
			pr0("package " + packageName + ";");
			pr0();
			pr0("import java.net.*;");
			pr0("import org.apache.pivot.wtk.converter.CodeEmitterRuntime;");
			pr0("import org.apache.pivot.wtk.*;");
			pr0("import org.apache.pivot.wtk.content.*;");
			pr0("import org.apache.pivot.wtk.effects.easing.*;");
			pr0("import org.apache.pivot.wtk.effects.*;");
			pr0("import org.apache.pivot.wtk.media.*;");
			pr0("import org.apache.pivot.wtk.skin.*;");
			pr0("import org.apache.pivot.wtk.text.*;");
			pr0("import org.apache.pivot.wtk.validation.*;");
			pr0("import org.apache.pivot.collections.adapter.*;");
			pr0("import org.apache.pivot.collections.concurrent.*;");
			pr0("import org.apache.pivot.collections.immutable.*;");
			pr0("import org.apache.pivot.collections.*;");
			pr0();
			pr0("public class " + targetClassName + " implements Application {");
			pr0("    private Window window = null;");
			pr0();
			pr0("    @Override");
			pr0("    public void startup(Display display, Map<String, String> properties) throws Exception {");
			pr0("        //BXMLSerializer bxmlSerializer = new BXMLSerializer();");
			pr0("        //window = (Window)bxmlSerializer.readObject(getClass().getResource("+bxmlFileNameExp+"));");
			pr0("        final Object obj = getComponent();");
			pr0("        if (obj instanceof Window) {;");
			pr0("            window = (Window)obj;");
			pr0("        } else if (obj instanceof Component) {");
			pr0("            window = new Window();");
			pr0("            window.setContent((Component)obj);");
			pr0("            window.setTitle("+bxmlFileNameExp+");");
			pr0("        } else {");
			pr0("            System.out.println(\"getComponent returned object with type: \"+obj.getClass());");
			pr0("        }");
			pr0("        window.open(display);");
			pr0("    }");
			pr0();
			pr0("    @Override");
			pr0("    public boolean shutdown(boolean optional) {");
			pr0("        if (window != null) {");
			pr0("            window.close();");
			pr0("        }");
			pr0();
			pr0("        return false;");
			pr0("    }");
			pr0();
			pr0("    @Override");
			pr0("    public void suspend() {");
			pr0("    }");
			pr0();
			pr0("    @Override");
			pr0("    public void resume() {");
			pr0("    }");
			pr0();
			pr0("    public static void main(String[] args) {");
			pr0("        DesktopApplicationContext.main("+targetClassName+".class, args);");
			pr0("    }");
			pr0();
			pr0("    public static Object getComponent() {");
			pr0("		try {");
		}
		call_depth++;
	}
	
	public void code_end() {
		call_depth--;
		if (call_depth == 0) {
			pr0("            return "+ctx.env.first_var()+";");
			pr0("        } catch (Exception e) {");
			pr0("            e.printStackTrace();");
			pr0("            throw new RuntimeException(e);");
			pr0("        }");
			pr0("    }");
			pr0("}");
			wr.flush();
		}
	}

	public void code_new(final Class<?> type, final Object obj) {
		if (obj instanceof BXMLSerializer) {
			return;
		}
		final String var = ctx.env.declare(obj);
		final String clsName = ctx.getShortClassName(type);
		pr(clsName + " " + var + " = new " + clsName + "();");
	}

	public void code_set_element(final Object obj, final String name, final Object value) {
		if (obj == null) {
			throw new RuntimeException("[code_set_attr] obj must not be null: name: "+name+", value: "+value);
		}
		
		if (obj instanceof Dictionary<?, ?>) {
			final Dictionary<?, ?> dictionary = (Dictionary<?, ?>)obj;
			if (dictionary instanceof BeanAdapter) {
				code_bean_setter0(((BeanAdapter)dictionary).getBean(), ctx.getSetterName(name), null, value);
			} else {
				code_put_item(dictionary, name, null, value);
			}
		} else {
			code_set_prop(obj, name, value);
		}
	}
	
	public void code_add_element(final Sequence<?> sequence, final Object value) {
		final String ovar = ctx.getVarExp(sequence);
		final String vexp = ctx.getValueExp(value);
		pr(ovar + ".add(" + vexp + ");" );
	}

	public void code_set_attr(final Object obj, final String name, Class<?> propertyClass, final Object value) {
		if (obj == null) {
			throw new RuntimeException("[code_set_attr] obj must not be null: name: "+name+", value: "+value);
		}
		
		if (obj instanceof Dictionary<?, ?>) {
			
			final Dictionary<?, ?> dictionary = (Dictionary<?, ?>)obj;
			if (propertyClass == null) {
				propertyClass = (Class<?>)ctx.findDictionaryValueType(dictionary);
			}
			if (dictionary instanceof BeanAdapter) {
				code_bean_setter0(((BeanAdapter)dictionary).getBean(), ctx.getSetterName(name), propertyClass, value);
			} else {
				code_put_item(dictionary, name, propertyClass, value);
			}
		} else {
			code_set_prop(obj, name, value);
		}
	}

	//
	private void code_set_prop(Object obj, String name, Object value) {
		final String setterName = ctx.getSetterName(name);
		final Class<?> propertyClass = ctx.getSetterType(obj, setterName);
		if (propertyClass == null) {
			// this happen when parent attr is referenced..
			//System.out.println("[code_set_prop] no setter fund for "+setterName+", "+obj);
			return;
		}
		code_bean_setter0(obj, setterName, propertyClass, value);
	}

	private void code_put_item(final Dictionary<?, ?> dictionary, final String attrName, Class<?> propertyClass, final Object attrValue) {
		ctx.env.declare(dictionary);
		if (propertyClass == null)  propertyClass = (Class<?>)ctx.findDictionaryValueType(dictionary);
		final String ovar = ctx.getVarExp(dictionary);
		final String vexp = ctx.getValueExp(propertyClass, attrValue);
		pr(ovar + ".put(\"" + attrName + "\", "+vexp+");" );
	}
	
	private void code_bean_setter0(final Object obj, String setterName, Class<?> propertyClass, Object value) {
		ctx.env.declare(obj);
		final String var = ctx.getVarExp(obj);
		String vexp = ctx.getValueExp(propertyClass, value);
		if (vexp.endsWith("*")) {
			vexp = vexp.substring(0, vexp.length()-1);
			pr(var + "." + setterName + "(" + vexp +", true);");
		} else {
			pr(var + "." + setterName + "(" + vexp + ");");
		}
	}
	
	//
	public void code_static_set_attr(final Method setterMethod, final Object object, final Object value) {
		final Class<?> cls = setterMethod.getParameterTypes()[0];
		final String var = ctx.getVarExp(object);
		final String vexp = ctx.getValueExp(cls, value);
		final String setterName = setterMethod.getName();
		final Class<?> dclCls = setterMethod.getDeclaringClass();
		final String className = ctx.getShortClassName(dclCls);
		pr(className + "." + setterName + "(" + var+", "+vexp + ");");
	}
	
	public void code_decl_default_value(final Object obj, final String defaultPropertyName, final Object defaultPropertyValue) {
		if (defaultPropertyValue == null || ctx.env.isDefined(defaultPropertyValue)) {
			return;
		}
		final String var = ctx.env.declare(defaultPropertyValue);
		
		final Class<?> cls = defaultPropertyValue.getClass();
		final String className = ctx.getShortClassName(cls);
		final String ovar = ctx.getVarExp(obj);
		final String getterName = ctx.getGetterName(defaultPropertyName);
		pr(className + " " + var + " = ("+className+")"+ovar+"."+getterName+"();" );
	}
	
	public void code_bind(final Field field, final Object object, final Object value) {
		final String var = ctx.getVarExp(object);
		final String vexp = ctx.getValueExp(value);
		final String fieldName = field.getName();
		indent_level++;
		pr("CodeEmitterRuntime.bind("+var+", \""+fieldName+"\", "+vexp+");");
		indent_level--;
	}
	
	public void start_element() {
		wr.println();
		indent_level++;
	}
	
	public void end_element() {
		indent_level--;
	}

    //
	private void pr0(String s) {
		wr.println(s);
	}
	private void pr0() {
		wr.println();
	}
	
	private void pr(String s) {
		tab();         
		wr.println(s);
	}
	
	private void tab() {
		for (int i = 0; i < indent_level; i++) {
			wr.print("    ");
		}
	}
}
