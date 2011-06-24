/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2011 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library;  If not, see <http://www.gnu.org/licenses/>. 

    http://code.google.com/p/xxl/

*/

package xxl.core.xxlinq.usecases;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XXLinqDATA {

	/**
	 * 
	 */
	public static List<Product> PRODUCTS;
	/**
	 * 
	 */
	public static List<Customer> CUSTOMERS;
	
	public static DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN);
	static{
		PRODUCTS = Arrays.asList( 
				new Product("LCD_TV", "BROADCAST", 200.0 , 20),
				new Product("RADIO", "BROADCAST", 20.0 , 10),
				new Product("PC", "COMPUTER", 400.0 , 0),
				new Product("LCD_MONITOR", "DEVICE", 120.0 , 0),
				new Product("PRINTER", "DEVICE", 80, 2), 
				new Product("DIGITAL_CAMERA", "PHOTO", 140, 8),  
				new Product("NOTEBOOK", "COMPUTER", 500, 4), 
				new Product("SCANNER", "DEVICE", 50, 2));
		// TODO
		int ordercounter = 10500;
		
			try {
				CUSTOMERS = Arrays.asList(
						new Customer(1, "ORACLE", "Max Mustermann", "WA",
								Arrays.asList(new Order(ordercounter++, 191.10, df.parse("01.02.1998")),
										new Order(ordercounter++, 330.0, df.parse("20.01.1998")),
										new Order(ordercounter++, 671.0, df.parse("21.02.1998")),
										new Order(ordercounter++, 88.80, df.parse("21.03.1998")),
										new Order(ordercounter++, 898.03, df.parse("18.04.1999")),
										new Order(ordercounter++, 407.70, df.parse("16.05.1999")),
										new Order(ordercounter++, 500.0, df.parse("01.06.2000")),
										new Order(ordercounter++, 498.80, df.parse("01.12.2000")))),
						new Customer(2, "MICROSOFT", "Lisa Mueller", "NY",
								Arrays.asList(new Order(ordercounter++, 676.76, df.parse("01.02.1998")),
										new Order(ordercounter++, 123.45, df.parse("01.02.1998")),
										new Order(ordercounter++, 222.33, df.parse("11.03.1999")),
										new Order(ordercounter++, 432.1, df.parse("21.04.1999")),
										new Order(ordercounter++, 56.78, df.parse("22.05.2000")),
										new Order(ordercounter++, 46.80, df.parse("23.06.2000")))),
						new Customer(3, "GOOGLE", "Martin Schneider", "TX",
								Arrays.asList(new Order(ordercounter++, 512.6, df.parse("15.02.1998")),
										new Order(ordercounter++, 543.4, df.parse("11.05.1998")),
										new Order(ordercounter++, 222.99, df.parse("28.08.1998")),
										new Order(ordercounter++, 234.45, df.parse("02.01.2000")))), 
						new Customer(4, "MICROSOFT", "Karl Schmidt", "WA",
								Arrays.asList(new Order(ordercounter++, 458.50, df.parse("01.09.1998")),
										new Order(ordercounter++, 494.20, df.parse("01.09.1998")),
										new Order(ordercounter++, 75.0, df.parse("01.07.1998")),
										new Order(ordercounter++, 68.10, df.parse("01.07.1998")),
										new Order(ordercounter++, 486.10, df.parse("01.04.1999")),
										new Order(ordercounter++, 667.11, df.parse("01.04.1999")),
										new Order(ordercounter++, 589.30, df.parse("01.03.1999")),
										new Order(ordercounter++, 91.20, df.parse("01.02.1999")),
										new Order(ordercounter++, 457.0, df.parse("01.02.1999")),
										new Order(ordercounter++, 199.0, df.parse("01.01.1999")),
										new Order(ordercounter++, 294.0, df.parse("01.12.2000")),
										new Order(ordercounter++, 687.20, df.parse("01.12.2000")),
										new Order(ordercounter++, 919.2, df.parse("01.11.2000")))), 
						new Customer(0, "YAHOO", "Britta Mustermann", "CA",
								Arrays.asList(new Order(ordercounter++, 753.0, df.parse("02.02.1997")),
										new Order(ordercounter++, 654.0, df.parse("03.03.1997")),
										new Order(ordercounter++, 245.0, df.parse("04.04.1999")),
										new Order(ordercounter++, 298.0, df.parse("05.05.1999")),
										new Order(ordercounter++, 639.0, df.parse("06.06.1999")),
										new Order(ordercounter++, 483.0, df.parse("07.07.1999")),
										new Order(ordercounter++, 986.0, df.parse("08.08.1999")),
										new Order(ordercounter++, 444.0, df.parse("09.09.2000"))))
						);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
	} 
	/**
	 * 
	 * 
	 *
	 */
	public static class Product{
		private String productName;
		private String category;
		private double price;
		private int numberInStock;
		
		
		public Product(String productName, String category, double price, int numberInStock) {
			super();
			this.productName = productName;
			this.category = category;
			this.price = price;
			this.numberInStock = numberInStock;
		}
		
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public double getPrice() {
			return price;
		}
		public void setPrice(double price) {
			this.price = price;
		}
		public int getNumberInStock() {
			return numberInStock;
		}
		public void setNumberInStock(int numberInStock) {
			this.numberInStock = numberInStock;
		}
		
		@Override
		public String toString() {
			return "Product [numberInStoc=" + numberInStock + ", category="+ category + ", price=" + price
					+ ", productName=" + productName + "]";
		}
	}
	/**
	 * 
	 * 
	 *
	 */
	public static class Customer{
		private int id;
		private String companyName;
		private String name;
		private String region;
		private List<Order> orders;

		public Customer(int id, String companyName, String name, String region,
				List<Order> orders) {
			super();
			this.id = id;
			this.companyName = companyName;
			this.name = name;
			this.region = region;
			this.orders = orders;
		}

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getCompanyName() {
			return companyName;
		}
		public void setCompanyName(String companyName) {
			this.companyName = companyName;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getRegion() {
			return region;
		}
		public void setRegion(String region) {
			this.region = region;
		}
		public List<Order> getOrders() {
			return orders;
		}

		public void setOrders(List<Order> orders) {
			this.orders = orders;
		}
		@Override
		public String toString() {
			return "Customer [" +
						"companyName=" + companyName + ", " +
						"id= " 		+ id 	+ ", " +
						"name= " 	+ name 	+ ", " +
						"region= " 	+ region + 
					"]";
		}
	
	}
	public static class Order{
		private int orderID;
		private double total;
		private Date date;
		
		public Order(int orderID, double total, Date date) {
			super();
			this.orderID = orderID;
			this.total = total;
			this.date = date;
		}
		public int getOrderID() {
			return orderID;
		}
		public void setOrderID(int orderID) {
			this.orderID = orderID;
		}
		public double getTotal() {
			return total;
		}
		public void setTotal(double total) {
			this.total = total;
		}
		public Date getDate() {
			return date;
		}
		public void setDate(Date date) {
			this.date = date;
		}
		@Override
		public String toString() {
			
			return "Order [" +
					"ID: "		+ orderID + ", " +
					 "Total: " 	+ total + 
					"]";
		}
		
		
	}
	
}
