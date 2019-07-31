package com.example.retrofitexample.BoxOffice.Item;

//JSON 첫 객체 클래스
public class BoxOfficeResult {

    private String yearWeekTime;
    private DailyBoxOfficeList[] dailyBoxOfficeList;

    public String getYearWeekTime() {
        return yearWeekTime;
    }

    public DailyBoxOfficeList[] getDailyBoxOfficeList() {
        return dailyBoxOfficeList;
    }

}
