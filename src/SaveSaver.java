
import java.io.File;
import java.util.Arrays;
import java.util.List;


public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> <backupNumber>]
    arguments:
    \t<savePath>\t\tpath to the Minecraft save folder
    options:
    \t-u,--upload\t\tupload the save to the cloud
    \t-d, --download \t\tdownload the save from the cloud
    \t\t<cloudPath>\t\tpath to the cloud save folder
    
    """;

    private File savePath;
    private boolean upload;
    private boolean download;
    private File cloudPath;
    private List<Backup> backups;

    private class Backup {
        public File path;
        public int number;

        public Backup(File path, int number) {
            this.path = path;
            this.number = number;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(USAGE_STRING);
        System.out.println(Arrays.toString(args));
    }
}
