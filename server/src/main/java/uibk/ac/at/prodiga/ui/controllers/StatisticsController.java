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
    private PieChartModel last24hourAnalysisPie;
    private PieChartModel lastWeekAnalysisPie;
    private PieChartModel lastMonthAnalysisPie;
    private PieChartModel last24hourTeamAnalysisPie;
    private PieChartModel lastWeekTeamAnalysisPie;
    private PieChartModel lastMonthTeamAnalysisPie;
    private PieChartModel last24hourDepartmentAnalysisPie;
    private PieChartModel lastWeekDepartmentAnalysisPie;
    private PieChartModel lastMonthDepartmentAnalysisPie;
    private PieChartModel test;

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
    }

    @PostConstruct
    public void init() {
        createLast24hourStatistic();
        createLast24hourTeamStatistic();
        createLast24hourDepartmentStatistic();

        createLastWeekStatistic();
        createLastWeekTeamStatistic();
        createLastWeekDepartmentStatistic();

        createLastMonthStatistic();
        createLastMonthTeamStatistic();
        createLastMonthDepartmentStatistic();

    }

    public PieChartModel getLastWeekAnalysisPie() {return lastWeekAnalysisPie;}
    public PieChartModel getLast24hourAnalysisPie(){return last24hourAnalysisPie;}
    public PieChartModel getLastMonthAnalysisPie(){return lastMonthAnalysisPie;}
    public PieChartModel getLastWeekTeamAnalysisPie(){return lastWeekTeamAnalysisPie;}
    public PieChartModel getLastMonthTeamAnalysisPie(){return lastMonthTeamAnalysisPie;}
    public PieChartModel getLast24hourTeamAnalysisPie(){return last24hourTeamAnalysisPie;}
    public PieChartModel getLast24hourDepartmentAnalysisPie(){return last24hourDepartmentAnalysisPie;}
    public PieChartModel getLastWeekDepartmentAnalysisPie(){return lastWeekDepartmentAnalysisPie;}
    public PieChartModel getLastMonthDepartmentAnalysisPie(){return lastMonthDepartmentAnalysisPie;}

    private void createLast24hourStatistic() {
        last24hourAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLast24hourStatisticForCurrentUser();
        last24hourAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            last24hourAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        last24hourAnalysisPie.setTitle("User: Last 24h productivity analysis");
        last24hourAnalysisPie.setLegendPosition("e");
        last24hourAnalysisPie.setFill(false);
        last24hourAnalysisPie.setShowDataLabels(true);
        last24hourAnalysisPie.setDiameter(200);
        last24hourAnalysisPie.setShadow(false);
    }

    private void createLastWeekStatistic() {
        lastWeekAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForCurrentUser();
        lastWeekAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastWeekAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastWeekAnalysisPie.setTitle("User: Last week productivity analysis");
        lastWeekAnalysisPie.setLegendPosition("e");
        lastWeekAnalysisPie.setFill(false);
        lastWeekAnalysisPie.setShowDataLabels(true);
        lastWeekAnalysisPie.setDiameter(200);
        lastWeekAnalysisPie.setShadow(false);
    }

    private void createLastMonthStatistic() {
        lastMonthAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLastMonthsStatisticForCurrentUser();
        lastMonthAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastMonthAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastMonthAnalysisPie.setTitle("User: Last Months productivity analysis");
        lastMonthAnalysisPie.setLegendPosition("e");
        lastMonthAnalysisPie.setFill(false);
        lastMonthAnalysisPie.setShowDataLabels(true);
        lastMonthAnalysisPie.setDiameter(200);
        lastMonthAnalysisPie.setShadow(false);
    }

    private void createLast24hourTeamStatistic() {
        last24hourTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLast24hourStatisticForTeam();
        last24hourTeamAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            last24hourTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        last24hourTeamAnalysisPie.setTitle("Team: Last 24 hour team productivity analysis");
        last24hourTeamAnalysisPie.setLegendPosition("e");
        last24hourTeamAnalysisPie.setFill(false);
        last24hourTeamAnalysisPie.setShowDataLabels(true);
        last24hourTeamAnalysisPie.setDiameter(200);
        last24hourTeamAnalysisPie.setShadow(false);
    }

    private void createLastWeekTeamStatistic() {
        lastWeekTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForTeam();
        lastWeekTeamAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastWeekTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastWeekTeamAnalysisPie.setTitle("Team: Last weeks team productivity analysis");
        lastWeekTeamAnalysisPie.setLegendPosition("e");
        lastWeekTeamAnalysisPie.setFill(false);
        lastWeekTeamAnalysisPie.setShowDataLabels(true);
        lastWeekTeamAnalysisPie.setDiameter(200);
        lastWeekTeamAnalysisPie.setShadow(false);
    }

    private void createLastMonthTeamStatistic() {
        lastMonthTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLastMonthsStatisticForTeam();
        lastMonthTeamAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastMonthTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastMonthTeamAnalysisPie.setTitle("Team: Last Months team productivity analysis");
        lastMonthTeamAnalysisPie.setLegendPosition("e");
        lastMonthTeamAnalysisPie.setFill(false);
        lastMonthTeamAnalysisPie.setShowDataLabels(true);
        lastMonthTeamAnalysisPie.setDiameter(200);
        lastMonthTeamAnalysisPie.setShadow(false);
    }

    private void createLast24hourDepartmentStatistic() {
        last24hourDepartmentAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLast24hourStatisticForTeam();
        last24hourDepartmentAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            last24hourDepartmentAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        last24hourDepartmentAnalysisPie.setTitle("Department: Last 24 hour department productivity analysis");
        last24hourDepartmentAnalysisPie.setLegendPosition("e");
        last24hourDepartmentAnalysisPie.setFill(false);
        last24hourDepartmentAnalysisPie.setShowDataLabels(true);
        last24hourDepartmentAnalysisPie.setDiameter(200);
        last24hourDepartmentAnalysisPie.setShadow(false);
    }

    private void createLastWeekDepartmentStatistic() {
        lastWeekDepartmentAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForDepartment();
        lastWeekDepartmentAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastWeekDepartmentAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastWeekDepartmentAnalysisPie.setTitle("Department: Last weeks department productivity analysis");
        lastWeekDepartmentAnalysisPie.setLegendPosition("e");
        lastWeekDepartmentAnalysisPie.setFill(false);
        lastWeekDepartmentAnalysisPie.setShowDataLabels(true);
        lastWeekDepartmentAnalysisPie.setDiameter(200);
        lastWeekDepartmentAnalysisPie.setShadow(false);
    }

    private void createLastMonthDepartmentStatistic() {
        lastMonthDepartmentAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getLastMonthsStatisticForDepartment();
        lastMonthDepartmentAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            lastMonthDepartmentAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        lastMonthDepartmentAnalysisPie.setTitle("Department: Last Months department productivity analysis");
        lastMonthDepartmentAnalysisPie.setLegendPosition("e");
        lastMonthDepartmentAnalysisPie.setFill(false);
        lastMonthDepartmentAnalysisPie.setShowDataLabels(true);
        lastMonthDepartmentAnalysisPie.setDiameter(200);
        lastMonthDepartmentAnalysisPie.setShadow(false);
    }



}
