package fi.vm.sade.util;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

/**
 * Pieni wrapperi jonka avulla NoSqlUnit-kirjastoa ja annotaatioita voi käyttää JUnit5-testeissä
 */
public class NoSqlUnitInterceptor implements InvocationInterceptor {
    private MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");
    @Override
    public void interceptTestMethod(InvocationInterceptor.Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        mongoDbRule.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                invocation.proceed();
            }
        }, new FrameworkMethod(invocationContext.getExecutable()), invocationContext.getTarget().get()).evaluate();
    }
}
