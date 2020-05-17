using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Prodiga.SQLFileGenerator
{
    public static class DefaultData
    {
        public static readonly string ADMIN_USERNAME = "admin";

        public static readonly string DEFAULT_PASSWORD = "$2a$10$d8cQ7Euz2hM43HOHWolUGeCEZSS/ltJVJYiJAmczl1X5FKzCjg6PC";

        public static readonly Dictionary<int, string> DEFAULT_BOOKING_CAT_NAMES = new Dictionary<int, string>
        {
            { 1, "Pause / Vacation" },
            { 2, "Conceptualizing" },
            { 3, "Design" },
            { 4, "Implementation" },
            { 5, "Testing" },
            { 6, "Documentation" },
            { 7, "Debugging" },
            { 8, "Meeting" },
            { 9, "Customer Support" },
            { 10, "Education and Training" },
            { 11, "Project Management" },
            { 12, "Other" },
        };

        public static readonly DateTime FIRST_DATE = DateTime.Parse("1997-08-23");

        public static void For<T>(this IEnumerable<T> value, Action<T> action)
        {
            if (value == null)
            {
                throw new ArgumentNullException(nameof(value));
            }

            if (action == null)
            {
                return;
            }

            value.ToList().ForEach(action);
        }
    }
}
