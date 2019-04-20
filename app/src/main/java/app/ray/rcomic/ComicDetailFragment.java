package app.ray.rcomic;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.ray.rcomic.control.ComicWrapper;
import app.ray.rcomic.control.RComic;

/**
 * A fragment representing a single Comic detail screen.
 * This fragment is either contained in a {@link ComicListActivity}
 * in two-pane mode (on tablets) or a {@link ComicDetailActivity}
 * on handsets.
 */
public class ComicDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ComicWrapper mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ComicDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.

            RComic.get().searchAllById(getArguments().getString(ARG_ITEM_ID), item -> {
                mItem = item;

                Activity activity = this.getActivity();

                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }

                CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
                if (appBarLayout != null) {
                    appBarLayout.setTitle(mItem.getName());
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.comic_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.comic_detail)).setText(mItem.getDescription());
        }

        return rootView;
    }
}
