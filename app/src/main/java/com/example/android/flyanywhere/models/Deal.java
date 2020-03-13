package com.example.android.flyanywhere.models;

import java.util.ArrayList;

/**
 * Created by baroc on 02/05/2018.
 */

public class Deal {
    private String dealId;
    private String searchLink;
    private String departureCity;
    private String destination;
    private String departureDates;
    private String returnDates;
    private String title;
    private String imageUrl;
    private String description;
    private String postedBy;
    private ArrayList<String> dealTags = new ArrayList<>();
    private ArrayList<String> expiredReports = new ArrayList<>();
    private Integer dealType;
    private Integer dealPrice;
    private long createdDate;


    public Deal(String dId, String departureCity, String destination, String departureDates,
                String returnDates, String title, String imageUrl, ArrayList<String> dTags,
                String description, String postedBy, ArrayList<String> eReports, Integer dealType,
                Integer price, long date, String sLink) {
        this.dealId = dId;
        this.departureCity = departureCity;
        this.destination = destination;
        this.departureDates = departureDates;
        this.returnDates = returnDates;
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.postedBy = postedBy;
        this.expiredReports = eReports;
        this.dealType = dealType;
        this.dealPrice = price;
        this.createdDate = date;
        this.dealTags = dTags;
        this.searchLink = sLink;
    }

    public String getSearchLink() {
        return searchLink;
    }

    public void setSearchLink(String searchLink) {
        this.searchLink = searchLink;
    }

    public ArrayList<String> getDealTags() {
        return dealTags;
    }

    public void setDealTags(ArrayList<String> dealTags) {
        this.dealTags = dealTags;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public Integer getDealPrice() {
        return dealPrice;
    }

    public void setDealPrice(Integer dealPrice) {
        this.dealPrice = dealPrice;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureDates() {
        return departureDates;
    }

    public void setDepartureDates(String departureDates) {
        this.departureDates = departureDates;
    }

    public String getReturnDates() {
        return returnDates;
    }

    public void setReturnDates(String returnDates) {
        this.returnDates = returnDates;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostedBy() {
        return postedBy;
    }

    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    public ArrayList<String> getExpiredReports() {
        return expiredReports;
    }

    public void setExpiredReports(ArrayList<String> expiredReports) {
        this.expiredReports = expiredReports;
    }

    public Integer getDealType() {
        return dealType;
    }

    public void setDealType(Integer dealType) {
        this.dealType = dealType;
    }
}


