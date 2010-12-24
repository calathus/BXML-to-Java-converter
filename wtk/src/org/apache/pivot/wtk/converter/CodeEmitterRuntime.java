/*
 * author: calathus
 * create date: 12/24/2010
 */

package org.apache.pivot.wtk.converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BindException;

public class CodeEmitterRuntime {
	
    public static void bind(Object object, String name, Object value) throws BindException {
        if (object == null) {
            throw new IllegalArgumentException();
        }
        Class<?> type = object.getClass();

        Field[] fields = type.getDeclaredFields();

        // Process bind annotations
        for (int j = 0, n = fields.length; j < n; j++) {
            Field field = fields[j];
            String fieldName = field.getName();
            if (!fieldName.equals(name)) continue;
            
            int fieldModifiers = field.getModifiers();

            BXML bindingAnnotation = field.getAnnotation(BXML.class);

            if (bindingAnnotation != null) {
                // Ensure that we can write to the field
                if ((fieldModifiers & Modifier.FINAL) > 0) {
                    throw new BindException(fieldName + " is final.");
                }

                if ((fieldModifiers & Modifier.PUBLIC) == 0) {
                    try {
                        field.setAccessible(true);
                    } catch (SecurityException exception) {
                        throw new BindException(fieldName + " is not accessible.");
                    }
                }

                String id = bindingAnnotation.id();
                if (id.equals("\0")) {
                    id = field.getName();
                }

                try {
                	//System.out.println(">>>>> fieldName: "+fieldName+", name: "+name+", value: "+value+", vc: "+value.getClass()+", fc: "+field.getType());
                    field.set(object, value);
                } catch (IllegalAccessException exception) {
                    throw new BindException(exception);
                }
            }
        }
    }

}
