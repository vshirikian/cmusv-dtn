package edu.cmu.sv.geocamdtn.lib;

public interface Constants {
	// Intent registrations for cross application/service communication
	static final String ACTION_CREATE_DTN_BUNDLE = "edu.cmu.sv.geocamdtn.ACTION_CREATE_DTN_BUNDLE";
	static final String ACTION_MEDIATE_DTN_BUNDLE = "edu.cmu.sv.geocamdtn.ACTION_MEDIATE_DTN_BUNDLE";
	static final String ACTION_RECEIVE_DTN_BUNDLE = "edu.cmu.sv.geocamdtn.ACTION_RECEIVE_DTN_BUNDLE";
	
	// The key for the intent extra fields
	static final String IKEY_DTN_BUNDLE_PAYLOAD = "edu.cmu.sv.geocamdtn.DTN_BUNDLE_PAYLOAD";
	
	// The key for mapping the photo in the intent sent to GeoCamDTNService (as specified in GeoCam source code)
	static final String FILE_KEY = "photo";

	// The keys for mapping the intent sent dictionary to DTNMediatorService
	static final String DTN_DEST_EID_KEY = "dest_eid";
	static final String DTN_EXPIRATION_KEY = "expiration";
	static final String DTN_PAYLOAD_KEY = "payload";

	// The key for mapping the intent sent from BundleDaemon for return receipts
	static final String DTN_BUNDLE_KEY = "bundle";
	
	// DTN bundle constants
	static final String STATIC_GATEWAY_EID = "dtn://staticgw.dtn/geocam";
	static final int BUNDLE_EXPIRATION = 3600; // Never expires
	static final int BUNDLE_DOPTS = 0; // No option processing for now
}