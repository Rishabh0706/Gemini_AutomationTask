package com.restCountries.qa.util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.aventstack.extentreports.ExtentTest;

import com.restCountries.qa.base.TestBase;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class TestUtil extends TestBase {
	
//	static Response response;
	
	public static Response getResponseFromApi() {
		
		//Specify base URI
		RestAssured.baseURI = prop.getProperty("url");
		
		//Request object
		RequestSpecification request = RestAssured.given();
		
		//Get Response object
		Response response = request.get();
		
		return response;
	}
	
	
	public static ArrayList<String> getCountriesFromApi(ExtentTest logger) {
		
		Response response = getResponseFromApi();
		logger.pass("API Executed: "+prop.getProperty("url"));
		
		JsonPath jsonPathEvaluator = response.jsonPath();
		
		ArrayList<String> countriesList = jsonPathEvaluator.get("name");
		
		return countriesList;
		
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getCapitalCurrencyDataFromApi(ExtentTest logger) {
		
		Response response = getResponseFromApi();
		logger.pass("API Executed: "+prop.getProperty("url"));
		
		ArrayList<LinkedHashMap<String,?>> jsonAsArrayList = response.jsonPath().get("");
		
		JSONArray apiDataArray = new JSONArray();
		
		for (int i = 0; i<jsonAsArrayList.size(); i++) {
			
			LinkedHashMap<String,?> obj = jsonAsArrayList.get(i);
		
			Object countryName = obj.get("name");
			
			Object capital = obj.get("capital");
			
			Object currencies = obj.get("currencies");
			ArrayList<LinkedHashMap<String, String>> currencyArray = (ArrayList<LinkedHashMap<String,String>>)currencies;
			LinkedHashMap<String, String> currency = currencyArray.get(0);
			String currencyCode = currency.get("code"); 
			
			JSONObject json = new JSONObject();
			
			json.put("Country Name", countryName);
			json.put("Capital", capital);
			json.put("Currency_Code", currencyCode);
			
			apiDataArray.add(json);
		}
		
		return apiDataArray;
		
	}
	
	@SuppressWarnings("unchecked")
	public static String getCountryWithMaxBordersFromApi(ExtentTest logger) {
		
		Response response = getResponseFromApi();
		logger.pass("API Executed: "+prop.getProperty("url"));
		
		String countryName = "";
		
		ArrayList<LinkedHashMap<String,?>> jsonAsArrayList = response.jsonPath().get("");
		
		// HashMap to store Country name and border count pair
		LinkedHashMap<String, Integer> hm = new LinkedHashMap<String, Integer>();
		
		for (int i = 0; i < jsonAsArrayList.size(); i++) {
			
			LinkedHashMap<String,?> obj = jsonAsArrayList.get(i);
			
			String name = obj.get("name").toString();
			
			Object bordersList = obj.get("borders");
			ArrayList<LinkedHashMap<String, String>> bordersArray = (ArrayList<LinkedHashMap<String,String>>)bordersList;
			int bordersCount = bordersArray.size();
			
			hm.put(name, bordersCount);
		}

		int maxCount = 0;
		for(Map.Entry<String, Integer> m:hm.entrySet()) {
			
			if ((int)m.getValue() > maxCount) {
				maxCount = (int)m.getValue();
				countryName = m.getKey().toString();
			}
		}
		
		return countryName;
		
	}
	
	public static ResultSet getDataFromDB(String query, ExtentTest logger) {
		
		ResultSet rs = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			String host = prop.getProperty("host");
			String port = prop.getProperty("port");
			String dataBase = prop.getProperty("database");
			String user = prop.getProperty("user");
			String password = prop.getProperty("password");
			
			connect = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+dataBase,user,password);
			logger.info("Connection made to DB");
			
			Statement stmt = connect.createStatement();
			
			rs = stmt.executeQuery(query);
			logger.info("Query Executed: "+query );

		} catch(Exception e) {
			System.out.println(e);
		}
		return rs;
	}
	
	
	@SuppressWarnings("unchecked")
	public static JSONArray compareJson(JSONArray apiDataArray, JSONArray dbDataArray) {
		
		JSONArray diffArray = new JSONArray();
				
		if (!apiDataArray.toString().equals(dbDataArray.toString())) {
			
			for (int i = 0; i < dbDataArray.size(); i++) {
				
				JSONObject dbObj = (JSONObject) dbDataArray.get(i);
				
				for (int j = 0; j < apiDataArray.size(); j++) {
					
					JSONObject apiObj = (JSONObject) apiDataArray.get(j);
										
					LinkedHashMap<String,Object> diffObj = new LinkedHashMap<String,Object>();
					
					if (dbObj.get("Country Name").equals(apiObj.get("Country Name"))) {
						
						if (!dbObj.get("Capital").equals(apiObj.get("Capital"))) {
							diffObj.put("Country Name", dbObj.get("Country Name"));
							diffObj.put("Data", "Capital");
							diffObj.put("API Value", apiObj.get("Capital"));
							diffObj.put("DB Value", dbObj.get("Capital"));
							
							diffArray.add(diffObj);
						}
						
						if (!dbObj.get("Currency_Code").equals(apiObj.get("Currency_Code"))) {
							diffObj.put("Country Name", dbObj.get("Country Name"));
							diffObj.put("Data", "Currency");
							diffObj.put("API Value", apiObj.get("Currency_Code"));
							diffObj.put("DB Value", dbObj.get("Currency_Code"));
							
							diffArray.add(diffObj);
						}		
					}
				}
			}
		}
		return diffArray;
	}
	
	
	
	public static void writeArrayListToFile(ArrayList<String> arrayList, String filename, ExtentTest logger) {
		
		File file = new File(filename);
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			PrintWriter writer = new PrintWriter(fo);
			
			for (String line : arrayList) {
		        writer.println(line);
		    }
			
			writer.flush();
		    writer.close();
		    fo.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static void writeJSONArrayToFile(JSONArray jsonArray, String filename, ExtentTest logger) {
		
		File file = new File(filename);
		FileOutputStream fo;
		try {
			fo = new FileOutputStream(file);
			PrintWriter writer = new PrintWriter(fo);
		    
		    for (int i = 0; i < jsonArray.size(); i++) {

		    	LinkedHashMap<String, Object> mapObj = (LinkedHashMap<String, Object>) jsonArray.get(i);
		    	
		    	String[] result = mapObj.toString().replaceAll("[{}]", "").replaceAll("=", ": ").split(", ");

		    	for (String s : result) {
		    		writer.println(s);
		    	}
		    	writer.println();
		    }

		    writer.flush();
		    writer.close();
		    fo.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	
	public static String getCurrentDateTime() {
		
		DateFormat customFormat = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		
		Date currentDate = new Date();
		
		return customFormat.format(currentDate);
	}

}




