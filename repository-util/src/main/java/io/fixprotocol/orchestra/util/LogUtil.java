package io.fixprotocol.orchestra.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public final class LogUtil {

  public static Logger initializeDefaultLogger(Level level, Class<?> clazz) {
    final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    final Configuration config = ctx.getConfiguration();
    final ConsoleAppender appender = ConsoleAppender.newBuilder().setName("Console").build();
    config.addAppender(appender);
    final AppenderRef ref = AppenderRef.createAppenderRef("Console", level, null);
    final AppenderRef[] refs = new AppenderRef[] {ref};
    final LoggerConfig loggerConfig =
        LoggerConfig.createLogger(true, level, clazz.getName(), null, refs, null, config, null);
    config.addLogger(clazz.getName(), loggerConfig);
    ctx.updateLoggers();
    return LogManager.getLogger(clazz);
  }

  public static Logger initializeFileLogger(String fileName, Level level, Class<?> clazz) {
    final ConfigurationBuilder<BuiltConfiguration> builder =
        ConfigurationBuilderFactory.newConfigurationBuilder();
    final AppenderComponentBuilder appenderBuilder =
        builder.newAppender("file", "File").addAttribute("fileName", fileName);
    builder.add(appenderBuilder);
    builder.add(
        builder.newLogger(clazz.getCanonicalName(), level).add(builder.newAppenderRef("file")));
    builder.add(builder.newRootLogger(level).add(builder.newAppenderRef("file")));
    final LoggerContext ctx = Configurator.initialize(builder.build());
    return LogManager.getLogger(clazz);
  }
}
