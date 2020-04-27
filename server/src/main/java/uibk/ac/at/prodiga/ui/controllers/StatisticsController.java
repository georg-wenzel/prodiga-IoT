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

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
    }

    @PostConstruct
    public void init() {
        createWeeklyStatistic();
    }

    public PieChartModel getWeeklyAnalysisPie() {
        return weeklyAnalysisPie;
    }

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

}
