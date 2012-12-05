package uk.ac.tgac.rampart.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.dao.RampartSettingsDao;
import uk.ac.tgac.rampart.data.RampartSettings;
import uk.ac.tgac.rampart.util.RampartHibernate;

@Repository("rampartSettingsDaoImpl")
public class RampartSettingsDaoImpl implements RampartSettingsDao {

	@Autowired
    private SessionFactory sessionFactory;
	
	@Override
	public RampartSettings getRampartSettings(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		RampartSettings rs = (RampartSettings)session.load(RampartSettings.class, id);
		return rs;
	}

	@Override
	public List<RampartSettings> getAllRampartSettings() {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from RampartSettings");
		List<RampartSettings> rampartSettingsList = RampartHibernate.listAndCast(q);
		return rampartSettingsList;
	}

	@Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(RampartSettings.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}

	@Override
	public void persist(RampartSettings rampartSettings) {
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(rampartSettings);
	}

}
