package beauty.requestable.core.annotations.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestableFilterOperation {
	int order() default 0;
	String[] parameters() default {};
}