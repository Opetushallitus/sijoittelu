package fi.vm.sade.sijoittelu.laskenta.actors.creators;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

/**
 * Created with IntelliJ IDEA.
 * User: kjsaila
 * Date: 18/12/13
 * Time: 12:27
 * To change this template use File | Settings | File Templates.
 */
public class SpringActorProducer implements IndirectActorProducer {
    final ApplicationContext applicationContext;
    final String actorBeanName;

    public SpringActorProducer(ApplicationContext applicationContext,
                               String actorBeanName) {
        this.applicationContext = applicationContext;
        this.actorBeanName = actorBeanName;
    }

    @Override
    public Actor produce() {
        return (Actor) applicationContext.getBean(actorBeanName);
    }

    @Override
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext.getType(actorBeanName);
    }
}