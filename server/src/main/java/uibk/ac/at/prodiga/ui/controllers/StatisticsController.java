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
import java.time.DayOfWeek;
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

    private boolean showDaily = true;
    private boolean showWeekly = true;
    private boolean showMonthly = true;
    private boolean showWeeklyTeam = true;
    private boolean showMonthlyTeam = true;
    private boolean showMonthlyDepartment = true;

    private Date selectedDate;
    private int backstepDays;
    private int backstepWeeks;
    private int backstepMonths;

    private boolean firstInit = true;

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

    public boolean getAtLeastLastMonth()
    {
        return calculateBackstepMonths(calculateBackstepDays()) >= 1;
    }

    public boolean getAtLeastLastWeek()
    {
        return calculateBackstepWeeks(calculateBackstepDays()) >= 1;
    }

    private int calculateBackstepWeeks(int backstepDays)
    {
        LocalDate now = LocalDate.now();
        LocalDate target = LocalDate.now().minusDays(backstepDays);

        int weeksChanged = 0;


        while(target.isBefore(now))
        {
            now = now.minusDays(1);
            if(now.getDayOfWeek() == DayOfWeek.SUNDAY) weeksChanged++;
        }

        return weeksChanged;
    }

    private int calculateBackstepMonths(int backstepDays)
    {
        LocalDate now = LocalDate.now();
        LocalDate target = LocalDate.now().minusDays(backstepDays);

        int monthsPassed = 0;

        while(target.isBefore(now))
        {
            if(now.getMonth() != now.minusDays(1).getMonth()) monthsPassed++;
            now = now.minusDays(1);
        }

        return monthsPassed;
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
        if(selectedDate == null)
        {
            selectedDate = Date.from(LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
            backstepDays = 1;
            backstepWeeks = calculateBackstepWeeks(1);
            backstepMonths = calculateBackstepMonths(1);
            init();
        }
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

    public List<Map.Entry<BookingCategory, Double>> getStatisticForCurrentUserByDay(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForCurrentUserByDay(backstepDays).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Double>> getStatisticForCurrentUserByWeek(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForCurrentUserByWeek(backstepWeeks).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Double>> getStatisticForCurrentUserByMonth(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForCurrentUserByMonth(backstepMonths).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Double>> getStatisticForTeamByWeek(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForTeamByWeek(backstepWeeks).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Double>> getStatisticForTeamByMonth(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForTeamByMonth(backstepMonths).entrySet();
        return new ArrayList<>(set);
    }

    public List<Map.Entry<BookingCategory, Double>> getStatisticForDepartmenByMonth(){
        Set<Map.Entry<BookingCategory, Double>> set = productivityAnalysisService.getStatisticForDepartmenByMonth(backstepMonths).entrySet();
        return new ArrayList<>(set);
    }

    public boolean isShowDaily() {
        return showDaily;
    }

    public boolean isShowWeekly() {
        return showWeekly;
    }

    public boolean isShowMonthly() {
        return showMonthly;
    }

    public boolean isShowWeeklyTeam() {
        return showWeeklyTeam;
    }

    public boolean isShowMonthlyTeam() {
        return showMonthlyTeam;
    }

    public boolean isShowMonthlyDepartment() {
        return showMonthlyDepartment;
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
        Map<BookingCategory,Double> map = productivityAnalysisService.getStatisticForCurrentUserByWeek(backstepWeek);
        for(Map.Entry<BookingCategory,Double> entry : map.entrySet()){
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

        showWeekly = hours.size() > 0;

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
        Map<BookingCategory, Double> map = productivityAnalysisService.getStatisticForCurrentUserByDay(backstepDay);
        for (Map.Entry<BookingCategory, Double> entry : map.entrySet()) {
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

        showDaily = hours.size() > 0;

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

        Map<BookingCategory,Double> map = productivityAnalysisService.getStatisticForCurrentUserByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Double> entry : map.entrySet()){
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

        showMonthly = hours.size() > 0;

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

        Map<BookingCategory,Double> map = productivityAnalysisService.getStatisticForTeamByWeek(backstepWeek);
        for(Map.Entry<BookingCategory,Double> entry : map.entrySet()){
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

        showWeeklyTeam = hours.size() > 0;

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

        Map<BookingCategory,Double> map = productivityAnalysisService.getStatisticForTeamByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Double> entry : map.entrySet()){
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

        showMonthlyTeam = hours.size() > 0;

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

        Map<BookingCategory,Double> map = productivityAnalysisService.getStatisticForDepartmenByMonth(backstepMonth);
        for(Map.Entry<BookingCategory,Double> entry : map.entrySet()){
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

        showMonthlyDepartment = hours.size() > 0;

        barDataSet.setData(values);
        barDataSet.setBackgroundColor(mycolors);
        barDataSet.setBorderColor(mycolors);
        dataBar.addChartDataSet(barDataSet);
        dataBar.setLabels(labelsBar);
        monthlyDepartmentAnalysisBar.setData(dataBar);
        barDataSet.setLabel("Categories");
    }



}
