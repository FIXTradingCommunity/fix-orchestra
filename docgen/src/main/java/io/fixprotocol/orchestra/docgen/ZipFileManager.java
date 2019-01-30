/*
 * Copyright 2018 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package io.fixprotocol.orchestra.docgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Abstracts differences between default FileSystem and Zip file
 * 
 * @author Don Mendelson
 */
class ZipFileManager implements PathManager {

  private static final String TEMP_PREFIX = "temp";
  private static final String ZIP_EXTENSION = ".zip";

  private static long copyStreamToStream(InputStream source, OutputStream sink) throws IOException {
    long nread = 0L;
    byte[] buf = new byte[8192];
    int n;
    while ((n = source.read(buf)) > 0) {
      sink.write(buf, 0, n);
      nread += n;
    }
    return nread;
  }

  private File zipFile;
  private ZipOutputStream zipOutputStream;
  private OutputStreamWriter zipWriter;


  @Override
  public void close() throws Exception {
    this.zipOutputStream.close();
  }

  public long copyStreamToPath(InputStream in, Path path) throws IOException {
    this.zipOutputStream.putNextEntry(createZipEntry((ZipPath) path));
    OutputStream out = this.zipOutputStream;
    return copyStreamToStream(in, out);
  }

  public OutputStream getOutputStream(Path path) throws IOException {
    this.zipOutputStream.putNextEntry(createZipEntry((ZipPath) path));
    return this.zipOutputStream;
  }

  @Override
  public File getRootPath() {
    return this.zipFile;
  }

  public Writer getWriter(Path path) throws IOException {
    this.zipOutputStream.putNextEntry(createZipEntry((ZipPath) path));
    return this.zipWriter;
  }

  @Override
  public boolean isSupported(Path path) {
    return path.getFileName().toString().endsWith(ZIP_EXTENSION);
  }

  public Path makeDirectory(Path path) throws IOException {
    // no directories in zip file, only entries
    return path;
  }

  @Override
  public ZipPath makeRootPath(Path path) throws IOException {
    this.zipFile = createZipFile(path);
    return ZipPath.ROOT;
  }

  private ZipEntry createZipEntry(ZipPath path) {
    // strip off leading '/' if present since its not shown in Zip files, unlike other file systems
    String name = path.toString();
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    return new ZipEntry(name);
  }

  private File createZipFile(Path path) throws IOException {
    File file;
    if (path.getFileName().toString().contains(TEMP_PREFIX)) {
      file = File.createTempFile(TEMP_PREFIX, ZIP_EXTENSION);
    } else {
      file = path.toFile();
    }
    this.zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
    this.zipWriter = new OutputStreamWriter(this.zipOutputStream);
    return file;
  }
}
