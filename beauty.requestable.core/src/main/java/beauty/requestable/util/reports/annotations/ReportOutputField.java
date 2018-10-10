package beauty.requestable.util.reports.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportOutputField {
	int		order() default -1;
	String	alias() default "";
	String	render() default "";
	int columnWidth() default 0;
	ColumnFormat columnFormat() default ColumnFormat.NONE;
}
