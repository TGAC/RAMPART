package uk.ac.tgac.rampart.util;

/**
 * Created with IntelliJ IDEA.
 * User: maplesod
 * Date: 22/01/14
 * Time: 11:51
 * To change this template use File | Settings | File Templates.
 */
public interface Service {

    /**
     * The only thing that a service needs is a name to identify it
     * @return  The name of this service
     */
    String getName();
}
