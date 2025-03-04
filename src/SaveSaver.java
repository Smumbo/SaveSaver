
import java.io.File;
import java.util.Arrays;
import java.util.List;


public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> <backupCount>...]

    arguments:
    \t<savePath>\t\tpath to the game save folder

    options:
    \t-u, --upload\t\tupload the save to the cloud
    \t-d, --download \t\tdownload the save from the cloud
    \t    <cloudPath>\t\tpath to the cloud save folder

    \t-b, --backup\t\tcreate a backup of the save folder
    \t    <backupPath>\twhere to save the backup to
    \t    <backupNumber>\tnumber of rotating backups to keep at path
    """;
    private static final List<String> UPLOAD_OPTIONS = List.of("-u", "--upload");
    private static final List<String> DOWNLOAD_OPTIONS = List.of("-d", "--download");
    private static final List<String> BACKUP_OPTIONS = List.of("-b", "--backup");

    private static File savePath;
    private static boolean upload = false;
    private static boolean download = false;
    private static File cloudPath;
    private static List<Backup> backups;

    private static void parse(String[] args) {
        System.out.println(Arrays.toString(args));

        // Save path
        savePath = new File(args[0]);
        if (!savePath.exists()) {
            System.err.println(String.format("Invalid save path (%s), path does not exist", args[0]));
            System.exit(1);
        }
        if (!savePath.isDirectory()) {
            System.err.println(String.format("Invalid save path (%s), path must be a directory", args[0]));
            System.exit(1);
        }

        for (int i = 1; i < args.length; i++) {
            String token = args[i];

            // Upload or Download
            if (UPLOAD_OPTIONS.contains(token)) {
                upload = true;
            }
            if (DOWNLOAD_OPTIONS.contains(token)) {
                download = true;
                
            }
            if (upload && download) {
                System.err.println("Cannot upload and download at the same time");
                System.exit(1);
            }
            if (upload || download) { 
                if (i + 1 >= args.length) {
                    System.err.println("Missing cloud path");
                    System.exit(1);
                }
                cloudPath = new File(args[i + 1]);
                i++;
                if (download) {
                    if (!cloudPath.exists()) {
                        System.err.println(String.format("Invalid cloud path (%s), path does not exist", args[i + 1]));
                        System.exit(1);
                    }
                    if (!cloudPath.isDirectory()) {
                        System.err.println(String.format("Invalid cloud path (%s), path must be a directory", args[i + 1]));
                        System.exit(1);
                    }
                }
                continue;
            }

            // Backups
            if (BACKUP_OPTIONS.contains(token)) {
                if (i + 2 >= args.length) {
                    System.err.println("Missing backup arguments");
                    System.exit(1);
                }
                File backupPath = new File(args[i + 1]);
                if (!backupPath.isDirectory()) {
                    System.err.println(String.format("Invalid backup path (%s), backup path must be a directory", args[i + 1]));
                    System.exit(1);
                }
                int backupNumber = Integer.parseInt(args[i + 2]);
                backups.add(new Backup(backupPath, backupNumber));
                i += 2;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(USAGE_STRING);
        parse(args);
    }
}
