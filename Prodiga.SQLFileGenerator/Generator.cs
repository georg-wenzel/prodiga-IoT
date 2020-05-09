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

            genateDataTable("booking_category", model.AllData["booking_category"].Values, builder);
            genateDataTable("department", model.AllData["department"].Values, builder);
            genateDataTable("team", model.AllData["team"].Values, builder);
            genateDataTable("booking_category_teams", model.AllData["booking_category_teams"].Values, builder);
            genateDataTable("user", model.UserData.Values, builder);
            genateDataTable("user_user_role", model.AllData["user_user_role"].Values, builder);
            genateDataTable("room", model.AllData["room"].Values, builder);
            genateDataTable("raspberry_pi", model.AllData["raspberry_pi"].Values, builder);
            genateDataTable("dice", model.AllData["dice"].Values, builder);
            genateDataTable("dice_side", model.AllData["dice_side"].Values, builder);
            genateDataTable("vacation", model.AllData["vacation"].Values, builder);
            genateDataTable("booking", model.AllData["booking"].Values, builder);
            genateDataTable("badgedb", model.AllData["badgedb"].Values, builder);


            File.WriteAllText(targetFile.FullName, builder.ToString());
        }

        private static void generateAdminUser(StringBuilder builder)
        {
            builder.AppendLine(
                "INSERT INTO user (enabled, first_name, last_name, password, username, create_user_username," +
                $" create_date) VALUES(TRUE, 'Admin', 'Istrator', '{DefaultData.DEFAULT_PASSWORD}'," +
                $" '{DefaultData.ADMIN_USERNAME}', 'admin', '2016-01-01 00:00:00');");
        }

        private static void genateDataTable(string tableName, IEnumerable<Dictionary<string, object>> entries, StringBuilder builder)
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
