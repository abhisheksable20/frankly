package com.sakesnake.frankly.home.postfeeds.homePostFeedsAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.Constants;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.BlogOrNewsAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.DreamsOrIdeasAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.ImagesOrMemesHomeAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.QuotesQuestionHomeAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.StoryOrPoemsAdapter;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.ImagesHomeAdapterCallback;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;

import java.util.List;

public class HomeFeedsAdapter extends RecyclerView.Adapter<HomeFeedsAdapter.HomeFeedsViewHolder> {

    private Context mContext;

    private List<HomeFeedsModel> homeFeedsModelList;

    private ImagesHomeAdapterCallback imagesHomeAdapterCallback;

    private PostFeedsCallback postFeedsCallback;

    private int contentType;

    private final RecyclerView.RecycledViewPool recycledViewPool = new RecyclerView.RecycledViewPool();

    public HomeFeedsAdapter(Context mContext, List<HomeFeedsModel> homeFeedsModelList, int contentType,
                            ImagesHomeAdapterCallback imageOrMemeCallback, PostFeedsCallback postFeedsCallback) {
        this.mContext = mContext;
        this.homeFeedsModelList = homeFeedsModelList;
        this.contentType = contentType;
        this.imagesHomeAdapterCallback = imageOrMemeCallback;
        this.postFeedsCallback = postFeedsCallback;
    }

    public class HomeFeedsViewHolder extends RecyclerView.ViewHolder {
        TextView contentTitleTextView;
        RecyclerView contentRecyclerView;
        public HomeFeedsViewHolder(@NonNull View itemView) {
            super(itemView);
            contentTitleTextView = (TextView) itemView.findViewById(R.id.home_feed_content_title);
            contentRecyclerView = (RecyclerView) itemView.findViewById(R.id.home_feed_content_recycler_view);
        }
    }

    @NonNull
    @Override
    public HomeFeedsAdapter.HomeFeedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeFeedsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.home_feeds_contents,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeFeedsAdapter.HomeFeedsViewHolder holder, int position) {
        HomeFeedsModel homeFeedsModel = homeFeedsModelList.get(position);

        holder.contentTitleTextView.setText(homeFeedsModel.getFeedTitle());

        switch (contentType){
            case Constants.UPLOAD_IMAGE:{
                ImagesOrMemesHomeAdapter imagesOrMemesHomeAdapter = new ImagesOrMemesHomeAdapter(homeFeedsModel.getFeedData(),
                        mContext,imagesHomeAdapterCallback,true);

                holder.contentRecyclerView.setAdapter(imagesOrMemesHomeAdapter);
                break;
            }
            case Constants.UPLOAD_DREAM:{
                DreamsOrIdeasAdapter dreamsOrIdeasAdapter = new DreamsOrIdeasAdapter(homeFeedsModel.getFeedData(),
                        R.layout.home_ideas_and_dream_list_view,mContext,postFeedsCallback,true);

                holder.contentRecyclerView.setAdapter(dreamsOrIdeasAdapter);
                break;
            }
            case Constants.UPLOAD_BLOG:{
                BlogOrNewsAdapter blogOrNewsAdapter = new BlogOrNewsAdapter(homeFeedsModel.getFeedData(),mContext,
                        R.layout.home_blog_and_news_list_view,postFeedsCallback,true);

                holder.contentRecyclerView.setAdapter(blogOrNewsAdapter);
                break;
            }
            case Constants.UPLOAD_QUOTE_AND_THOUGHTS:{
                QuotesQuestionHomeAdapter homeAdapter;
                if (homeFeedsModel.getContentType() == Constants.UPLOAD_QUOTE_AND_THOUGHTS) {
                    homeAdapter = new QuotesQuestionHomeAdapter(homeFeedsModel.getFeedData(), mContext,
                            R.layout.home_quotes_list_view, postFeedsCallback,true);

                }
                else{
                    homeAdapter = new QuotesQuestionHomeAdapter(homeFeedsModel.getFeedData(), mContext,
                            R.layout.home_questions_list_view, postFeedsCallback,true);

                }
                holder.contentRecyclerView.setAdapter(homeAdapter);
                break;
            }
            case Constants.UPLOAD_STORY:{
                StoryOrPoemsAdapter storyOrPoemsAdapter = new StoryOrPoemsAdapter(homeFeedsModel.getFeedData(),
                        R.layout.home_poem_and_story_list_view,mContext,postFeedsCallback,true);

                holder.contentRecyclerView.setAdapter(storyOrPoemsAdapter);
                break;
            }

            default:
                return;
        }

        holder.contentRecyclerView.setLayoutManager(new LinearLayoutManager(mContext,LinearLayoutManager.HORIZONTAL,false));
        holder.contentRecyclerView.setRecycledViewPool(recycledViewPool);
    }

    @Override
    public int getItemCount() {
        return homeFeedsModelList.size();
    }

    // Whole data will be changed here
    public void updateData(final List<HomeFeedsModel> newData){
        homeFeedsModelList.clear();
        homeFeedsModelList.addAll(newData);
        notifyDataSetChanged();
    }
}
