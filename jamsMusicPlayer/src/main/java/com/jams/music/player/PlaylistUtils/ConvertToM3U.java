package com.jams.music.player.PlaylistUtils;

import java.io.File;

/*******************************************************
 * This helper class utilizes the Lizzy library to 
 * convert an M3U playlist file to another file format. 
 * 
 * @author Saravan Pantham
 *******************************************************/
public class ConvertToM3U {

	//Playlist parameters
	private File mPlaylistFile;
	private String mOutputFilePath;
	
	//Success/Error codes.
	public static String SUCCESS = "Playlist converted.";
	public static String INPUT_FILE_IO_EXCEPTION = "The playlist file could not be read.";
	public static String INPUT_FILE_INVALID = "The playlist file is invalid or corrupt.";
	public static String PLAYLIST_COULD_NOT_BE_CONVERTED = "The playlist could not be converted.";
	public static String PLAYLIST_COULD_NOT_BE_SAVED = "The converted playlist could not be saved.";
	
	public ConvertToM3U(File playlistFile, String outputFilePath) {
		mPlaylistFile = playlistFile;
		mOutputFilePath = outputFilePath;
	}
	
	/* Runs the actual conversion process. Returns "SUCCESS" if the 
	 * operation succeeded. Returns an error code otherwise. */
	public String convertPlaylistFile() {
		
		return SUCCESS;
	}
	
}
