package com.datecs.fileselector;

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.File;
import java.io.FileFilter;

/**
 * A set of tools for file operations
 */
public class FileUtils {

	/** Filter which accepts every file */
	public static final String FILTER_ALLOW_ALL = "*.*";

	/**
	 * This method checks that the file is accepted by the filter
	 * 
	 * @param file
	 *            - file that will be checked if there is a specific type
	 * @param filter
	 *            - criterion - the file type(for example ".jpg")
	 * @return true - if file meets the criterion - false otherwise.
	 */
	public static boolean accept(final File file, final String filter) {
		if (filter.compareTo(FILTER_ALLOW_ALL) == 0) {
			return true;
		}
		if (file.isDirectory()) {
			return true;
		}
		int lastIndexOfPoint = file.getName().lastIndexOf('.');
		if (lastIndexOfPoint == -1) {
			return false;
		}
		String fileType = file.getName().substring(lastIndexOfPoint).toLowerCase();
		return fileType.compareTo(filter) == 0;
	}

	public static void rescanFolder(Context ctx, String dest){
		// Scan files only (not folders);
		File[] files = new File(dest).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return  pathname.isFile();
			}
		});

		String[] paths = new String[files.length];
		for (int co=0; co< files.length; co++)
			paths[co] = files[co].getAbsolutePath();

		MediaScannerConnection.scanFile(ctx, paths, null, null);

		// and now recursively scan subfolders
		files = new File(dest).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		for (int co=0; co<files.length; co++)
			rescanFolder(ctx,files[co].getAbsolutePath());
	}


}
