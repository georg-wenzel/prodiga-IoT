package uibk.ac.at.prodiga.services;

import org.primefaces.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uibk.ac.at.prodiga.model.*;
import uibk.ac.at.prodiga.utils.ProdigaUserLoginManager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Component
@Scope("application")
public class ProductivityAnalysisService {
    private final BookingCategoryService bookingCategoryService;
    private final ProdigaUserLoginManager userLoginManager;
    private final BookingService bookingService;
    private final UserService userService;
    private final BadgeDBService badgesDBService;
    private final DepartmentService departmentService;


    public ProductivityAnalysisService(ProdigaUserLoginManager userLoginManager, BookingCategoryService bookingCategoryService, BookingService bookingService, UserService userService, BadgeDBService badgesDBService, DepartmentService departmentService) {
        this.userLoginManager = userLoginManager;
        this.bookingCategoryService = bookingCategoryService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.badgesDBService = badgesDBService;
        this.departmentService = departmentService;
    }

    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByDay(int backstepDay){
        return getStatisticForUserByDay(backstepDay, userLoginManager.getCurrentUser());
    }
    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByWeek(int backstepWeek){
        return getStatisticForUserByWeek(backstepWeek, userLoginManager.getCurrentUser());
    }
    public HashMap<BookingCategory,Long> getStatisicForCurrentUserByMonth(int backstepMonth){
        return getStatisticForUserByMonth(backstepMonth, userLoginManager.getCurrentUser());
    }

    public HashMap<BookingCategory, Long> getStatisticForUserByDay(int backstepDay, User user){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByDay(user,backstepDay)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime())/(1000*60*60);
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory,Long> getStatisticForUserByWeek(int backstepWeek, User user) {
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByWeek(user,backstepWeek)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) /(1000*60*60);;
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory,Long> getStatisticForUserByMonth(int backstepMonth, User user){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        long hours;
        for(Booking booking: bookingService.getUsersBookingInRangeByMonth(user,backstepMonth)){
            hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) /(1000*60*60);
            if(hashMap.containsKey(booking.getBookingCategory())){
                long before = hashMap.get(booking.getBookingCategory());
                hashMap.put(booking.getBookingCategory(),before+hours);
            }
            else{
                hashMap.put(booking.getBookingCategory(),hours);
            }
        }
        return hashMap;
    }



    public HashMap<BookingCategory,Long> getStatisicForTeamByWeek(int backstepWeek){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        long hours = 0;
        long before = 0;
        if(user.getRoles().contains(UserRole.TEAMLEADER))
        {
            for(User teamMember: userService.getUsersByTeam(myTeam)) {
                for (Booking booking : bookingService.getUsersBookingInRangeByWeek(teamMember, backstepWeek)) {
                    hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                    if (hashMap.containsKey(booking.getBookingCategory())) {
                        before = hashMap.get(booking.getBookingCategory());
                        hashMap.put(booking.getBookingCategory(), before + hours);
                    } else {
                        hashMap.put(booking.getBookingCategory(), hours);
                    }
                }
            }
        }
        return hashMap;
    }
    public HashMap<BookingCategory,Long> getStatisicForTeamByMonth(int backstepMonth){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        Team myTeam = user.getAssignedTeam();
        long hours = 0;
        long before = 0;
        if(user.getRoles().contains(UserRole.TEAMLEADER)){
            for(User teamMember: userService.getUsersByTeam(myTeam)) {
                for (Booking booking : bookingService.getUsersBookingInRangeByMonth(teamMember, backstepMonth)) {
                    hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                    if (hashMap.containsKey(booking.getBookingCategory())) {
                        before = hashMap.get(booking.getBookingCategory());
                        hashMap.put(booking.getBookingCategory(), before + hours);
                    } else {
                        hashMap.put(booking.getBookingCategory(), hours);
                    }
                }
            }
        }
        return hashMap;
    }

    public HashMap<BookingCategory,Long> getStatisicForDepartmenByMonth(int backstepMonth){
        HashMap<BookingCategory, Long> hashMap = new HashMap<>();
        User user = userLoginManager.getCurrentUser();
        long hours = 0;
        long before = 0;

        Map<String, User> userPerName = new HashMap<>();

        if(user.getRoles().contains(UserRole.ADMIN)) {
            departmentService.getAllDepartments().forEach(x -> {
                userService.getUsersByDepartment(x).forEach(y -> {
                    userPerName.put(y.getUsername(), y);
                });
            });


        } else if(user.getRoles().contains(UserRole.DEPARTMENTLEADER)) {
            Department myDepartment = user.getAssignedDepartment();
            userService.getUsersByDepartment(myDepartment).forEach(x -> {
                userPerName.put(x.getUsername(), x);
            });
        }

        for(User departmentMember: userPerName.values()) {
            for (Booking booking : bookingService.getUsersBookingInRangeByMonth(departmentMember, backstepMonth)) {
                hours = (booking.getActivityEndDate().getTime() - booking.getActivityStartDate().getTime()) / (1000 * 60 * 60);
                if (hashMap.containsKey(booking.getBookingCategory())) {
                    before = hashMap.get(booking.getBookingCategory());
                    hashMap.put(booking.getBookingCategory(), before + hours);
                } else {
                    hashMap.put(booking.getBookingCategory(), hours);
                }
            }
        }
        return hashMap;
    }

    public void createJSON(FrequencyType frequencyType, User user){
        JSONObject json = null;
        String jsonString = null;
        Collection<BadgeDB> badgesByUser = badgesDBService.getLastWeeksBadgesByUser(user);
        if(frequencyType.equals(FrequencyType.DAILY)){
            HashMap<BookingCategory, Long> hashMapDaily = getStatisticForUserByDay(1, user);
            json = new JSONObject(hashMapDaily);
            jsonString = json.toString();
        }
        else if(frequencyType.equals(FrequencyType.MONTHLY)) {
            HashMap<BookingCategory, Long> hashMapMonthly = getStatisticForUserByMonth(1, user);
            json = new JSONObject(hashMapMonthly);
            jsonString = json.toString();
        }
        else if(frequencyType.equals(FrequencyType.WEEKLY)){
            HashMap<BookingCategory, Long> hashMapWeekly = getStatisticForUserByWeek(1, user);
            json = new JSONObject(hashMapWeekly);
            jsonString = json.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>\n" +
                "<head>\n" +
                "    <meta charset=\"utf-8\"/>\n" +
                "    <title>Chart.js demo</title>\n" +
                "  \n" +
                "</head>\n" +
                "<body>\n" +
                "\t\n" +
                "\t<script src=\"https://cdn.jsdelivr.net/npm/chart.js@2.8.0\"></script>\n" +
                "\t<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\" type=\"text/javascript\"></script>\n" +
                "\n");

        stringBuilder.append("<div style=\"height:5vh\"><h2 style=\"font-family:sans-serif; text-align:center; padding: 20px;\">Time spent on categories - "+ frequencyType.getLabel() + "</h2></div>\n" +
                        "\t\n" +
                        "\t<div style=\"position: center; height:55vh; width:100vw\">\n" +
                        "\t<canvas id=\"chart\"></canvas>\n" +
                        "\t</div>\n" +
                        "\t\n" +
                        "\t<div style=\"width: 45vw; float:left; height:40vh; margin:10px\">\n" +
                        "\t<canvas id=\"chart2\"></canvas>\n" +
                        "\t</div>\n" +
                        "\t<div style=\"width: 45vw; float:left; height:40vh;margin:10px\">\n" +
                        "\t<canvas id=\"chart3\"></canvas>\n" +
                        "\t</div>\n" +
                        "\n" +
                        "\t<div style=\"height:5vh; clear:both;\"><h2 style=\"font-family:sans-serif; text-align:center; padding: 20px;\">Last weeks badges</h2></div>\n" +
                        "\t<br><br>\n"+
                        "\t<div style=\"text-align: center\">\n");

        for(BadgeDB badgeDB : badgesByUser){
            stringBuilder.append("<img src=\"" + badgeDB.getBadgeName() + ".png\" alt=\"" + badgeDB.getBadgeName() + "\" style=\"width: 20vw; margin:30px\">\n");
        }

        stringBuilder.append("\t</div>\n" +
                "\n" +
                "\t<script>");

        stringBuilder.append("var json = " + jsonString + ";");
        stringBuilder.append("\n" +
                "\t\tvar labels = Object.keys(json);\n" +
                        "\t\tvar data = Object.values(json);\n" +
                        "\t\tvar chart = document.getElementById('chart').getContext('2d');\n" +
                        "\t\tvar chart2 = document.getElementById('chart2').getContext('2d');\n" +
                        "\t\tvar chart3 = document.getElementById('chart3').getContext('2d');\n" +
                        "\t\tvar chart = new Chart(chart, {\n" +
                        "\t\t\ttype: 'bar',\n" +
                        "\t\t\tdata: {\n" +
                        "\t\t\t\tlabels: labels,\n" +
                        "\t\t\t\tdatasets: [{\n" +
                        "\t\t\t\t\tlabels: ['asdf', 'sadf'],\n" +
                        "\t\t\t\t\tdata: data,\n" +
                        "\t\t\t\t\tbackgroundColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,0.50)'\n" +
                        "\t\t\t\t\t],\n" +
                        "\t\t\t\t\tborderColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,1)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,1)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,1)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,1)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,1)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,1)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,1)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,1)'\n" +
                        "\t\t\t\t\t],\n" +
                        "\t\t\t\t\tborderWidth: 1\n" +
                        "\t\t\t\t}]\n" +
                        "\t\t\t},\n" +
                        "\t\t\toptions: {\n" +
                        "\t\t\t\tmaintainAspectRatio: false,\n" +
                        "\t\t\t  legend: { display: false },\n" +
                        "\t\t\t   title: {\n" +
                        "\t\t\t\tdisplay: true,\n");

        stringBuilder.append("\n" +
                "\t\t\t\tfontSize: 20,\n" +
                "\t\t\t\tpadding: 30\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\tscales: {\n" +
                "\t\t\t\t\tyAxes: [{\n" +
                "\t\t\t\t\t\tticks: {\n" +
                "\t\t\t\t\t\t\tbeginAtZero: true\n" +
                "\t\t\t\t\t\t}\n" +
                "\t\t\t\t\t}]\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\tlayout: {\n" +
                "\t\t\t\tpadding: {\n" +
                "\t\t\t\t\tleft: 50,\n" +
                "\t\t\t\t\tright: 50,\n" +
                "\t\t\t\t\ttop: 50,\n" +
                "\t\t\t\t\tbottom: 50\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t});\n" +
                "\t\t\n" +
                "\t\tvar chart2 = new Chart(chart2, {\n" +
                "\t\t\ttype: 'pie',\n" +
                "\t\t\tdata: {\n" +
                "\t\t\t\tlabels: labels,\n" +
                "\t\t\t\tdatasets: [{\n" +
                "\t\t\t\t\tlabels: ['asdf', 'sadf'],\n" +
                "\t\t\t\t\tdata: data,\n" +
                "\t\t\t\t\tbackgroundColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,0.50)'\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\tborderColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,1)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,1)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,1)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,1)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,1)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,1)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,1)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,1)'\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\tborderWidth: 1\n" +
                "\t\t\t\t}]\n" +
                "\t\t\t},\n" +
                "\t\t\toptions: {\n" +
                "\t\t\t\tmaintainAspectRatio: false,\n" +
                "\t\t\t  legend: { position: 'bottom'},\n" +
                "\t\t\t   \n" +
                "\t\t\t\t\n" +
                "\t\t\t\tlayout: {\n" +
                "\t\t\t\tpadding: {\n" +
                "\t\t\t\t\tleft: 50,\n" +
                "\t\t\t\t\tright: 50,\n" +
                "\t\t\t\t\ttop: 50,\n" +
                "\t\t\t\t\tbottom: 50\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t});\n" +
                "\t\t\n" +
                "\t\tvar chart3 = new Chart(chart3, {\n" +
                "\t\t\ttype: 'polarArea',\n" +
                "\t\t\tdata: {\n" +
                "\t\t\t\tlabels: labels,\n" +
                "\t\t\t\tdatasets: [{\n" +
                "\t\t\t\t\tlabels: ['asdf', 'sadf'],\n" +
                "\t\t\t\t\tdata: data,\n" +
                "\t\t\t\t\tbackgroundColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,0.50)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,0.50)'\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\tborderColor: [\n" +
                "\t\t\t\t\t\t'rgba(224,35,101,1)',\n" +
                "\t\t\t\t\t\t'rgba(45,142,227,1)',\n" +
                "\t\t\t\t\t\t'rgba(68,190,44,1)',\n" +
                "\t\t\t\t\t\t'rgba(238,178,16,1)',\n" +
                "\t\t\t\t\t\t'rgba(171,68,188,1)',\n" +
                "\t\t\t\t\t\t'rgba(33,98,176,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,208,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(255,44,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(0,208,255,1)',\n" +
                "\t\t\t\t\t\t'rgba(185,255,0,1)',\n" +
                "\t\t\t\t\t\t'rgba(235,7,197,1)',\n" +
                "\t\t\t\t\t\t'rgba(26,143,10,1)'\n" +
                "\t\t\t\t\t],\n" +
                "\t\t\t\t\tborderWidth: 1\n" +
                "\t\t\t\t}]\n" +
                "\t\t\t},\n" +
                "\t\t\toptions: {\n" +
                "\t\t\t\tmaintainAspectRatio: false,\n" +
                "\t\t\t  legend: { position: 'bottom'},\n" +
                "\t\t\t   \n" +
                "\t\t\t\tlayout: {\n" +
                "\t\t\t\tpadding: {\n" +
                "\t\t\t\t\tleft: 50,\n" +
                "\t\t\t\t\tright: 50,\n" +
                "\t\t\t\t\ttop: 50,\n" +
                "\t\t\t\t\tbottom: 50\n" +
                "\t\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t\t}\n" +
                "\t\t});\n" +
                "\t\t\n" +
                "\t\t\n" +
                "\t</script>\n" +
                "</body>\n" +
                "</html>\t");

        try{
            FileWriter file = new FileWriter("src/main/java/uibk/ac/at/prodiga/utils/charts/"+user.getUsername()+"-"+frequencyType.getLabel().toLowerCase()+".html");
            file.write(stringBuilder.toString());
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
