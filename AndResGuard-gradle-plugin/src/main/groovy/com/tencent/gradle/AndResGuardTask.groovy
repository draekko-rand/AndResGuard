package com.tencent.gradle

import com.tencent.mm.androlib.res.util.StringUtil
import com.tencent.mm.directory.PathNotExist
import com.tencent.mm.resourceproguard.InputParam
import com.tencent.mm.resourceproguard.Main
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

/**
 * The configuration properties.
 *
 * @author Sim Sun (sunsj1231@gmail.com)
 */
class AndResGuardTask extends DefaultTask {

  @Internal
  AndResGuardExtension configuration

  @Internal
  def debug = 0

  @Internal
  def android

  @Internal
  def buildConfigs = []

  AndResGuardTask() {
    description = 'Assemble Resource Proguard APK'
    group = 'andresguard'
    outputs.upToDateWhen { false }
    android = project.extensions.android
    configuration = project.andResGuard

    if (StringUtil.isPresent(configuration.digestalg) && !configuration.digestalg.contains('-')) {
      throw new RuntimeException("Plz add - in your digestalg, such as SHA-1 or SHA-256")
    }
    android.applicationVariants.all { variant ->
      variant.outputs.every { output ->
        String taskVariantName = this.name["resguard".length()..-1]
        if (taskVariantName.equalsIgnoreCase(variant.buildType.name as String)) {
          if (debug == 1) {
            println("=== Building Config ===")
            println("[AndResGuard] variant.buildType.name: $variant.buildType.name")
            println("[AndResGuard] taskVariantName: $taskVariantName")
            println("[AndResGuard] signingConfig: $variant.variantData.variantDslInfo.signingConfig")
            println("[AndResGuard] output: $output")
            println(outputFile: "$output.outputFile")
            println("===========================================================")
          }
          buildConfigs << new BuildInfo(
                  output.outputFile,
                  variant.variantData.variantDslInfo.signingConfig,
                  variant.variantData.variantDslInfo.applicationId,
                  variant.buildType.name,
                  variant.productFlavors,
                  taskVariantName,
                  variant.mergedFlavor.minSdkVersion.apiLevel,
                  variant.mergedFlavor.targetSdkVersion.apiLevel,
          )
        }
      }
    }
    if (!project.plugins.hasPlugin('com.android.application')) {
      throw new GradleException('generateARGApk: Android Application plugin required')
    }
  }

  static useFolder(file) {
    //remove .apk from filename
    def fileName = file.name[0..-5]
    return "${file.parent}/AndResGuard_${fileName}/"
  }

  @Internal
  def getZipAlignPath() {
    return "${android.getSdkDirectory().getAbsolutePath()}/build-tools/${android.buildToolsVersion}/zipalign"
  }

  @TaskAction
  run() {
    project.logger.info("[AndResGuard] configuartion:$configuration")
    project.logger.info("[AndResGuard] BuildConfigs:$buildConfigs")
    if (debug == 1) println("[AndResGuard] configuartion:$configuration")
    if (debug == 1) println("[AndResGuard] BuildConfigs:$buildConfigs")

    buildConfigs.each { config ->
      if (config.file == null || !config.file.exists()) {
        throw new PathNotExist("Original APK doesn't exist")
      }
      RunGradleTask(config, config.file.getAbsolutePath(), config.minSDKVersion, config.targetSDKVersion)
    }
  }

  def RunGradleTask(config, String absPath, int minSDKVersion, int targetSDKVersion) {
    def signConfig = config.signConfig
    if (debug == 1) println("[AndResGuard] signConfig:$signConfig")
    String packageName = config.packageName
    ArrayList<String> whiteListFullName = new ArrayList<>()
    ExecutorExtension sevenzip = project.extensions.findByName("sevenzip") as ExecutorExtension
    configuration.whiteList.each { res ->
      if (res.startsWith("R")) {
        whiteListFullName.add(packageName + "." + res)
      } else {
        whiteListFullName.add(res)
      }
    }

    InputParam.Builder builder = new InputParam.Builder()
        .setMappingFile(configuration.mappingFile)
        .setWhiteList(whiteListFullName)
        .setUse7zip(configuration.use7zip)
        .setMetaName(configuration.metaName)
        .setFixedResName(configuration.fixedResName)
        .setKeepRoot(configuration.keepRoot)
        .setMergeDuplicatedRes(configuration.mergeDuplicatedRes)
        .setCompressFilePattern(configuration.compressFilePattern)
        .setZipAlign(getZipAlignPath())
        .setSevenZipPath(sevenzip.path)
        .setOutBuilder(useFolder(config.file))
        .setApkPath(absPath)
        .setUseSign(configuration.useSign)
        .setDigestAlg(configuration.digestalg)
        .setMinSDKVersion(minSDKVersion)
        .setTargetSDKVersion(targetSDKVersion)

    if (configuration.finalApkBackupPath != null && configuration.finalApkBackupPath.length() > 0) {
      builder.setFinalApkBackupPath(configuration.finalApkBackupPath)
    } else {
      builder.setFinalApkBackupPath(absPath)
    }

    if (configuration.useSign) {
      if (signConfig == null) {
        throw new GradleException("can't the get signConfig for release build")
      }
      builder.setSignFile(signConfig.storeFile)
          .setKeypass(signConfig.keyPassword)
          .setStorealias(signConfig.keyAlias)
          .setStorepass(signConfig.storePassword)
      if (signConfig.hasProperty('v2SigningEnabled') && signConfig.v2SigningEnabled) {
        builder.setSignatureType(InputParam.SignatureType.SchemaV2)
        println "Using Signature Schema V2"
      } else if (signConfig.hasProperty('v3SigningEnabled') && signConfig.v3SigningEnabled) {
        builder.setSignatureType(InputParam.SignatureType.SchemaV3)
        println "Using Signature Schema V3"
      } else if (signConfig.hasProperty('v4SigningEnabled') && signConfig.v4SigningEnabled) {
        builder.setSignatureType(InputParam.SignatureType.SchemaV4)
        println "Using Signature Schema V4"
      } else {
        builder.setSignatureType(InputParam.SignatureType.SchemaV1)
        println "Using Signature Schema V1"
      }
    }
    InputParam inputParam = builder.create()
    Main.gradleRun(inputParam)
  }
}
