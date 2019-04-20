package app.ray.rcomic.control;

import android.support.annotation.NonNull;

import net.xuite.blog.ray00000test.library.comicsdk.Comic;
import net.xuite.blog.ray00000test.library.comicsdk.Episode;
import net.xuite.blog.ray00000test.library.comicsdk.R8Comic;
import net.xuite.blog.ray00000test.library.comicsdk.R8Comic.OnLoadListener;
import net.xuite.blog.ray00000test.library.net.EasyHttp;
import net.xuite.blog.ray00000test.library.net.EasyHttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import app.ray.rcomic.utils.ThreadPool;

/**
 * 下載漫畫的核心
 *
 * @author Ray Lee Created on 2017/08/16
 */
public class RComic {
    private static RComic sInstance;

    private ThreadPool mTaskPool;
    private ThreadPool mFIFOPool;
    private R8Comic mR8Comic = R8Comic.get();
    private List<ComicWrapper> mComics;
    private List<ComicWrapper> mNewComics;
    private Map<String, String> mHostList;
    private Thread mSearchThread;
    private SearchTask mSearchTask;

    private String mEpisodeImageUrlSchema = "https:";

    private RComic() {
        initialize();
    }

    public static RComic get() {
        if (sInstance == null) {
            synchronized (RComic.class) {
                if (sInstance == null) {
                    sInstance = new RComic();
                }
            }
        }
        return sInstance;
    }

    private void initialize() {
        mComics = new CopyOnWriteArrayList<>();
        mNewComics = new CopyOnWriteArrayList<>();
        mTaskPool = new ThreadPool(10);
        mFIFOPool = new ThreadPool(1);
        mSearchThread = new Thread(() -> startSearch());
        mSearchThread.start();
    }

    public R8Comic getR8Comic() {
        return mR8Comic;
    }

    public List<ComicWrapper> getComics() {
        return mComics;
    }

    public Map<String, String> getHostList() {
        return mHostList;
    }

    public void preprogress(Runnable completeListener) {
        CountDownLatch countdonw = new CountDownLatch(3);

        // 戴入全部漫畫
        mR8Comic.getAll(new OnLoadListener<List<Comic>>() {

            @Override
            public void onLoaded(List<Comic> comics) {
                mComics = new CopyOnWriteArrayList<>();

                for (Comic comic : comics) {
                    mComics.add(new ComicWrapper(comic));
                }
                countdonw.countDown();
            }

        });

        mR8Comic.getNewest(new OnLoadListener<List<Comic>>() {

            @Override
            public void onLoaded(List<Comic> comics) {
                mNewComics = new CopyOnWriteArrayList<ComicWrapper>();

                for (Comic comic : comics) {
                    mNewComics.add(new ComicWrapper(comic));
                }
                countdonw.countDown();
            }

        });

        // 戴入漫漫host列表
        mR8Comic.loadSiteUrlList(new OnLoadListener<Map<String, String>>() {

            @Override
            public void onLoaded(final Map<String, String> hostList) {
                mHostList = hostList;
                countdonw.countDown();
            }
        });

        try {
            countdonw.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (completeListener != null) {
            completeListener.run();
        }
    }

    public void addTask(Runnable run) {
        mTaskPool.executeTask(run);
    }

    public void loadEpisodesImagesPagesUrl(Episode episode, Consumer<Episode> consumer) {
        String downloadHost = mHostList.get(episode.getCatid());

        if (!episode.getUrl().startsWith("http")) {
            episode.setUrl(downloadHost + episode.getUrl());
        }

        R8Comic.get().loadEpisodeDetail(episode, result -> {
            result.setUpPages();

            if (consumer != null) {
                consumer.accept(result);
            }
        });
    }

    /**
     * 取得網站上全部漫畫列表
     *
     * @return
     */
    public List<ComicWrapper> getAllComics() {
        return mComics;
    }

    /**
     * 取得目前最新漫畫列表
     *
     * @return
     */
    public List<ComicWrapper> getNewComics() {
        return mNewComics;
    }

    public String getComicDetailUrl(String comicId) {
        return mR8Comic.getConfig().getComicDetailUrl(comicId);
    }

    public String requestGetHttp(String url, String charset) throws IOException {
        String result = null;
        EasyHttp request = new EasyHttp.Builder().setUrl(url).setMethod("GET").setIsRedirect(true)
                .setReadCharset(charset).setWriteCharset(charset)
                .putHeader("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .putHeader("Accept-Encoding", "gzip, deflate, br")
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36")
                .build();

        Response response = request.connect();
        result = response.getBody();

        return result;
    }

    private void startSearch() {
        while (true) {
            try {
                synchronized (mSearchThread) {
                    TimeUnit.MILLISECONDS.timedWait(mSearchThread, 500);
                }
                SearchTask task = mSearchTask;

                if (task == null || task.isFinish()) {
                    continue;
                }

                for (; ; ) {
                    task = mSearchTask;

                    if (!task.isFinish()) {
                        task.execute();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 搜尋漫畫名稱是否有符合關鍵字
     *
     * @param keyword
     * @param consumer
     */
    public void search(String keyword, Consumer<List<ComicWrapper>> consumer) {
        synchronized (mSearchThread) {
            mSearchTask = new SearchTask(() -> {
                ArrayList<ComicWrapper> list = new ArrayList<>();

                mComics.stream().filter(o -> o.getName().contains(keyword)).forEach(list::add);
                if (consumer != null) {
                    consumer.accept(list);
                }
            });
        }
        synchronized (mSearchThread) {
            mSearchThread.notifyAll();
        }
    }

    public void searchAllById(String id, @NonNull Consumer<ComicWrapper> consumer) {
        ComicWrapper comic = mComics.parallelStream().filter(o -> o.getId().equals(id)).findFirst().get();

        consumer.accept(comic);
    }

    public ComicWrapper searchAllById(String id) {
        return mComics.stream().filter(o -> o.getId().equals(id)).findFirst().get();
    }

    public ComicWrapper searchNewById(String id) {
        return mNewComics.stream().filter(o -> o.getId().equals(id)).findFirst().get();
    }

    // 執行使用關鍵字搜尋漫畫的任務
    private class SearchTask {
        private volatile boolean mIsFinish;
        private Runnable mRunnable;

        public SearchTask(Runnable task) {
            mRunnable = task;
        }

        public void execute() {
            try {
                mRunnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mIsFinish = true;
            }
        }

        public boolean isFinish() {
            return mIsFinish;
        }
    }
}
