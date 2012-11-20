package uk.ac.tgac.rampart.util;

import java.util.List;

import org.hibernate.Query;

public class RampartHibernate {

	public static <T> List<T> listAndCast(Query q) {
	    @SuppressWarnings("unchecked")
	    List<T> list = q.list();
	    return list;
	}
}
