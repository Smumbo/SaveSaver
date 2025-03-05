import java.nio.file.Path;

public class Backup {
    public Path path;
    public int number;

    public Backup(Path path, int number) {
        this.path = path;
        this.number = number;
    }
}
