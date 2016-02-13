package net.geant.coco.agent.portal.rest;

public class Student {  
	 public int id;  
	 public String firstName;  
	 public String lastName;  
	 public String email;  
	 public String phone;  
	  
	 protected Student() {  
	 }  
	  
	 public Student(int id, String firstName, String lastName, String email,  
	   String phone) {  
	  this.id = id;  
	  this.firstName = firstName;  
	  this.lastName = lastName;  
	  this.email = email;  
	  this.phone = phone;  
	 }  
}
