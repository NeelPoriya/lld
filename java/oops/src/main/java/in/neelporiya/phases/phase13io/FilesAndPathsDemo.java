package in.neelporiya.phases.phase13io;

import in.neelporiya.runner.Concept;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FilesAndPathsDemo implements Concept {
    @Override
    public String title() {
        return "Files and Paths Demo";
    }

    @Override
    public String description() {
        return "Files CRUD using Java";
    }

    @Override
    public void run() throws IOException {
        // prefer java.nio.file over legacy java.io
        Path path = Path.of("demo.txt");
        Files.writeString(path, "line 1\nline 2\nline 3");
        String content = Files.readString(path);
        List<String> lines = Files.readAllLines(path);
        System.out.println(lines);

        try (Stream<String> stream = Files.lines(path)) {
            long count = stream.filter(l -> !l.isBlank()).count();
            System.out.println(count);
        }

        System.out.println(Files.exists(path));
        Files.deleteIfExists(path);
    }
}
