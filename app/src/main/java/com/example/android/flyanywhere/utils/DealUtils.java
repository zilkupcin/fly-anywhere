package com.example.android.flyanywhere.utils;

import android.content.res.Resources;
import android.util.Log;

import com.example.android.flyanywhere.R;
import com.example.android.flyanywhere.models.Deal;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

/**
 * Created by baroc on 14/05/2018.
 */

public class DealUtils {

    private static final String TAG = "DealUtils";

    public static ArrayList<Deal> getDealsFromSnapshot(DataSnapshot snapshot) {
        ArrayList<Deal> dealList = new ArrayList<>();
        for (DataSnapshot deal : snapshot.getChildren()) {
            String dealId = deal.getKey();
            try {
                long createdDate = Long.parseLong(deal.child("createdDate").getValue().toString());
                Integer dealPrice = Integer.parseInt(deal.child("dealPrice").getValue().toString());
                Integer dealType = Integer.parseInt(deal.child("dealType").getValue().toString());
                String departureCity = deal.child("departureCity").getValue().toString();
                String destination = deal.child("destination").getValue().toString();
                String departureDates = deal.child("departureDates").getValue().toString();
                String description = deal.child("description").getValue().toString();
                String searchLink = deal.child("searchLink").getValue().toString();
                String returnDates;
                if (deal.child("returnDates").getValue() != null) {
                    returnDates = deal.child("returnDates").getValue().toString();
                } else {
                    returnDates = "";
                }
                String title = deal.child("title").getValue().toString();
                String postedBy = deal.child("postedBy").getValue().toString();
                String imageUrl = deal.child("imageUrl").getValue().toString();
                ArrayList<String> expiredReports = new ArrayList<>();
                if (deal.child("expiredReports").getValue() != null) {
                    for (DataSnapshot reports : deal.child("expiredReports").getChildren()) {
                        String currentReport = reports.getValue().toString();
                        expiredReports.add(currentReport);
                    }
                }
                ArrayList<String> dealTags = new ArrayList<>();

                for (DataSnapshot tag : deal.child("dealTags").getChildren()) {
                    String currentTag = tag.getValue().toString();
                    dealTags.add(currentTag);
                }
                dealList.add(new Deal(dealId,
                        departureCity,
                        destination,
                        departureDates,
                        returnDates,
                        title,
                        imageUrl,
                        dealTags,
                        description,
                        postedBy,
                        expiredReports,
                        dealType,
                        dealPrice,
                        createdDate,
                        searchLink));
            } catch (NullPointerException e) {
                Log.e(TAG, "getDealsFromSnapshot: ", e);
            }

        }

        return dealList;
    }

    public static Deal getSingleDealFromSnapshot(DataSnapshot snapshot) {
        try {
            String dealId = snapshot.getKey();
            long createdDate = Long.parseLong(snapshot.child("createdDate").getValue().toString());
            Integer dealPrice = Integer.parseInt(snapshot.child("dealPrice").getValue().toString());
            Integer dealType = Integer.parseInt(snapshot.child("dealType").getValue().toString());
            String departureCity = snapshot.child("departureCity").getValue().toString();
            String destination = snapshot.child("destination").getValue().toString();
            String departureDates = snapshot.child("departureDates").getValue().toString();
            String description = snapshot.child("description").getValue().toString();
            String searchLink = snapshot.child("searchLink").getValue().toString();
            String returnDates;
            if (snapshot.child("returnDates").getValue() != null) {
                returnDates = snapshot.child("returnDates").getValue().toString();
            } else {
                returnDates = "";
            }
            String title = snapshot.child("title").getValue().toString();
            String postedBy = snapshot.child("postedBy").getValue().toString();
            String imageUrl = snapshot.child("imageUrl").getValue().toString();
            ArrayList<String> expiredReports = new ArrayList<>();
            if (snapshot.child("expiredReports").getValue() != null) {
                for (DataSnapshot reports : snapshot.child("expiredReports").getChildren()) {
                    String currentReport = reports.getValue().toString();
                    expiredReports.add(currentReport);
                }
            }
            ArrayList<String> dealTags = new ArrayList<>();

            for (DataSnapshot tag : snapshot.child("dealTags").getChildren()) {
                String currentTag = tag.getValue().toString();
                dealTags.add(currentTag);
            }
            return new Deal(dealId,
                    departureCity,
                    destination,
                    departureDates,
                    returnDates,
                    title,
                    imageUrl,
                    dealTags,
                    description,
                    postedBy,
                    expiredReports,
                    dealType,
                    dealPrice,
                    createdDate,
                    searchLink);
        } catch (NullPointerException e) {
            Log.e(TAG, Resources.getSystem().getString(R.string.method_getSingleDealFromSnapshot), e);
        }
        return getEmptyDeal();
    }

    public static Deal getEmptyDeal() {
        return new Deal("",
                "",
                "",
                "",
                "",
                "",
                "",
                new ArrayList<String>(),
                "",
                "",
                new ArrayList<String>(),
                0,
                0,
                0,
                "");
    }

    public static ArrayList<Deal> filterDealsByRelevancy(
            ArrayList<String> subscribedTags, ArrayList<Deal> fullDealList) {
        ArrayList<Deal> filteredDealList = new ArrayList<>();
        for (String subscribedTag : subscribedTags) {
            for (Deal deal : fullDealList) {
                for (String dealTag : deal.getDealTags()) {
                    if (subscribedTag.equals(dealTag)) {
                        filteredDealList.add(deal);
                    }
                }
            }
        }
        return removeDuplicates(filteredDealList);
    }

    public static ArrayList<Deal> filterDealsBySaved(
            ArrayList<String> savedDealIds, ArrayList<Deal> fullDealList) {
        ArrayList<Deal> filteredDealList = new ArrayList<>();
        for (String savedDealId : savedDealIds) {
            for (Deal deal : fullDealList) {
                if (deal.getDealId().equals(savedDealId)) {
                    filteredDealList.add(deal);
                }
            }
        }
        return filteredDealList;
    }

    public static ArrayList<Deal> removeDuplicates(ArrayList<Deal> filteredList) {
        ArrayList<Deal> finalList = new ArrayList<>();
        for (int i = 0; i < filteredList.size(); i++) {
            if (i == 0) {
                finalList.add(filteredList.get(i));
            } else {
                boolean duplicateFound = false;
                for (Deal finalListDeal : finalList) {
                    if (finalListDeal.getDealId().equals(filteredList.get(i).getDealId())) {
                        duplicateFound = true;
                    }
                }
                if (!duplicateFound) {
                    finalList.add(filteredList.get(i));
                }
            }
        }
        return finalList;
    }

    public static Integer getNewDealCount(ArrayList<Deal> dealList, long lastVisitedTime) {
        Integer newDealCount = 0;
        for (Deal deal : dealList) {
            if (deal.getCreatedDate() > lastVisitedTime) {
                newDealCount++;
            }
        }
        return newDealCount;
    }
}
