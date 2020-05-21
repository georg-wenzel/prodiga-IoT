package uibk.ac.at.prodiga.ui.controllers;


import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.services.ProductivityAnalysisService;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.*;

@Component
@Scope("view")
@RequestScoped
@SuppressWarnings("Duplicates")
public class StatisticsController implements Serializable {
    private final ProductivityAnalysisService productivityAnalysisService;
    private final BookingCategoryController bookingCategoryController;
    private PieChartModel dailyAnalysisPie;
    private PieChartModel weeklyAnalysisPie;
    private PieChartModel monthlyAnalysisPie;
    private PieChartModel weeklyTeamAnalysisPie;
    private PieChartModel monthlyTeamAnalysisPie;
    private PieChartModel monthlyDepartmentAnalysisPie;

    private BarChartModel dailyAnalysisBar;
    private BarChartModel weeklyAnalysisBar;
    private BarChartModel monthlyAnalysisBar;
    private BarChartModel weeklyTeamAnalysisBar;
    private BarChartModel monthlyTeamAnalysisBar;
    private BarChartModel monthlyDepartmentAnalysisBar;

    private Date selectedDate;
    private int backstepDays = 1;
    private int backstepWeeks = 1;
    private int backstepMonths = 1;

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void doSaveBackstep(){
        Date today = new Date();
        backstepDays = (int)(today.getTime() - this.selectedDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        backstepWeeks = (backstepDays / 7) + 1;
        backstepMonths = (int) backstepWeeks / 4 + 1;
        init();
    }

    private HashMap<String,String> colorByCategory = new HashMap<String, String>(){{

    }};
    private HashMap<String,String> defaultColor = new HashMap<String,String>(){{
        put("Pause / Vacation","#e02365");
        put("Conceptualizing","#2D8EE3");
        put("Design","#44be2c");
        put("Implementation","#eeb210");
        put("Testing","#AB44BC");
        put("Documentation","#2162b0");
        put("Debugging","#FFD000");
        put("Meeting","#ff2c00");
        put("Customer Support","#00d0ff");
        put("Education and Training","#b9ff00");
        put("Project Management","#eb07c5");
        put("Other","#1a8f0a");
    }};

    private String actualColor;
    private String bookingName;

    public void doSaveColorByCategory(String bookingName, String actualColor){
        this.bookingName = bookingName;
        String pref = "#";
        actualColor = pref.concat(actualColor);
        this.actualColor = actualColor;
        colorByCategory.put(bookingName,actualColor);
        init();
    }
    public String getActualColor() {
        return this.actualColor;
    }
    public void setActualColor(String actualColor) {
        this.actualColor = actualColor;
    }
    public String getBookingName() {
        return bookingName;
    }
    public void setBookingName(String bookingName) {
        this.bookingName = bookingName;
    }

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService, BookingCategoryController bookingCategoryController) {
        this.productivityAnalysisService = productivityAnalysisService;
        this.bookingCategoryController = bookingCategoryController;

    }

    @PostConstruct
    public void init() {
        createDailyAnalysisPie(backstepDays);
        createWeeklyAnalysisPie(backstepWeeks);
        createMonthlyAnalysisPie(backstepMonths);
        createWeeklyTeamAnalysisPie(backstepWeeks);
        createMonthlyTeamAnalysisPie(backstepMonths);
        createMonthlyDepartmentAnalysisPie(backstepMonths);
    }

    public PieChartModel getDailyAnalysisPie() {
        return dailyAnalysisPie;
    }

    public PieChartModel getWeeklyAnalysisPie() {
        return weeklyAnalysisPie;
    }

    public PieChartModel getMonthlyAnalysisPie() {
        return monthlyAnalysisPie;
    }

    public PieChartModel getWeeklyTeamAnalysisPie() {
        return weeklyTeamAnalysisPie;
    }

    public PieChartModel getMonthlyTeamAnalysisPie() {
        return monthlyTeamAnalysisPie;
    }

    public PieChartModel getMonthlyDepartmentAnalysisPie() {
        return monthlyDepartmentAnalysisPie;
    }

    public BarChartModel getDailyAnalysisBar() {
        return dailyAnalysisBar;
    }

    public BarChartModel getWeeklyAnalysisBar() {
        return weeklyAnalysisBar;
    }

    public BarChartModel getMonthlyAnalysisBar() {
        return monthlyAnalysisBar;
    }

    public BarChartModel getWeeklyTeamAnalysisBar() {
        return weeklyTeamAnalysisBar;
    }

    public BarChartModel getMonthlyTeamAnalysisBar() {
        return monthlyTeamAnalysisBar;
    }

    public BarChartModel getMonthlyDepartmentAnalysisBar() {
        return monthlyDepartmentAnalysisBar;
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForCurrentUserByDay(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByDay(1).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForCurrentUserByWeek(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByWeek(1).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForCurrentUserByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByMonth(1).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisicForTeamByWeek(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForTeamByWeek(1).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisicForTeamByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForTeamByMonth(1).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisicForDepartmenByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForDepartmenByMonth(1).entrySet();
        return new ArrayList<>(set);
    }


    private void createWeeklyAnalysisPie(int backstepWeek) {
        weeklyAnalysisPie = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        weeklyAnalysisBar = new BarChartModel();

        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("My First Dataset");
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByWeek(backstepWeek);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }

            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);

        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        weeklyAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        weeklyAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }

    private void createDailyAnalysisPie(int backstepDay) {
        dailyAnalysisPie = new PieChartModel();
        dailyAnalysisBar = new BarChartModel();
        ChartData data = new ChartData();

        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("My First Dataset");
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        Map<BookingCategory, Long> map = productivityAnalysisService.getStatisicForCurrentUserByDay(backstepDay);
        for (Map.Entry<BookingCategory, Long> entry : map.entrySet()) {
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        dailyAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        dailyAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }

    private void createMonthlyAnalysisPie(int backstepMonth) {
        monthlyAnalysisPie = new PieChartModel();
        monthlyAnalysisBar = new BarChartModel();
        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        barDataSet.setLabel("My First Dataset");
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        monthlyAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }

    private void createWeeklyTeamAnalysisPie(int backstepWeek) {
        weeklyTeamAnalysisPie = new PieChartModel();
        weeklyTeamAnalysisBar = new BarChartModel();
        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByWeek(backstepWeek);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        weeklyTeamAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        weeklyTeamAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
        barDataSet.setLabel("Categories");
    }

    private void createMonthlyTeamAnalysisPie(int backstepMonth) {
        monthlyTeamAnalysisPie = new PieChartModel();
        monthlyTeamAnalysisBar = new BarChartModel();
        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();


        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyTeamAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        monthlyTeamAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }

    private void createMonthlyDepartmentAnalysisPie(int backstepMonth) {
        monthlyDepartmentAnalysisPie = new PieChartModel();
        monthlyDepartmentAnalysisBar = new BarChartModel();
        ChartData dataBar = new ChartData();
        BarChartDataSet barDataSet = new BarChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labelsBar = new ArrayList<>();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForDepartmenByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            values.add(entry.getValue());
            labelsBar.add(entry.getKey().getName());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyDepartmentAnalysisPie.setData(data);

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        monthlyDepartmentAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }



}
