package edu.brown.cs.student.main.server;

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

        // TODO: Implement BroadbandHandler, ViewCSVHandler and SearchCSVHandler according to the user story
        Spark.get("/loadcsv", new LoadCSVHandler());
        Spark.get("/viewcsv", new ViewCSVHandler());
        Spark.get("/searchcsv", new SearchCSVHandler());
        Spark.get("/broadband", new BroadbandHandler());
        Spark.init();
        Spark.awaitInitialization();
        System.out.println("Server is running on http://localhost:" + port);
    }
}
