package org.apache.pivot.wtk.converter.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pivot.wtk.converter.BXML2JavaConverter;
import org.junit.Test;

public class BXML2JavaConverterTest {

	final static String srcDir = "/share/workspace/pivot/tutorials/src";
	final static String xfiles[] = {
		/*
			"json_table_view.bxml", // json file is not supported ...
			
			// javascript runtime error..
			"flow_panes.bxml",
			"table_panes_configure_row.bxml",
			"table_panes_configure_cell.bxml",
			"lists.bxml",
			"menu_section.bxml",
			"repeatable_list_buttons.bxml",
			"text.bxml",
			*/
	};
	
	public static String getPackage(File file) {
		String fileName = file.getParent().toString();
		if (fileName.startsWith(srcDir)) {
			return fileName.substring(srcDir.length()+1).replace('/', '.');
		}
		return null;
	}
	
	public static class ErrorInfo {
		public final File file;
		public final Exception e;
		public ErrorInfo(final File file, final Exception e) {
			this.file = file;
			this.e = e;
		}
	}
	
	static List<File> testedFiles = new ArrayList<File>();
	static List<ErrorInfo> errorInfos = new ArrayList<ErrorInfo>();
	
	public static void enumerateBXMFile(BXML2JavaConverter b2j, File folder) {
		testedFiles.clear();
		errorInfos.clear();
		
		enumerateBXMFile0(b2j, folder);
		
		System.out.println("\n==== success ====");
		for (File file: testedFiles) {
			System.out.println(" "+file.getName());
		}
		
		System.out.println("\n==== fail ====");
		for (ErrorInfo ei: errorInfos) {
			System.out.println(" "+ei.file.getName());
		}
		
		System.out.println("------------------------------------");
		System.out.println("total failed file number: "+errorInfos.size()+"/"+testedFiles.size());
		
	}
	static void enumerateBXMFile0(BXML2JavaConverter b2j, File folder) {
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				String bxmlFileName = file.getName();
				if (Arrays.asList(xfiles).contains(bxmlFileName)) {
					continue;
				}
				if (bxmlFileName.endsWith(".bxml")) {
					String packageName = getPackage(file);
					String className = "_"+BXML2JavaConverter.toJavaClassName(bxmlFileName);// assume no classname in pivot start with _.
					System.out.println(file + ", "+packageName+", "+className);
					try {
						testedFiles.add(file);
						new BXML2JavaConverter().convert(file.toString(), packageName, className, null, false);						
					} catch (Exception e) {
						// e.printStackTrace();
						System.out.println("###### FAILED #### => "+file.toString());
						errorInfos.add(new ErrorInfo(file, e));
						File classFile = new File(file.getParent(), className+".java");
						File faileClassFile = new File(file.getParent(), className+".java.1");
						classFile.renameTo(faileClassFile);
					}
				}
			} else if (file.isDirectory()) {
				enumerateBXMFile0(b2j, file);
			}
		}
	}
	
	@Test
	public void allTutorialBXMLTest() {
		try {
			final BXML2JavaConverter b2j = new BXML2JavaConverter();

			enumerateBXMFile(b2j, new File(srcDir));

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed: " + e.getMessage());
		}
	}

	void translate(BXML2JavaConverter b2j, String simpleBXMLFileName, String packageName, String className) {
		final String packagePath = packageName.replace('.', '/');
		final String bxmlFileName = srcDir+"/"+packagePath+"/"+simpleBXMLFileName;
		b2j.convert(bxmlFileName, packageName, className, null, false);
	}
	void translate(BXML2JavaConverter b2j, String[] args) {
		translate(b2j, args[0], args[1], args[2]);
	}
	
//	@Test
	public void generateTest() {
		try {
			final BXML2JavaConverter b2j = new BXML2JavaConverter();

			final String argss[][] = {
					{ "stock_tracker_window.bxml", "org.apache.pivot.tutorials.stocktracker", "TestCodeGenerator" },
					//{ "custom_table_view.bxml", "org.apache.pivot.tutorials.tableviews", "TestCodeGenerator" }, 
					//{ "alerts.bxml", "org.apache.pivot.tutorials", "TestCodeGenerator" }, 
					//{ "kitchen_sink.bxml", "org.apache.pivot.tutorials", "TestCodeGenerator" }, 
					//{ "component_explorer_window.bxml", "org.apache.pivot.tutorials.explorer", "TestCodeGenerator" }, 
					//{ "spinners.bxml", "org.apache.pivot.tutorials", "TestCodeGenerator" }, 
					//{ "accordions.bxml", "org.apache.pivot.tutorials.navigation", "TestCodeGenerator" }, 
					//{ "json_table_view.bxml", "org.apache.pivot.tutorials.tableviews", "TestCodeGenerator"},
					};
			for (final String[] args : argss) {
				translate(b2j, args);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed: " + e.getMessage());
		}
	}
	

}
