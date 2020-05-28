using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace Prodiga.SQLFileGenerator
{
    class Program
    {
        static async Task Main(string[] args)
        {
            HashSet<string> usernames = null;
            FileInfo dataFile = GetSQlDataPath();
            DataModel model = new DataModel();

            if (args.Length == 1 && File.Exists(args[0]))
            {
                parseUsersFromCsv(args[0], model);
                model.GenerateBookingData(model.UserData.Count);
            }
            else
            {
                using var client = new NamesAPiClient();

                var resultTask = client.GetNamesAsync(20);

                model.GenerateDataForBookingCategories();
                model.GenerateDataForRooms();
                model.GenerateDataForRaspi();
                model.GenerateDataForBookingCatTeams();

                usernames = await resultTask;
                model.GenerateRoleData(usernames);
                model.GenerateDataForDice(usernames.Count);
                model.GenerateDataForDiceSide(usernames.Count);
                model.GenerateDataForDepartments();
                model.GenerateDataForTeams();
                model.GenerateDataForUser(usernames);
                model.GenerateBookingData(usernames.Count);

            }
            Generator.DoGenerate(model, dataFile);

        }

        public static FileInfo GetSQlDataPath()
        {
            DirectoryInfo currentDir = new DirectoryInfo(Directory.GetCurrentDirectory());

            while (!currentDir.Name.Equals("prodiga", StringComparison.OrdinalIgnoreCase))
            {
                currentDir = currentDir.Parent;
            }

            FileInfo dataFile = new FileInfo(Path.Combine(currentDir.FullName,
                "server", "src", "main", "resources", "bookings.sql"));

            if (!dataFile.Exists)
            {
                dataFile.Create();
            }

            return dataFile;
        }

        private static void parseUsersFromCsv(string path, DataModel model)
        {
            File.ReadAllLines(path)
                .Where(x => !string.IsNullOrEmpty(x))
                .ToList()
                .ForEach(x =>
                {
                    string[] splitted = x.Split(",");
                    string username = splitted[0].Trim();
                    model.UserData[username] = new Dictionary<string, object>
                    {
                        {"username", username},
                        {"assigned_department_id", int.Parse(splitted[1].Trim())},
                        {"assigned_team_id", int.Parse(splitted[2].Trim())},
                    };
                });
        }
    }
}
