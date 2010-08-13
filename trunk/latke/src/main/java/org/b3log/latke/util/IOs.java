/*
 * Copyright (C) 2009, 2010, B3log Team
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
package org.b3log.latke.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Scanner;

/**
 * IO utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Aug 13, 2010
 */
public final class IOs {

    /**
     * Generated serial version uid.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Line separator.
     */
    public static final String LINE_SEPARATOR =
            System.getProperty("line.separator");

    /**
     * Writes the specified content to a file of the specified file path.
     *
     * @param content the specified content
     * @param filePath the specified file path
     * @throws IOException io exception
     */
    public static void writeContent(final String content,
                                    final String filePath) throws IOException {
        final FileWriter fileWriter = new FileWriter(new File(filePath));

        fileWriter.write(content);
        fileWriter.close();
    }

    /**
     * Reads content of a file of the specified file path line by line.
     *
     * @param filePath the specified file path
     * @return content of the file
     * @throws IOException io exception
     */
    public static String readContent(final String filePath) throws IOException {
        final Scanner scanner = new Scanner(new File(filePath));

        final StringBuilder content = new StringBuilder();
        while (scanner.hasNextLine()) {
            content.append(scanner.nextLine());
            content.append(LINE_SEPARATOR);
        }

        scanner.close();

        return content.toString();
    }

    /**
     * Gets a byte array from the specified input stream.
     *
     * @param inputStream the specified input stream
     * @param bufferSize buffer size for input stream reading
     * @return a byte array
     * @throws IOException io exception
     */
    public static byte[] getBytes(final InputStream inputStream,
                                  final int bufferSize)
            throws IOException {
        byte[] ret = null;
        final Collection chunks = new ArrayList<Byte[]>();
        final byte[] buffer = new byte[bufferSize];
        int read = -1;
        int size = 0;

        read = inputStream.read(buffer);
        while (-1 != read) {
            if (read > 0) {
                final Byte[] chunk = new Byte[read];
                System.arraycopy(buffer, 0, chunk, 0, read);
                chunks.add(chunk);
                size += chunk.length;
            }
        }

        if (size > 0) {
            ByteArrayOutputStream bos = null;
            try {
                bos = new ByteArrayOutputStream(size);

                final Iterator i = chunks.iterator();
                while (i.hasNext()) {
                    final byte[] chunk = (byte[]) i.next();
                    bos.write(chunk);
                }

                ret = bos.toByteArray();
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }
        }

        return ret;
    }

    /**
     * Private default constructor.
     */
    private IOs() {
    }
}
