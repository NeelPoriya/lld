package in.neelporiya.phases.phase12concurrency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.neelporiya.runner.Concept;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class VirtualThreadsDemo implements Concept {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false
            );
    final int initial = 30;
    String BASE_URL = "https://jsonplaceholder.typicode.com";

    @Override
    public String title() {
        return "";
    }

    @Override
    public String description() {
        return "";
    }

    record Post (Integer id, Integer userId, String title, String body) {}
    record Comment (Integer id, Integer postId, String name, String email, String body) {}

    String fetch(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url)).build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return res.body();
    }

    <T> List<T> deserializeList(String body, Class<T> clazz) throws JsonProcessingException {
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(body, type);
    }

    <T> List<T> getList(String uri, Class<T> clazz) throws IOException, InterruptedException {
        String json = fetch(uri);
        return deserializeList(json, clazz);
    }

    public void fetchPostsWithComments() throws IOException, InterruptedException {
        Instant start = Instant.now();
        List<Post> posts = getList(BASE_URL + "/posts", Post.class);
        List<Post> postsFiltered = posts.stream().limit(initial).toList();

        for (Post post : postsFiltered) {
            String commentUrl = String.format(BASE_URL + "/comments/?postId=" + post.id);
            getList(commentUrl, Comment.class);
        }

        System.out.println("[Serial] Duration: " + Duration.between(start, Instant.now()).toMillis() + " ms.");
    }

    public void fetchPostWithCommentUsingVT() throws IOException, InterruptedException {
        int MAX_CONCURRENT_REQUEST = 30;
        Semaphore sem = new Semaphore(MAX_CONCURRENT_REQUEST);

        Instant start = Instant.now();
        List<Post> posts = getList(BASE_URL + "/posts", Post.class);
        List<Post> filtered = posts.stream().filter(post -> post.id <= initial).toList();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Post post : filtered) {
                executor.submit(() -> {
                    try {
                        sem.acquire();
                        String commentUrl = String.format(BASE_URL + "/comments/?postId=" + post.id);
                        getList(commentUrl, Comment.class);
                    } catch (InterruptedException | IOException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        sem.release();
                    }
                });
            }
        }

        System.out.println("[Virtual Thread] Duration: " + Duration.between(start, Instant.now()).toMillis() + " ms.");
    }

    @Override
    public void run() throws Exception {
        // simpleVirtualThread();
        for (int i = 0; i < 3; ++i) {
            fetchPostWithCommentUsingVT();
            fetchPostsWithComments();
        }
    }
}
