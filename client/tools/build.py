#!/usr/bin/env python
import os, shutil, codecs, json, subprocess, sys, zipfile
from collections import namedtuple

BASEDIR = os.path.split(os.path.realpath(__file__))[0]
BASEDIR = os.path.join(BASEDIR, '..')
CHANNEL_DIR = os.path.join(BASEDIR, 'channels')
CHANNELINFO_DIR = os.path.join(BASEDIR, 'channelinfo')
CPP_SRC_ROOT = os.path.join(BASEDIR, 'chameleon', 'src', 'main', 'chameleoncb')
BUILD_SCRIPT_DIR = os.path.join(BASEDIR, 'tools', 'buildscript')
BUILD_TOOL_DIR = os.path.join(BASEDIR, 'tools', 'buildtool')

def copyFileInList(srcroot, targetroot, filelist):
    for f in filelist:
        fullpath = os.path.join(targetroot, f)
        path, fname = os.path.split(fullpath)
        if not os.path.exists(path):
            os.makedirs(path)
        shutil.copy2(os.path.join(srcroot, f), fullpath)

ChannelInfo = namedtuple('ChannelInfo', 'name path cfg script')

def archive(p, rootdir, basedir):
    shutil.make_archive(p, 'zip', rootdir, os.path.relpath(basedir, rootdir))

def loadJsonFile(channelPath):
    cfgJsonFile = os.path.join(channelPath, 'chameleon_build', 'cfg.json')
    with codecs.open(cfgJsonFile, 'r', 'utf8') as f:
        obj = json.load(f)
        if not ('name' in obj and
                'chamversion' in obj and
                'version' in obj and
                'cfgitem' in obj):
            raise RuntimeError('illegal cfg in %s %s' %(cfgJsonFile, obj))
        return obj

def getChannelScript(channelPath):
    scriptPath = os.path.join(channelPath, 'chameleon_build', 'script.js')
    if os.path.exists(scriptPath):
        return scriptPath
    else:
        return None

def getChannelInfo(channel, channelPath):
    scriptFile = getChannelScript(channelPath)
    cfg = loadJsonFile(channelPath)
    if scriptFile:
        cfg['script'] = '%s_script' %channel
    return ChannelInfo(channel, channelPath, cfg, scriptFile)

def collectChannelInfo(channelParentFolder):
    folders = os.listdir(channelParentFolder)
    result = []
    for folder in folders:
        try:
            folderPath = os.path.join(channelParentFolder, folder)
            if os.path.isdir(folderPath) and \
                os.path.exists(os.path.join(folderPath, 'chameleon_build', 'cfg.json')):
                result.append(getChannelInfo(folder,
                    os.path.join(channelParentFolder, folder)))
            else:
                print(sys.stdout, 'ignore %s' %folder)
        except Exception as e:
            print(sys.stderr, 'Fail to collect channel info for %s' %folder)
            raise e
    return result

def packChannels(channelParentFolder, targetParentFolder):
    channelInfos = collectChannelInfo(channelParentFolder)
    build_channel_path = os.path.join(BUILD_TOOL_DIR, 'chameleon_tool')
    build_channel = os.path.join(build_channel_path, 'build_channel.py')
    print(build_channel)
    print('*********************start build channels**************************')
    for ci in channelInfos:
        copyChannel(build_channel, ci.name, CHANNEL_DIR, targetParentFolder, {"version": ci.cfg["chamversion"], "realVer": ci.cfg["version"]})
    print('*********************end build channels**************************')
    return channelInfos

def copyChannel(buildchannel, channel, channelPath,  targetPath,  versionInfo):
    if not os.path.exists(targetPath):
        os.makedirs(targetPath)
    genCmd(buildchannel, channel, channelPath, targetPath)

def genCmd(buildchannel, channel, channelPath,  targetPath):
    paras = []
    paras.append(('python', buildchannel))
    paras.append(('-c', channel))
    paras.append(('-r', channelPath))
    paras.append(('-g', targetPath))
    os.system(' '.join([x+' '+y for (x,y) in paras]))

def initTargetScriptFolder(targetScriptFolder):
    if not os.path.exists(targetScriptFolder):
        os.makedirs(targetScriptFolder)

def copyChameleonCpp(targetFolder):
    cppTargetFolder = os.path.join(targetFolder, 'Resource', 'chameleon', 'chameleoncb')
    archive(os.path.join(targetFolder, 'Resource', 'chameleoncb'), os.path.join(CPP_SRC_ROOT, '..'), CPP_SRC_ROOT)


# copy channel folders and channel resources, generate build info json
def initProjectFolder(targetFolder, version):
    targetSDKPath = os.path.join(targetFolder, 'sdk')
    #this is channels zip list
    targetScriptPath = os.path.join(targetFolder, 'script')
    toolPath = os.path.join(targetFolder, 'tools')
    infoJsonFile = os.path.join(targetFolder, 'info.json')
    channelListFile = os.path.join(CHANNELINFO_DIR, 'channellist.json')
    shutil.copytree(BUILD_SCRIPT_DIR, targetFolder)
    copyChameleonCpp(targetFolder)
    initTargetScriptFolder(targetScriptPath)
    os.makedirs(toolPath)

    #packChannels
    channelInfos = packChannels(CHANNEL_DIR, targetSDKPath)

    with codecs.open(channelListFile, 'r', 'utf8') as channelListFObj:
        channellistobj = json.load(channelListFObj)
    cfg = {'version': version, 'channels': [x.cfg for x in channelInfos],
            'channellist': channellistobj}
    with codecs.open(infoJsonFile, 'w', 'utf8') as f:
        json.dump(cfg, f, indent=4)
    for ci in channelInfos:
        if ci.script:
            shutil.copy2(ci.script, os.path.join(targetScriptPath, ci.name+'_script.js'))
    shutil.copytree(CHANNELINFO_DIR, os.path.join(targetFolder, 'channelinfo'))
    shutil.copytree(BUILD_TOOL_DIR, os.path.join(toolPath, 'buildtool'))

def compileChannel(gradleCmd, channel):
    taskTarget = ':channels:%s' %channel
    def task(task):
        return taskTarget+':%s'%task
    ret = subprocess.check_call([gradleCmd, task('assembleRelease')])
    if ret != 0:
        raise RuntimeError('Fail to assemble the chameleon sdk')

if sys.platform == 'win32':
    GRADLE_CWD = 'gradlew.bat'
    NPM_CMD = 'npm.cmd'
    BOWER_CMD = 'bower.cmd'
else:
    GRADLE_CWD = 'gradlew'
    NPM_CMD = 'npm'
    BOWER_CMD = 'bower'

def compileAllChannels(channels):
    olddir = os.getcwd()
    os.chdir(BASEDIR)
    try:
        gradleCmd = os.path.join(BASEDIR, GRADLE_CWD)
        for channel in channels:
            compileChannel(gradleCmd, channel)
    finally:
        os.chdir(olddir)


def buildChameleonLib():
    olddir = os.getcwd()
    os.chdir(BASEDIR)
    try:
        gradleCmd = os.path.join(BASEDIR, GRADLE_CWD)
        ret = subprocess.check_call([gradleCmd, 'chameleon:clean'])
        if ret != 0:
            raise RuntimeError('Fail to clean the chameleon sdk')
        ret = subprocess.check_call([gradleCmd, 'chameleon_unity:clean'])
        if ret != 0:
            raise RuntimeError('Fail to clean the chameleon unity sdk')
        ret = subprocess.check_call([gradleCmd, 'chameleon:assembleRelease'])
        if ret != 0:
            raise RuntimeError('Fail to assemble the chameleon sdk')
        ret = subprocess.check_call([gradleCmd, 'chameleon_unity:assembleRelease'])
        if ret != 0:
            raise RuntimeError('Fail to assemble the chameleon unity sdk')

        chameleonLibPath = os.path.join("chameleon", "chameleon_build")
        if not os.path.exists(chameleonLibPath):
            os.makedirs(chameleonLibPath)
        shutil.copy2(os.path.join("chameleon", "build", "intermediates", "bundles", "release", "classes.jar"),
            os.path.join(chameleonLibPath, 'chameleon.jar'))
        shutil.copy2(os.path.join("chameleon_unity", "build", "intermediates", "bundles", "release", "classes.jar"),
            os.path.join(chameleonLibPath, 'chameleon_unity.jar'))
    finally:
        os.chdir(olddir)

def cleanOldBuild(targetFolder):
    if os.path.exists(targetFolder):
        print('cleaning existing build folder %s ...' %(targetFolder))
        shutil.rmtree(targetFolder)

def getversion():
    versionFolder = os.path.join(BASEDIR, '..', 'version')
    versionFile = os.path.join(versionFolder, 'version.txt')
    with open(versionFile, 'r') as f:
        l = f.readline()
        return l.strip('\n')


def unzipFiles(zf, targetDir):
    with zipfile.ZipFile(zf) as zipf:
        zipf.extractall(targetDir)

def buildChameleonClient(chameleonFolder, targetFolder, place):
    if not os.path.exists(targetFolder):
        os.mkdir(targetFolder)
    appPath = os.path.join(targetFolder, 'app')
    if not os.path.exists(appPath):
        os.mkdir(appPath)
    shutil.copytree(chameleonFolder, os.path.join(targetFolder, 'app', 'chameleon'))
    placePlatformStartScript(targetFolder)
    if place is not None:
        place(os.path.join(targetFolder, 'nw'))

def buildChameleonClientMacOS(chameleonFolder, targetFolder, place):
    if not os.path.exists(targetFolder):
        os.mkdir(targetFolder)
    shutil.copytree(chameleonFolder, os.path.join(targetFolder, 'chameleon'))
    if place is not None:
        place(targetFolder)

def placePlatformStartScript(targetFolder):
    if sys.platform == 'win32':
        with open(os.path.join(targetFolder, 'chameleon.bat'), 'w') as f:
            f.write('start nw\\nw.exe chameleon_client')
    else:
        with open(os.path.join(targetFolder, 'chameleon.bat'), 'w') as f:
            f.write('open nw\\nw.app chameleon_client')

def placeNodeWebkitWin(targetFolder):
    src = os.path.join('nodewebkit-bin', 'nodewebkit-win.zip')
    unzipFiles(src, targetFolder)

def placeNodeWebkitOsx(targetFolder):
    src = os.path.join('nodewebkit-bin', 'node-webkit.app')
    shutil.copytree(src, os.path.join(targetFolder, 'node-webkit.app'))

def addNodeWebkit(targetFolder):
    chameleonFolder = os.path.join(targetFolder, 'chameleon')
    if sys.platform == 'win32':
        buildChameleonClient(chameleonFolder, os.path.join(targetFolder, 'chameleon_client_win'), placeNodeWebkitWin)
    else:
        buildChameleonClientMacOS(chameleonFolder, os.path.join(targetFolder, 'chameleon_client_osx'), placeNodeWebkitOsx)

def gruntNode():
    print(os.getcwd())
    os.chdir('chameleon_client')
    print(os.getcwd())
    os.system('grunt pack --force')

def build():
    targetFolder = os.path.join(BASEDIR, 'chameleon_build')
    cleanOldBuild(targetFolder)
    version = getversion()
    chameleonTarget = os.path.join(targetFolder, 'chameleon')
    print('get version is %s' %version)

    print('build chameleon libs...')
    buildChameleonLib()

    print('start initing build folder...')
    initProjectFolder(chameleonTarget, version)

    print('build chameleon client node-webkit nw.exe...')
    addNodeWebkit(targetFolder)

    #execute grunt
    print('build chameleon client execute grunt...')
    gruntNode()

    print('done')

build()
