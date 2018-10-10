package beauty.requestable.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import beauty.requestable.core.enums.RequestableType;




@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestableClass{
	// for services
	String	identifier() default "";
	
	//for web page
	String uri() default "";
	
	//used for web page and component only
	String render() default "";
	

	RequestableType type();
}
