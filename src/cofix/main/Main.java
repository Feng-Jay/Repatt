/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved. Unauthorized copying of this file via any
 * medium is strictly prohibited Proprietary and Confidential. Written by Jiajun
 * Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.OchiaiResult;
import org.apache.commons.io.FileUtils;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;

/**
 * @author Jiajun
 * @date Jun 19, 2017
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Constant.PATCH_NUM = 1;

        Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
        Pair<String, Set<Integer>> options = parseCommandLine(args, projInfo);
        if (Constant.PROJECT_HOME == null
                || options.getFirst() == null
                || options.getSecond().size() == 0) {
            printUsage();
            System.exit(0);
        }

        Configure.configEnvironment();
        System.out.println(Constant.PROJECT_HOME);

        System.setProperty("proj_name", options.getFirst() + "_" + options.getSecond());

        flexibelConfigure(options.getFirst(), options.getSecond(), projInfo);
    }

    private static void trySplitFix(Subject subject, boolean purify) throws IOException {

        String logFile =
                Constant.PROJ_LOG_BASE_PATH + "/" + subject.getName() + "/" + subject.getId() + ".log";
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("=================================================\n");
        stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yy/MM/dd HH:mm");
        stringBuffer.append("start : " + simpleFormat.format(new Date()) + "\n");
        System.out.println(stringBuffer);
        JavaFile.writeStringToFile(logFile, stringBuffer.toString(), true);

        ProjectInfo.init(subject);

        // backup source files.
        subject.backup(subject.getHome() + subject.getSsrc());
        subject.backup(subject.getHome() + subject.getTsrc());

        // delete compiled bytecode.
        FileUtils.deleteDirectory(new File(subject.getHome() + subject.getTbin()));
        FileUtils.deleteDirectory(new File(subject.getHome() + subject.getSbin()));

        // TODO:change back to SBFL Locator after testing
        AbstractFaultlocalization sbfLocator = new OchiaiResult(subject);

        subject.setAbstractFaultlocalization(sbfLocator);

        // Do preprocess
        subject.doPreProcess();

        subject.restore(subject.getHome() + subject.getSsrc());
        subject.restore(subject.getHome() + subject.getTsrc());
        System.exit(0);
    }

    private static Pair<String, Set<Integer>> parseCommandLine(
            String[] args, Map<String, Pair<Integer, Set<Integer>>> projInfo) {
        Pair<String, Set<Integer>> options = new Pair<String, Set<Integer>>();
        if (args.length < 3) {
            return options;
        }
        String projName = null;
        Set<Integer> idSet = new HashSet<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--proj_home=")) {
                Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
            } else if (args[i].startsWith("--proj_name=")) {
                projName = args[i].substring("--proj_name=".length());
            } else if (args[i].startsWith("--bug_id=")) {
                String idseq = args[i].substring("--bug_id=".length());
                if (idseq.equalsIgnoreCase("single")) {
                    idSet.addAll(projInfo.get(projName).getSecond());
                } else if (idseq.equalsIgnoreCase("multi")) {
                    for (int id = 1; id <= projInfo.get(projName).getFirst(); id++) {
                        if (projInfo.get(projName).getSecond().contains(id)) {
                            continue;
                        }
                        idSet.add(id);
                    }
                } else if (idseq.equalsIgnoreCase("all")) {
                    for (int id = 1; id <= projInfo.get(projName).getFirst(); id++) {
                        idSet.add(id);
                    }
                } else if (idseq.contains("-")) {
                    int start = Integer.parseInt(idseq.substring(0, idseq.indexOf("-")));
                    int end = Integer.parseInt(idseq.substring(idseq.indexOf("-") + 1));
                    for (int id = start; id <= end; id++) {
                        idSet.add(id);
                    }
                } else {
                    String[] split = idseq.split(",");
                    for (String string : split) {
                        int id = Integer.parseInt(string);
                        idSet.add(id);
                    }
                }
            }
        }
        options.setFirst(projName);
        options.setSecond(idSet);
        return options;
    }

    private static void printUsage() {
        // --proj_home=/home/user/d4j/projects --proj_name=chart --bug_id=3-5/all/1
        System.err.println(
                "Usage : --proj_home=\"project home\" --proj_name=\"project name\" --bug_id=\"3-5/all/1/1,2,5/single/multi\"");
    }

    private static void flexibelConfigure(
            String projName, Set<Integer> ids, Map<String, Pair<Integer, Set<Integer>>> projInfo)
            throws IOException {
        Pair<Integer, Set<Integer>> bugIDs = projInfo.get(projName);
        for (Integer id : ids) {
            Subject subject = Configure.getSubject(projName, id);

            // TODO: DISABLE PURIFY NOW!!! NEED TO DECIDED WHETHER TO REMOVE IT.
            trySplitFix(subject, false);
        }
    }
}
