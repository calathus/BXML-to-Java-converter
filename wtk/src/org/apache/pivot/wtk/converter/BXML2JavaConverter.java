/*
 * author: calathus
 * create date: 12/24/2010
 */
package org.apache.pivot.wtk.converter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;

public class BXML2JavaConverter {
	private final BXMLSerializer bxmlSerializer;
	
	public BXML2JavaConverter(final BXMLSerializer bxmlSerializer) {
		this.bxmlSerializer = bxmlSerializer;
	}
	public BXML2JavaConverter() {
		this(new BXMLSerializer());
	}
	
    public void convert(final File bxmlfile, final String packageName, final String targetClassName) {
    	convert(bxmlfile, packageName, targetClassName, System.out);
    }
    
    public void convert(final File bxmlfile, final Resources resources, final String packageName, final String targetClassName) {
    	convert(bxmlfile, packageName, targetClassName, System.out);
    }
    
    public void convert(final File bxmlfile, final String packageName, final String targetClassName, final OutputStream out) {
    	convert(bxmlfile, null, packageName, targetClassName, out);
    }

    public void convert(final File bxmlfile, final Resources resources, final String packageName, final String targetClassName, final OutputStream out) {
    	final CodeEmitter gen = new CodeEmitter(bxmlfile, packageName, targetClassName, out);
    	bxmlSerializer.setCodeEmitter(gen);
        try {
        	bxmlSerializer.readObject(bxmlfile.toURL(), resources);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (SerializationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		bxmlSerializer.setCodeEmitter(null);
    }
    
    public void convert(final String bxmlFileName, final String packageName, final String className, final String language, final boolean debug) {
    	try {
    		final File bxmlFile = new File(bxmlFileName);
    		final String parentFolderName = bxmlFile.getParent();
    		
    		final String outFileName = parentFolderName + "/" + className+".java";
    		final OutputStream out = (debug)?System.out:new FileOutputStream(outFileName);

    		final Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
    		final String resourceClassName = packageName+"."+toJavaClassName(bxmlFile.getName());
            Resources resources = null;
            try {
            	resources = new Resources(resourceClassName, locale);
            } catch (java.util.MissingResourceException e) {
            	System.out.println(">> Warning: MissingResourceException: "+e.getMessage());
            }
            convert(bxmlFile, resources, packageName, className, out);
            out.flush();
			if (out != null && out != System.out) {
				out.close();
			}
            System.out.println(">> generated: "+outFileName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
    }
	
    public static String toJavaClassName(final String s) {
    	final StringBuilder sb = new StringBuilder();
    	final int size = s.length();
    	boolean foundUnderScore = false;
    	if (size > 0) sb.append(Character.toUpperCase(s.charAt(0)));
    	for (int i = 1; i < size; i++) {
    		final char ch = s.charAt(i);
    		if (ch == '_') {
    			foundUnderScore = true;
    		} else if (ch == '.') {
    			break;
    		} else {
    			sb.append((foundUnderScore)?Character.toUpperCase(ch):ch);
    			foundUnderScore = false;
    		}
    	}
    	return sb.toString();
    }
    
	//
    public static void main(String[] args) {
    	if (args.length == 0) {
//   		args = new String[]{"/share/workspace/pivot/tutorials/src/org/apache/pivot/tutorials/stocktracker/stock_tracker_window.bxml", "org.apache.pivot.tutorials.stocktracker", "TestCodeGenerator"};
    		args = new String[]{"/share/workspace/pivot/tutorials/src/org/apache/pivot/tutorials/tableviews/custom_table_view.bxml", "org.apache.pivot.tutorials.tableviews", "TestCodeGenerator"};
    	} else if (args.length < 3) {
    		System.out.println("require arguments for bxml file name, packageName, className.");
    		return;
    	}
    	String bxmlFileName = args[0];
    	String packageName = args[1];
    	String className = args[2];

    	try {
    		BXML2JavaConverter b2j = new BXML2JavaConverter();
    		b2j.convert(bxmlFileName, packageName, className, null, false);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed: "+e.getMessage());
		}
    }
}
