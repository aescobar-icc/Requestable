package beauty.requestable.core.annotations.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import beauty.requestable.core.enums.FilterApply;

@Repeatable(value=RequestableFilterGroup.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
/**
 * Allow to mark a class as RequestableClass's filter
 * @author aescobar
 *
 */
public @interface RequestableFilter{
	Class<?> filter();
	Class<?> onWebPageFault() default Class.class;
	FilterApply apply() default FilterApply.ALL_METHOD;
}
