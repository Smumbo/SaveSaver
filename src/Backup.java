import java.nio.file.Path;

public class Backup {
    public Path path;
    public int max;

    public Backup(Path path, int max) {
        this.path = path;
        this.max = max;
    }
}
