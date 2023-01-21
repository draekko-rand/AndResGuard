package com.tencent.mm.androlib;

import com.tencent.mm.util.FileOperation;
import com.tencent.mm.util.TypedValue;
import com.tencent.mm.util.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceRepackage {

  private final String zipalignPath;
  private final String sevenZipPath;
  private File mSignedApk;
  private File mSignedWith7ZipApk;
  private File mAlignedWith7ZipApk;
  private File m7zipOutPutDir;
  private File mStoredOutPutDir;
  private String mApkName;
  private File mOutDir;

  public ResourceRepackage(String zipalignPath, String zipPath, File signedFile) {
    this.zipalignPath = zipalignPath;
    this.sevenZipPath = zipPath;
    mSignedApk = signedFile;
  }

  public void setOutDir(File outDir) {
    mOutDir = outDir;
  }

  public void repackageApk() throws IOException, InterruptedException {
    insureFileName();

    repackageWith7z();
    alignApk();
    deleteUnusedFiles();
  }

  private void deleteUnusedFiles() {
    //删除目录
    FileOperation.deleteDir(m7zipOutPutDir);
    FileOperation.deleteDir(mStoredOutPutDir);
    if (mSignedWith7ZipApk.exists()) {
      mSignedWith7ZipApk.delete();
    }
  }

  /**
   * There is a little difference here, that is, when the output directory exists,
   * the directory will not be forcibly deleted.
   *
   * @throws IOException
   */
  private void insureFileName() throws IOException {
    if (!mSignedApk.exists()) {
      throw new IOException(String.format("can not found the signed apk file to repackage" + ", path=%s",
          mSignedApk.getAbsolutePath()
      ));
    }
    //You need to install 7zip yourself
    String apkBasename = mSignedApk.getName();
    mApkName = apkBasename.substring(0, apkBasename.indexOf(".apk"));
    // If it has been set up outside, there is no need to set it up
    if (mOutDir == null) {
      mOutDir = new File(mSignedApk.getAbsoluteFile().getParent(), mApkName);
    }

    mSignedWith7ZipApk = new File(mOutDir.getAbsolutePath(), mApkName + "_channel_7zip.apk");
    mAlignedWith7ZipApk = new File(mOutDir.getAbsolutePath(), mApkName + "_channel_7zip_aligned.apk");

    m7zipOutPutDir = new File(mOutDir.getAbsolutePath(), TypedValue.OUT_7ZIP_FILE_PATH);
    mStoredOutPutDir = new File(mOutDir.getAbsolutePath(), "storefiles");
    //Delete the directory, because the previous method is to delete the entire output directory,
    //so there will be no problem, not now, so delete it separately
    FileOperation.deleteDir(m7zipOutPutDir);
    FileOperation.deleteDir(mStoredOutPutDir);
    FileOperation.deleteDir(mSignedWith7ZipApk);
    FileOperation.deleteDir(mAlignedWith7ZipApk);
  }

  private void repackageWith7z() throws IOException, InterruptedException {
    System.out.printf("use 7zip to repackage: %s, will cost much more time\n", mSignedWith7ZipApk.getName());
    HashMap<String, Integer> compressData = FileOperation.unZipAPk(mSignedApk.getAbsolutePath(),
        m7zipOutPutDir.getAbsolutePath()
    );
    //First generate a one-time installation package that is all compressed
    generalRaw7zip();
    ArrayList<String> storedFiles = new ArrayList<>();
    //For uncompressed ones, update them back
    for (String name : compressData.keySet()) {
      File file = new File(m7zipOutPutDir.getAbsolutePath(), name);
      if (!file.exists()) {
        continue;
      }
      int method = compressData.get(name);
      if (method == TypedValue.ZIP_STORED) {
        storedFiles.add(name);
      }
    }

    addStoredFileIn7Zip(storedFiles);
    if (!mSignedWith7ZipApk.exists()) {
      throw new IOException(String.format(
          "[repackageWith7z]7z repackage signed apk fail,you must install 7z command line version first, linux: p7zip, window: 7za, path=%s",
          mSignedWith7ZipApk.getAbsolutePath()
      ));
    }
  }

  private void generalRaw7zip() throws IOException, InterruptedException {
    System.out.printf("general the raw 7zip file\n");
    String outPath = m7zipOutPutDir.getAbsoluteFile().getAbsolutePath();
    String path = outPath + File.separator + "*";

    String cmd = Utils.isPresent(sevenZipPath) ? sevenZipPath : TypedValue.COMMAND_7ZIP;
    ProcessBuilder pb = new ProcessBuilder(cmd, "a", "-tzip", mSignedWith7ZipApk.getAbsolutePath(), path, "-mx9");
    Process pro = pb.start();

    InputStreamReader ir = new InputStreamReader(pro.getInputStream());
    LineNumberReader input = new LineNumberReader(ir);
    //如果不读会有问题，被阻塞
    while (input.readLine() != null) {
    }
    //destroy the stream
    pro.waitFor();
    pro.destroy();
  }

  private void addStoredFileIn7Zip(ArrayList<String> storedFiles) throws IOException, InterruptedException {
    System.out.printf("[addStoredFileIn7Zip]rewrite the stored file into the 7zip, file count:%d\n",
        storedFiles.size()
    );
    String storedParentName = mStoredOutPutDir.getAbsolutePath() + File.separator;
    String outputName = m7zipOutPutDir.getAbsolutePath() + File.separator;
    for (String name : storedFiles) {
      FileOperation.copyFileUsingStream(new File(outputName + name), new File(storedParentName + name));
    }
    storedParentName = storedParentName + File.separator + "*";
    //极限压缩
    String cmd = Utils.isPresent(sevenZipPath) ? sevenZipPath : TypedValue.COMMAND_7ZIP;
    ProcessBuilder pb = new ProcessBuilder(cmd,
        "a",
        "-tzip",
        mSignedWith7ZipApk.getAbsolutePath(),
        storedParentName,
        "-mx0"
    );
    Process pro = pb.start();

    InputStreamReader ir = new InputStreamReader(pro.getInputStream());
    LineNumberReader input = new LineNumberReader(ir);
    //如果不读会有问题，被阻塞
    while (input.readLine() != null) {
    }
    //destroy the stream
    pro.waitFor();
    pro.destroy();
  }

  private void alignApk() throws IOException, InterruptedException {
    if (mSignedWith7ZipApk.exists()) {
      alignApk(mSignedWith7ZipApk, mAlignedWith7ZipApk);
    }
  }

  private void alignApk(File before, File after) throws IOException, InterruptedException {
    System.out.printf("zipaligning apk: %s\n", before.getName());
    if (!before.exists()) {
      throw new IOException(String.format("can not found the raw apk file to zipalign, path=%s",
          before.getAbsolutePath()
      ));
    }
    String cmd = Utils.isPresent(zipalignPath) ? zipalignPath : TypedValue.COMMAND_ZIPALIGIN;
    ProcessBuilder pb = new ProcessBuilder(cmd, "4", before.getAbsolutePath(), after.getAbsolutePath());
    Process pro = pb.start();
    //destroy the stream
    pro.waitFor();
    pro.destroy();
  }
}
