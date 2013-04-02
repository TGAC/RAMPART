/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.ac.tgac.conan.core.dao.TgacConanHibernate;
import uk.ac.tgac.rampart.dao.RampartSettingsDao;
import uk.ac.tgac.rampart.data.RampartSettings;

import java.util.List;

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
		List<RampartSettings> rampartSettingsList = TgacConanHibernate.listAndCast(q);
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
