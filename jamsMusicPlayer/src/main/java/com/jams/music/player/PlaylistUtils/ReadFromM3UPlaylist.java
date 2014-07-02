package com.jams.music.player.PlaylistUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

public class ReadFromM3UPlaylist {

    public ReadFromM3UPlaylist() throws Exception {
    	//Constructor.
    }

    public String convertStreamToString(java.io.InputStream is) {
    	
	    try {
	    	return new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (NoSuchElementException e) {
	    	return "";
	    }
	    
    }

    public M3UHolder parseFile(File f) throws FileNotFoundException {
        if (f.exists()) {
            String stream = convertStreamToString(new FileInputStream(f));
            stream = stream.replaceAll("#EXTM3U", "").trim();
            String[] arr = stream.split("#EXTINF.*,");
            String urls = "", data = "";
            	
            for (int n = 0; n < arr.length; n++) {
                if (arr[n].contains("http")) {
                        String nu = arr[n].substring(arr[n].indexOf("http://"),
                                        arr[n].indexOf(".mp3") + 4);

                        urls = urls.concat(nu);
                        data = data.concat(arr[n].replaceAll(nu, "").trim())
                                        .concat("&&&&");
                        urls = urls.concat("####");
                }
                
            }
            return new M3UHolder(data.split("&&&&"), urls.split("####"));
        }
        return null;
    }

    public class M3UHolder {
        private String[] data, url;

        public M3UHolder(String[] names, String[] urls) {
            this.data = names;
            this.url = urls;
        }

        public int getSize() {
            if (url != null)
                    return url.length;
            return 0;
        }

        public String getName(int n) {
            return data[n];
        }

        public String getUrl(int n) {
            return url[n];
        }
        
    }
    
}
