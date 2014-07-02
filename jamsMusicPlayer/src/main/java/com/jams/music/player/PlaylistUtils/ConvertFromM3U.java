package com.jams.music.player.PlaylistUtils;

import java.io.File;

/*******************************************************
 * This helper class utilizes the Lizzy library to 
 * convert an M3U playlist file to another file format. 
 * 
 * @author Saravan Pantham
 *******************************************************/
public class ConvertFromM3U {

	//Playlist parameters
	private File mPlaylistFile;
	private String mOutputFormat;
	private String mOutputFilePath;
	
	//Success/Error codes.
	public static String SUCCESS = "Playlist converted.";
	public static String INPUT_FILE_IO_EXCEPTION = "The playlist file could not be read.";
	public static String INPUT_FILE_INVALID = "The playlist file is invalid or corrupt.";
	public static String PLAYLIST_COULD_NOT_BE_CONVERTED = "The playlist could not be converted.";
	public static String PLAYLIST_COULD_NOT_BE_SAVED = "The converted playlist could not be saved.";
	
	//Playlist format codes.
	public static String ASX = "asx";
	public static String ATOM = "atom";
	public static String B4S = "b4s";
	public static String HYPETAPE = "hypetape";
	public static String KPL = "kpl";
	public static String M3U = "m3u";
	public static String MPCPL = "mpcpl";
	public static String PLA = "pla";
	public static String PLIST = "plist";
	public static String PLP = "plp";
	public static String PLS = "pls";
	public static String RMP = "rmp";
	public static String RSS = "rss";
	public static String SMIL = "smil";
	public static String WPL = "wpl";
	public static String XSPF = "xspf";
	
	public ConvertFromM3U(File playlistFile, String outputFormat, String outputFilePath) {
		mPlaylistFile = playlistFile;
		mOutputFormat = outputFormat;
		mOutputFilePath = outputFilePath;
	}
	
	/* Runs the actual conversion process. Returns "SUCCESS" if the 
	 * operation succeeded. Returns an error code otherwise. */
	public String convertPlaylistFile() {
		
		
		
		return SUCCESS;
	}
	
}
