package io.fixprotocol.orchestra.docgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.nio.file.Path;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZipPathTest {

  @Disabled
  @Test
  public void testHashCode() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testCompareTo() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testEndsWithPath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testEndsWithString() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testEqualsObject() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetFileName() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetFileSystem() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetName() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetNameCount() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetParent() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testGetRoot() {
    fail("Not yet implemented");
  }

  @Test
  public void testIsAbsolute() {
    ZipPath path1 = new ZipPath("/first/second");
    assertTrue(path1.isAbsolute());
    ZipPath path2 = new ZipPath("first/second");
    assertFalse(path2.isAbsolute());
  }

  @Disabled
  @Test
  public void testIterator() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testNormalize() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testRegisterWatchServiceKindOfQArray() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testRegisterWatchServiceKindOfQArrayModifierArray() {
    fail("Not yet implemented");
  }

  @Test
  public void testRelativize() {
    ZipPath path1 = new ZipPath("/first/second");
    ZipPath path2 = new ZipPath("/first/second/third");
    Path path3 = path1.relativize(path2);
    assertEquals("third", path3.toString());
  }

  @Test
  public void testResolvePath() {
    Path first = ZipPath.ROOT.resolve("first");
    assertEquals("/first", first.toString());
    Path second = new ZipPath("second");
    Path path = first.resolve(second);
    assertEquals("/first/second", path.toString());
  }

  @Test
  public void testResolveString() {
    Path first = ZipPath.ROOT.resolve("first");
    assertEquals("/first", first.toString());
    Path second = first.resolve("second");
    assertEquals("/first/second", second.toString());
  }

  @Disabled
  @Test
  public void testResolveSiblingPath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testResolveSiblingString() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testStartsWithPath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testStartsWithString() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testSubpath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testToAbsolutePath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testToFile() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testToRealPath() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testToString() {
    fail("Not yet implemented");
  }

  @Disabled
  @Test
  public void testToUri() {
    fail("Not yet implemented");
  }

}
