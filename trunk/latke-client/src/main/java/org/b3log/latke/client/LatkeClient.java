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

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.HelpFormatter;

/**
 * Latke client.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Apr 11, 2012
 */
public final class LatkeClient {

    /**
     * Repository APIs URI prefix.
     */
    private static String repositoryAPI = "/latke/remote/repositories/";
    /**
     * Server address, starts with http://.
     */
    private static String serverAddress = "http://";

    /**
     * Main entry.
     * 
     * @param args the specified command line arguments
     */
    public static void main(final String[] args) {
        final Options options = getOptions();

        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("latke-client", options);

        final CommandLineParser parser = new PosixParser();

        try {
            final CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("server_address")) {
                serverAddress += cmd.getOptionValue("server_address");
                System.out.println(serverAddress);
            } else {
                System.out.println("Expected [server_address]");
            }
            
            
        } catch (final ParseException e) {
            System.err.println("Parsing args failed, caused by: " + e.getMessage());
        }
    }

    /**
     * Gets options.
     * 
     * @return options
     */
    public static Options getOptions() {
        final Options ret = new Options();

        final Option srvAddress = new Option("server_address", "sa", true, "Uses SERVER_ADDRESS");
        srvAddress.setArgName("SERVER_ADDRESS");
        ret.addOption(srvAddress);

        final Option repositoryStatus = new Option("repository_status", "rs", false, "Prints repositories status");

        final Option help = new Option("help", "h", false, "Prints this message");
        final Option version = new Option("version", "v", false, "Prints the cient version information and exit");

        ret.addOption(help).addOption(repositoryStatus).addOption(version);

        return ret;
    }

    /**
     * Private constructor.
     */
    private LatkeClient() {
    }
}
