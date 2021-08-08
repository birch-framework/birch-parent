/*===============================================================
 = Copyright (c) 2021 Birch Framework
 = This program is free software: you can redistribute it and/or modify
 = it under the terms of the GNU General Public License as published by
 = the Free Software Foundation, either version 3 of the License, or
 = any later version.
 = This program is distributed in the hope that it will be useful,
 = but WITHOUT ANY WARRANTY; without even the implied warranty of
 = MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 = GNU General Public License for more details.
 = You should have received a copy of the GNU General Public License
 = along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ==============================================================*/
package org.birchframework.framework.cli;

import java.util.List;
import javax.annotation.Nonnull;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Base class for Spring Boot applications that have command line options.
 * @author Keivan Khalichi
 * @see AbstractCommandLineApplication#commandLineRunner(ConfigurableApplicationContext)
 */
@Slf4j
public abstract class AbstractCommandLineApplication {

   protected static final Option DAEMON = noArgsOption("dm", "daemon", "run as daemon (i.e. does not terminate)");

   private final Options     options = new Options();
   private       CommandLine commandLine;

   protected static Option requiredWithArgsOption(@Nonnull final String shortOption, final String longOption, final String description) {
      return option(shortOption, longOption, true, true, description);
   }

   protected static Option requiredNoArgsOption(@Nonnull final String shortOption, final String longOption, final String description) {
      return option(shortOption, longOption, true, false, description);
   }

   protected static Option withArgsOption(@Nonnull final String shortOption, final String longOption, final String description) {
      return option(shortOption, longOption, false, true, description);
   }

   protected static Option noArgsOption(@Nonnull final String shortOption, final String longOption, final String description) {
      return option(shortOption, longOption, false, false, description);
   }

   protected static Option option(@Nonnull final String shortOption, final String longOption, final boolean required, final boolean hasArgs,
                                  final String description) {
      return Option.builder(shortOption).longOpt(longOption).required(required).hasArg(hasArgs).desc(description).build();
   }

   protected abstract List<Option> options();

   protected abstract void run() throws CommandLineApplicationException;

   @SuppressWarnings("VariableArgumentMethod")
   void init(final String... theArgs) throws ParseException {
      this.options().forEach(this.options::addOption);
      this.commandLine = new DefaultParser().parse(this.options, theArgs);
   }

   protected String optionValue(@Nonnull final String theOption) {
      return this.commandLine.getOptionValue(theOption);
   }

   protected String optionValue(@Nonnull final Option theOption) {
      return this.commandLine.getOptionValue(theOption.getOpt());
   }

   protected boolean hasOption(@Nonnull final String theOption) {
      return this.commandLine.hasOption(theOption);
   }

   protected boolean hasOption(@Nonnull final Option theOption) {
      return this.commandLine.hasOption(theOption.getOpt());
   }

   /**
    * Creates a bean that checks if a there are command-line arguments, and if so, initializes the class and runs the {@link #run()} abstract method.  Unless
    * the command-line option {@code -dnt} (or {@code --do-not-terminate}) is present explicitly, upon completion of the {@link #run()} method,
    * the Spring application will terminate.
    * @param theContext the application context
    * @return an instance of Spring command-line runner
    */
   @Bean
   CommandLineRunner commandLineRunner(ConfigurableApplicationContext theContext) {
      return args -> {
         if (ArrayUtils.isNotEmpty(args)) {
            log.debug("Command line: {}", (Object) args);
            try {
               this.init(args);
               this.run();
            }
            catch (ParseException | NumberFormatException e) {
               final var aFormatter = new HelpFormatter();
               aFormatter.printHelp(120, this.getClass().getSimpleName(), "\noptions:\n\n", this.options, null, true);
            }
            catch (CommandLineApplicationException e) {
               log.error("Error running command-line application; error code: {}; message: {}",
                         e.getErrorCode().asString(), Throwables.getRootCause(e).getMessage());
            }
            finally {
               if (!this.hasOption(DAEMON)) {
                  theContext.close();
               }
            }
         }
      };
   }
}