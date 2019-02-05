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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


/**
 * Path operations for default FileSystem
 * 
 * @author Don Mendelson
 */
class FileManager implements PathManager {

  private Path rootPath;

  @Override
  public void close() throws Exception {
    
  }

  public long copyStreamToPath(InputStream in, Path path) throws IOException {
    return Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
  }

  public OutputStream getOutputStream(Path path) throws IOException {
    return new FileOutputStream(path.toFile());
  }

  @Override
  public File getRootPath() {
    return rootPath.toFile();
  }

  public Writer getWriter(Path path) throws IOException {
    return new FileWriter(path.toString());
  }

  public boolean isSupported(Path path) {
    return true;
  }

  public Path makeDirectory(Path path) throws IOException {
    // default file attributes
    return Files.createDirectories(path);
  }

  @Override
  public Path makeRootPath(Path path) throws IOException {
    this.rootPath = makeDirectory(path);
    return rootPath;
  }

}
