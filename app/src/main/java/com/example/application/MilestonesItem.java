package com.example.application;

public class MilestonesItem {

    //PROPERTIES
    private int order;
    private String timePlus, timeMilestone;


    //CONSTRUCTOR
    public MilestonesItem(int order, String timePlus, String timeMilestone) {
        this.order = order;
        this.timePlus = timePlus;
        this.timeMilestone = timeMilestone;
    }


    //FUNCTIONS GET AND SET FOR ITEM
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getTimePlus() {
        return timePlus;
    }

    public void setTimePlus(String timePlus) {
        this.timePlus = timePlus;
    }

    public String getTimeMilestone() {
        return timeMilestone;
    }

    public void setTimeMilestone(String timeMilestone) {
        this.timeMilestone = timeMilestone;
    }

    public static MilestonesItem createMilestonesItem(int orderItem,String timePlusItem,String timeMilestone){
        return new MilestonesItem(orderItem,timePlusItem,timeMilestone);
    }
}
