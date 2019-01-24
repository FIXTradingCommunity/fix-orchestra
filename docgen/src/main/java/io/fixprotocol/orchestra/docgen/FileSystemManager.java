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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Abstracts differences between default FileSystem and Zip file
 * @author  Don Mendelson
 */
class FileSystemManager {

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
  
  private ZipOutputStream zipOutputStream;
  private OutputStreamWriter zipWriter;


  private ZipEntry getZipEntry(ZipPath path) {
    // strip off leading '/' if present since its not shown in Zip files, unlike other file systems
    String name = path.toString();
    if (name.startsWith("/")) {
      name = name.substring(1);
    }
    return new ZipEntry(name);
  }

  long copyStreamToPath(InputStream in, Path path) throws IOException {
    OutputStream out;
    if (path instanceof ZipPath) {
      this.zipOutputStream.putNextEntry(getZipEntry((ZipPath) path));
      out = this.zipOutputStream;
    } else {
      out = new FileOutputStream(path.toFile());
    }
    return copyStreamToStream(in, out);
  }

  OutputStream getOutputStream(Path path) throws IOException {
    if (path instanceof ZipPath) {
      this.zipOutputStream.putNextEntry(getZipEntry((ZipPath) path));
      return this.zipOutputStream;
    } else {
      return new FileOutputStream(path.toFile());
    }
  }
  
  Writer getWriter(Path path) throws IOException {
    if (path instanceof ZipPath) {
      this.zipOutputStream.putNextEntry(getZipEntry((ZipPath) path));
      return this.zipWriter;
    } else {
      return new FileWriter(path.toString());
    }
  }

  Path makeDirectory(Path path) throws IOException {
    if (path instanceof ZipPath) {
      // no directories in zip file, only entries
      return path;
    } else if (path.getFileName().toString().endsWith(".zip")) {
      this.zipOutputStream = new ZipOutputStream(new FileOutputStream(path.toFile()));
      this.zipWriter = new OutputStreamWriter(this.zipOutputStream);
      return ZipPath.ROOT;
    } else {
      // default file attributes
      return Files.createDirectories(path);
    }
  }


}