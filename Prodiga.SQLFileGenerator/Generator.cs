using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace Prodiga.SQLFileGenerator
{
    public static class Generator
    {
        public static void DoGenerate(DataModel model, FileInfo targetFile)
        {
            StringBuilder builder = new StringBuilder();

            generateAdminUser(builder);

            genateDataTable("booking_category", model.AllData, builder);
            genateDataTable("department", model.AllData, builder);
            genateDataTable("team", model.AllData, builder);
            genateDataTable("booking_category_teams", model.AllData, builder);
            generateTable("user", model.UserData.Values, builder);
            genateDataTable("user_user_role", model.AllData, builder);
            genateDataTable("room", model.AllData, builder);
            genateDataTable("raspberry_pi", model.AllData, builder);
            genateDataTable("dice", model.AllData, builder);
            genateDataTable("dice_side", model.AllData, builder);
            genateDataTable("vacation", model.AllData, builder);
            genateDataTable("booking", model.AllData, builder);
            genateDataTable("badgedb", model.AllData, builder);


            File.WriteAllText(targetFile.FullName, builder.ToString());
        }

        private static void generateAdminUser(StringBuilder builder)
        {
            builder.AppendLine(
                "INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username," +
                $" create_date) VALUES(TRUE, 'Admin', 'Istrator', '{DefaultData.DEFAULT_PASSWORD}'," +
                $" '{DefaultData.ADMIN_USERNAME}', 'admin', '2016-01-01 00:00:00');");

            builder.AppendLine("INSERT INTO user_user_role(user_username, ROLES) VALUES ('admin', 'ADMIN');");
            builder.AppendLine("INSERT INTO user_user_role(user_username, ROLES) VALUES ('admin', 'EMPLOYEE');");
        }

        private static void genateDataTable(string tableName, Dictionary<string, Dictionary<int, Dictionary<string, object>>> entries, StringBuilder builder)
        {
            if(entries.TryGetValue(tableName, out Dictionary<int, Dictionary<string, object>>? data))
            {
                generateTable(tableName, data.Values, builder);
            }
        }

        private static void generateTable(string tableName, IEnumerable<Dictionary<string, object>> entries, StringBuilder builder)
        {
            foreach (Dictionary<string, object> data in entries)
            {
                builder.Append("INSERT INTO ");
                builder.Append(tableName);
                builder.Append("(");
                builder.Append(string.Join(", ", data.Keys));
                builder.Append(") VALUES (");
                builder.Append(string.Join(", ", data.Values.Select(escapeObject)));
                builder.AppendLine(");");
            }

        }

        private static string escapeObject(object value)
        {
            return value switch
            {
                int i => i.ToString(),
                DateTime dt => "'" + dt.ToString("yyyy-MM-dd HH:mm:ss") + "'",
                bool b => b.ToString(),
                string s => "'" + s.Replace("'", "''") + "'",
                _ => "'" + value + "'"
            };
        }
    }
}
