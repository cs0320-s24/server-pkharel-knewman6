package edu.brown.cs.student.main.server;

import edu.brown.cs.student.main.server.Endpoints.BroadbandHandler;
import edu.brown.cs.student.main.server.Endpoints.LoadCSVHandler;
import edu.brown.cs.student.main.server.Endpoints.SearchCSVHandler;
import edu.brown.cs.student.main.server.Endpoints.ViewCSVHandler;
import spark.Spark;
import static spark.Spark.after;

public class Server {
    public static void main(String[] args) {
        int port = 3232;
        Spark.port(port);

        after(
                (request, response) -> {
                    response.header("Access-Control-Allow-Origin", "*");
                    response.header("Access-Control-Allow-Methods", "GET");
                });

        Spark.get("/loadcsv", new LoadCSVHandler());
        Spark.get("/viewcsv", new ViewCSVHandler());
        Spark.get("/searchcsv", new SearchCSVHandler());
        Spark.get("/broadband", new BroadbandHandler());
        Spark.init();
        Spark.awaitInitialization();
        System.out.println("Server is running on http://localhost:" + port);
    }
}
