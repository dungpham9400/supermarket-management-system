package vn.com.project.services.finance.model;

import java.util.ArrayList;

import java.util.Calendar;
import java.util.Collection;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.util.Tuple;
import vn.com.project.services.employee.model.Employee;
import vn.com.project.services.employee.model.Manager;
import vn.com.project.services.employee.model.Month;
import vn.com.project.services.employee.model.Salary;

@DClass(schema = "project")
public class OperatingExpense {
	private static final String O_id = "id";
	private static final String O_utility = "utility";
	private static final String O_rent = "rent";
	private static final String O_tax = "tax";
	private static final String O_salary = "totalSalary";
	private static final String O_month = "month";
	private static final String O_total = "total";

	@DAttr(name = O_id, id = true, type = Type.String, auto = true, length = 6, mutable = false, optional = false)
	private String id;
	// static variable to keep track of employee id
	private static int idCounter = 0;
	
	@DAttr(name = O_month, type = Type.Domain, length = 30, optional = false, cid=true)
	private Month month;

	@DAttr(name = O_utility, type = Type.Long, optional = false, length = 15)
	private Long utility;

	@DAttr(name = O_rent, type = Type.Long, optional = false, length = 15)
	private Long rent;

	@DAttr(name = O_tax, type = Type.Long, optional = false, length = 15)
	private Long tax;

	@DAttr(name = O_salary, type = Type.Long, optional = false, length = 15)
	private Long totalSalary;
	
	

	@DAttr(name = O_total, type = Type.Long, optional = false, length = 15, mutable = false, serialisable = false, auto = true, derivedFrom= {"utility", "rent", "tax"})
	private Long total;
	
//	@DAttr(name="salaries",type=Type.Collection,optional = false,
//		      serialisable=false,filter=@Select(clazz=Salary.class))
//		  @DAssoc(ascName="operatingExpense-has-salaries",role="operatingExpense",
//		      ascType=AssocType.One2Many,endType=AssocEndType.One,
//		    associate=@Associate(type=Salary.class,cardMin=0,cardMax=30))
//		  private Collection<Salary> salaries; 
//	
//	private int sCount;

	@DOpt(type = DOpt.Type.RequiredConstructor)
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	public OperatingExpense( @AttrRef("month") Month month, @AttrRef("utility") Long utility, @AttrRef("rent") Long rent, @AttrRef("tax") Long tax, @AttrRef("totalSalary") Long totalSalary) {
		this(null, month, utility, rent, tax, totalSalary);
	}

	public OperatingExpense(@AttrRef("id") String id, 
			@AttrRef("month") Month month, 
			@AttrRef("utility") Long utility, 
			@AttrRef("rent") Long rent,
			@AttrRef("tax") Long tax,
			@AttrRef("totalSalary") Long totalSalary) throws ConstraintViolationException {
		this.id = nextID(id);
		this.month = month;
		this.utility = utility;
		this.rent = rent;
		this.tax = tax;
		this.totalSalary = totalSalary;
		
//		salaries = new ArrayList<>();
//		sCount = 0;
		
		updateTotal();
	}

	private String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
//			if (idCounter == 0) {
//				idCounter = Calendar.getInstance().get(Calendar.YEAR);
//			} else {
//				idCounter++;
//			}
			idCounter++;
			String stringIdCounter = String.format("%05d", idCounter);
			return "O" + stringIdCounter;
		} else {
			// update id
			int num;
			try {
				num = Integer.parseInt(id.substring(1));
			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { id });
			}

			if (num > idCounter) {
				idCounter = num;
			}

			return id;
		}
	}

	@DOpt(type = DOpt.Type.AutoAttributeValueSynchroniser)
	public static void updateAutoGeneratedValue(DAttr attrib, Tuple derivingValue, Object minVal, Object maxVal)
			throws ConstraintViolationException {

		if (minVal != null && maxVal != null) {
			// TODO: update this for the correct attribute if there are more than one auto
			// attributes of this class

			String maxId = (String) maxVal;

			try {
				int maxIdNum = Integer.parseInt(maxId.substring(1));

				if (maxIdNum > idCounter) // extra check
					idCounter = maxIdNum;

			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { maxId });
			}
		}
	}

	@DOpt(type=DOpt.Type.DerivedAttributeUpdater)
	@AttrRef(value="total")
	public void updateTotal() {
		total = utility + rent + tax + totalSalary;
	}
	
	public void setMonth(Month month) {
		this.month = month;
	}

	public void setUtility(Long utility) {
		this.utility = utility;
		updateTotal();
	}

	public void setRent(Long rent) {
		this.rent = rent;
		updateTotal();
	}

	public void setTax(Long tax) {
		this.tax = tax;
		updateTotal();
	}

	public void setTotalSalary(Long totalSalary) {
		this.totalSalary = totalSalary;
		updateTotal();
	}
	
	public void setTotal(Long total) {
		this.total = total;
	}
	
	
	public String getId() {
		return id;
	}
	
	public Month getMonth() {
		return month;
	}
	
	public Long getUtility() {
		return utility;
	}
	
	public Long getRent() {
		return rent;
	}
	
	public Long getTotalSalary() {
		return totalSalary;
	}
	
	public Long getTax() {
		return tax;
	}
	
	public Long getTotal() {
		return total;
	}
	
	
	  
	  // ------------------------------------------------------------------------------
	
	@Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((id == null) ? 0 : id.hashCode());
	    return result;
	  }
	  
	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    OperatingExpense other = (OperatingExpense) obj;
	    if (id == null) {
	      if (other.id != null)
	        return false;
	    } else if (!id.equals(other.id))
	      return false;
	    return true;
	  }
	  
	@Override  
	public String toString() {
		return rent + ", "+ totalSalary;
	}

}