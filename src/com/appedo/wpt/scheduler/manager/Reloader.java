package com.appedo.wpt.scheduler.manager;

public class Reloader extends ClassLoader {
	byte[] baClass = null;
	
	public Reloader(byte[] baClass) {
		this.baClass = baClass;
	}
	
	public Class<?> loadClass(String strClassName) {
		return findClass(strClassName);
	}
	
	public Class<?> findClass(String strClassName) {
		try {
			return defineClass(strClassName, baClass, 0, baClass.length);
		} catch (Throwable th) {
			try {
				return super.loadClass(strClassName);
			} catch (ClassNotFoundException ignore) { }
			
			th.printStackTrace(System.out);
			return null;
		}
	}
}
