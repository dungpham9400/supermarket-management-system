package vn.com.project.services.employee.reports;

import java.util.Collection;
import java.util.Map;

import domainapp.basics.core.dodm.dsm.DSMBasic;
import domainapp.basics.core.dodm.qrm.QRM;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.Oid;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.query.Query;
import domainapp.basics.model.query.QueryToolKit;
import domainapp.basics.model.query.Expression.Op;
import domainapp.basics.modules.report.model.meta.Output;
import vn.com.project.services.employee.model.Address;
import vn.com.project.services.employee.model.Employee;
import vn.com.project.services.employee.reports.EmployeeByAddressJoinReport;


/**
 * @overview 
 * 	Represent a report about students by city whose view is expressed by a join query.
 * 
 * @author ducmle
 *
 * @version 5.3
 */
@DClass(schema="project",serialisable=false)
public class EmployeeByAddressJoinReport {
	@DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
	  private int id;
	  private static int idCounter = 0;

	  /**input: city name */
	  @DAttr(name = "address", type = Type.String, length = 30, optional = false)
	  private String address;
	  
	  /**output: students whose names match {@link #cityName} */
	  @DAttr(name="employees",type=Type.Collection,optional=false, mutable=false,
	      serialisable=false,filter=@Select(clazz=Employee.class, 
	      attributes={Employee.E_id, Employee.E_gender, Employee.E_name, Employee.E_dob, 
	    		  Employee.E_address, Employee.E_email, Employee.E_phone, Employee.E_department, 
	          Employee.E_rptEmployeeByAddress})
	      ,derivedFrom={"address"}
	      )
	  @DAssoc(ascName="employees-by-address-report-has-employees",role="report",
	      ascType=AssocType.One2Many,endType=AssocEndType.One,
	    associate=@Associate(type=Employee.class,cardMin=0,cardMax=MetaConstants.CARD_MORE
	    ))
	  @Output
	  private Collection<Employee> employees;

	  /**output: number of employees found (if any), derived from {@link #employees} */
	  @DAttr(name = "numEmployees", type = Type.Integer, length = 20, auto=true, mutable=false)
	  @Output
	  private int numEmployees;
	  
	  /**
	   * @effects 
	   *  initialise this with <tt>cityName</tt> and use {@link QRM} to retrieve from data source 
	   *  all {@link Student} whose addresses match {@link City}, whose names match <tt>cityName</tt>.
	   *  initialise {@link #students} with the result if any.
	   *  
	   *  <p>throws NotPossibleException if failed to generate data source query; 
	   *  DataSourceException if fails to read from the data source
	   * 
	   */
	  @DOpt(type=DOpt.Type.ObjectFormConstructor)
	  @DOpt(type=DOpt.Type.RequiredConstructor)
	  public EmployeeByAddressJoinReport(@AttrRef("address") String name) throws NotPossibleException, DataSourceException {
	    this.id=++idCounter;
	    
	    this.address = name;
	    
	    doReportQuery();
	  }
	  
	  /**
	   * @effects return cityName
	   */
	  public String getAddress() {
	    return address;
	  }

	  /**
	   * @effects <pre>
	   *  set this.name = cityName
	   *  if cityName is changed
	   *    invoke {@link #doReportQuery()} to update the output attribute value
	   *    throws NotPossibleException if failed to generate data source query; 
	   *    DataSourceException if fails to read from the data source.
	   *  </pre>
	   */
	  public void setAddress(String name) throws NotPossibleException, DataSourceException {
//	    boolean doReportQuery = (cityName != null && !cityName.equals(this.name));
	    
	    this.address = name;
	    
	    // DONOT invoke this here if there are > 1 input attributes!
	    doReportQuery();
	  }

	  /**
	   * This method is invoked when the report input has be set by the user. 
	   * 
	   * @effects <pre>
	   *   formulate the object query
	   *   execute the query to retrieve from the data source the domain objects that satisfy it 
	   *   update the output attributes accordingly.
	   *  
	   *  <p>throws NotPossibleException if failed to generate data source query; 
	   *  DataSourceException if fails to read from the data source. </pre>
	   */
	  @DOpt(type=DOpt.Type.DerivedAttributeUpdater)
	  @AttrRef("employees")
	  public void doReportQuery() throws NotPossibleException, DataSourceException {
	    // the query manager instance
	    
	    QRM qrm = QRM.getInstance();
	    
	    // create a query to look up Student from the data source
	    // and then populate the output attribute (students) with the result
	    DSMBasic dsm = qrm.getDsm();
	    
	    ////TODO: create a 2-way join query
	    Query q = QueryToolKit.createSimpleJoinQuery(dsm, Employee.class, Address.class,  
	        Employee.E_address, 
	        Address.A_name, 
	        Op.MATCH, 
	        "%"+address+"%");
	    
	    Map<Oid, Employee> result = qrm.getDom().retrieveObjects(Employee.class, q);
	    
	    if (result != null) {
	      // update the main output data 
	      employees = result.values();
	      
	      // update other output (if any)
	      numEmployees = employees.size();
	    } else {
	      // no data found: reset output
	      resetOutput();
	    }
	  }

	  /**
	   * @effects 
	   *  reset all output attributes to their initial values
	   */
	  private void resetOutput() {
	    employees = null;
	    numEmployees = 0;
	  }

	  /**
	   * A link-adder method for {@link #students}, required for the object form to function.
	   * However, this method is empty because students have already be recorded in the attribute {@link #students}.
	   */
	  @DOpt(type=DOpt.Type.LinkAdder)
	  public boolean addEmployee(Collection<Employee> employees) {
	    // do nothing
	    return false;
	  }
	  
	  /**
	   * @effects return students
	   */
	  public Collection<Employee> getEmployees() {
	    return employees;
	  }
	  
	  /**
	   * @effects return numStudents
	   */
	  public int getNumEmployees() {
	    return numEmployees;
	  }

	  /**
	   * @effects return id
	   */
	  public int getId() {
	    return id;
	  }

	  /* (non-Javadoc)
	   * @see java.lang.Object#hashCode()
	   */
	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + id;
	    return result;
	  }

	  /* (non-Javadoc)
	   * @see java.lang.Object#equals(java.lang.Object)
	   */
	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    EmployeeByAddressJoinReport other = (EmployeeByAddressJoinReport) obj;
	    if (id != other.id)
	      return false;
	    return true;
	  }

	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public String toString() {
	    return this.getClass().getSimpleName()+ " (" + id + ", " + address + ")";
	  }
}
