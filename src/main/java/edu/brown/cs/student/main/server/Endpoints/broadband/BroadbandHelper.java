package edu.brown.cs.student.main.server.Endpoints.broadband;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BroadbandHelper {

    public static void initializeStateCodes(Map<String, String> mapToPopulate)
            throws URISyntaxException, IOException, InterruptedException {
        String stateDirectoryUri = "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*";
        HttpRequest stateRequest =
                HttpRequest.newBuilder().uri(new URI(stateDirectoryUri)).GET().build();
        HttpResponse<String> stateResponse =
                HttpClient.newHttpClient().send(stateRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = stateResponse.body();
        String[] lines = responseBody.split("\n", 2);
        String statesData = lines.length > 1 ? lines[1] : "";
        Pattern pattern = Pattern.compile("\\[\"(.*?)\",\"(\\d{2})\"\\]");
        Matcher matcher = pattern.matcher(statesData);

        while (matcher.find()) {
            String stateName = matcher.group(1).replace("\"", "");
            String stateCode = matcher.group(2).replace("\"", "");
            mapToPopulate.put(stateName, stateCode);
        }
    }

    public static String getStateCode(String stateName, Map<String, String> mapOfCodes)
            throws IllegalArgumentException {
        if (stateName.equals("*")) {
            return "*";
        }
        String stateCode = mapOfCodes.get(stateName);
        if (stateCode == null) {
            throw new IllegalArgumentException("State name not found: " + stateName);
        }
        return stateCode;
    }

    public static String getCountyCode(String stateCode, String countyName, HttpClient client)
            throws IOException, InterruptedException, URISyntaxException, IllegalArgumentException {
        if (countyName.equals("*")) {
            return "*";
        }
        String countyDirectoryUri =
                "https://api.census.gov/data/2010/dec/sf1?get=NAME&for=county:*&in=state:" + stateCode;
        HttpRequest countyRequest =
                HttpRequest.newBuilder().uri(new URI(countyDirectoryUri)).GET().build();
        HttpResponse<String> countyResponse =
                client.send(countyRequest, HttpResponse.BodyHandlers.ofString());

        String responseBody = countyResponse.body();
        Pattern pattern =
                Pattern.compile("\\[\"(" + countyName + ", .*?)\",\"" + stateCode + "\",\"(\\d{3})\"\\]");
        Matcher matcher = pattern.matcher(responseBody);

        while (matcher.find()) {
            String matchedCounty = matcher.group(1).split(",")[0].trim();
            if (matchedCounty.equalsIgnoreCase(countyName)) {
                return matcher.group(2);
            }
        }

        throw new IllegalArgumentException("County name not found: " + countyName);
    }

    public static String fetchDataFromApi(String stateCode, String county, HttpClient client)
            throws URISyntaxException, IOException, InterruptedException {
        String baseUri = "https://api.census.gov/data/2021/acs/acs1/subject/variables";
        String queryParam = "?get=NAME,S2802_C03_022E&for=county:" + county + "&in=state:" + stateCode;
        URI uri = new URI(baseUri + queryParam);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
