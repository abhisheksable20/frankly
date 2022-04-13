package com.sakesnake.frankly.home.bottomnavsearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class SearchListRecyclerAdapter extends RecyclerView.Adapter<SearchListRecyclerAdapter.SearchListViewHolder> {

    private Context mContext;

    private ArrayList<SearchList> searchListArrayList;

    private SetSearchedSelectedPos setSearchedSelectedPos;

    public interface SetSearchedSelectedPos{
        void getSearchedSelectedPos(int pos);
    }

    public SearchListRecyclerAdapter(Context mContext, ArrayList<SearchList> searchListArrayList,SetSearchedSelectedPos setSearchedSelectedPos) {
        this.mContext = mContext;
        this.searchListArrayList = searchListArrayList;
        this.setSearchedSelectedPos = setSearchedSelectedPos;
    }

    public class SearchListViewHolder extends RecyclerView.ViewHolder {
        ImageView searchFragmentDisplayEffectImageView;
        TextView searchFragmentDisplayListTextView;
        MaterialCardView searchFragmentListParentCardView;

        public SearchListViewHolder(@NonNull View itemView) {
            super(itemView);
            searchFragmentDisplayEffectImageView = (ImageView) itemView.findViewById(R.id.search_card_effect);
            searchFragmentDisplayListTextView = (TextView) itemView.findViewById(R.id.search_fragment_display_list_text_view);
            searchFragmentListParentCardView = (MaterialCardView) itemView.findViewById(R.id.search_item_list_parent_card_view);

            searchFragmentListParentCardView.setOnClickListener(view -> {
                if (setSearchedSelectedPos != null)
                    setSearchedSelectedPos.getSearchedSelectedPos(searchListArrayList.get(getAdapterPosition()).getContentType());
            });
        }
    }

    @NonNull
    @Override
    public SearchListRecyclerAdapter.SearchListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View rootView = inflater.inflate(R.layout.bottom_search_item_list_recycler_view_v2, parent, false);
        return new SearchListViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchListRecyclerAdapter.SearchListViewHolder holder, int position) {
        holder.searchFragmentDisplayListTextView.setText(searchListArrayList.get(position).getSearchName());
        holder.searchFragmentDisplayEffectImageView.setColorFilter(searchListArrayList.get(position).getConstraintLayoutColor());
    }

    @Override
    public int getItemCount() {
        return searchListArrayList.size();
    }
}
