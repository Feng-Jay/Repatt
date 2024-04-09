/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.config;

/**
 * This class contains all constant variables
 *
 * @author Jiajun
 */
public class Constant {

    public static final String HOME = System.getProperty("user.dir");

    // common info
    public static final String SOURCE_FILE_SUFFIX = ".java";

    public static final String JAR_FILE_SUFFIX = ".jar";

    // build failed flag
    public static final String ANT_BUILD_FAILED = "BUILD FAILED";

    // code search configure
    public static final int MAX_BLOCK_LINE = 10;
    public static final boolean USE_GROUNDTRUTH = true;
    public static int PATCH_NUM = 1;

    // useful file path
    public static String PROJECT_HOME = null;
    public static String ORI_FAULTLOC = HOME + "/d4j-info/location/ochiai";
    public static String CONVER_FAULTLOC = HOME + "/d4j-info/location/conver";
    public static String PROJ_INFO = HOME + "/d4j-info/src_path";
    public static String PROJ_JSON_FILE = HOME + "/d4j-info/project.json";
    public static String PROJ_LOG_BASE_PATH = HOME + "/log";
    public static String PROJ_REALTIME_LOC_BASE = HOME + "/d4j-info/realtime/location";

    // command configuration
    public static final String COMMAND_CD = "cd ";
    public static final int COMPILE_TIMEOUT = 120;
    public static final int SBFL_TIMEOUT = 3600;

    public static final boolean ENABLE_STMT_FIX = false;
    public static final boolean ENABLE_TAC_FIX = true;

    public static final boolean ENABLE_TOKEN_FIX = true;
    public static final boolean ENABLE_TEST = true;

    public static final int MAX_LOCATION_FOR_A_PROJECT = 100;

    public static final String PATH_TO_ASTNN = "/data/Transformed-ASTNN";
    public static final String PATH_TO_PYTHON = "/root/miniconda3/envs/transastnn/bin/python";

    public static final boolean USE_FILTER = false;

    //public static final String PATH_TO_ASTNN = "/Users/higgs/PycharmProjects/TransASTNN/Transformed-ASTNN";
    //public static final String PATH_TO_PYTHON = "/Users/higgs/PycharmProjects/TransASTNN/venv/bin/python";

    public static final int TOKEN_MIN_SUPPORT = 3;

    public static final int TOKEN_MAX_PATCH_ONE_LOCATION = 3;

    public static final int STMT_MAX_PATCH_ONE_LOCATION = 3;

    public static final int ALL_TOKEN_MAX_PATCH_ONE_LOCATION = 200;

    public static final int ALL_STMT_MAX_PATCH_ONE_LOCATION = 1000;

    public static final int MAX_GENERATED_PATCH = 1000;

    public static final String LOCATOR_HOME = HOME + "/sbfl";
    public static final String COMMAND_LOCATOR = LOCATOR_HOME + "/sbfl.sh ";
    public static final String LOCATOR_SUSP_FILE_BASE = LOCATOR_HOME + "/ochiai";
    public static String ENV_D4J = "DEFECTS4J_HOME";
    public static String COMMAND_TIMEOUT = "/usr/bin/timeout ";

    public static boolean is_server = false;
    public static String COMMAND_D4J = null;
}
