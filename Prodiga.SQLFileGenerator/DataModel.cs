using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

namespace Prodiga.SQLFileGenerator
{
    public class DataModel
    {
        #region fields

        private readonly int _roomAmount = 4;
        private readonly int _departmentAmount = 4;
        private readonly int _teamAmount;
        private readonly int _bookingCategoriesAmount = 12;
        private readonly int _raspiAmount;
        private int _badgeIndex = 1;

        #endregion

        #region properties

        public Dictionary<string, Dictionary<int, Dictionary<string, object>>> AllData { get; }

        public Dictionary<string, Dictionary<string, object>> UserData { get; }

        #endregion

        #region ctor

        public DataModel()
        {
            UserData = new Dictionary<string, Dictionary<string, object>>();
            AllData = new Dictionary<string, Dictionary<int, Dictionary<string, object>>>();
            _teamAmount = _departmentAmount * 2;
            _raspiAmount = _roomAmount * 2;
        }

        #endregion

        #region methods

        public void GenerateDataForBookingCategories()
        {
            AllData["booking_category"] = new Dictionary<int, Dictionary<string, object>>();

            Enumerable.Range(1, _bookingCategoriesAmount)
                .For(i => AllData["booking_category"][i] = getDefaultValues(i, DefaultData.DEFAULT_BOOKING_CAT_NAMES[i], false));
        }

        public void GenerateDataForRooms()
        {
            AllData["room"] = new Dictionary<int, Dictionary<string, object>>();

            Enumerable.Range(1, _roomAmount)
                .For(i => AllData["room"][i] = getDefaultValues(i, "Test Room"));
        }

        public void GenerateDataForDepartments()
        {
            AllData["department"] = new Dictionary<int, Dictionary<string, object>>();

            Enumerable.Range(1, _departmentAmount)
                .For(i => AllData["department"][i] = getDefaultValues(i, "Test Department"));
        }

        public void GenerateDataForTeams()
        {
            AllData["team"] = new Dictionary<int, Dictionary<string, object>>();

            int currentDept = 1;
            Enumerable.Range(1, _teamAmount)
                .For(i =>
                {
                    Dictionary<string, object> value = getDefaultValues(i, "Test Team");
                    value["department_id"] = currentDept;
                    AllData["team"][i] = value;

                    if (i % (_teamAmount / _departmentAmount) == 0)
                    {
                        currentDept++;
                    }
                });
        }

        public void GenerateDataForRaspi()
        {
            AllData["raspberry_pi"] = new Dictionary<int, Dictionary<string, object>>();

            int currentRoom = 1;
            Enumerable.Range(1, _raspiAmount)
                .For(i =>
                {
                    Dictionary<string, object> value = getDefaultValues(i);
                    value["assigned_room_id"] = currentRoom;
                    value["internal_id"] = "test" + i;
                    value["password"] = DefaultData.DEFAULT_PASSWORD;
                    AllData["raspberry_pi"][i] = value;

                    if (i % (_raspiAmount / _roomAmount) == 0)
                    {
                        currentRoom++;
                    }
                });
        }

        public void GenerateDataForDice(int userAmount)
        {
            if (UserData.Count != userAmount)
            {
                throw new Exception("User _data and amount not the same");
            }

            AllData["dice"] = new Dictionary<int, Dictionary<string, object>>();

            int currentRaspi = 1;
            Enumerable.Range(1, userAmount)
                .For(i =>
                {
                    Dictionary<string, object> value = getDefaultValues(i);
                    value["is_active"] = true;
                    value["assigned_raspberry_id"] = currentRaspi;
                    value["user_username"] = UserData.Skip(i - 1).First().Key;
                    value["internal_id"] = "testDice" + i;
                    AllData["dice"][i] = value;

                    if (i % (userAmount / _raspiAmount) == 0 && i < _raspiAmount)
                    {
                        currentRaspi++;
                    }
                });
        }

        public void GenerateDataForDiceSide(int userAmount)
        {
            AllData["dice_side"] = new Dictionary<int, Dictionary<string, object>>();

            int id = 1;
            for (int i = 0; i < userAmount; i++)
            {
                for (int j = 1; j <= _bookingCategoriesAmount; j++)
                {
                    Dictionary<string, object> value = getDefaultValues(id);
                    value["side"] = j;
                    value["side_friendly_name"] = j;
                    value["booking_category_id"] = j;
                    value["dice_id"] = i + 1;
                    AllData["dice_side"][id] = value;
                    id++;
                }
            }
        }

        public void GenerateDataForUser(HashSet<string> usernames)
        {

            Random rnd = new Random();

            foreach (var username in usernames)
            {
                string[] splitted = username.Split("_");
                string firstName = splitted[0];
                string lastName = splitted[1];

                int currentTeam = rnd.Next(1, _teamAmount + 1);

                int currentDept = (int)AllData["team"][currentTeam]["department_id"];

                Dictionary<string, object> values = new Dictionary<string, object>
                {
                    {"enabled", true},
                    {"first_name", firstName},
                    {"last_name", lastName},
                    {"username", username},
                    {"password", DefaultData.DEFAULT_PASSWORD},
                    {"create_user_username", DefaultData.ADMIN_USERNAME},
                    {"create_date", DefaultData.FIRST_DATE},
                    {"assigned_team_id", currentTeam},
                    {"assigned_department_id", currentDept},
                };

                UserData[username] = values;
            }
        }

        public void GenerateBookingData(int userAmount)
        {
            AllData["booking"] = new Dictionary<int, Dictionary<string, object>>();
            DateTime endDate = DateTime.Today.AddDays(-1);
            DateTime startDate = endDate.AddMonths(-3);
            int days = (endDate - startDate).Days;

            int index = 1;
            int holidayIndex = 1;

            Random rnd = new Random();

            Dictionary<int, Dictionary<string, int>> badgeData = new Dictionary<int, Dictionary<string, int>>();

            for (int i = 1; i <= userAmount; i++)
            {
                Dictionary<string, object> userData = UserData.Skip(i - 1).First().Value;

                string username = (string) userData["username"];
                int diceID = i;
                int deptID = (int) userData["assigned_department_id"];
                int teamID = (int) userData["assigned_team_id"];

                for (int j = 0; j < days; j++)
                {
                    DateTime start = startDate.AddDays(j);

                    switch (start.DayOfWeek)
                    {
                        case DayOfWeek.Sunday:
                            GenerateBadgeDBData(badgeData, start);
                            badgeData = new Dictionary<int, Dictionary<string, int>>();
                            continue;
                        case DayOfWeek.Saturday:
                            continue;
                    }

                    if (rnd.Next(1, 13) == 1)
                    {
                        GenerateVacationData(holidayIndex, username, start);
                        holidayIndex++;
                        continue;
                    }

                    start = start.AddHours(8);

                    int catID = rnd.Next(2, 13);

                    // 08:00 - 11:00
                    DateTime end = start.AddHours(3);
                    AllData["booking"][index] = getBookingValues(index, username, catID, deptID, teamID, diceID, start, end);

                    index++;

                    addToBadgeData(badgeData, username, catID, 3);

                    catID = rnd.Next(2, 13);

                    // 11:00 - 12:00
                    start = end;
                    end = start.AddHours(1);
                    AllData["booking"][index] = getBookingValues(index, username, catID, deptID, teamID, diceID, start, end);

                    index++;

                    addToBadgeData(badgeData, username, catID, 1);

                    catID = rnd.Next(2, 13);

                    // 13:00 - 15:00
                    start = end.AddHours(1);
                    end = start.AddHours(2);
                    AllData["booking"][index] = getBookingValues(index, username, catID, deptID, teamID, diceID, start, end);

                    index++;

                    addToBadgeData(badgeData, username, catID, 2);

                    catID = rnd.Next(2, 13);

                    // 15:00 - 17:00
                    start = end;
                    end = start.AddHours(2);
                    AllData["booking"][index] = getBookingValues(index, username, catID, deptID, teamID, diceID, start, end);

                    addToBadgeData(badgeData, username, catID, 2);

                    index++;
                }
            }
        }

        public void GenerateVacationData(int index, string username, DateTime date)
        {
            Dictionary<int, Dictionary<string, object>> values = new Dictionary<int, Dictionary<string, object>>();

            if (AllData.TryGetValue("vacation", out Dictionary<int, Dictionary<string, object>>? v))
            {
                values = v;
            }
            else
            {
                AllData["vacation"] = values;
            }

            values[index] = new Dictionary<string, object>
            {
                {"id", index},
                {"object_created_user_username", username},
                {"object_created_date_time", date},
                {"user_username", username},
                {"begin_date", date},
                {"end_date", date.AddDays(1)},
            };
        }

        public void GenerateBadgeDBData(Dictionary<int, Dictionary<string, int>> data, DateTime date)
        {
            Dictionary<int, Dictionary<string, object>> values = new Dictionary<int, Dictionary<string, object>>();

            if (AllData.TryGetValue("badgedb", out Dictionary<int, Dictionary<string, object>>? v))
            {
                values = v;
            }
            else
            {
                AllData["badgedb"] = values;
            }

            foreach ((int key, Dictionary<string, int> value) in data)
            {
                string username = value.OrderByDescending(x => x.Value).First().Key;
                string badgeName = "";
                string desciption = "";

                switch (key)
                {
                    case 1:
                        badgeName = "The Sloth";
                        desciption = "Most hours pause/vacation";
                        break;
                    case 3:
                        badgeName = "Frontend Laura";
                        desciption = "Most hours frontend";
                        break;
                    case 4:
                        badgeName = "Code Raptor Georg";
                        desciption = "Most hours implementation";
                        break;
                    case 7:
                        badgeName = "Bugsimilian";
                        desciption = "Most hours debugging";
                        break;
                    case 10:
                        badgeName = "Educated Gabbo";
                        desciption = "Most hours training and testing";
                        break;
                    case 11:
                        badgeName = "Busy Bee Jamie";
                        desciption = "Most hours managing";
                        break;
                }

                if (!string.IsNullOrEmpty(badgeName))
                {
                    DateTime startDate = date.AddDays(-6);

                    values[_badgeIndex] = new Dictionary<string, object>()
                    {
                        {"id", _badgeIndex},
                        {"badge_name", badgeName},
                        {"explanation", desciption},
                        {"user_username", username},
                        {"from_date", startDate},
                        {"to_date", date},
                    };
                    _badgeIndex++;
                }
            }
        }

        public void GenerateRoleData(HashSet<string> usernames)
        {
            List<int> completedTeamIDs = new List<int>();
            List<int> completedDeptIDs = new List<int>();

            AllData["user_user_role"] = new Dictionary<int, Dictionary<string, object>>();

            int i = 1;

            foreach (string username in usernames)
            {
                int deptID = (int) UserData[username]["assigned_department_id"];
                int teamID = (int)UserData[username]["assigned_team_id"];

                string role;

                if (!completedDeptIDs.Contains(deptID))
                {
                    role = "DEPARTMENTLEADER";
                    completedDeptIDs.Add(deptID);
                }
                else if (!completedTeamIDs.Contains(teamID))
                {
                    role = "TEAMLEADER";
                    completedTeamIDs.Add(teamID);
                }
                else
                {
                    role = "EMPLOYEE";
                }

                AllData["user_user_role"][i] = new Dictionary<string, object>
                {
                    {"user_username", username},
                    {"ROLES", role}
                };

                i++;

                if (!role.Equals("EMPLOYEE"))
                {
                    AllData["user_user_role"][i] = new Dictionary<string, object>
                    {
                        {"user_username", username},
                        {"ROLES", "EMPLOYEE"}
                    };
                }

                i++;
            }
        }

        public void GenerateDataForBookingCatTeams()
        {
            AllData["booking_category_teams"] = new Dictionary<int, Dictionary<string, object>>();

            int i = 0;

            foreach (int teamID in AllData["team"].Keys)
            {
                foreach (int catID in DefaultData.DEFAULT_BOOKING_CAT_NAMES.Keys)
                {
                    AllData["booking_category_teams"][i] = new Dictionary<string, object>
                    {
                        {"booking_category_id", catID},
                        {"teams_id", teamID},
                    };
                    i++;
                }
            }
        }

        private void addToBadgeData(Dictionary<int, Dictionary<string, int>> data, string userName, int id, int dur)
        {
            Dictionary<string, int> userData = new Dictionary<string, int>();

            if (data.TryGetValue(id, out var t))
            {
                userData = t;
            }
            else
            {
                data[id] = userData;
            }

            if (userData.ContainsKey(userName))
            {
                userData[userName]+= dur;
            }
            else
            {
                userData[userName] = dur;
            }
        }

        private Dictionary<string, object> getBookingValues(int index, string username, int catID, int deptID,
            int teamID, int diceID, DateTime start, DateTime end)
        {
            return new Dictionary<string, object>()
            {
                {"id", index},
                {"object_created_date_time", end},
                {"object_created_user_username", username},
                {"booking_category_id", catID},
                {"dept_id", deptID},
                {"dice_id", diceID},
                {"team_id", teamID},
                {"activity_start_date", start},
                {"activity_end_date", end},
            };
        }

        private Dictionary<string, object> getDefaultValues(int index, string? name = null, bool includeIndex = true)
        {
            Dictionary<string, object> result = new Dictionary<string, object>
            {
                {"id", index},
                {"object_created_user_username", DefaultData.ADMIN_USERNAME},
                {"object_created_date_time", DefaultData.FIRST_DATE},
                {"object_changed_user_username", DefaultData.ADMIN_USERNAME},
                {"object_changed_date_time", DefaultData.FIRST_DATE},
            };

            if (!string.IsNullOrWhiteSpace(name))
            {
                if (includeIndex)
                {
                    result.Add("name", name + " " + index);
                }
                else
                {
                    result.Add("name", name);
                }
            }

            return result;
        }

        #endregion

    }
}
