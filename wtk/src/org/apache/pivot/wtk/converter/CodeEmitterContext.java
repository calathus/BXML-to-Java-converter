/*
 * author: calathus
 * create date: 12/24/2010
 */

package org.apache.pivot.wtk.converter;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Iterator;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSONSerializer;

public class CodeEmitterContext {
	
	// since List override equals, hashCode cannot be the same value for mutable List, therefore it cannot be used for HashMap!
	//private final Map<Object, String> rev_decls = new HashMap<Object, String>(); // 
	public final CodeEmitterEnv env = new CodeEmitterEnv(); 

	private final String[] pkgNames;
	
	public CodeEmitterContext(String packageName) {
		this.pkgNames  = new String[]{ 
				"java.net.",
				"org.apache.pivot.wtk.content.",
				"org.apache.pivot.wtk.effects.easing.",
				"org.apache.pivot.wtk.effects.",
				"org.apache.pivot.wtk.media.",
				"org.apache.pivot.wtk.skin.terra.",
				"org.apache.pivot.wtk.skin.",
				"org.apache.pivot.wtk.text.",
				"org.apache.pivot.wtk.validation.",
				"org.apache.pivot.wtk.",
				"org.apache.pivot.collections.adapter.",
				"org.apache.pivot.collections.concurrent.",
				"org.apache.pivot.collections.immutable.",
				"org.apache.pivot.collections.",
				packageName+"."};
	}

	public String getVarExp(final Object obj) {
		final String var = env.findVar(obj);
		if (var == null) {
			env.dump();
			throw new RuntimeException("object not declared: " + obj);
		}
		return var;
	}

	public String getValueExp(final Object value) {
		return getValueExp(null, value);
	}

	public String getValueExp(final Class<?> propertyClass, final Object value) {
		String sv = null;
		if (propertyClass == null) {
			sv = getValueExp0(value);
		} else if (propertyClass.equals(boolean.class) || Boolean.class.isAssignableFrom(propertyClass)) {
			sv =  ""+value;
		} else if (propertyClass.equals(char.class) || Character.class.isAssignableFrom(propertyClass)) {
			sv =  "'" + value + "'";
		} else if (propertyClass.equals(byte.class) || Byte.class.isAssignableFrom(propertyClass)) {
			sv =  value + "";
		} else if (propertyClass.equals(short.class) || Short.class.isAssignableFrom(propertyClass)) {
			sv =  value + "";
		} else if (propertyClass.equals(int.class) || Integer.class.isAssignableFrom(propertyClass)) {
			sv =  value + "";
		} else if (propertyClass.equals(long.class) || Long.class.isAssignableFrom(propertyClass)) {
			sv =  value + "L";
		} else if (propertyClass.equals(float.class) || Float.class.isAssignableFrom(propertyClass)) {
			sv =  value + "f";
		} else if (propertyClass.equals(double.class) || Double.class.isAssignableFrom(propertyClass)) {
			sv =  value + "d";
		} else if (propertyClass.isEnum()) {
			sv = getShortClassName(propertyClass) + "."+value.toString().toUpperCase();
		} else if (propertyClass.equals(java.text.NumberFormat.class)) {
			// [nn] assume Pivot convert string to format
			sv =  "\"" + value + "\"";
		} else if (org.apache.pivot.collections.List.class.isAssignableFrom(propertyClass) && value instanceof String) {
			sv =  "\"" + value + "\"";
		} else if (org.apache.pivot.collections.Set.class.isAssignableFrom(propertyClass)) {
			sv =  "\"" + value + "\"";
		} else if (org.apache.pivot.collections.Map.class.isAssignableFrom(propertyClass)) {
			sv =  "\"" + value + "\"";
		} else {
			sv = getValueExp0(value);
		}
		if (sv.contains("start:") && sv.contains("end:") && propertyClass.equals(int.class)) {
			try {
				org.apache.pivot.collections.HashMap json = (org.apache.pivot.collections.HashMap)JSONSerializer.parse(sv);
				Integer start = (Integer)json.get("start");
				Integer end = (Integer)json.get("end");
				sv = "new org.apache.pivot.wtk.Span("+start+","+end+")";
			} catch (Exception e) {
				System.out.println(">> json error "+sv+", "+e.getMessage());
			}
		}
		return sv; // TODO
	}
	
	private String getValueExp0(final Object value) {
		String sv = "";
		String var = env.findVar(value);
		if (var != null) {
			sv = var;
		} else {
			if (value instanceof String) {
				sv = "\"" + ((String)value).replace("\"", "\\\"") + "\"";
			} else if (value instanceof Character) {
				sv = "'" + value + "'";
			} else if (value instanceof Long) {
				sv =  value + "L";
			} else if (value instanceof Float) {
				sv =  value + "f";
			} else if (value instanceof Double) {
				sv =  value + "d";
			} else if (value instanceof URL) { // assume pivot API convention...
				sv = "new URL(\"" + value + "\")";
			} else if (value instanceof org.apache.pivot.collections.List) {
				// this will work for setter taking Map.. [nn]
				sv =  getListExp((org.apache.pivot.collections.List<?>)value);
			} else if (value instanceof org.apache.pivot.collections.Set) {
				// this will work for setter taking Map.. [nn]
				sv =  getSetExp((org.apache.pivot.collections.Set<?>)value);
			} else if (value instanceof org.apache.pivot.collections.Map) {
				// this will work for setter taking Map.. [nn]
				sv =  getMapExp((org.apache.pivot.collections.Map<?,?>)value);
			} else {
				sv = "" + value;
			}
		}
		return sv; // TODO
	}
	
	private String getListExp(final org.apache.pivot.collections.List<?> list) {
		final StringBuilder sb = new StringBuilder("new ArrayList(");
		for (final Iterator<?> iter = list.iterator(); iter.hasNext(); ) {
			final Object obj = iter.next();
			sb.append(getValueExp(obj));
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String getSetExp(final org.apache.pivot.collections.Set<?> set) {
		final StringBuilder sb = new StringBuilder("new HashSet(");
		for (final Iterator<?> iter = set.iterator(); iter.hasNext(); ) {
			final Object obj = iter.next();
			sb.append(getValueExp(obj));
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	private String getMapExp(final org.apache.pivot.collections.Map map) {
		final StringBuilder sb = new StringBuilder("new HashMap(");
		for (final Iterator iter = map.iterator(); iter.hasNext(); ) {
			final Object key = iter.next();
			final Object value = map.get(key);
			final String kexp = getValueExp(key);
			final String vexp = getValueExp(value);
			sb.append("new Dictionary.Pair("+kexp+", "+vexp+")");
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public String getSetterName(final String name) {
		 return "set"+getHeadLowCaseName(name);
	}
	
	public String getGetterName(final String name) {
		return "get"+getHeadLowCaseName(name);
	}
	
	public String getHeadLowCaseName(final String name) {
		return name.substring(0, 1).toUpperCase()+name.substring(1);
	}
	
	public String getShortClassName(final Class<?> type) {
		String clsName = type.getName();
		
		for (final String pkgName : pkgNames) {
			if (clsName.startsWith(pkgName)) {
				final String clsName0 = clsName.substring(pkgName.length());
				if (clsName0.contains(".") && Character.isLowerCase(clsName0.charAt(0))) {
					// assume Pivot does not use class which starts with lower casee letter.
					// to avoid creating like tools.A where tools is part of package path
					continue;
				}
				clsName = clsName0;
				break;
			}
		}
		clsName = clsName.replace("$", ".");
		return clsName;
	}
	
	public Class<?> getSetterType(Object obj, String setterName) {
		for (Method method: obj.getClass().getMethods()) {
			if (method.getName().equals(setterName)) {
				return method.getParameterTypes()[0];
			}
		}
		return null;
	}

	public Type findDictionaryValueType(final Object dictionary) {
		if (dictionary instanceof Dictionary<?, ?>) {
			return findDictionaryValueType(dictionary.getClass());
		}
		return null;
	}
	
	public Type findDictionaryValueType(final Class<?> cls) {
		Type vt = null;
		for (Type t0 : cls.getGenericInterfaces()) {
			// System.out.println(">> [findDictionaryValueType] t0: " + t0 + ", " + t0.getClass());
			if (t0.toString().startsWith("org.apache.pivot.collections.Dictionary<")) {
				vt = ((ParameterizedType) t0).getActualTypeArguments()[1];
				// System.out.println("==>> [findDictionaryValueType] t0: " + t0 + ", " + t0.getClass()+", vt: "+vt);
				break;
			}
		}
		return vt;
	}
}
