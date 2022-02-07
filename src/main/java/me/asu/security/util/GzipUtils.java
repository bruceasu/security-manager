package me.asu.security.util;
/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  A collection of utility methods for working on GZIPed data.
 */
public class GzipUtils {

    private static final int EXPECTED_COMPRESSION_RATIO = 5;
    private static final int BUF_SIZE = 4096;

    public static final byte[] unzipBestEffort(byte[] in) {
        return unzipBestEffort(in, Integer.MAX_VALUE);
    }

    public static final byte[] unzipBestEffort(byte[] in, int sizeLimit) {
        try {
            // decompress using GZIPInputStream
            ByteArrayOutputStream outStream = new ByteArrayOutputStream(
                    EXPECTED_COMPRESSION_RATIO * in.length);

            GZIPInputStream inStream = new GZIPInputStream(
                    new ByteArrayInputStream(in));

            byte[] buf = new byte[BUF_SIZE];
            int written = 0;
            while (true) {
                try {
                    int size = inStream.read(buf);
                    if (size <= 0)
                        break;
                    if ((written + size) > sizeLimit) {
                        outStream.write(buf, 0, sizeLimit - written);
                        break;
                    }
                    outStream.write(buf, 0, size);
                    written += size;
                } catch (Exception e) {
                    break;
                }
            }
            try {
                outStream.close();
            } catch (IOException e) {
            }

            return outStream.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }


    public static final byte[] unzip(byte[] in) {
        return unzipBestEffort(in);
    }

    public static final byte[] zip(byte[] in) {
        try {
            // compress using GZIPOutputStream
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream(in.length
                    / EXPECTED_COMPRESSION_RATIO);

            GZIPOutputStream outStream = new GZIPOutputStream(byteOut);

            try {
                outStream.write(in);
            } catch (Exception e) {
            }

            try {
                outStream.close();
            } catch (IOException e) {
            }

            return byteOut.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }

}
