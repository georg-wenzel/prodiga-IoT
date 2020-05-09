package uibk.ac.at.prodiga.ui.controllers;

import org.primefaces.model.chart.*;
import org.primefaces.model.chart.PieChartModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.BookingCategory;
import uibk.ac.at.prodiga.services.ProductivityAnalysisService;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.util.Map;

@Component
@Scope("view")
@RequestScoped
@SuppressWarnings("Duplicates")
public class StatisticsController implements Serializable {
    private final ProductivityAnalysisService productivityAnalysisService;
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

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
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
        weeklyAnalysisBar = new BarChartModel();
        ChartSeries categories = new ChartSeries();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        weeklyAnalysisPie.setTitle("User: Last week productivity analysis");
        weeklyAnalysisPie.setLegendPosition("e");
        weeklyAnalysisPie.setFill(false);
        weeklyAnalysisPie.setShowDataLabels(true);
        weeklyAnalysisPie.setDiameter(200);
        weeklyAnalysisPie.setExtender("skinPie");

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
        ChartSeries categories = new ChartSeries();
        Map<BookingCategory, Long> map = productivityAnalysisService.getStatisicForCurrentUserByDay(1);
        for (Map.Entry<BookingCategory, Long> entry : map.entrySet()) {
            dailyAnalysisPie.set(entry.getKey().getName(), entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        dailyAnalysisPie.setTitle("User: Last day productivity analysis");
        dailyAnalysisPie.setLegendPosition("e");
        dailyAnalysisPie.setFill(false);
        dailyAnalysisPie.setShowDataLabels(true);
        dailyAnalysisPie.setDiameter(200);
        dailyAnalysisPie.setExtender("skinPie");

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
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        monthlyAnalysisPie.setTitle("User: Last Months productivity analysis");
        monthlyAnalysisPie.setLegendPosition("e");
        monthlyAnalysisPie.setFill(false);
        monthlyAnalysisPie.setShowDataLabels(true);
        monthlyAnalysisPie.setDiameter(200);
        monthlyAnalysisPie.setExtender("skinPie");

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
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        weeklyTeamAnalysisPie.setTitle("Team: Last weeks team productivity analysis");
        weeklyTeamAnalysisPie.setLegendPosition("e");
        weeklyTeamAnalysisPie.setFill(false);
        weeklyTeamAnalysisPie.setShowDataLabels(true);
        weeklyTeamAnalysisPie.setDiameter(200);
        weeklyTeamAnalysisPie.setExtender("skinPie");

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
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        monthlyTeamAnalysisPie.setTitle("Team: Last Months team productivity analysis");
        monthlyTeamAnalysisPie.setLegendPosition("e");
        monthlyTeamAnalysisPie.setFill(false);
        monthlyTeamAnalysisPie.setShowDataLabels(true);
        monthlyTeamAnalysisPie.setDiameter(200);
        monthlyTeamAnalysisPie.setExtender("skinPie");

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
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForDepartmenByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyDepartmentAnalysisPie.set(entry.getKey().getName(),entry.getValue());
            categories.set(entry.getKey().getName(), entry.getValue());
        }
        monthlyDepartmentAnalysisPie.setTitle("Department: Last Months department productivity analysis");
        monthlyDepartmentAnalysisPie.setLegendPosition("e");
        monthlyDepartmentAnalysisPie.setFill(false);
        monthlyDepartmentAnalysisPie.setShowDataLabels(true);
        monthlyDepartmentAnalysisPie.setDiameter(200);
        monthlyDepartmentAnalysisPie.setExtender("skinPie");

        monthlyDepartmentAnalysisBar.addSeries(categories);
        monthlyDepartmentAnalysisBar.setTitle("Bar Chart");

        Axis xAxis = monthlyDepartmentAnalysisBar.getAxis(AxisType.X);
        xAxis.setLabel("Category");

        Axis yAxis = monthlyDepartmentAnalysisBar.getAxis(AxisType.Y);
        yAxis.setLabel("Time");

        monthlyDepartmentAnalysisBar.setExtender("skinBar");
    }



}
