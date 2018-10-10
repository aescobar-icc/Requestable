package beauty.requestable.core;

import java.lang.reflect.Method;
import java.util.List;

import beauty.requestable.core.annotations.inject.InjectCreator;
import beauty.requestable.core.annotations.inject.InjectDestroyer;
import beauty.requestable.util.reflection.UtilReflection;

public class RequestableInject {
	
	private Object instance = null;
	private Method creator = null;
	private Method destroyer = null;
	
	public RequestableInject() {
	}
	public boolean isReady() {
		return instance != null;
	}
	private Method getCreator(Class<?> type) {
		if(creator == null) {
			List<Method> methods = UtilReflection.getAllAnnotatedMethods(instance.getClass(), InjectCreator.class);
			for(Method m:methods) {
				InjectCreator annota = m.getAnnotation(InjectCreator.class);
				if(annota.type().isAssignableFrom(type)) {
					creator = m;
					break;
				}
			}
		}
		return creator;
	}
	private Method getDestroyer(Class<?> type) {
		if(destroyer == null) {
			List<Method> methods = UtilReflection.getAllAnnotatedMethods(instance.getClass(), InjectDestroyer.class);
			for(Method m:methods) {
				InjectDestroyer annota = m.getAnnotation(InjectDestroyer.class);
				if(annota.type().isAssignableFrom(type)) {
					destroyer = m;
					break;
				}
			}
		}
		return destroyer;
	}
	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> type){
		try {
			if(instance == null) {
				
				Class<?> factory = ServletUtil.getBeanFactory(type);
				instance = factory.newInstance();
			}

			return (T) getCreator(type).invoke(instance, new Object[] {});
			
		}catch(Throwable e) {
			e = UtilReflection.getRootCause(e);
			System.out.format("[RequestableInject] ERROR creating %s ,detail:%s \n",type.getName(),e.getMessage());
		}
		
		return null;
	}
	public <T> void destroy(Class<T> type){
		try {
			if(instance != null)
				getDestroyer(type).invoke(instance, new Object[] {});
			
		}catch(Throwable e) {
			e = UtilReflection.getRootCause(e);
			System.out.format("[RequestableInject] ERROR destroying %s ,detail:%s \n",type.getName(),e.getMessage());
		}finally {
			instance = null;
		}
	}

}
