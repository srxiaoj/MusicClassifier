import java.io.File;
import com.google.common.flogger.FluentLogger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MusicClassifier {

  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final Path ROOT = Paths.get("E:\\");
  private static final String COPIED_FILE = "(1).";
  private static final String SPLITTER = "-";

  public static void main(String[] args) {
    // Path path = Paths.get(ROOT.toString(), "CloudMusic - 副本");
    // removeDuplicatedFiles(path);

    Path path = Paths.get(ROOT.toString(), "123");
    moveSongToSingerDir(path);
  }

  private static void moveSongToSingerDir(Path path) {
    File dir = new File(path.toString());
    File[] array = dir.listFiles();

    Map<String, Long> map = getSizeMap(path);
    for (int i = 0; i < array.length; i++) {
      File file = array[i];
      if (file.isHidden()) {
        continue;
      }
      if (file.isFile()) {
        try {
          Optional<String> maybeSinger = getSinger(file);
          if (maybeSinger.isPresent()) {
            Path toPath = Paths.get(path.toString(), maybeSinger.get());
            File toDir = new File(toPath.toString());
            if (!toDir.exists()) {
              boolean result = toDir.mkdir();
              // logger.atInfo().log("Creating dir: %s, status: %s", toDir.getName(), result);
            }
            Path toFile = Paths.get(toPath.toString(), file.getName());
            logger.atInfo().log("Moving file from %s to %s", file.toPath(), toFile);
            Files.move(file.toPath(), toFile);
          }
        } catch (Exception e) {
          logger.atWarning().log(e.getMessage());
        }
      }
    }
  }

  private static Map<String, Long> getSizeMap(Path path) {
    Map<String, Long> map = new HashMap<>();
    File dir = new File(path.toString());
    File[] array = dir.listFiles();

    for (int i = 0; i < array.length; i++) {
      File file = array[i];
      if (file.isHidden()) {
        continue;
      }
      if (file.isFile()) {
        // logger.atInfo().log("%s size is %s kb", file.getName(), file.length() / 1024);
        map.put(file.getName(), file.length());
      }
    }
    return map;
  }

  // 删除重复文件
  private static void removeDuplicatedFiles(Path path) {
    File dir = new File(path.toString());
    File[] array = dir.listFiles();

    for (int i = 0; i < array.length; i++) {
      File file = array[i];
      if (file.isHidden()) {
        continue;
      }
      if (file.isFile()) {
        if (isDuplicatedFile(file)) {
          try {
            deleteFile(file);
          } catch (Exception e) {
            logger.atWarning().log(e.getMessage());
          }
        }
      } else {
        removeDuplicatedFiles(Paths.get(array[i].getPath()));
      }
    }
  }

  private static Optional<String> getSinger(File file) throws Exception {
    if (file.isDirectory()) {
      throw new Exception("the file to be deleted is a directory: " + file.getName());
    }
    if (!file.getName().contains(SPLITTER)) {
      logger.atWarning().log("Cannot find singer for file: %s", file.getName());
      return Optional.empty();
    }
    String[] pair = file.getName().replaceAll("\\s", "").split(SPLITTER);
    return Optional.of(pair[0]);
  }

  private static boolean isDuplicatedFile(File file) {
    if (file.getName().contains(COPIED_FILE)) {
      return true;
    }
    return false;
  }

  private static void deleteFile(File file) throws Exception {
    if (file.isDirectory()) {
      throw new Exception("the file to be deleted is a directory: " + file.getName());
    }
    logger.atInfo().log("Deleting file: %s", file.getName());
    boolean isDeleted = file.delete();
    logger.atInfo().log("result: %s", isDeleted);
  }

}
