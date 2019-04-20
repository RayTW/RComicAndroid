package app.ray.rcomic;

import android.app.Application;

import app.ray.rcomic.control.RComic;

public class RComicApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initializeRComic();
    }

    private void initializeRComic() {
        RComic.get().addTask(() -> {
            // 戴入全部漫畫
            RComic.get().preprogress(() -> {
                ComicListActivity.reload();
            });
        });
    }
}
