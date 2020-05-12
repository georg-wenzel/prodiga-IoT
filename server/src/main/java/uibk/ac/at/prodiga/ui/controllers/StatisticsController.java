package uibk.ac.at.prodiga.ui.controllers;

import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.services.ProductivityAnalysisService;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private HashMap<String,String> colorByCategory = new HashMap<String, String>(){{

    }};
    private HashMap<String,String> defaultColor = new HashMap<String,String>(){{
        put("Pause / Vacation","#e02365");
        //put("Conceptualizing","#2D8EE3");
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
        createDailyAnalysisPie();
        createWeeklyAnalysisPie();
        createMonthlyAnalysisPie();
        createWeeklyTeamAnalysisPie();
        createMonthlyTeamAnalysisPie();
        createMonthlyDepartmentAnalysisPie();
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

    private void createWeeklyAnalysisPie() {
        weeklyAnalysisPie = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        weeklyAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }

            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);

        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        weeklyAnalysisPie.setData(data);
        //weeklyAnalysisPie.setExtender("skinPie");

        weeklyAnalysisBar.addSeries(categories);
        weeklyAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = weeklyAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = weeklyAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        weeklyAnalysisBar.setExtender("skinBar");
    }

    private void createDailyAnalysisPie() {
        dailyAnalysisPie = new PieChartModel();
        dailyAnalysisBar = new BarChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        ChartSeries categories = new ChartSeries();
        Map<BookingCategory, Long> map = productivityAnalysisService.getStatisicForCurrentUserByDay(1);
        for (Map.Entry<BookingCategory, Long> entry : map.entrySet()) {
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        dailyAnalysisPie.setData(data);
        //dailyAnalysisPie.setExtender("skinPie");

        dailyAnalysisBar.addSeries(categories);
        dailyAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = dailyAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = dailyAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        dailyAnalysisBar.setExtender("skinBar");
    }

    private void createMonthlyAnalysisPie() {
        monthlyAnalysisPie = new PieChartModel();
        monthlyAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyAnalysisPie.setData(data);
        //monthlyAnalysisPie.setExtender("skinPie");

        monthlyAnalysisBar.addSeries(categories);
        monthlyAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = monthlyAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = monthlyAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        monthlyAnalysisBar.setExtender("skinBar");
    }

    private void createWeeklyTeamAnalysisPie() {
        weeklyTeamAnalysisPie = new PieChartModel();
        weeklyTeamAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        weeklyTeamAnalysisPie.setData(data);
        //weeklyTeamAnalysisPie.setExtender("skinPie");

        weeklyTeamAnalysisBar.addSeries(categories);
        weeklyTeamAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = weeklyTeamAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = weeklyTeamAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        weeklyTeamAnalysisBar.setExtender("skinBar");
    }

    private void createMonthlyTeamAnalysisPie() {
        monthlyTeamAnalysisPie = new PieChartModel();
        monthlyTeamAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyTeamAnalysisPie.setData(data);
        //monthlyTeamAnalysisPie.setExtender("skinPie");

        monthlyTeamAnalysisBar.addSeries(categories);
        monthlyTeamAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = monthlyTeamAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = monthlyTeamAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        monthlyTeamAnalysisBar.setExtender("skinBar");
    }

    private void createMonthlyDepartmentAnalysisPie() {
        monthlyDepartmentAnalysisPie = new PieChartModel();
        monthlyDepartmentAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();

        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();
        List<String> mycolors = new ArrayList<>();
        List<Number> hours = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForDepartmenByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            hours.add(entry.getValue());
            labels.add(entry.getKey().getName());
            if(colorByCategory != null && colorByCategory.containsKey(entry.getKey().getName())){
                mycolors.add(colorByCategory.get(entry.getKey().getName()));
            }
            else {
                mycolors.add(defaultColor.get(entry.getKey().getName()));
            }
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dataSet.setData(hours);
        dataSet.setBackgroundColor(mycolors);
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        monthlyDepartmentAnalysisPie.setData(data);
        //monthlyDepartmentAnalysisPie.setExtender("skinPie");

        monthlyDepartmentAnalysisBar.addSeries(categories);
        monthlyDepartmentAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = monthlyDepartmentAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = monthlyDepartmentAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        monthlyDepartmentAnalysisBar.setExtender("skinBar");
    }



}
