package beauty.requestable.core;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import beauty.requestable.core.annotations.inject.InjectCreator;
import beauty.requestable.core.annotations.inject.InjectDestroyer;
import beauty.requestable.util.reflection.UtilReflection;

public class RequestableInjectGeneric<T> {

	private Class<T> type;
	private Object instance = null;
	
	@SuppressWarnings("unchecked")
	public RequestableInjectGeneric() {
		this.type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	@SuppressWarnings("unchecked")
	public T create(){
		try {
			if(instance == null) {
				
				Class<?> factory = ServletUtil.getBeanFactory(type);
				instance = factory.newInstance();
			}
			
			List<Method> methods = UtilReflection.getAllAnnotatedMethods(instance.getClass(), InjectCreator.class);
			for(Method m:methods) {
				InjectCreator annota = m.getAnnotation(InjectCreator.class);
				if(annota.type().isAssignableFrom(type)) {
					return (T) m.invoke(instance, new Object[] {});
				}
			}
			
		}catch(Throwable e) {
			e = UtilReflection.getRootCause(e);
			System.out.format("[RequestableInject] ERROR creating %s ,detail:%s \n",type.getName(),e.getMessage());
		}
		
		return null;
	}
	public void destroy(){
		try {
			
			List<Method> methods = UtilReflection.getAllAnnotatedMethods(instance.getClass(), InjectDestroyer.class);
			for(Method m:methods) {
				InjectDestroyer annota = m.getAnnotation(InjectDestroyer.class);
				if(annota.type().isAssignableFrom(type)) {
					m.invoke(instance, new Object[] {});
				}
			}
			
		}catch(Throwable e) {
			e = UtilReflection.getRootCause(e);
			System.out.format("[RequestableInject] ERROR destroying %s ,detail:%s \n",type.getName(),e.getMessage());
		}finally {
			instance = null;
		}
	}

}
