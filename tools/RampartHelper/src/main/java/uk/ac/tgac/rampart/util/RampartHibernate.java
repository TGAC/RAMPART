package uk.ac.tgac.rampart.util;

import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public enum RampartHibernate {

	HBN_SESSION;
	
	private SessionFactory sessionFactory;
	private ServiceRegistry serviceRegistry;
	private Session session;
	
	private RampartHibernate() {
		Configuration configuration = new Configuration();
		configuration.configure();
		serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry(); 
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);		
		session = sessionFactory.openSession();
		session.setFlushMode(FlushMode.AUTO);
	}
	
	public Session getSession() {
		
		return session;
	}
	
	public static <T> List<T> listAndCast(Query q) {
	    @SuppressWarnings("unchecked")
	    List<T> list = q.list();
	    return list;
	}
}
