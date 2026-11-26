package com.cinematography.translationapi.cinematography.dtos;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cinematography.core.constants.CommonResponseCodes;

public class ApiResponseCode extends CommonResponseCodes {
    
	private ApiResponseCode() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static List<String> getStatusListCustomer() {
		return STATUS_LIST_CUSTOMER;
	}

	public static List<String> getStatusListCustomerD() {
		return STATUS_LIST_CUSTOMER_D;
	}

	public static List<String> getStatusListContract() {
		return STATUS_LIST_CONTRACT;
	}

	public static List<String> getStatusListContractD() {
		return STATUS_LIST_CONTRACT_D;
	}

	public static List<String> getStatusListContractApi() {
		return STATUS_LIST_CONTRACT_API;
	}

	public static List<String> getSUMMARYTYPES() {
		return SUMMARY_TYPES;
	}

	public static List<String> getCUSTOMERUSAGES() {
		return CUST_USAGES;
	}

	public static List<String> getCONTSTAGES() {
		return CONT_STAGES;
	}

	public static List<String> getALLOWEDCONTENTTYPE() {
		return ALLOWED_TYPES;
	}

	public static List<String> getCONTRATSTAGE() {
		return CONT_STAGES_BOTH;
	}

	public static List<String> getCONTSTAGE() {
		return CONT_STAGE;
	}

	public static List<String> getGender() {
		return USER_GENDER;
	}

	public static List<String> getSecured() {
		return USER_SECURED;
	}

	public static List<Integer> getRoleId() {
		return USR_ROLE_ID;
	}

	public static List<Long> getUSERPERMISSION() {
		return USR_PERMISSION;
	}
	

	public static final String CUSTID_NUM_MSG = "Customer ID should be numeric";
	public static final String CONTID_NUM_MSG = "Contract ID should be numeric";
	public static final String USERTID_NUM_MSG = "User Id should be numeric";
	public static final String ACTIVE = "ACTIVE";
	public static final String CANCELLED = "CANCELLED";
	public static final String DELETED = "DELETED";
	public static final String EXHAUSTED = "EXHAUSTED";
	public static final String EXPIRED = "EXPIRED";
	public static final String INACTIVE = "INACTIVE";
	public static final String CONT_PROD = "Production";
	public static final String CONT_TRIAL = "Trial";
	public static final String MALE = "MALE";
	public static final String FEMALE = "FEMALE";
	public static final String OTHERS = "OTHERS";
	public static final String YES = "YES";
	public static final String NO = "NO";

	public static final String CC_A = "A";
	public static final String CC_C = "C";
	public static final String CC_D = "D";
	public static final String CC_E = "E";
	public static final String C_A = "A";
	public static final String C_I = "I";
	public static final String CONT_P = "P";
	public static final String CONT_T = "T";
	public static final String USR_GEN_M = "M";
	public static final String USR_GEN_F = "F";
	public static final String USR_GEN_O = "O";
	public static final String USER_SECURED_Y = "Y";
	public static final String USER_SECURED_N = "N";

	private static final List<String> STATUS_LIST_CUSTOMER = Collections.unmodifiableList(Arrays.asList("A", "I"));
	private static final List<String> STATUS_LIST_CUSTOMER_D = Arrays.asList("A", "I", "D");
	private static final List<String> STATUS_LIST_CONTRACT = Arrays.asList("A", "C", "E");
	private static final List<String> STATUS_LIST_CONTRACT_D = Arrays.asList("AA", "A", "C", "E", "D");
	private static final List<String> STATUS_LIST_CONTRACT_API = Arrays.asList("A", "C");
	private static final List<String> SUMMARY_TYPES = Arrays.asList("D", "W", "M", "Y");
	private static final List<String> CUST_USAGES = Arrays.asList("M", "Y");
	private static final List<String> CONT_STAGES = Arrays.asList(CONT_PROD, CONT_TRIAL);
	private static final List<String> CONT_STAGES_BOTH = Arrays.asList("BOTH", "PRODUCTION", "TRIAL");
	private static final List<String> CONT_STAGE = Arrays.asList("AA", "P", "T");

	private static final List<String> USER_GENDER = Arrays.asList("M", "F", "O");
	private static final List<String> USER_SECURED = Arrays.asList("Y", "N");
	private static final List<Integer> USR_ROLE_ID = Arrays.asList(1, 2, 3, 4);

	private static final List<Long> USR_PERMISSION = Arrays.asList(1L, 2L,3L, 4L, 5L, 6L);

	public static final String DB_MSG = "Error closing DB connection";
	public static final String NO_CONTENT = "404";

	public static final String USER_ID_NOT_ZERO = "User ID cannot be zero";
	public static final String USER_EXIST = "User is already exist";
	public static final String USER_DET_NOT_FOUND = "User Details not found";
	public static final String USER_ID_NOT_EXIST = "User Id does not exist in the database. Please provide the correct User ID";
	public static final String USER_ID_REQ = "User ID is required";
	public static final String PER_DET_NOT_FOUND = "Permission Details not found";
	public static final String USER_STATUS_CHANGE = "User has status change successfully";

	public static final String CUST_ID = "customerId";
	public static final String CUST_ID_REQ = "Customer ID is required";
	public static final String CUST_ID_NOT_ZERO = "Customer ID cannot be zero or less than zero";
	public static final String CUST_DET_NOT_FOUND = "Customer Details not found";
	public static final String CUST_DETAIL_DELETE = "Customer Details deleted successfully";
	public static final String CUST_ID_NOT_EXIST = "Customer Id does not exist in the database. Please provide the correct Customer ID";
	public static final String CUST_ID_LENGTH = "Customer Id should be between 4 and 20 characters";

	public static final String CONT_ID_REQ = "Contract ID is required";
	public static final String CONT_ID_NOT_ZERO = "Contract ID cannot be zero or less than zero";
	public static final String CONT_DET_NOT_FOUND = "Contract Details not found";
	public static final String CONT_DELAIL_DELETE = "Contract Details deleted successfully";
	public static final String CONT_ID_LENGTH = "Contract Id should not exceed 20 characters";
	public static final String CONT_ID_NOT_EXIST = "Contract Id does not exist in the database. Please provide the correct Contract ID.";

	public static final String CONT_API_ID_REQ = "Contract Api Id is required";
	public static final String CONT_API_ID_NOT_ZERO = "Contract Api ID cannot be zero";
	public static final String CONT_API_DET_NOT_FOUND = "Contract Api Details not found";
	public static final String CONT_API_DETAIL_DELETE = "Contract Api Details deleted successfully";
	public static final String CONT_API_ID_LENGTH = "Contract Api Id should not exceed 20 characters";
	public static final String CONT_API_ID_NOT_EXIST = "Contract Api Id does not exist in the database. Please provide the correct Contract Api ID";
	public static final String CONT_API_ID_NUM_MSG = "Contract Api ID should be numeric";
	public static final String CONT_API_EXIST = "Contract Id, Customer Id, Api ID exists for this Contract Api Id. Please update the Contract Api Id";

	public static final String API_ID_REQ = "API ID is required";
	public static final String APIID_NOT_ZERO = "API ID cannot be zero";
	public static final String API_DET_NOT_FOUND = "Api Details not found";
	public static final String API_ID_NUM_MSG = "Api ID should be numeric";
	public static final String API_NAME_LENGTH = "Api Name should not exceed 20 characters";

	public static final String AUDIT_ID_NOT_FOUND = "Audit Details not found";
	// CI-138: audit date-range report
	public static final String AUDIT_DATE_RANGE_REQ = "fromDate and toDate are required";
	public static final String AUDIT_DATE_FORMAT = "fromDate and toDate must be in yyyy-MM-dd format";
	public static final String AUDIT_DATE_ORDER = "fromDate must be on or before toDate";
	public static final String AUDIT_DATE_RANGE_MAX = "Date range must not exceed 365 days";
	public static final int AUDIT_REPORT_MAX_RANGE_DAYS = 365;
	// CI-138: filters on the actionable audit fields
	public static final String AUDIT_COUNTRY_FORMAT = "importCountryCode and exportCountryCode must be 2-letter country codes";
	public static final String AUDIT_TYPE_FORMAT = "type must be at most 50 letters, digits or hyphens";
	public static final String AUDIT_CLASSIFICATION_FORMAT = "classification must be at most 50 letters, digits or dots";
	// CI-138: audit search paging/sorting and id checks
	public static final String AUDIT_CUST_ID_LENGTH = "Customer Id should not exceed 20 characters";
	public static final String AUDIT_SORT_BY_INVALID = "sortBy must be one of ";
	public static final String AUDIT_SORT_DIR_INVALID = "sortDir must be asc or desc";

	// US-1634186: contract expiry dashboard
	public static final int DASH_MIN_WINDOW_DAYS = 1;
	public static final int DASH_MAX_WINDOW_DAYS = 365;
	/** Window used when the user has not saved one (AC-08: defaults to 30 days). */
	public static final int DASH_DEFAULT_WINDOW_DAYS = 30;
	public static final String DASH_DAYS_INVALID = "days: must be a whole number between " + DASH_MIN_WINDOW_DAYS
			+ " and " + DASH_MAX_WINDOW_DAYS;
	public static final String DASH_MIN_DAYS_INVALID = "minDays: must be a whole number from 0 to maxDays";
	public static final String DASH_MAX_DAYS_INVALID = "maxDays: must be a whole number from minDays to days";
	public static final String DASH_PAGE_INVALID = "page: must be a whole number of 0 or more";
	public static final String DASH_SIZE_INVALID = "size: must be one of ";
	public static final String DASH_SORT_INVALID = "sort: must be one of ";
	public static final String DASH_SORT_DIR_INVALID = "sort: direction must be asc or desc";
	public static final String DASH_TOKEN_REQ = "Authorization token is required";
	public static final String DASH_WINDOW_SAVED = "Expiry window set to ";
	public static final String DASH_LIST_ERROR = "Error occurred while fetching expiring contracts.";
	public static final String DASH_SUMMARY_ERROR = "Error occurred while summarising expiring contracts.";
	public static final String DASH_PREF_GET_ERROR = "Error occurred while fetching the dashboard preference.";
	public static final String DASH_PREF_SAVE_ERROR = "Error occurred while saving the dashboard preference.";
	public static final int DASH_DEFAULT_PAGE = 0;
	public static final int DASH_DEFAULT_SIZE = 10;
	public static final String DASH_DEFAULT_SORT = "annualEffectiveTo";
	public static final String DASH_TIE_BREAK_SORT = "contractId";
	/** Sortable API fields and the query property each one sorts on. */
	public static final Map<String, String> DASH_SORT_FIELDS = Map.of(CUST_ID, CUST_ID, "customerName",
			"customerName", DASH_TIE_BREAK_SORT, DASH_TIE_BREAK_SORT, "annualEffectiveFrom", "effectiveFrom",
			DASH_DEFAULT_SORT, "effectiveTo", "daysToExpiry", "effectiveTo", "contractEnd", "contractEndDate",
			"orderNumber", "orderNumber");
	public static final String DASH_AGREEMENT_ENDS = "AGREEMENT_ENDS";
	public static final String DASH_ANNUAL_RENEWAL = "ANNUAL_RENEWAL";

	public static final String COUNTRIES_NOT_FOUND = "Countries not found";

	public static final String PARSING_DATE_ERROR_MSG = "Error parsing date string: {} with pattern: {}";

	public static final String INVALID_NO = " not valid numbers";
	public static final String INVALID_ID = "Invalid IDs: ";
	public static final String MIN_LENGTH = " must be at least 5 digits long";
	public static final String POSITIVE_NO = " must be greater than zero";
	public static final String NOT_FOUND = " not found in the database";
	public static final String USE_VALID = " Please use valid numeric API IDs from the available list: ";
	public static final String API_ID_NOT_FOUND = " API IDs not found in database";

	protected static final List<String> ALLOWED_TYPES = Arrays.asList("application/pdf", "application/msword",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-powerpoint",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation", "image/png", "image/jpeg",
			"text/plain", "application/zip", "application/json");
	public static final String CONTRACTSTATUS = "contractStatus";
	
	public static final String USERNAME = "username";
	public static final String REPORT_REQUEST = "reportRequest must not be null";
	public static final String USER_VALUE = "UserValue must not be null";
	public static final String ROLEID = "roleId";
	public static final String EMAILID = "emailId";
	public static final String SECURE = "secure";
	public static final String  BEARER = "Bearer ";
	public static final String AUTHORIZATION = "Authorization";
	public static final String  PASSWORD = "password";
	public static final String NO_DATA_SOURCE = "No DataSource available.";
	public static final String AUTH_API_FAILED= ">>> [ADMIN-API.{}.{}] -> failed: {} <<<";
	
	public static final String STATUS_SHOULD = "Status should be :";
	public static final String TOKEN = "token";
	public static final String UNEXPECTED_ERROR = "An unexpected error occurred while processing your request.";
	public static final String CUSTOMER_ID = "customer_id";
	public static final String MONTH_YEAR = "monthYear";

	public static final String MONTH_PATTERN = "^(0[1-9]|1[0-2])$";
	public static final String YEAR_PATTERN = "^\\d{4}$";
	public static final String MONTH_INVALID = "month.invalid";
	public static final String MONTH_YEAR_INVALID = "monthYear.invalid";
	public static final String NOT_FOUND_POWERBI_TRANS_DATA = "No data found for powerbi transaction report";
	public static final String OTHER = "OTHER";
	public static final String  POSTMAN_COLL_FILE= "Postman collection file '";
	public static final String FILE = "File '";
	public static final String CONTRACT_PDF = "CONTRACT_PDF";
	public static final String CONTRACT_WORD = "CONTRACT_WORD";
	public static final String POSTMAN_COLLECTION = "POSTMAN_COLLECTION";

	public static final String PAGE = "page";
	public static final String SIZE = "size";
	public static final String PAGE_INVALID = "page.invalid";
	public static final String SIZE_INVALID = "size.invalid";
	public static final List<Integer> ALLOWED_PAGE_SIZES =  List.of(10, 20, 30, 50, 100);
	public static final String PAGE_SIZE = "pageSize";
	public static final String STATUS = "status";
	public static final String SKU_NUMBER = "skuNumber";
	public static final String TOTAL_ROWS =  "TotalRows";
}
