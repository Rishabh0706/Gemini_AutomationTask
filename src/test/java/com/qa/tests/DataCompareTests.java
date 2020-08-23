package com.qa.tests;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeMethod;

import com.restCountries.qa.base.TestBase;
import com.restCountries.qa.util.TestUtil;

public class DataCompareTests extends TestBase {
	
	TestBase testBase;
	String url;
	
	@BeforeMethod
	public void setUp() {
		testBase = new TestBase();
		url = prop.getProperty("url");
	}
	
	@Test
	public void TC01(Method method) {
		
		logger = report.createTest("All countries that are missing in DB table");
		
		logger.info("Get data from API");
		ArrayList<String> countriesList = TestUtil.getCountriesFromApi(logger);
		
		logger.info("Get data from DB");
		String query = "select Country from countries";
		ResultSet dataSet = TestUtil.getDataFromDB(query, logger);
		
		// output file to save the list
		String outFileName = System.getProperty("user.dir")+"/Output/missingCountries_"+method.getName()+".txt";
		
		if(dataSet != null) {
			try {
				while(dataSet.next()) {
					countriesList.remove(dataSet.getString("Country"));
				}
				logger.info("Data Matched");
			} catch(SQLException e) {
				System.out.println(e);
				logger.error(e);
			}
			
			if(countriesList.size() >= 1) {
				logger.info("writing missing countries list to a file");
				TestUtil.writeArrayListToFile(countriesList, outFileName, logger);
				
				logger.fail(countriesList.size()+" coutries are missing from DB. "
						+ "Click <a href='"+outFileName+"'>Here</a> to open list");
			} else
				logger.pass("API and DB data is in sync");
		} else {
			logger.fail("No data available in DB to match");
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void TC02(Method method) {
		
		logger = report.createTest("All Countries of which data "
				+ "(Capital and Currency) between DB and API having mismatch");
		
		logger.info("Get data from API");
		JSONArray apiDataArray =  TestUtil.getCapitalCurrencyDataFromApi(logger);
		
		logger.info("Get data from DB");
		String query = "select * from countries";
		ResultSet dataSet = TestUtil.getDataFromDB(query, logger);
		
		JSONArray dbDataArray = new JSONArray();
		
		if(dataSet != null) {
			try {
				while(dataSet.next()) {
					JSONObject json = new JSONObject();
					
					json.put("Country Name", dataSet.getString("Country"));
					json.put("Capital", dataSet.getString("Capital"));
					json.put("Currency_Code", dataSet.getString("Currency_Code"));
					
					dbDataArray.add(json);
				}
			} catch(SQLException e) {
				logger.error(e);
			}
			
			JSONArray diffArray = TestUtil.compareJson(apiDataArray, dbDataArray);
			logger.info("Data Matched");
			
			// output file to save the mismatched data
			String outFileName = System.getProperty("user.dir")+"/Output/dataMisMatch_"+method.getName()+".txt";
			
			if(diffArray.size() >= 1) {
				logger.info("writing mismatched data to a file");
				TestUtil.writeJSONArrayToFile(diffArray, outFileName, logger);
				
				logger.fail("Data for " + diffArray.size()+" coutries between table and API having mismatch. "
						+ "Click <a href='"+outFileName+"'>Here</a> to see mismatched data");
			} else
				logger.pass("No mismatch between API and DB data");
		} else {
			logger.fail("No data available in DB to match");
		}
	}
	
	
	@Test
	public void TC03() {
		
		logger = report.createTest("To check whether country which has "
				+ "maximum number of borders are same or not between API and DB");
		
		logger.info("Get data from API");
		String countryNameFromApi = TestUtil.getCountryWithMaxBordersFromApi(logger);
		
		logger.info("Get data from DB");
		String query = "SELECT borders.C_ID, countries.Country  ,COUNT(*) AS BorderCount " + 
				"FROM borders INNER JOIN countries ON borders.C_ID = countries.C_ID " + 
				"GROUP BY C_ID order by BorderCount DESC LIMIT 1";
		
		ResultSet dataSet = TestUtil.getDataFromDB(query, logger);
		
		String countryNameFromDB = "";
		
		if (dataSet != null) {
			try {
				while(dataSet.next()) {
					countryNameFromDB = dataSet.getString("Country");
				}
			} catch (SQLException e) {
				logger.error(e);
			}
		} else {
			logger.fail("No data available in DB to match");
		}
		
		if (countryNameFromApi.equals(countryNameFromDB))
			logger.pass("Coutries with maximum borders in API and DB are same");
		else 
			logger.fail("Coutries with maximum borders in API and DB are different");
		
		SoftAssert softAssert = new SoftAssert();
		softAssert.assertEquals(countryNameFromApi, countryNameFromDB, "Coutries with maximum borders in API and DB are different");
		softAssert.assertAll();
		
	}
}
