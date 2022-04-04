package trans.rights.event.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.

/**
 * Used to mark something to be added to the subscriber registry
 * <p>
 * If you annotate a method you will need to specify the priority in this
 * annotation if you want to have a custom priority
 * <p>
 * If you are annotating an object then define the priority in the object,
 * changing it here will do nothing
 *
 * @author Austin
 * @see Priority
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface EventHandler {
    /**
     * Priority of the method
     */
    int priority() default Priority.DEFAULT;
}
