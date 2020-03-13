package com.example.android.flyanywhere.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.models.Tag;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by baroc on 02/05/2018.
 */


public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private ArrayList<Tag> tagList = new ArrayList<Tag>();
    private OnListItemClickListener mListItemClickListener;

    public TagAdapter(OnListItemClickListener mListItemClickListener) {
        this.mListItemClickListener = mListItemClickListener;
    }

    @Override
    public int getItemCount() {
        if (tagList != null) {
            return tagList.size();
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_item, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        if (tagList.get(position).getTagIsSelected()) {
            holder.tagSelectedIcon.setImageDrawable(holder.itemView
                    .getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_tag_selected));
        } else {
            holder.tagSelectedIcon.setImageDrawable(holder.itemView
                    .getContext()
                    .getResources()
                    .getDrawable(R.drawable.ic_tag));
        }
        holder.tagName.setText(tagList.get(position).getTagName());
    }

    public void setTagList(ArrayList<Tag> tList) {
        tagList = tList;
        notifyDataSetChanged();
    }

    public interface OnListItemClickListener {
        void onListItemClicked(int position);
    }

    class TagViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_is_selected)
        ImageView tagSelectedIcon;
        @BindView(R.id.tv_tag_name)
        TextView tagName;

        public TagViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListItemClickListener.onListItemClicked(getAdapterPosition());
        }
    }
}
