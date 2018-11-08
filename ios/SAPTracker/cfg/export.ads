import static groovy.io.FileType.FILES
import java.io.File

SIGN_DOCKER_FILE_PY = ".xmake/codesign/sign_docker_files.py"
MAVENCLIENT_VERSION = "1.1.1"
CODESIGN_TOOL_DIR =  "${importdir}"

def execute(String... cmd) {
  println "Executing command: ${cmd.join(' ')}"

  def process = new ProcessBuilder(cmd).redirectErrorStream(true).start()
  process.inputStream.eachLine { println it }
  process.waitForOrKill(java.util.concurrent.TimeUnit.MINUTES.toMillis(1))
  return process.exitValue()
}

//check if proper version of mavenclient imported
def mavenclient = new File(CODESIGN_TOOL_DIR, "mavenclient-${MAVENCLIENT_VERSION}/bin/mavenclient")
assert mavenclient.exists()
println "mavenclient check passed"

def setup_android_tool(String toolname) {
  def tool = new File(CODESIGN_TOOL_DIR, "android-8.1.0/" + toolname )
  //check zipalign in the exploded build-tools dir
  tool.setExecutable(true)
  assert tool.canExecute()
  println toolname + " check passed"
  return tool
}

//tools required in the exploded build-tools dir
def zipalign = setup_android_tool("zipalign")
def apksigner = setup_android_tool("apksigner")

if (new File(SIGN_DOCKER_FILE_PY).exists()){
  // central signing available only during release builds
  println "Execute APK central signing and alignment ..."

  //calling xmake signing scrip
  //paths are relative to gen/out in files2sign.json
  assert execute("python", SIGN_DOCKER_FILE_PY, "${gendir}", "${cfgdir}/files2sign.json", "${importdir}") == 0

  //align the already signed apk
  def apkToDeploy = "$gendir/validationapp-release-centralsigned.apk" 
  def apkToSign = "$gendir/src/validationapp/build/outputs/apk/release/validationapp-release-unsigned.apk"
  assert execute(zipalign.absolutePath, "-v", "4", apkToSign, apkToDeploy) == 0

  // google play store checks the APK with this tool - below API level 18 only SHA1 digest algorithm allowed
  // 
  // if you see similar message:
  //   uses digest algorithm SHA-512 and signature algorithm RSA which is not supported on API Level(s) 15-17 for which this APK is being verified
  // 
  // for further info please check "Support of Android 4.2 will be dropped for new apps" here:
  //   https://wiki.wdf.sap.corp/wiki/display/NAAS/Mobile+Announcements#MobileAnnouncements-Android 
  assert execute(apksigner.absolutePath, "verify", "--print-certs", apkToDeploy) == 0
} else {
  // snapshot and milestone builds are not eligible for central signing
  apkExtension = /\.apk$/
  repodir = new File(gendir, "src/java/com.sap.sailing.www/apps")
  repodir.traverse(type : FILES, nameFilter: ~/.*unsigned*.*${apkExtension}/) { apkFile ->
    def apkToDeploy = "$gendir/${apkFile.getName()}"
    def apkToSign = apkFile.getAbsolutePath()

    println "Execute APK local signing ..."
    assert execute(apksigner.absolutePath, "sign", "--ks", "${CODESIGN_TOOL_DIR}/localSigningKeystore-1.0.0.jks", "--ks-pass", "pass:localSigningPassword", "-in", "${apkToSign}", "-out", "${apkToDeploy}" ) == 0 

    assert execute(apksigner.absolutePath, "verify", "--print-certs", apkToDeploy) == 0
  }
}

artifacts builderVersion: "1.1", {
  def groupId = "com.sap.sailing.android"

  apkExtension = /\.apk$/
  repodir = new File(gendir, "src/java/com.sap.sailing.www/apps")
  repodir.traverse(type : FILES, nameFilter: ~/.*unsigned*.*${apkExtension}/) { apkFile ->
    def apkToDeploy = "$gendir/${apkFile.getName()}"
    def artifactId = apkFile.getName().replace("com.sap.sailing.", "")
    def startOfVersion = artifactId.indexOf("-")
    artifactId = artifactId.substring(0, startOfVersion)

    group "$groupId", {
      artifact "$artifactId", {
        file "$apkToDeploy"
      }
    }
  }
}