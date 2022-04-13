package com.sakesnake.frankly.home.bottomnavprofile.settings.accountSettings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;

import java.util.List;

public class ActiveSessionFragment extends Fragment implements ActiveSessionsAdapter.ActiveSessionAdapterCallback{

    private View rootView;

    private Toolbar fragmentToolbar;

    private RecyclerView activeSessionRecyclerView;

    private ProgressBar loadingProgressBar;

    private Context mContext;

    private NavController navController;

    private ParseSession currentActiveSession;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_active_session, container, false);

        // Getting nav controller
        navController = Navigation.findNavController(getActivity(),R.id.fragment_bottom_nav_container);

        initViews();

        fetchSessionsData();

        return rootView;
    }

    private void initViews(){
        fragmentToolbar = (Toolbar) rootView.findViewById(R.id.active_session_fragment_toolbar);
        activeSessionRecyclerView = (RecyclerView) rootView.findViewById(R.id.active_session_recycler_view);
        loadingProgressBar = (ProgressBar) rootView.findViewById(R.id.bottom_progress_bar);
    }

    private void fetchSessionsData(){
        // Fetching current session
        ParseSession.getCurrentSessionInBackground((object, e) -> {
            if (e == null){
                currentActiveSession = object;
                fetchActiveSession();
            }
            else
                Log.e(Constants.TAG, "Cannot fetch current session: "+e.getMessage());
        });
    }

    private void fetchActiveSession(){
        ParseQuery<ParseSession> sessionParseQuery = ParseSession.getQuery();
        sessionParseQuery.whereEqualTo(ParseConstants.ACTIVE_SESSION_USER, ParseUser.getCurrentUser());
        sessionParseQuery.findInBackground((objects, e) -> {
            if (e == null)
                setRecyclerViewAdapter(objects);
            else
                Log.e(Constants.TAG, "Cannot fetch active session: "+e.getMessage());

            loadingProgressBar.setVisibility(View.GONE);
        });
    }

    private void setRecyclerViewAdapter(final List<ParseSession> parseSessionList){
        ActiveSessionsAdapter activeSessionsAdapter = new ActiveSessionsAdapter(parseSessionList,mContext,this,currentActiveSession);
        activeSessionRecyclerView.setAdapter(activeSessionsAdapter);
        activeSessionRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void getSessionToDelete(ParseSession parseSession) {
        if (parseSession != null){
            parseSession.deleteInBackground(e -> {
                if (e != null)
                    Log.e(Constants.TAG, "Cannot delete session: "+parseSession.getObjectId());
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Navigating to previous fragment
        fragmentToolbar.setNavigationOnClickListener(view1 -> navController.navigateUp());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
        fragmentToolbar = null;
        activeSessionRecyclerView = null;
        loadingProgressBar = null;
    }

}