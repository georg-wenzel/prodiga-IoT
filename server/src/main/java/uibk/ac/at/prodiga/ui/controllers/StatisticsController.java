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
import uibk.ac.at.prodiga.utils.MessageType;
import uibk.ac.at.prodiga.utils.ProdigaGeneralExpectedException;

import javax.annotation.PostConstruct;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Scope("view")
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
    private int backstepDays;
    private int backstepWeeks;
    private int backstepMonths;

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    private int calculateBackstepDays()
    {
        Calendar c = Calendar.getInstance();
        Date date = new Date();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY,0);
        Date today = c.getTime();
        return (int)((today.getTime() - this.selectedDate.getTime()) / (1000 * 60 * 60 * 24));
    }

    private int calculateBackstepWeeks(int backstepDays)
    {
        LocalDate now = LocalDate.now();
        LocalDate target = LocalDate.now().minusDays(backstepDays);

        return (int) ChronoUnit.WEEKS.between(target, now);
    }

    private int calculateBackstepMonths(int backstepDays)
    {
        LocalDate now = LocalDate.now();
        LocalDate target = LocalDate.now().minusDays(backstepDays);

        return (int) ChronoUnit.MONTHS.between(target, now);
    }

    public void verifyBackstepUser() throws ProdigaGeneralExpectedException
    {
        //check that day is max now
        if(calculateBackstepDays() < 0)
            throw new ProdigaGeneralExpectedException("Date may not lie in the future.", MessageType.ERROR);

        doSaveBackstep();
    }

    public void verifyBackstepTeam() throws ProdigaGeneralExpectedException
    {
        //check that date is minimum in the last week
        if(calculateBackstepWeeks(calculateBackstepDays()) < 1)
            throw new ProdigaGeneralExpectedException("Date must be at least in last week.", MessageType.ERROR);

        doSaveBackstep();
    }

    public void verifyBackstepDept() throws ProdigaGeneralExpectedException
    {
        //check that date is minimum in the last month
        if(calculateBackstepMonths(calculateBackstepDays()) < 1)
            throw new ProdigaGeneralExpectedException("Date must be at least in last month.", MessageType.ERROR);

        doSaveBackstep();
    }

    public void doSaveBackstep()
    {
        backstepDays = calculateBackstepDays();
        backstepWeeks = calculateBackstepWeeks(backstepDays);
        backstepMonths = calculateBackstepMonths(backstepDays);
        init();
    }

    public void userPageInit()
    {
        if(selectedDate == null) selectedDate = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        backstepDays = 1;
        backstepWeeks = calculateBackstepWeeks(1);
        backstepMonths = calculateBackstepMonths(1);
        init();
    }

    public void teamPageInit()
    {
        if(selectedDate == null)
        {
            selectedDate = Date.from(LocalDate.now().minusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            backstepDays = (int) ChronoUnit.DAYS.between(LocalDate.now().minusWeeks(1), LocalDate.now());
            backstepWeeks = calculateBackstepWeeks(backstepDays);
            backstepMonths = calculateBackstepMonths(backstepDays);
            init();
        }
    }

    public void deptPageInit()
    {
        if(selectedDate == null)
        {
            selectedDate = Date.from(LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            backstepDays = (int) ChronoUnit.DAYS.between(LocalDate.now().minusMonths(1), LocalDate.now());
            backstepWeeks = calculateBackstepWeeks(backstepDays);
            backstepMonths = calculateBackstepMonths(backstepDays);
            init();
        }
    }

    public void statisticsInit()
    {
        if(selectedDate == null)
        {
            selectedDate = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            backstepDays = 1;
            backstepWeeks = calculateBackstepWeeks(1);
            backstepMonths = calculateBackstepMonths(1);
            init();
        }
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

    public void init()
    {
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
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByDay(backstepDays).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForCurrentUserByWeek(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByWeek(backstepWeeks).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForCurrentUserByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForCurrentUserByMonth(backstepMonths).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForTeamByWeek(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForTeamByWeek(backstepWeeks).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForTeamByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForTeamByMonth(backstepMonths).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Long>> getStatisticForDepartmenByMonth(){
        Set<Map.Entry<BookingCategory, Long>> set = productivityAnalysisService.getStatisicForDepartmenByMonth(backstepMonths).entrySet();
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
