package com.example.android.flyanywhere.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.ui.TagActivity;
import com.example.android.flyanywhere.models.Deal;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by baroc on 03/05/2018.
 */

public class DealAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_INFO = 0;
    private static final int TYPE_DEAL = 1;
    private static final int FRAGMENT_RELEVANT_DEALS = 1;
    private static final int FRAGMENT_SAVED_DEALS = 0;
    private static final int FRAGMENT_ALL_DEALS = 2;

    private static final int DEAL_TYPE_ONE_WAY = 0;
    private static final int DEAL_TYPE_RETURN = 1;
    private static final int DEAL_TYPE_AROUND_THE_WORLD = 2;
    private static final int DEAL_TYPE_MULTI_DESTINATION = 3;

    private static final String DEAL_TYPE_TEXT_ONE_WAY = "ONE WAY";
    private static final String DEAL_TYPE_TEXT_RETURN = "RETURN";
    private static final String DEAL_TYPE_TEXT_AROUND_THE_WORLD = "AROUND THE WORLD";
    private static final String DEAL_TYPE_TEXT_MULTI_DESTINATION = "MULTI DESTINATION";
    private static final String DEAL_PRICE_FROM = "FROM £";

    private int fragmentType;
    private ArrayList<Deal> dealList = new ArrayList<>();
    private OnListItemClickListener mOnListItemClickListener;

    private boolean contentLoaded;

    public DealAdapter(int fragmentType, OnListItemClickListener onListItemClickListener) {
        this.fragmentType = fragmentType;
        mOnListItemClickListener = onListItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DEAL) {
            View dealView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.deal_item, parent, false);
            return new DealViewHolder(dealView);
        } else {
            View infoView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.info_item, parent, false);
            return new InfoViewHolder(infoView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_INFO:
                InfoViewHolder infoViewHolder = (InfoViewHolder) holder;
                if (contentLoaded) {
                    infoViewHolder.mProgressBar.setVisibility(View.GONE);
                    infoViewHolder.infoLayout.setVisibility(View.VISIBLE);
                }
                break;
            case TYPE_DEAL:
                if (!contentLoaded) {
                    holder.itemView.setVisibility(View.GONE);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    Integer dealPosition = position - 1;
                    DealViewHolder dealViewHolder = (DealViewHolder) holder;
                    dealViewHolder.dealTitleTextView
                            .setText(dealList.get(dealPosition).getTitle());
                    dealViewHolder.departureCitiesTextView
                            .setText(dealList.get(dealPosition).getDepartureCity());
                    dealViewHolder.arrivalCitiesTextView
                            .setText(dealList.get(dealPosition).getDestination());
                    dealViewHolder.oneWayDatesTextView
                            .setText(dealList.get(dealPosition).getDepartureDates());

                    if (dealList.get(dealPosition).getDealType() == DEAL_TYPE_ONE_WAY) {
                        dealViewHolder.returnDatesIcon
                                .setVisibility(View.GONE);
                        dealViewHolder.returnDatesTextView
                                .setVisibility(View.GONE);
                        dealViewHolder.dealTypeTextView
                                .setText(DEAL_TYPE_TEXT_ONE_WAY);
                    } else if (dealList.get(dealPosition).getDealType() == DEAL_TYPE_RETURN) {
                        dealViewHolder.returnDatesTextView
                                .setText(dealList.get(dealPosition).getReturnDates());
                        dealViewHolder.dealTypeTextView
                                .setText(DEAL_TYPE_TEXT_RETURN);
                    } else if (dealList.get(dealPosition).getDealType() == DEAL_TYPE_AROUND_THE_WORLD) {
                        dealViewHolder.dealTypeTextView
                                .setText(DEAL_TYPE_TEXT_AROUND_THE_WORLD);
                        dealViewHolder.returnDatesTextView
                                .setText(dealList.get(dealPosition).getReturnDates());
                    } else if (dealList.get(dealPosition).getDealType() == DEAL_TYPE_MULTI_DESTINATION) {
                        dealViewHolder.dealTypeTextView
                                .setText(DEAL_TYPE_TEXT_MULTI_DESTINATION);
                        dealViewHolder.returnDatesTextView
                                .setText(dealList.get(dealPosition).getReturnDates());
                    }
                    if (dealList.get(dealPosition).getExpiredReports().size() >= 5) {
                        dealViewHolder.priceButton.setText(R.string.button_text_expired);
                        dealViewHolder.priceButton.setEnabled(false);
                    } else {
                        dealViewHolder.priceButton.setText(
                                DEAL_PRICE_FROM + dealList.get(dealPosition).getDealPrice());
                    }
                    Picasso.get()
                            .load(dealList.get(dealPosition).getImageUrl())
                            .placeholder(R.drawable.placeholder)
                            .into(dealViewHolder.dealHeaderImage);
                    break;
                }
        }
    }

    @Override
    public int getItemCount() {
        if (dealList == null) {
            return 0;
        } else {
            return dealList.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_INFO;
        } else {
            return TYPE_DEAL;
        }
    }

    public void setDealList(ArrayList<Deal> dList) {
        dealList = dList;
        contentLoaded = true;
        notifyDataSetChanged();
    }

    public interface OnListItemClickListener {
        void onListItemClicked(int position);
    }


    class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_deal_header_image)
        ImageView dealHeaderImage;
        @BindView(R.id.iv_one_way_dates_icon)
        ImageView oneWayDatesIcon;
        @BindView(R.id.iv_return_dates_icon)
        ImageView returnDatesIcon;
        @BindView(R.id.tv_deal_type)
        TextView dealTypeTextView;
        @BindView(R.id.tv_deal_title)
        TextView dealTitleTextView;
        @BindView(R.id.tv_departure_cities)
        TextView departureCitiesTextView;
        @BindView(R.id.tv_arrival_cities)
        TextView arrivalCitiesTextView;
        @BindView(R.id.tv_one_way_dates)
        TextView oneWayDatesTextView;
        @BindView(R.id.tv_return_dates)
        TextView returnDatesTextView;
        @BindView(R.id.btn_price_button)
        Button priceButton;

        public DealViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            priceButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnListItemClickListener.onListItemClicked(getAdapterPosition() - 1);
        }
    }

    class InfoViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_tags_button)
        TextView tagsButton;
        @BindView(R.id.tv_info)
        TextView infoTextView;
        @BindView(R.id.ll_info_linear_layout)
        LinearLayout infoLayout;
        @BindView(R.id.pb_info_progress_bar)
        ProgressBar mProgressBar;

        public InfoViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (fragmentType == FRAGMENT_RELEVANT_DEALS) {
                tagsButton = (TextView) itemView.findViewById(R.id.tv_tags_button);
                tagsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, TagActivity.class);
                        context.startActivity(intent);
                    }
                });

                infoTextView.setText(R.string.info_item_relevant_deals);

            } else if (fragmentType == FRAGMENT_SAVED_DEALS) {
                infoTextView.setText(R.string.info_item_saved_deals);
                tagsButton.setVisibility(View.GONE);
            } else if (fragmentType == FRAGMENT_ALL_DEALS) {
                infoTextView.setText(R.string.info_item_alL_deals);
                tagsButton.setVisibility(View.GONE);
            }
        }
    }
}
