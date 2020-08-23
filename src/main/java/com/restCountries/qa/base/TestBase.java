package com.restCountries.qa.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeSuite;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.restCountries.qa.util.TestUtil;

public class TestBase {
	
	public static Properties prop;
	public ExtentReports report;
	public ExtentTest logger;
	protected static Connection connect;
	
	public TestBase(){
		
		try {
			prop = new Properties();
			FileInputStream ip = new FileInputStream(System.getProperty("user.dir")+"/src/main/java/com/restCountries"
					+ "/qa/config/config.properties");
			prop.load(ip);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@BeforeSuite
	public void setUpSuite() {
		
		ExtentSparkReporter extent = new ExtentSparkReporter(new File(System.getProperty("user.dir")+
				"/Reports/CoutriesTest_"+TestUtil.getCurrentDateTime()+".html"));
		report = new ExtentReports();
		report.attachReporter(extent);
		
	}
	
	@AfterMethod
	public void tearDownMethod() {
		report.flush();
		try {
			connect.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	

}
