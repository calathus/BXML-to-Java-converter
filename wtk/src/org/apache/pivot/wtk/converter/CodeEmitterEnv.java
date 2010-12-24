/*
 * author: calathus
 * create date: 12/24/2010
 */

package org.apache.pivot.wtk.converter;

import java.util.ArrayList;
import java.util.List;

// [nn]
// org.apache.pivot.collections.ArrayList<T> implements equals,and hashCode is created from the current its elements. so if list is added new element, the hashCode will be changed dynamically.
// this is the reason for get return null even the list is there.
// essentially, either putting this list to map is bad idea or should not implement hash for immutable object like this.
// ----------------
// it fails to find rev_decls.get(obj)
// where obj is instance of org.apache.pivot.collections.ArrayList<T>.
// probably equals, hashCode are not consistent, i.e, e1.equals(e2) ==> e1.hashCode() == e2.hashCode() is not satisfied..
// ==> initially this condition was satisfied, but aster new elements are added, this condition will not hold.
public class CodeEmitterEnv {
	static class Item {
		public final String var;
		public final Object value;
		public Item(final String var, final Object value) {
			this.var = var;
			this.value = value;
		}
	}
	
	private final List<Item> decls = new ArrayList<Item>();
	private String first_var = null;
	private long gvid = 0;
	
	public String declare(final Object obj) {
		String var = findVar(obj);
		if (var == null) {
			var = createVar(obj);
			if (first_var == null) {
				first_var = var;
			}
			decl(var, obj);
			
		}
		return var;
	}
	
	public String findVar(final Object obj) {
		return getVar(obj);
	}

	public boolean isDefined(final Object obj) {
		return findVar(obj) != null;
	}

	public String first_var() {
		return first_var;
	}
	
	public void dump() {
		System.out.println("=== Decls dump vars ===");
		for (Item item: decls) {
			System.out.println("  " + item.var);
		}
	}

	//
	private void decl(String var, Object value) {
		decls.add(new Item(var, value));
	}
	private String getVar(Object value) {
		for (Item item: decls) {
			// [nn] using == is important, need to avoid equals!
			if (item.value == value) {
				return item.var;
			}
		}
		return null;
	}	
	private String createVar(final Object obj) {
		final String v = getVarHeaderName(obj.getClass().getSimpleName());
		final String var = v + "_"+(gvid++);
		return var;
	}
	private String getVarHeaderName(final String name) {
		 return name.substring(0, 1).toLowerCase()+name.substring(1);
	}
}
