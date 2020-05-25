using System;
using System.Collections.Generic;
using System.IO;
using System.Threading.Tasks;

namespace Prodiga.SQLFileGenerator
{
    class Program
    {
        static async Task Main(string[] args)
        {
            FileInfo dataFile = GetSQlDataPath();

            DataModel model = new DataModel();

            using var client = new NamesAPiClient();
            
            var resultTask = client.GetNamesAsync(20);

            model.GenerateDataForBookingCategories();
            model.GenerateDataForRooms();
            model.GenerateDataForDepartments();
            model.GenerateDataForTeams();
            model.GenerateDataForRaspi();
            model.GenerateDataForBookingCatTeams();

            HashSet<string> usernames = await resultTask;

            model.GenerateDataForUser(usernames);
            model.GenerateRoleData(usernames);
            model.GenerateDataForDice(usernames.Count);
            model.GenerateDataForDiceSide(usernames.Count);
            model.GenerateBookingData(usernames.Count);

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
                "server", "src", "main", "resources", "data_generated_small.sql"));

            if (!dataFile.Exists)
            {
                dataFile.Create();
            }
            return dataFile;
        }
    }
}
