package org.hibernate.envers.test.integration.entityNames.oneToManyAudited;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.MappingException;
import org.hibernate.envers.test.AbstractOneSessionTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hern&aacute;n Chanfreau
 * 
 */

@Test(sequential=true)
public class ReadEntityWithAuditedCollectionTest extends AbstractOneSessionTest{

	private long id_car1;
	private long id_car2;
	
	private Car currentCar1;
	private Person currentPerson1;
	
	private long id_pers1;
	
	private Car car1_1;
	private Person person1_1;	
	
	protected void initMappings() throws MappingException, URISyntaxException {
		URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/entityNames/oneToManyAudited/mappings.hbm.xml");
        config.addFile(new File(url.toURI()));
	}

	
    @BeforeClass(dependsOnMethods = "init")
    public void initData() {
    	
    	initializeSession();

        Person pers1 = new Person("Hernan", 28);
        Person pers2 = new Person("Leandro", 29);
        Person pers4 = new Person("Camomo", 15);

        List<Person > owners = new ArrayList<Person>();
        owners.add(pers1);
        owners.add(pers2);
        Car car1 = new Car(5, owners);

        //REV 1 
        getSession().getTransaction().begin();
        getSession().persist(car1);
        getSession().getTransaction().commit();
        id_pers1 = pers1.getId();
        id_car1 = car1.getId();

        owners = new ArrayList<Person>();
        owners.add(pers2);
        owners.add(pers4);
        Car car2 = new Car(27, owners);
        //REV 2
        getSession().getTransaction().begin();
        Person person1 = (Person)getSession().get("Personaje", id_pers1);
        person1.setName("Hernan David");
        person1.setAge(40);
        getSession().persist(car1);
        getSession().persist(car2);
        getSession().getTransaction().commit();
        id_car2 = car2.getId();

    }
    
    private void loadDataOnSessionAndAuditReader() {
    	
    	currentCar1 = (Car)getSession().get(Car.class, id_car1);
    	currentPerson1 = (Person)getSession().get("Personaje", id_pers1);
    	
    	person1_1 = getAuditReader().find(Person.class,"Personaje", id_pers1, 1);
    	car1_1 = getAuditReader().find(Car.class, id_car1, 2);
    	Car car2 = getAuditReader().find(Car.class, id_car2, 2);

    	for (Person owner : car1_1.getOwners()) {
    		owner.getName(); 
    		owner.getAge();
		}
    	for (Person owner : car2.getOwners()) {
    		owner.getName(); 
    		owner.getAge();
		}
    }

	private void checkEntityNames() {
		String currCar1EN = getSession().getEntityName(currentCar1);
    	String currPerson1EN = getSession().getEntityName(currentPerson1);
    	
    	String car1_1EN = getAuditReader().getEntityName(id_car1, 2, car1_1);
    	assert(currCar1EN.equals(car1_1EN));
    	
    	String person1_1EN = getAuditReader().getEntityName(id_pers1, 1, person1_1);
    	assert(currPerson1EN.equals(person1_1EN));
	}        
	
	@Test	
    public void testObtainEntityNameAuditedCollectionWithEntityName() {
    	
    	this.loadDataOnSessionAndAuditReader();
    	
    	checkEntityNames();
    	
    }


    
    @Test(dependsOnMethods="testObtainEntityNameAuditedCollectionWithEntityName")
    public void testObtainEntityNameAuditedCollectionWithEntityNameInNewSession() {
    	// force a new session and AR
    	forceNewSession();
    	
    	loadDataOnSessionAndAuditReader();
    	
    	checkEntityNames();
    	
    }
}
