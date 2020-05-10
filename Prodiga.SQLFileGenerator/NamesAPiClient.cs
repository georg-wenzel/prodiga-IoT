using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using System.Threading.Tasks;

namespace Prodiga.SQLFileGenerator
{
    public class NamesAPiClient : IDisposable
    {
        #region const

        private const string BASE_URL = "http://names.drycodes.com/";
        private const string PATH_FIRST = "{0}?nameOptions=starwarsFirstNames";
        private const string PATH_LAST = "{0}?nameOptions=starwarsLastNames";

        #endregion

        #region fields

        private readonly HttpClient client;

        #endregion

        #region ctor

        public NamesAPiClient()
        {
            client = new HttpClient()
            {
                BaseAddress = new Uri(BASE_URL),
            };
            client.DefaultRequestHeaders.Clear();
            client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }

        #endregion

        #region methods

        public async Task<HashSet<string>> GetNamesAsync(int amount)
        {
            Task<List<string>> result1 = makeRequest(PATH_FIRST, amount);
            List<string> listLastNames = await makeRequest(PATH_LAST, amount);
            List<string> listFirstNames = await result1;

            if (listFirstNames.Count != listLastNames.Count)
            {
                throw new Exception("Not equal first and last names");
            }

            return Enumerable.Range(0, listFirstNames.Count)
                .Select(x => listFirstNames[x].Replace("_", " ") + "_" + listLastNames[x].Replace("_", " "))
                .ToHashSet();
        }

        public void Dispose()
        {
            client.Dispose();
            GC.SuppressFinalize(this);
        }

        private async Task<List<string>> makeRequest(string path, int amount)
        {
            HttpResponseMessage response = await client.GetAsync(string.Format(path, amount));

            if (!response.IsSuccessStatusCode)
            {
                throw new Exception("Error while getting random names - got " + response.StatusCode);
            }

            string resultContent = await response.Content.ReadAsStringAsync();

            if (string.IsNullOrWhiteSpace(resultContent))
            {
                throw new Exception("Empty response while getting names");
            }

            return JsonSerializer.Deserialize<List<string>>(resultContent).ToList();
        }


        #endregion

    }
}
