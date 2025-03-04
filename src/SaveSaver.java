
import java.io.File;
import java.util.Arrays;
import java.util.List;


public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> <backupNumber>...]

    arguments:
    \t<savePath>\t\tpath to the game save folder

    options:
    \t-u, --upload\t\tupload the save to the cloud
    \t-d, --download \t\tdownload the save from the cloud
    \t    <cloudPath>\t\tpath to the cloud save folder

    \t-b, --backup\t\tcreate a backup of the save folder
    \t    <backupPath>\twhere to save the backup to
    \t    <backupNumber>\tnumber of rotating backups to keep
    """;
    private static final List<String> UPLOAD_OPTIONS = List.of("-u", "--upload");
    private static final List<String> DOWNLOAD_OPTIONS = List.of("-d", "--download");
    private static final List<String> BACKUP_OPTIONS = List.of("-b", "--backup");

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

    public static void parse(String[] args) {

    }

    public static void main(String[] args) throws Exception {
        System.out.println(USAGE_STRING);
        System.out.println(Arrays.toString(args));
        parse(args);
    }
}
