/*
 * Copyright (c) 2009, 2010, 2011, 2012, B3log Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.latke.client;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Latke client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Apr 11, 2012
 */
public final class LatkeClient {

    /**
     * Client version.
     */
    private static final String VERSION = "0.1.0";
    /**
     * Gets repository names.
     */
    private static final String GET_REPOSITORY_NAMES = "/latke/remote/repository/names";
    /**
     * Sets repositories writable.
     */
    private static final String SET_REPOSITORIES_WRITABLE = "/latke/remote/repositories/writable";
    /**
     * Server address, starts with http://.
     */
    private static String serverAddress = "";
    /**
     * Backup directory.
     */
    private static File backupDir;
    /**
     * User name.
     */
    private static String userName = "";
    /**
     * Password.
     */
    private static String password = "";
    /**
     * Verbose.
     */
    private static boolean verbose;

    /**
     * Main entry.
     * 
     * @param args the specified command line arguments
     */
    public static void main(String[] args) {
        args = new String[]{
            "-backup", "-repository_names", "-verbose", "-s", "localhost:8080", "-u", "test", "-p", "1", "-backup_dir",
            "C:/b3log_backup", "-w", "true"};

        final Options options = getOptions();

        final CommandLineParser parser = new PosixParser();

        try {
            final CommandLine cmd = parser.parse(options, args);

            serverAddress = cmd.getOptionValue("s");

            backupDir = new File(cmd.getOptionValue("backup_dir"));
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }

            userName = cmd.getOptionValue("u");

            if (cmd.hasOption("verbose")) {
                verbose = true;
            }

            password = cmd.getOptionValue("p");

            if (verbose) {
                System.out.println("Requesting server[" + serverAddress + "]");
            }

            final HttpClient httpClient = new DefaultHttpClient();

            final List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("userName", userName));
            qparams.add(new BasicNameValuePair("password", password));

            if (cmd.hasOption("repository_names")) {
                try {
                    final URI uri = URIUtils.createURI("http", serverAddress, -1, GET_REPOSITORY_NAMES,
                                                       URLEncodedUtils.format(qparams, "UTF-8"), null);
                    final HttpGet request = new HttpGet();
                    request.setURI(uri);

                    if (verbose) {
                        System.out.println("Getting repository names[" + GET_REPOSITORY_NAMES + "]");
                    }

                    final HttpResponse httpResponse = httpClient.execute(request);
                    final InputStream contentStream = httpResponse.getEntity().getContent();
                    final String content = IOUtils.toString(contentStream).trim();

                    if (verbose) {
                        printResponse(content);
                    }

                    final JSONObject result = new JSONObject(content);
                    final JSONArray repositoryNames = result.getJSONArray("repositoryNames");

                    for (int i = 0; i < repositoryNames.length(); i++) {
                        final String repositoryName = repositoryNames.getString(i);
                        final File dir = new File(backupDir.getPath() + File.separatorChar + repositoryName);
                        if (!dir.exists() && verbose) {
                            dir.mkdir();
                            System.out.println("Created a directory[name=" + dir.getName() + "] under backup directory[path="
                                               + backupDir.getPath() + "]");
                        }
                    }

                } catch (final Exception e) {
                    System.err.println("Requests server error: " + e.getMessage());
                }
            }

            if (cmd.hasOption("w")) {
                try {
                    final String writable = cmd.getOptionValue("w");
                    qparams.add(new BasicNameValuePair("writable", writable));
                    final URI uri = URIUtils.createURI("http", serverAddress, -1, SET_REPOSITORIES_WRITABLE,
                                                       URLEncodedUtils.format(qparams, "UTF-8"), null);
                    final HttpPut request = new HttpPut();
                    request.setURI(uri);

                    if (verbose) {
                        System.out.println("Setting repository writable[" + writable + "]");
                    }

                    final HttpResponse httpResponse = httpClient.execute(request);
                    final InputStream contentStream = httpResponse.getEntity().getContent();
                    final String content = IOUtils.toString(contentStream).trim();

                    if (verbose) {
                        printResponse(content);
                    }


                } catch (final Exception e) {
                    System.err.println("Requests server error: " + e.getMessage());
                }
            }

            if (cmd.hasOption("backup")) {
                System.out.println("Make sure you have disabled repository writes with [-w false], continue?(y)");
                final Scanner scanner = new Scanner(System.in);
                final String input = scanner.next();

                if ("y".equals(input)) {
                    if (verbose) {
                        System.out.println("Starting backup data");
                    }
                }

                scanner.close();
            }

            if (cmd.hasOption("v")) {
                System.out.println(VERSION);
            }

            if (cmd.hasOption("h")) {
                printHelp(options);
            }


        } catch (final ParseException e) {
            System.err.println("Parsing args failed, caused by: " + e.getMessage());
            printHelp(options);
        }
    }

    /**
     * Prints the specified content as response.
     * 
     * @param content the specified content
     */
    private static void printResponse(final String content) {
        System.out.println("Response[");
        System.out.println("    " + content);
        System.out.println("]");
    }

    /**
     * Prints help with the specified options.
     * 
     * @param options the specified options
     */
    private static void printHelp(final Options options) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("latke-client", options);
    }

    /**
     * Gets options.
     * 
     * @return options
     */
    private static Options getOptions() {
        final Options ret = new Options();

        ret.addOption(OptionBuilder.withArgName("server").hasArg().withDescription(
                "For server address. For example, localhost:8080").isRequired().create('s'));
        ret.addOption(OptionBuilder.withArgName("username").hasArg().withDescription("Username").isRequired().create('u'));
        ret.addOption(OptionBuilder.withArgName("password").hasArg().withDescription("Password").isRequired().create('p'));
        ret.addOption(OptionBuilder.withArgName("backup_dir").hasArg().withDescription("Backup directory").isRequired().
                create("backup_dir"));
        ret.addOption(OptionBuilder.withDescription("Backup data").create("backup"));
        ret.addOption(OptionBuilder.withArgName("writable").hasArg().
                withDescription("Disable/Enable repository writes. For example, -w true").create('w'));
        ret.addOption(OptionBuilder.withDescription("Prints repository names").create("repository_names"));
        ret.addOption(OptionBuilder.withDescription("Extras verbose").create("verbose"));
        ret.addOption(OptionBuilder.withDescription("Prints help").create('h'));
        ret.addOption(OptionBuilder.withDescription("Prints this client version").create('v'));

        return ret;
    }

    /**
     * Private constructor.
     */
    private LatkeClient() {
    }
}
