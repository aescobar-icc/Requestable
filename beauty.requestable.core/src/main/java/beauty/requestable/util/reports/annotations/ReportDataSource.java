package beauty.requestable.util.reports.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportDataSource {
	int   reportId() default -1;
	String   reportName() default "";
	String[] parameters() default {};
	boolean showDescriptor() default false;
	boolean[] showParameters() default {};
}
