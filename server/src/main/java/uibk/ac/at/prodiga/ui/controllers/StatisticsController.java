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

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
    }

    @PostConstruct
    public void init() {
        createWeeklyStatistic();
        createLast24hourStatistic();
        createLastMonthStatistic();
    }

    public PieChartModel getWeeklyAnalysisPie() {
        return weeklyAnalysisPie;
    }
    public PieChartModel getLast24hourAnalysisPie(){return last24hourAnalysisPie;}
    public PieChartModel getLastMonthAnalysisPie(){return lastMonthAnalysisPie;}

    private void createWeeklyStatistic() {
        weeklyAnalysisPie = new PieChartModel();
        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForCurrentUser();
        weeklyAnalysisPie = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            weeklyAnalysisPie.set(entry.getKey().getName(),entry.getValue());
        }
        weeklyAnalysisPie.setTitle("Weekly productivity analysis");
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
        last24hourAnalysisPie.setTitle("Last 24h productivity analysis");
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
        lastMonthAnalysisPie.setTitle("Last Months productivity analysis");
        lastMonthAnalysisPie.setLegendPosition("e");
        lastMonthAnalysisPie.setFill(false);
        lastMonthAnalysisPie.setShowDataLabels(true);
        lastMonthAnalysisPie.setDiameter(200);
        lastMonthAnalysisPie.setShadow(false);
    }

}
