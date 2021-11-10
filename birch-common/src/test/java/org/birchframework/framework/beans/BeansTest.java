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
package org.birchframework.framework.beans;

import org.birchframework.framework.spring.SpringContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.birchframework.framework.beans.Status.*;

/**
 * Tests for {@link Beans}.
 * @author Keivan Khalichi
 */
@SpringBootTest(classes = TestConfiguration.class)
@SuppressWarnings("AutoBoxing")
public class BeansTest {

   private static final String  FIRST_NAME = "Ziggy";
   private static final String  LAST_NAME  = "Stardust";
   private static final Integer AGE        = null;
   private static final Integer TEST_AGE   = 29;
   private static final boolean IS_ACTIVE  = false;
   private static final Status  STATUS     = RETIRED;

   private TestBean testBean;

   /**
    * Setup before each test.
    */
   @BeforeEach
   public void setUp() {
      this.testBean = new TestBean(FIRST_NAME, LAST_NAME, AGE, IS_ACTIVE, RETIRED);
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Object)} without pre-registrations of the mapping.
    */
   @Test
   public void testMapProperties() {
      final var aTestBean = new TestBean();
      aTestBean.setAge(TEST_AGE);
      aTestBean.setActive(true);
      Beans.mapProperties(this.testBean, aTestBean);
      assertThat(aTestBean.getAge()).isNull();
      assertThat(aTestBean.isActive()).isFalse();
      assertThat(aTestBean.getStatus()).isEqualTo(RETIRED);
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Object)} with registration of excluded properties and mapping nulls.
    */
   @Test
   public void testMapPropertiesExcludeProperties() {
      final var aTestBean = new TestBean();
      aTestBean.setAge(TEST_AGE);
      aTestBean.setActive(true);
      Beans.mapProperties(this.testBean, aTestBean, true, "active");
      assertThat(aTestBean.getAge()).isNull();
      assertThat(aTestBean.isActive()).isTrue();
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Object)} with registration of not mapping nulls.
    */
   @Test
   public void testMapPropertiesIgnoreNulls() {
      final var aTestBean = new TestBean();
      aTestBean.setAge(TEST_AGE);
      aTestBean.setActive(true);
      Beans.mapProperties(this.testBean, aTestBean, false);
      assertThat(aTestBean.getAge()).isNotNull();
      assertThat(aTestBean.getAge()).isEqualTo(TEST_AGE);
      assertThat(aTestBean.isActive()).isFalse();
   }

   /**
    * Tests {@link Beans#mapProperties(Object, Object)} with registration of excluded properties and not mapping nulls.
    */
   @Test
   public void testMapPropertiesIgnoreNullsAndExcludeProperties() {
      final var aTestBean = new TestBean();
      aTestBean.setAge(TEST_AGE);
      aTestBean.setActive(true);
      Beans.mapProperties(this.testBean, aTestBean, false, "active");
      assertThat(aTestBean.getAge()).isNotNull();
      assertThat(aTestBean.getAge()).isEqualTo(TEST_AGE);
      assertThat(aTestBean.isActive()).isTrue();
   }

   /**
    * Tests {@link Beans#valueOf(Class, String)}.
    */
   @Test
   void testValueOf() {
      assertThat(Beans.valueOf(Status.class, "RETIRED")).isEqualTo(RETIRED);
      assertThat(Beans.valueOf(Status.class, "WORKING")).isEqualTo(WORKING);
      assertThat(Beans.valueOf(long.class, "90125")).isEqualTo(90125L);
      assertThat(Beans.valueOf(int.class, "42")).isEqualTo(42);
      assertThat(Beans.valueOf(Character.class, "A")).isEqualTo('A');
      assertThat(Beans.valueOf(Character.class, "test")).isNotEqualTo("test");
      assertThat(Beans.valueOf(Character.class, "Bob")).isEqualTo('B');
      assertThat(Beans.valueOf(Double.class, "3.14159265358979323846")).isEqualTo(3.14159265358979323846D);
   }

   /**
    * Tests {@link Beans#findBeanOrCreateInstance(Class)} and {@link Beans#findBeanOrCreateInstance(Class, Object...)}.
    */
   @Test
   void testFindBeanOrCreateInstance() {
      try {
         final var aTestBean = Beans.findBeanOrCreateInstance(TestBean.class);
         assertThat(aTestBean).isNotNull();
         final var aTestBeanInitialized = Beans.findBeanOrCreateInstance(TestBean.class, FIRST_NAME, LAST_NAME, AGE, IS_ACTIVE, STATUS);
         assertThat(aTestBeanInitialized).isNotNull();
         assertThat(aTestBeanInitialized).isEqualTo(this.testBean);
         final var anOtherTestBean = new TestBean("Farnaz", "Mehdavi", 41, true, WORKING);
         SpringContext.registerBean(TestBean.class, () -> anOtherTestBean);
         final var aTestBeanBean = Beans.findBeanOrCreateInstance(TestBean.class);
         assertThat(aTestBeanBean).isNotNull();
         assertThat(aTestBeanBean).isNotEqualTo(this.testBean);
         assertThat(aTestBeanBean).isEqualTo(anOtherTestBean);
      }
      catch (Exception e) {
         fail("Expected a bean of type {}, but received exception: {}: {}", TestBean.class.getName(), e, e.getMessage());
      }
   }
}