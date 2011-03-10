package edu.cmu.sv.geocamdtn.lib;

public interface Constants {
	static final String ACTION_CREATE_DTN_BUNDLE = "edu.cmu.sv.geocamdtn.ACTION_CREATE_DTN_BUNDLE";
	
	// The fileKey as specified in GeoCam source code.
	static final String FILE_KEY = "photo";

	
	// Keys for intent extra fields
	static final String IKEY_DTN_BUNDLE_PAYLOAD = "edu.cmu.sv.geocamdtn.DTN_BUNDLE_PAYLOAD";
	
	// DTN bundle constants
	static final String STATIC_GATEWAY_EID = "dtn://staticgw.dtn/geocam";
	static final int BUNDLE_EXPIRATION = 0; // Never expires
	static final int BUNDLE_DOPTS = 0; // no option processing for now
}