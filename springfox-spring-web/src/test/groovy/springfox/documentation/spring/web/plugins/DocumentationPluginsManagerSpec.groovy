/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package springfox.documentation.spring.web.plugins

import com.google.common.base.Optional
import spock.lang.Specification
import springfox.documentation.builders.OperationBuilder
import springfox.documentation.builders.ParameterBuilder
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.DocumentationPlugin
import springfox.documentation.spi.service.OperationBuilderPlugin
import springfox.documentation.spi.service.ParameterBuilderPlugin
import springfox.documentation.spi.service.ResourceGroupingStrategy
import springfox.documentation.spi.service.contexts.DocumentationContext
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.ParameterContext
import springfox.documentation.spi.service.contexts.PathContext
import springfox.documentation.spring.web.paths.RelativePathProvider
import springfox.documentation.spring.web.SpringGroupingStrategy
import springfox.documentation.spring.web.mixins.ServicePluginsSupport
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator

import javax.servlet.ServletContext

@Mixin(ServicePluginsSupport)
class DocumentationPluginsManagerSpec extends Specification {
  def "default documentation plugin always exists" () {
    given:
      def sut = defaultWebPlugins()
    expect:
      sut.documentationPlugins.size() == 0
      sut.documentationPlugins().size() == 1
  }

  def "Resource grouping strategy is defaulted to use SpringResourceGroupingStrategy" () {
    given:
      def sut = defaultWebPlugins()
    expect:
      sut.resourceGroupingStrategy(DocumentationType.SPRING_WEB) instanceof SpringGroupingStrategy
      sut.resourceGroupingStrategy(DocumentationType.SWAGGER_12) instanceof SpringGroupingStrategy
  }

  def "When documentation plugins are explicitly defined" () {
    given:
      def mockPlugin = Mock(DocumentationPlugin)
    and:
      mockPlugin.groupName >> "default"
      def sut = customWebPlugins([mockPlugin])
    expect:
      sut.documentationPlugins.size() == 1
      sut.documentationPlugins().first() == mockPlugin
  }

  def "When resource grouping strategy has been defined" () {
    given:
      def mockStrategy = Mock(ResourceGroupingStrategy)
    and:
      def sut = customWebPlugins([], [mockStrategy])
      mockStrategy.supports(_) >> true
    expect:
      sut.resourceGroupingStrategy(DocumentationType.SPRING_WEB) == mockStrategy
      sut.resourceGroupingStrategy(DocumentationType.SWAGGER_12) == mockStrategy
  }

  def "Path decorator plugins are applied" () {
    given:
      def pathContext = Mock(PathContext)
      def context = Mock(DocumentationContext)
    and:
      pathContext.pathProvider() >> new RelativePathProvider(Mock(ServletContext))
      pathContext.documentationContext() >> context
      context.getPathMapping() >> Optional.absent()
      pathContext.parameters >> []
    when:
      def sut = defaultWebPlugins()
      def decorator = sut.decorator(pathContext)
    then:
      decorator != null
      decorator.apply("") == "/"
  }
}
