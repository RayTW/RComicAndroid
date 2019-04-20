package app.ray.rcomic.control;

import net.xuite.blog.ray00000test.library.comicsdk.Comic;
import net.xuite.blog.ray00000test.library.comicsdk.Episode;

import java.util.List;
import java.util.function.Consumer;

import app.ray.rcomic.utils.UnicodeUtility;

/**
 * 擴充Comic類別的功能
 *
 * @author ray
 */
public class ComicWrapper extends Comic {
    private Comic mComic;

    public ComicWrapper(Comic comic) {
        mComic = comic;
    }

    @Override
    public String getAuthor() {
        return UnicodeUtility.unicodeToChineseAll(mComic.getAuthor());
    }

    @Override
    public void setAuthor(String author) {
        mComic.setAuthor(author);
    }

    @Override
    public String getDescription() {
        String description = mComic.getDescription();

        if (description != null) {
            return UnicodeUtility.unicodeToChineseAll(description);
        }
        return null;
    }

    @Override
    public void setDescription(String description) {
        mComic.setDescription(description);
    }

    @Override
    public List<Episode> getEpisodes() {
        return mComic.getEpisodes();
    }

    @Override
    public void setEpisodes(List<Episode> episodes) {
        mComic.setEpisodes(episodes);
    }

    @Override
    public String getIconUrl() {
        return mComic.getIconUrl();
    }

    @Override
    public void setIconUrl(String iconUrl) {
        mComic.setIconUrl(iconUrl);
    }

    @Override
    public String getId() {
        return mComic.getId();
    }

    @Override
    public void setId(String id) {
        mComic.setId(id);
    }

    @Override
    public String getLatestUpdateDateTime() {
        return mComic.getLatestUpdateDateTime();
    }

    @Override
    public void setLatestUpdateDateTime(String latestUpdateDateTime) {
        mComic.setLatestUpdateDateTime(latestUpdateDateTime);
    }

    public String getNameWithNewestEpisode() {
        return getName() + "[" + getNewestEpisode() + "]";
    }

    @Override
    public String getName() {
        return UnicodeUtility.unicodeToChineseAll(mComic.getName());
    }

    @Override
    public void setName(String name) {
        mComic.setName(name);
    }

    @Override
    public String getNewestEpisode() {
        return mComic.getNewestEpisode();
    }

    @Override
    public void setNewestEpisode(String newestEpisode) {
        mComic.setNewestEpisode(newestEpisode);
    }

    @Override
    public String getSmallIconUrl() {
        return mComic.getSmallIconUrl();
    }

    @Override
    public void setSmallIconUrl(String smallIconUrl) {
        mComic.setSmallIconUrl(smallIconUrl);
    }

    public Comic get() {
        return mComic;
    }

    public void getEpisodesName(Consumer<String[]> consumer) {
        mComic.getEpisodes().forEach(episode -> {
            if (consumer != null) {
                consumer.accept(new String[]{episode.getName()});
            }
        });
    }
}
