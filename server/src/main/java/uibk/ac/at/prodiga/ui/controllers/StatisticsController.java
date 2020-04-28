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
    private PieChartModel weeklyAnalysisPie;
    private PieChartModel last24hourAnalysisPie;
    private PieChartModel lastMonthAnalysisPie;
    private PieChartModel weeklyTeamAnalysisPie;
    private PieChartModel lastMonthTeamAnalysisPie;
    private PieChartModel last24hourTeamAnalysisPie;

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
    }

    @PostConstruct
    public void init() {
        createWeeklyStatistic();
        createLast24hourStatistic();
        createLastMonthStatistic();
        createLastWeekTeamStatistic();
        createLast24hourTeamStatistic();
        createLastMonthTeamStatistic();
    }

    public PieChartModel getWeeklyAnalysisPie() {return weeklyAnalysisPie;}
    public PieChartModel getLast24hourAnalysisPie(){return last24hourAnalysisPie;}
    public PieChartModel getLastMonthAnalysisPie(){return lastMonthAnalysisPie;}
    public PieChartModel getWeeklyTeamAnalysisPie(){return weeklyTeamAnalysisPie;}
    public PieChartModel getLastMonthTeamAnalysisPie(){return lastMonthTeamAnalysisPie;}
    public PieChartModel getLast24hourTeamAnalysisPie(){return last24hourTeamAnalysisPie;}

    private void createWeeklyStatistic() {
        weeklyAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForCurrentUser();
        weeklyAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        weeklyAnalysisPie.setTitle("User: Last week productivity analysis");
        weeklyAnalysisPie.setLegendPosition("e");
        weeklyAnalysisPie.setFill(false);
        weeklyAnalysisPie.setShowDataLabels(true);
        weeklyAnalysisPie.setDiameter(200);
        weeklyAnalysisPie.setShadow(false);
    }

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
        weeklyTeamAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForTeam();
        weeklyTeamAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyTeamAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        weeklyTeamAnalysisPie.setTitle("Team: Last weeks team productivity analysis");
        weeklyTeamAnalysisPie.setLegendPosition("e");
        weeklyTeamAnalysisPie.setFill(false);
        weeklyTeamAnalysisPie.setShowDataLabels(true);
        weeklyTeamAnalysisPie.setDiameter(200);
        weeklyTeamAnalysisPie.setShadow(false);
    }
}
