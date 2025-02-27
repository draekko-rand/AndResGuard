package com.tencent.mm.resourceproguard.cli;

import com.tencent.mm.androlib.ResourceRepackage;
import com.tencent.mm.resourceproguard.Configuration;
import com.tencent.mm.resourceproguard.InputParam;
import com.tencent.mm.resourceproguard.Main;
import com.tencent.mm.util.TypedValue;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Created by simsun on 1/9/16.
 */
public class CliMain extends Main {

  private static final String ARG_HELP = "--help";
  private static final String ARG_OUT = "-out";
  private static final String ARG_FINAL_APK_PATH = "-finalApkPath";
  private static final String ARG_CONFIG = "-config";
  private static final String ARG_7ZIP = "-7zip";
  private static final String ARG_ZIPALIGN = "-zipalign";
  private static final String ARG_SIGNATURE = "-signature";
  private static final String ARG_KEEPMAPPING = "-mapping";
  private static final String ARG_REPACKAGE = "-repackage";
  private static final String ARG_FIXEDRESNAME = "-fixedresname";
  private static final String ARG_SIGNATURE_TYPE = "-signatureType";
  private static final String VALUE_SIGNATURE_TYPE_V1 = "v1";
  private static final String VALUE_SIGNATURE_TYPE_V2 = "v2";
  private static final String VALUE_SIGNATURE_TYPE_V3 = "v3";
  private static final String VALUE_SIGNATURE_TYPE_V4 = "v4";

  public static void main(String[] args) {
    mBeginTime = System.currentTimeMillis();
    CliMain m = new CliMain();
    setRunningLocation(m);
    m.run(args);
  }

  private static void setRunningLocation(CliMain m) {
    mRunningLocation = m.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
    try {
      mRunningLocation = URLDecoder.decode(mRunningLocation, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (mRunningLocation.endsWith(".jar")) {
      mRunningLocation = mRunningLocation.substring(0, mRunningLocation.lastIndexOf(File.separator) + 1);
    }
    File f = new File(mRunningLocation);
    mRunningLocation = f.getAbsolutePath();
  }

  private static void printUsage(PrintStream out) {
    // TODO: Look up launcher script name!
    String command = "resousceproguard.jar"; //$NON-NLS-1$
    out.println();
    out.println();
    out.println("Usage: java -jar " + command + " input.apk");
    out.println("if you want to special the output path or config file path, you can input:");
    out.println("Such as: java -jar "
                + command
                + " "
                + "input.apk "
                + ARG_CONFIG
                + " yourconfig.xml "
                + ARG_OUT
                + " output_directory");
    out.println("if you want to special the sign or mapping data, you can input:");
    out.println("Such as: java -jar "
                + command
                + " "
                + "input.apk "
                + ARG_CONFIG
                + " yourconfig.xml "
                + ARG_OUT
                + " output_directory "
                + ARG_SIGNATURE
                + " signature_file_path storepass keypass storealias "
                + ARG_KEEPMAPPING
                + " mapping_file_path");
    out.println("if you want to special the signature type, you can input:");
    out.printf("Such as: java -jar %s input.apk %s %s/%s\n",
        command,
        ARG_SIGNATURE_TYPE,
        VALUE_SIGNATURE_TYPE_V1,
        VALUE_SIGNATURE_TYPE_V2,
        VALUE_SIGNATURE_TYPE_V3,
        VALUE_SIGNATURE_TYPE_V4
    );

    out.println("if you want to special 7za or zipalign path, you can input:");
    out.println("Such as: java -jar "
                + command
                + " "
                + "input.apk "
                + ARG_7ZIP
                + " /usr/bin/7za "
                + ARG_ZIPALIGN
                + " /home/username/sdk/tools/zipalign");

    out.println("if you just want to repackage an apk compress with 7z:");
    out.println("Such as: java -jar " + command + " " + ARG_REPACKAGE + " input.apk");
    out.println("if you want to special the output path, 7za or zipalign path, you can input:");
    out.println("Such as: java -jar "
                + command
                + " "
                + ARG_REPACKAGE
                + " input.apk"
                + ARG_OUT
                + " output_directory "
                + ARG_7ZIP
                + " /usr/bin/7za "
                + ARG_ZIPALIGN
                + "/home/username/sdk/tools/zipalign");
    out.println("if you want to special the final apk path, you can input:");
    out.printf("Such as: java -jar %s input.apk %s final_apk_path\n", command, ARG_FINAL_APK_PATH);
    out.println();
    out.println("Flags:\n");

    printUsage(out, new String[] {
        ARG_HELP, "This message.", "-h", "short for -help", ARG_OUT,
        "set the output directory yourself, if not, the default directory is the running location with name of the input file",
        ARG_CONFIG,
        "set the config file yourself, if not, the default path is the running location with name config.xml",
        ARG_SIGNATURE, "set sign property, following by parameters: signature_file_path storepass keypass storealias",
        "  ", "if you set these, the sign data in the config file will be overlayed", ARG_KEEPMAPPING,
        "set keep mapping property, following by parameters: mapping_file_path", "  ",
        "if you set these, the mapping data in the config file will be overlayed", ARG_7ZIP,
        "set the 7zip path, such as /home/shwenzhang/tools/7za, window will be end of 7za.exe", ARG_ZIPALIGN,
        "set the zipalign, such as /home/shwenzhang/sdk/tools/zipalign, window will be end of zipalign.exe",
        ARG_REPACKAGE, "usually, when we build the channeles apk, it may destroy the 7zip.", "  ",
        "so you may need to use 7zip to repackage the apk",
    });
    out.println();
    out.println("if you donot know how to write the config file, look at the comment in the default config.xml");
    out.println("if you want to use 7z, you must install the 7z command line version in window;");
    out.println("sudo apt-get install p7zip-full in linux");
  }

  private static void printUsage(PrintStream out, String[] args) {
    int argWidth = 0;
    for (int i = 0; i < args.length; i += 2) {
      String arg = args[i];
      argWidth = Math.max(argWidth, arg.length());
    }
    argWidth += 2;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < argWidth; i++) {
      sb.append(' ');
    }
    String indent = sb.toString();
    String formatString = "%1$-" + argWidth + "s%2$s"; //$NON-NLS-1$

    for (int i = 0; i < args.length; i += 2) {
      String arg = args[i];
      String description = args[i + 1];
      if (arg.length() == 0) {
        out.println(description);
      } else {
        out.print(wrap(String.format(formatString, arg, description), 300, indent));
      }
    }
  }

  private static String wrap(String explanation, int lineWidth, String hangingIndent) {
    int explanationLength = explanation.length();
    StringBuilder sb = new StringBuilder(explanationLength * 2);
    int index = 0;

    while (index < explanationLength) {
      int lineEnd = explanation.indexOf('\n', index);
      int next;

      if (lineEnd != -1 && (lineEnd - index) < lineWidth) {
        next = lineEnd + 1;
      } else {
        // Line is longer than available width; grab as much as we can
        lineEnd = Math.min(index + lineWidth, explanationLength);
        if (lineEnd - index < lineWidth) {
          next = explanationLength;
        } else {
          // then back up to the last space
          int lastSpace = explanation.lastIndexOf(' ', lineEnd);
          if (lastSpace > index) {
            lineEnd = lastSpace;
            next = lastSpace + 1;
          } else {
            // No space anywhere on the line: it contains something wider than
            // can fit (like a long URL) so just hard break it
            next = lineEnd + 1;
          }
        }
      }
      if (sb.length() > 0) {
        sb.append(hangingIndent);
      } else {
        lineWidth -= hangingIndent.length();
      }
      sb.append(explanation.substring(index, lineEnd));
      sb.append('\n');
      index = next;
    }

    return sb.toString();
  }

  private void run(String[] args) {
    synchronized (CliMain.class) {
      if (args.length < 1) {
        goToError();
      }
      final ReadArgs readArgs = new ReadArgs(args).invoke();
      final File configFile = readArgs.getConfigFile();
      final File signatureFile = readArgs.getSignatureFile();
      final File mappingFile = readArgs.getMappingFile();
      final String keypass = readArgs.getKeypass();
      final String storealias = readArgs.getStorealias();
      final String storepass = readArgs.getStorepass();
      final String signedFile = readArgs.getSignedFile();
      final File outputFile = readArgs.getOutputFile();
      final File finalApkFile = readArgs.getFinalApkFile();
      final String apkFileName = readArgs.getApkFileName();
      final InputParam.SignatureType signatureType = readArgs.getSignatureType();
      final String fixedResName = readArgs.getFixedResName();
      loadConfigFromXml(configFile, signatureFile, mappingFile, keypass, storealias, storepass, fixedResName);

      //For repackage mode, return directly regardless of the previous stuff
      if (signedFile != null) {
        ResourceRepackage repackage = new ResourceRepackage(config.mZipalignPath,
            config.m7zipPath,
            new File(signedFile)
        );
        try {
          if (outputFile != null) {
            repackage.setOutDir(outputFile);
          }
          repackage.repackageApk();
        } catch (IOException | InterruptedException e) {
          e.printStackTrace();
        }
        return;
      }
      System.out.printf("[AndResGuard] begin: %s, %s, %s\n", outputFile, finalApkFile, apkFileName);
      resourceProguard(outputFile, finalApkFile, apkFileName, signatureType);
      System.out.printf("[AndResGuard] done, total time cost: %fs\n", diffTimeFromBegin());
      System.out.printf("[AndResGuard] done, you can go to file to find the output %s\n", mOutDir.getAbsolutePath());
      clean();
    }
  }

  private void loadConfigFromXml(
      File configFile, File signatureFile, File mappingFile, String keypass, String storealias, String storepass, String fixedResName) {
    if (configFile == null) {
      configFile = new File(mRunningLocation + File.separator + TypedValue.CONFIG_FILE);
      if (!configFile.exists()) {
        System.err.printf("the config file %s does not exit", configFile.getAbsolutePath());
        printUsage(System.err);
        System.exit(ERRNO_USAGE);
      }
    }
    try {
      //No need to check command line settings
      if (!mSetSignThroughCmd) {
        signatureFile = null;
      }
      if (!mSetMappingThroughCmd) {
        mappingFile = null;
      }
      config = new Configuration(configFile,
          m7zipPath,
          mZipalignPath,
          mappingFile,
          signatureFile,
          keypass,
          storealias,
          storepass,
          fixedResName
      );
    } catch (IOException | ParserConfigurationException | SAXException e) {
      e.printStackTrace();
      goToError();
    }
  }

  public double diffTimeFromBegin() {
    long end = System.currentTimeMillis();
    return (end - mBeginTime) / 1000.0;
  }

  protected void goToError() {
    printUsage(System.err);
    System.exit(ERRNO_USAGE);
  }

  private class ReadArgs {
    private String[] args;
    private File configFile;
    private File outputFile;
    private File finalApkFile;
    private String apkFileName;
    private String fixedResName;
    private File signatureFile;
    private File mappingFile;
    private String keypass;
    private String storealias;
    private String storepass;
    private InputParam.SignatureType signatureType = InputParam.SignatureType.SchemaV1;
    private String signedFile;

    public ReadArgs(String[] args) {
      this.args = args;
    }

    public File getConfigFile() {
      return configFile;
    }

    public File getOutputFile() {
      return outputFile;
    }

    public File getFinalApkFile() {
      return finalApkFile;
    }

    public String getApkFileName() {
      return apkFileName;
    }

    public File getSignatureFile() {
      return signatureFile;
    }

    public File getMappingFile() {
      return mappingFile;
    }

    public String getKeypass() {
      return keypass;
    }

    public String getFixedResName() {
      return fixedResName;
    }

    public String getStorealias() {
      return storealias;
    }

    public String getStorepass() {
      return storepass;
    }

    public InputParam.SignatureType getSignatureType() {
      return signatureType;
    }

    public String getSignedFile() {
      return signedFile;
    }

    public ReadArgs invoke() {
      for (int index = 0; index < args.length; index++) {
        String arg = args[index];
        if (arg.equals(ARG_HELP) || arg.equals("-h")) {
          goToError();
        } else if (arg.equals(ARG_CONFIG)) {
          if (index == args.length - 1 || !args[index + 1].endsWith(TypedValue.XML_FILE)) {
            System.err.println("Missing XML configuration file argument");
            goToError();
          }
          configFile = new File(args[++index]);
          if (!configFile.exists()) {
            System.err.println(configFile.getAbsolutePath() + " does not exist");
            goToError();
          }
          System.out.printf("special configFile file path: %s\n", configFile.getAbsolutePath());
        } else if (arg.equals(ARG_OUT)) {
          if (index == args.length - 1) {
            System.err.println("Missing output file argument");
            goToError();
          }
          outputFile = new File(args[++index]);
          File parent = outputFile.getParentFile();
          if (parent != null && (!parent.exists())) {
            parent.mkdirs();
          }
          System.out.printf("special output directory path: %s\n", outputFile.getAbsolutePath());
        } else if (arg.equals(ARG_FINAL_APK_PATH)) {
          if (index == args.length - 1) {
            System.err.println("Missing output file argument");
            goToError();
          }
          finalApkFile = new File(args[++index]);
          File parent = finalApkFile.getParentFile();
          if (parent != null && (!parent.exists())) {
            parent.mkdirs();
          }
          System.out.printf("special final apk file path: %s\n", finalApkFile.getAbsolutePath());
        } else if (arg.equals(ARG_SIGNATURE)) {
          //Need to check if there are four parameters
          if (index == args.length - 1) {
            System.err.println("Missing signature data argument, should be "
                               + ARG_SIGNATURE
                               + " signature_file_path storepass keypass storealias");
            goToError();
          }

          //When setting later, it will check whether the file exists
          signatureFile = new File(args[++index]);

          if (index == args.length - 1) {
            System.err.println("Missing signature data argument, should be "
                               + ARG_SIGNATURE
                               + " signature_file_path storepass keypass storealias");
            goToError();
          }

          storepass = args[++index];

          if (index == args.length - 1) {
            System.err.println("Missing signature data argument, should be "
                               + ARG_SIGNATURE
                               + " signature_file_path storepass keypass storealias");
            goToError();
          }

          keypass = args[++index];

          if (index == args.length - 1) {
            System.err.println("Missing signature data argument, should be "
                               + ARG_SIGNATURE
                               + " signature_file_path storepass keypass storealias");
            goToError();
          }
          storealias = args[++index];
          mSetSignThroughCmd = true;
        } else if (arg.equals(ARG_SIGNATURE_TYPE)) {
          if (index == args.length - 1) {
            System.err.println("Missing signature type argument");
            goToError();
          }

          int indexL = ++index;
          if (VALUE_SIGNATURE_TYPE_V2.equalsIgnoreCase(args[indexL])) {
            signatureType = InputParam.SignatureType.SchemaV2;
            System.out.println("Selecting Signature type v2");
          } else if (VALUE_SIGNATURE_TYPE_V3.equalsIgnoreCase(args[indexL])) {
            signatureType = InputParam.SignatureType.SchemaV3;
            System.out.println("Selecting Signature type v3");
          } else if (VALUE_SIGNATURE_TYPE_V4.equalsIgnoreCase(args[indexL])) {
            signatureType = InputParam.SignatureType.SchemaV4;
            System.out.println("Selecting Signature type v4");
          }  else {
            signatureType = InputParam.SignatureType.SchemaV1;
            System.out.println("Selecting Signature type v1");
          }
        } else if (arg.equals(ARG_KEEPMAPPING)) {
          if (index == args.length - 1) {
            System.err.println("Missing mapping file argument");
            goToError();
          }
          //When setting later, it will check whether the file exists
          mappingFile = new File(args[++index]);
          mSetMappingThroughCmd = true;
        } else if (arg.equals(ARG_7ZIP)) {
          if (index == args.length - 1) {
            System.err.println("Missing 7zip path argument");
            goToError();
          }
          m7zipPath = args[++index];
        } else if (arg.equals(ARG_ZIPALIGN)) {
          if (index == args.length - 1) {
            System.err.println("Missing zipalign path argument");
            goToError();
          }

          mZipalignPath = args[++index];
        } else if (arg.equals(ARG_REPACKAGE)) {
          //In this mode, you can work directly and ignore other commands!
          if (index == args.length - 1) {
            System.err.println("Missing the signed apk file argument");
            goToError();
          }
          signedFile = args[++index];
        } else if (arg.equals(ARG_FIXEDRESNAME)) {
          //In this mode, you can work directly and ignore other commands!
          if (index == args.length - 1) {
            System.err.println("Missing the fixed resource name argument");
            goToError();
          }
          fixedResName = args[++index];
          System.out.println("fixedResName " + fixedResName);
        } else {
          apkFileName = arg;
        }
      }
      return this;
    }
  }
}
