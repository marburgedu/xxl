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

import static xxl.core.xxlinq.AdvPredicate.*;
import static xxl.core.xxlinq.AggregateUtils.*;
import static xxl.core.xxlinq.columns.ColumnUtils.*;
import static xxl.core.xxlinq.columns.Column.SubQueryType.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import xxl.core.functions.AbstractFunction;
import xxl.core.functions.Function;
import xxl.core.functions.Functional;
import xxl.core.functions.Functional.UnaryFunction;
import xxl.core.relational.tuples.ArrayTuple;
import xxl.core.relational.tuples.Tuple;
import xxl.core.xxlinq.AdvPredicate;
import xxl.core.xxlinq.AdvTupleCursor;
import xxl.core.xxlinq.columns.Column;
/**
 * this are examples which are similar to examples of microsoft 
 * http://msdn.microsoft.com/en-us/vcsharp/aa336746.aspx
 *
 */
public class Examples {
	/**
	 * 
	 * 
	 *
	 */
	public static class Product{
		private String productName;
		private double price;
		private int numberInStock;
		
		public Product(String productName, double price, int numberInStock) {
			super();
			this.productName = productName;
			this.price = price;
			this.numberInStock = numberInStock;
		}
		
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
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
			return "Product [numberInStoc=" + numberInStock + ", price=" + price
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
		
		
		
	
		public Customer(int id, String companyName, String name, String region) {
			super();
			this.id = id;
			this.companyName = companyName;
			this.name = name;
			this.region = region;
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
		
		@Override
		public String toString() {
			return "Customer [companyName=" + companyName + ", id=" + id
					+ ", name=" + name + ", region=" + region + "]";
		}
	
	}
	/**
	 * 
	 */
	public static final List<Product> PRODUCTS;
	/**
	 * 
	 */
	public static final List<Customer> CUSTOMERS;
	/**
	 * 
	 */
	static{
		PRODUCTS = Arrays.asList( 
				new Product("LCD_TV", 200.0 , 20),
				new Product("RADIO", 20.0 , 10),
				new Product("PC", 400.0 , 0),
				new Product("LCD_MONITOR", 120.0 , 0),
				new Product("PRINTER", 80, 2), 
				new Product("DIGITAL_CAMERA", 140, 8),  
				new Product("NOTEBOOK", 500, 4), 
				new Product("SCANNER", 50, 2));
		// TODO
		CUSTOMERS = Arrays.asList(
				new Customer(1, "ORACLE", "Max Mustermann", "WA"),
				new Customer(2, "MICROSOFT", "Lisa Mueller", "NY"),
				new Customer(3, "GOOGLE", "Martin Schneider", "TX"), 
				new Customer(4, "MICROSOFT", "Karl Schmidt", "WA"), 
				new Customer(0, "YAHOO", "Britta Mustermann", "CA")
				);
		
	} 
	
	/****************************************************************************
	 * Restriction Operators WHERE Clause
	 ****************************************************************************/
	
	public static void whereExample_1(){
		List<Integer> numbers = Arrays.asList(6, 2, 8, 5, 7, 9, 1, 4, 0, 3);
		AdvTupleCursor tupleCursor = 
		new AdvTupleCursor( numbers.iterator(), "numbers")
		.where(col("value").LT(val(4)));
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	
	public static void whereExample_2(){
		// sold products query
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor(PRODUCTS, "products").
				where(colOBJCALL("stock", col("value"), "getNumberInStock").EQ(val(0)));
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	} 
	
	public static void whereExample_3(){
		// products in stock and price > 100
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor(PRODUCTS, "products").
				where(colOBJCALL("stock", col("value"), "getNumberInStock").GT(val(0)).
						AND(colOBJCALL("price", col("value"), "getPrice").GEQ(val(new Double(100)))));
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	public static void whereExample_4(){
		// all customers from Texas
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor(CUSTOMERS, "customers").
				where(colOBJCALL("state", col("value"), "getRegion").EQ(val("TX")));
		// TODO
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	
	// we need an operator for combinig zip
	public static void whereExample_5(){
		List<String> digits = Arrays.asList("zero", 
				"one", 
				"two", 
				"three", 
				"four", 
				"five", 
				"six", 
				"seven",
				"eight",
				"nine");
		AdvTupleCursor tupleCursor = new AdvTupleCursor(digits.iterator(), "test").
		selectIndex(colOBJCALL("length", col("value"), "length"))
		.where(col("length").GEQ(col("index")));
		for(Tuple t : tupleCursor ){
			System.out.println(t);
		}
	}
	
	/****************************************************************************
	 * End
	 ****************************************************************************/
	/****************************************************************************
	 * Projection Operators SELECT Clause
	 ****************************************************************************/
	public static void selectExample_1(){
		List<Integer> numbers = Arrays.asList(6, 2, 8, 5, 7, 9, 1, 4, 0, 3);
		AdvTupleCursor tupleCursor = 
		new AdvTupleCursor( numbers.iterator(), "numbers")
		.select(col("value").ADD(val(1), "newValue")); 
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	public static void selectExample_2(){
		// name of products
		AdvTupleCursor tupleCursor = 
		new AdvTupleCursor( PRODUCTS, "products")
		.select(colOBJCALL("productName" , col("value"), "getProductName")); 
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	public static void selectExample_3(){
//		int[] numbers = { 5, 4, 1, 3, 9, 8, 6, 7, 2, 0 };
//	    string[] strings = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
//
//	    var textNums =
//	        from n in numbers
//	        select strings[n];
//
//	    Console.WriteLine("Number strings:");
//	    foreach (var s in textNums)
//	    {
//	        Console.WriteLine(s);
//	    }
		List<Integer> numbers = Arrays.asList( 5, 4, 1, 3, 9, 8, 6, 7, 2, 0 ); 
		List<String> digits = new ArrayList<String>();
		digits.add("zero");
		digits.add("one");
		digits.add("two");
		digits.add("three");
		digits.add("four");
		digits.add("five");
		digits.add("six");
		digits.add("seven");
		digits.add("eight");
		digits.add("nine");
		digits.add("ten");
	
		//FIXME Arrays.ArrayList is not accessible throught  public method
		// so it does not work with Arrays.asList lists produced
		AdvTupleCursor tupleCursor = new AdvTupleCursor(numbers, "test").
		select(colOBJCALL("digits names", val(digits, "digits"), "get", col("test.value")) );
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
		
	}
	
	public static void selectExample_4(){
//	 string[] words = { "aPPLE", "BlUeBeRrY", "cHeRry" };
//	    var upperLowerWords =
//	        from w in words
//	        select new { Upper = w.ToUpper(), Lower = w.ToLower() };
//
//	    foreach (var ul in upperLowerWords)
//	    {
//	        Console.WriteLine("Uppercase: {0}, Lowercase: {1}", ul.Upper, ul.Lower);
//	    }
		List<String> digits = Arrays.asList( "aPPLE", "BlUeBeRrY", "cHeRry");
		AdvTupleCursor tupleCursor = new AdvTupleCursor(digits.iterator(), "test").
		selectIndex(colOBJCALL("length", col("value"), "toUpperCase"),
				colOBJCALL("length", col("value"), "toLowerCase") );
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	
	
	public static void selectExample_5(){
//		int[] numbers = { 5, 4, 1, 3, 9, 8, 6, 7, 2, 0 };
//	    string[] strings = { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine" };
//
//	    var digitOddEvens =
//	        from n in numbers
//	        select new { Digit = strings[n], Even = (n % 2 == 0) };
//
//	    foreach (var d in digitOddEvens)
//	    {
//	        Console.WriteLine("The digit {0} is {1}.", d.Digit, d.Even ? "even" : "odd");
//	    }
		List<Integer> numbers = Arrays.asList( 5, 4, 1, 3, 9, 8, 6, 7, 2, 0 ); 
		List<String> digits = new ArrayList<String>();
		digits.add("zero");
		digits.add("one");
		digits.add("two");
		digits.add("three");
		digits.add("four");
		digits.add("five");
		digits.add("six");
		digits.add("seven");
		digits.add("eight");
		digits.add("nine");
		digits.add("ten");
	
		//FIXME Arrays.ArrayList is not accessible throught  public method
		// so it does not work with Arrays.asList lists produced
		AdvTupleCursor tupleCursor = new AdvTupleCursor(numbers, "test").
		select(colOBJCALL("digits names", val(digits, "digits"), "get", col("test.value")),
				
				NewLINQFunctions.function("Even", new Functional.UnaryFunction<Integer, Boolean>(){

					@Override
					public Boolean invoke(Integer arg) {					
						return arg % 2 == 0;
					}
					
				}, col("test.value")));
			for(Tuple obj : tupleCursor){
				System.out.println(obj);
			}
	}
	
	
	
	public static void joinExample(){
		AdvTupleCursor tupleCursor = 
			new AdvTupleCursor( PRODUCTS, "products");
		AdvTupleCursor tupleCurso2 = 
			new AdvTupleCursor( CUSTOMERS, "customers").join("joinCursor", 
					tupleCursor, NewLINQFunctions.function("a",new Functional.UnaryFunction<Customer,Integer >() {

						@Override
						public Integer invoke(Customer arg) {
							return arg.getId();
						}
						
					}, 
					col("customers.value") ).EQ(
					
							NewLINQFunctions.function("b", new Functional.UnaryFunction<Product, Integer>() {

								@Override
								public Integer invoke(Product arg) {
									return arg.numberInStock;
								}
								
								
							}, col("products.value")) 
							)
					);
		for(Tuple t : tupleCurso2){
			System.out.println(t);
		}
	}
	
	public static void selectExample(){
		Integer[] array = { 5, 4, 1, 3, 9, 8, 6, 7, 2, 0};
		AdvTupleCursor tupleCursor = new AdvTupleCursor("test", array ).
		select( col("value"));
		for(Tuple obj : tupleCursor){
			System.out.println(obj);
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println("++++++++++++++Where Examples+++++++++++++++++");
//		whereExample_1();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		whereExample_2();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		whereExample_3();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		whereExample_4();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		whereExample_5();
//		System.out.println("+++++++++++++++Select Examples++++++++++++++++");
//		selectExample_1();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		selectExample_2();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		selectExample_3();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		selectExample_4();
//		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//		selectExample_5();
//		joinExample();
//		selectExample();
	}

}
