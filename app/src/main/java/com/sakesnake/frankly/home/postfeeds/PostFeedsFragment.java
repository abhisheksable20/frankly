package com.sakesnake.frankly.home.postfeeds;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sakesnake.frankly.R;

/**
    This fragment contains just recycler view for all types of content view,
    but this recycler view will only show preview of the content
    except image feed and quotes feed

    -- Arguments Required in this fragment are
    1 - Content Type (Imp)
    2 - Form where this fragment is called (Imp)

    -- onSavedSateInstance
    1 - Content Type (Imp)
    2 - From where this fragment is called (Imp)

    Here data we have already, will be get from HomeFragment, SearchProfileFragment (If feature implemented).
    Data we have to fetch from network when this fragment is call from ProfileFragment, UserProfileFragment, SearchFragment.
**/

@Deprecated
public class PostFeedsFragment extends Fragment {

    private View rootView;

    private Toolbar toolbar;

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView usersPostRecyclerView;

    private PostFeedsViewModel postFeedsViewModel;

    private Context mContext;

    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_post_feeds, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(), R.id.fragment_bottom_nav_container);

        // getting view model
        postFeedsViewModel = new ViewModelProvider(requireActivity()).get(PostFeedsViewModel.class);

        // init views
        toolbar = (Toolbar) rootView.findViewById(R.id.users_posts_toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.users_posts_swipe_to_refresh_layout);
        usersPostRecyclerView = (RecyclerView) rootView.findViewById(R.id.users_posts_recycler_view);

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Refresh the recycler view with new ParseObject data
        swipeRefreshLayout.setOnRefreshListener(()->{

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        toolbar = null;
        swipeRefreshLayout = null;
        usersPostRecyclerView = null;
    }
}