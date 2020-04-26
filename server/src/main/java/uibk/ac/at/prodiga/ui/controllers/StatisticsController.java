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
    private PieChartModel pieModel2;

    public StatisticsController(ProductivityAnalysisService productivityAnalysisService) {
        this.productivityAnalysisService = productivityAnalysisService;
    }

    @PostConstruct
    public void init() {
        createWeeklyStatistic();
    }

    public PieChartModel getPieModel2() {
        return pieModel2;
    }

    private void createWeeklyStatistic() {
        pieModel2 = new PieChartModel();

        Map<BookingCategory,Long> map = productivityAnalysisService.getWeeklyStatisticForCurrentUser();
        pieModel2 = new PieChartModel();
        for(Map.Entry<BookingCategory,Long> entry : map.entrySet()){
            pieModel2.set(entry.getKey().getName(),entry.getValue());
        }

        pieModel2.setTitle("Weekly productivity analysis");
        pieModel2.setLegendPosition("e");
        pieModel2.setFill(false);
        pieModel2.setShowDataLabels(true);
        pieModel2.setDiameter(400);
        pieModel2.setShadow(false);
    }

}
