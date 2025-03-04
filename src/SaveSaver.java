
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;


public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> <backupCount>...]

    arguments:
    \t<savePath>\tpath to the game save folder

    options:
    \t-u, --upload\t\tupload the save to the cloud. This will override the cloud save.
    \t-d, --download\t\tdownload the save from the cloud. This will override the local save.
    \t\t<cloudPath>\tpath to the cloud save folder

    \t-b, --backup\t\tcreate a backup of the save folder
    \t\t<backupPath>\twhere to save the backup to
    \t\t<backupNumber>\tnumber of rotating backups to keep at path
    """;
    private static final List<String> UPLOAD_OPTIONS = List.of("-u", "--upload");
    private static final List<String> DOWNLOAD_OPTIONS = List.of("-d", "--download");
    private static final List<String> BACKUP_OPTIONS = List.of("-b", "--backup");

    private static File savePath;
    private static boolean upload = false;
    private static boolean download = false;
    private static File cloudPath;
    private static List<Backup> backups;

    // Parses the arguments, determines the operations, gets the path arguments, and checks for errors
    private static void parse(String[] args) {
        System.out.println(Arrays.toString(args));

        if (args.length < 2) {
            System.out.println(USAGE_STRING);
            System.exit(1);
        }

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

    // Performs the operations
    private static void process() throws IOException {
        if (upload) {
            System.out.println(String.format("Uploading save from %s to %s", savePath, cloudPath));
            copyDirectory(savePath, cloudPath);
        }
        else if (download) {
            System.out.println(String.format("Downloading save from %s to %s", cloudPath, savePath));
            copyDirectory(cloudPath, savePath);
        }

        for (Backup backup : backups) {
            System.out.println(String.format("Creating backup of %s at %s with %d backups", savePath, backup.path, backup.number));
        }
    }

    // Recursively copies the contents of a directory to another directory
    private static void copyDirectory(File source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        for (String name : source.list()) {
            File sourceFile = new File(source, name);
            File destinationFile = new File(destination, name);

            // Recursive case (folder)
            if (sourceFile.isDirectory()) {
                copyDirectory(sourceFile, destinationFile);
            }
            // Base case (file)
            else {
                try (InputStream in = new FileInputStream(sourceFile); 
                OutputStream out = new FileOutputStream(destinationFile)) {
                    byte[] buf = new byte[1024];
                    int length;
                    while ((length = in.read(buf)) > 0) {
                        out.write(buf, 0, length);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        parse(args);
        process();
    }
}
