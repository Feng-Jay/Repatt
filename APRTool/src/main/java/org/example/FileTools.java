package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

  public static List<String> ergodic(String path, String suffix) {
    List<String> fileList = new ArrayList<>();
    File directory = new File(path);
    File[] files = directory.listFiles();

    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          fileList.addAll(ergodic(file.getAbsolutePath(), suffix));
        } else if (file.getName().endsWith(suffix)) {
          fileList.add(file.getAbsolutePath());
        }
      }
    }
    return fileList;
  }

  public static String readFile2String(String path) {
    File file = new File(path);
    return readFile2String(file);
  }

  public static String readFile2String(File file) {
    try {
      byte[] encodedBytes = Files.readAllBytes(file.toPath());
      return new String(encodedBytes, StandardCharsets.UTF_8);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void writeString2File(String path, String content) {
    File file = new File(path);
    writeString2File(file, content);
  }

  public static void writeString2File(File file, String content) {
    try {
      Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
