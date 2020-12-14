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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;

/**
 * Abstracts file systems
 * 
 * @author Don Mendelson
 *
 */
interface PathManager extends AutoCloseable {

  long copyStreamToPath(InputStream in, Path path) throws IOException;
  
  OutputStream getOutputStream(Path path) throws IOException;
    
  Writer getWriter(Path path) throws IOException;
  
  boolean isSupported(String path);
  
  Path makeDirectory(Path path) throws IOException;
  
  Path makeRootPath(String path) throws IOException;
}
