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
package org.birchfw.test.framework.beans;

import org.birchframework.framework.beans.Beans;
import org.birchframework.framework.beans.MappingModel;
import org.birchframework.framework.beans.TestConfiguration;
import org.birchframework.framework.beans.TestDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.*;
import static org.birchframework.framework.beans.Status.RETIRED;

/**
 * Tests {@link org.birchframework.framework.beans.MappingAutoConfiguration}.
 * @author Keivan Khalichi
 */
@SuppressWarnings({"StaticVariableUsedBeforeInitialization", "StaticVariableMayNotBeInitialized", "AutoBoxing"})
public class MappingAutoConfigurationTest {

   private static final String  FIRST_NAME = "Ziggy";
   private static final String  LAST_NAME  = "Stardust";
   private static final Integer AGE        = null;
   private static final Integer TEST_AGE   = 29;
   private static final boolean IS_ACTIVE = false;

   private static ConfigurableApplicationContext context;

   private TestDTO  testDTO;

   /**
    * Because of classloader issues, this is the only way to start the Spring Boot test instead of the traditional way using
    * {@link org.springframework.boot.test.context.SpringBootTest} .
    */
   @BeforeAll
   static void beforeAll() {
      context = SpringApplication.run(TestConfiguration.class);
   }

   @AfterAll
   static void afterAll() {
      context.close();
   }

   /**
    * Setup before each test.
    */
   @BeforeEach
   public void setUp() {
      this.testDTO = new TestDTO(FIRST_NAME, LAST_NAME, AGE, IS_ACTIVE, RETIRED);
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Object)} on target model annotated with {@link MappingModel}.
    */
   @Test
   public void testMapPropertiesToMappingModelObject() {
      final var aTestModel = new TestModel();
      aTestModel.setAge(TEST_AGE);
      aTestModel.setActive(true);
      Beans.mapProperties(this.testDTO, aTestModel);
      assertThat(aTestModel.getAge()).isNull();
      assertThat(aTestModel.isActive()).isFalse();
      assertThat(aTestModel.getStatus()).isEqualTo(RETIRED);
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Class)} on target model annotated with {@link MappingModel}.
    */
   @Test
   public void testMapPropertiesToMappingModelClass() {
      final var aTestModel = Beans.mapProperties(this.testDTO, TestModel.class);
      assertThat(aTestModel.getAge()).isNull();
      assertThat(aTestModel.isActive()).isFalse();
      assertThat(aTestModel.getStatus()).isEqualTo(RETIRED);
   }
}