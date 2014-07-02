package it.sephiroth.android.library.picasso;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;

public class CustomBitmapHunter extends BitmapHunter {
  CustomBitmapHunter(Picasso picasso, Dispatcher dispatcher, Cache cache, Cache diskCache,
                     Stats stats, Action<?> action) {
    super(picasso, dispatcher, cache, diskCache, stats, action);
    this.loadedFrom = Picasso.LoadedFrom.DISK;
  }

  @Override
  Bitmap decode(Request data) throws IOException {
    return decodeContent(data);
  }

  Bitmap decodeContent(Request data) throws IOException {
    if (!data.hasGenerator()) {
      throw new IllegalStateException("Custom Uri can be used only with a Generator");
    }
    return data.generator.decode(data.uri);
  }

  @Override
  String getKey() {
    Log.i("picasso", "getKey");
    return super.getKey();
  }
}
