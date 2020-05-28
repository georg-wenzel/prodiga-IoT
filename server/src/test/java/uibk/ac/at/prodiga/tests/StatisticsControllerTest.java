package uibk.ac.at.prodiga.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import uibk.ac.at.prodiga.model.User;
import uibk.ac.at.prodiga.repositories.UserRepository;
import uibk.ac.at.prodiga.tests.helper.DataHelper;
import uibk.ac.at.prodiga.ui.controllers.StatisticsController;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
public class StatisticsControllerTest {

    @Autowired
    StatisticsController statisticsController;

    User admin = null;

    @BeforeEach
    public void initEach(@Autowired UserRepository userRepository) {
        admin = DataHelper.createAdminUser("admin", userRepository);
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void statisticsController_init_chartsGenerated() {
        statisticsController.init();

        Assertions.assertNotNull(statisticsController.getDailyAnalysisBar());
        Assertions.assertNotNull(statisticsController.getDailyAnalysisPie());
        Assertions.assertNotNull(statisticsController.getMonthlyAnalysisBar());
        Assertions.assertNotNull(statisticsController.getMonthlyAnalysisPie());
        Assertions.assertNotNull(statisticsController.getWeeklyAnalysisPie());
        Assertions.assertNotNull(statisticsController.getWeeklyAnalysisBar());
        Assertions.assertNotNull(statisticsController.getWeeklyTeamAnalysisBar());
        Assertions.assertNotNull(statisticsController.getWeeklyTeamAnalysisPie());
        Assertions.assertNotNull(statisticsController.getMonthlyTeamAnalysisBar());
        Assertions.assertNotNull(statisticsController.getMonthlyTeamAnalysisPie());
        Assertions.assertNotNull(statisticsController.getMonthlyDepartmentAnalysisBar());
        Assertions.assertNotNull(statisticsController.getMonthlyDepartmentAnalysisPie());

        Assertions.assertNotNull(statisticsController.getStatisticForCurrentUserByDay());
        Assertions.assertNotNull(statisticsController.getStatisticForCurrentUserByWeek());
        Assertions.assertNotNull(statisticsController.getStatisticForCurrentUserByMonth());
        Assertions.assertNotNull(statisticsController.getStatisticForTeamByWeek());
        Assertions.assertNotNull(statisticsController.getStatisticForTeamByMonth());
        Assertions.assertNotNull(statisticsController.getStatisticForDepartmenByMonth());
    }

    @DirtiesContext
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void statisticsController_initNoData_noChartsDisplayed() {
        statisticsController.init();

        Assertions.assertFalse(statisticsController.isShowDaily());
        Assertions.assertFalse(statisticsController.isShowWeekly());
        Assertions.assertFalse(statisticsController.isShowMonthly());
        Assertions.assertFalse(statisticsController.isShowMonthlyTeam());
        Assertions.assertFalse(statisticsController.isShowWeeklyTeam());
        Assertions.assertFalse(statisticsController.isShowMonthlyDepartment());
    }

}
