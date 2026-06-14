package in.neelporiya.phases.phase13io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.neelporiya.runner.Concept;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientDemo implements Concept {
    @Override
    public String title() {
        return "Http Client in Java";
    }

    @Override
    public String description() {
        return "Making network calls and parsing JSON";
    }

    record GithubUser(String login, long id, String name, int followers) {}

    @Override
    public void run() throws IOException, InterruptedException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/octocat"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("status: " + response.statusCode());
            System.out.println(response.body());

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            GithubUser user = mapper.readValue(response.body(), GithubUser.class);
            System.out.println(user);
        }
    }
}
