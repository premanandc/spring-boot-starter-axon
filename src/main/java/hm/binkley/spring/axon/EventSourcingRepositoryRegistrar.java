/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>.
 */

package hm.binkley.spring.axon;

import static java.lang.String.format;
import static org.axonframework.commandhandling.annotation.AggregateAnnotationCommandHandler.subscribe;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.EventSourcedAggregateRoot;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventstore.EventStore;
import org.axonframework.repository.AbstractRepository;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

@RequiredArgsConstructor
public class EventSourcingRepositoryRegistrar
    implements BeanPostProcessor, BeanFactoryAware {
  private final CommandBus commandBus;
  private final EventBus eventBus;
  private final EventStore eventStore;
  private ConfigurableListableBeanFactory beanFactory;

  @Override
  public void setBeanFactory(final BeanFactory beanFactory)
      throws BeansException {
    this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
  }

  @Override
  public Object postProcessBeforeInitialization(final Object bean,
                                                final String beanName)
      throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean,
                                               final String beanName)
      throws BeansException {
    final Class<?> beanType = bean.getClass();
    if (!EventSourcedAggregateRoot.class.isAssignableFrom(beanType)) {
      return bean;
    }

    final AbstractRepository repository = new EventSourcingRepository(beanType, eventStore);
    repository.setEventBus(eventBus);
    subscribe((Class<? extends EventSourcedAggregateRoot>) beanType, repository, commandBus);

    beanFactory.registerSingleton(format("%sRepository", beanName), repository);

    return bean;
  }
}
