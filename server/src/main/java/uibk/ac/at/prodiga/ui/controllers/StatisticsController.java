package uibk.ac.at.prodiga.ui.controllers;

import org.mapstruct.Named;
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
public class StatisticsController implements Serializable {
    private final ProductivityAnalysisService productivityAnalysisService;
    private PieChartModel dailyAnalysisPie;
    private PieChartModel weeklyAnalysisPie;
    private PieChartModel monthlyAnalysisPie;
    private PieChartModel weeklyTeamAnalysisPie;
    private PieChartModel monthlyTeamAnalysisPie;
    private PieChartModel monthlyDepartmentAnalysisPie;

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

    private void createWeeklyAnalysisPie() {
        weeklyAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        weeklyAnalysisPie.setTitle("User: Last week productivity analysis");
        weeklyAnalysisPie.setLegendPosition("e");
        weeklyAnalysisPie.setFill(false);
        weeklyAnalysisPie.setShowDataLabels(true);
        weeklyAnalysisPie.setDiameter(200);
        weeklyAnalysisPie.setExtender("skinPie");
    }

    private void createDailyAnalysisPie() {
        dailyAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByDay(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            dailyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        dailyAnalysisPie.setTitle("User: Last day productivity analysis");
        dailyAnalysisPie.setLegendPosition("e");
        dailyAnalysisPie.setFill(false);
        dailyAnalysisPie.setShowDataLabels(true);
        dailyAnalysisPie.setDiameter(200);
        dailyAnalysisPie.setExtender("skinPie");
    }

    private void createMonthlyAnalysisPie() {
        monthlyAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForCurrentUserByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        monthlyAnalysisPie.setTitle("User: Last Months productivity analysis");
        monthlyAnalysisPie.setLegendPosition("e");
        monthlyAnalysisPie.setFill(false);
        monthlyAnalysisPie.setShowDataLabels(true);
        monthlyAnalysisPie.setDiameter(200);
        monthlyAnalysisPie.setExtender("skinPie");
    }

    private void createWeeklyTeamAnalysisPie() {
        weeklyTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByWeek(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        weeklyTeamAnalysisPie.setTitle("Team: Last weeks team productivity analysis");
        weeklyTeamAnalysisPie.setLegendPosition("e");
        weeklyTeamAnalysisPie.setFill(false);
        weeklyTeamAnalysisPie.setShowDataLabels(true);
        weeklyTeamAnalysisPie.setDiameter(200);
        weeklyTeamAnalysisPie.setExtender("skinPie");
    }

    private void createMonthlyTeamAnalysisPie() {
        monthlyTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForTeamByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        monthlyTeamAnalysisPie.setTitle("Team: Last Months team productivity analysis");
        monthlyTeamAnalysisPie.setLegendPosition("e");
        monthlyTeamAnalysisPie.setFill(false);
        monthlyTeamAnalysisPie.setShowDataLabels(true);
        monthlyTeamAnalysisPie.setDiameter(200);
        monthlyTeamAnalysisPie.setExtender("skinPie");
    }

    private void createMonthlyDepartmentAnalysisPie() {
        monthlyDepartmentAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getStatisicForDepartmenByMonth(1);
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            monthlyDepartmentAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        monthlyDepartmentAnalysisPie.setTitle("Department: Last Months department productivity analysis");
        monthlyDepartmentAnalysisPie.setLegendPosition("e");
        monthlyDepartmentAnalysisPie.setFill(false);
        monthlyDepartmentAnalysisPie.setShowDataLabels(true);
        monthlyDepartmentAnalysisPie.setDiameter(200);
        monthlyDepartmentAnalysisPie.setExtender("skinPie");
    }



}
