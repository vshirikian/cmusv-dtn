/*
 *	  This file is part of the Master Thesis "Security in Bytewalla" by Sebastian Domancich.
 *    More information can be found at "http://www.tslab.ssvl.kth.se/csd/projects/1011248/".
 *    
 *    Copyright 2010 Telecommunication Systems Laboratory (TSLab), Royal Institute of Technology, Sweden.
 *    
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *    
 */

package se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BP_Local;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfoVec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.SDNV;

import android.util.Log;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.status_report_reason_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.BufferHelper;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.SerializableByteBuffer;

/**
 * Represents a generic ciphersuite. This superclass declares all the methods that a specific ciphersuite must implement.
 * This generic class handles suite registration and other general tasks.
 * 
 * "Block processor superclass for ciphersuites
 * This level handles suite registration and
 * similar activities but no specific work"[DTN2]
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */
public class Ciphersuite 
{

	public static boolean CS_FAIL_IF_NULL(BP_Local x)
	{
		if (x == null)
		{
			Log.e(TAG, String.format("error"));
			return true;
		}
		else
			return false;
	}

	private static String TAG = "Ciphersuite";

	private static final int CS_MAX =1024;

	/**
	 * Default buffer size
	 */
	//private final static int DATA_BUFFER_SIZE = CLConnection.DEFAULT_BLOCK_BUFFER_SIZE;

	/// @{ Import some typedefs from other classes
	//typedef BlockInfo::list_owner_t list_owner_t;
	//typedef BundleProtocol::status_report_reason_t status_report_reason_t;
	/// @}

	/**
	 * Values for security flags that appear in the ciphersuite flags field. Ex: HAS_SOURCE, HAS_DEST, HAS_PARAMS, HAS_PARAMS, HAS_CORRELATOR, has result.
	 * Defined in page 12 of security-draft15.
	 *  
	 */
	public static enum ciphersuite_flags_t 
	{
		CS_BLOCK_HAS_SOURCE("Ciphersuite Block has source",         (byte)0x10), 
		CS_BLOCK_HAS_DEST("Ciphersuite Block has destination",      (byte)0x08), 
		CS_BLOCK_HAS_PARAMS("Ciphersuite Block has ciphersuite-parameters field",     (byte)0x04), 
		CS_BLOCK_HAS_CORRELATOR("Ciphersuite Block has correlator", (byte)0x02),
		CS_BLOCK_HAS_RESULT("Ciphersuite Block has result",         (byte)0x01),
		;

		private static final Map<Byte, ciphersuite_flags_t> lookupCode = new HashMap<Byte, ciphersuite_flags_t>();
		private static final Map<String, ciphersuite_flags_t> lookupCaption = new HashMap<String, ciphersuite_flags_t>();

		static {
			for (ciphersuite_flags_t s : EnumSet
					.allOf(ciphersuite_flags_t.class))
			{ 
				lookupCode.put(s.getCode(), s);
				lookupCaption.put(s.getCaption(), s);
			}
		}

		private byte code;
		private String caption;
		private ciphersuite_flags_t(String caption, byte code) 
		{
			this.code = code;
			this.caption = caption;
		}

		public byte getCode() {
			return code;
		}

		public String getCaption() {
			return caption;
		}

		public static ciphersuite_flags_t get(byte code) {
			return lookupCode.get(code);
		}
	}


	/**
	 * Values for flags that appear in the 
	 * proc_flags_ field. [DNT2]
	 * XXX sd. Internal use and not in the standard?
	 */
	public static enum proc_flags_t 
	{
		CS_BLOCK_RESERVED0("Ciphersuite Block is reserved",                                        (byte)0x01), 
		CS_BLOCK_PROCESSED("Ciphersuite Block has been processed",                                 (byte)0x02), 
		CS_BLOCK_DID_NOT_FAIL("Ciphersuite Block did not fail",                                    (byte)0x04), 
		CS_BLOCK_FAILED_VALIDATION("Ciphersuite Block failed validation",                          (byte)0x08),
		CS_BLOCK_PASSED_VALIDATION("Ciphersuite Block passed validation",                          (byte)0x10),
		CS_BLOCK_COMPLETED_DO_NOT_FORWARD("Ciphersuite Block completed. Do not forward",           (byte)0x20),
		CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND("Ciphersuite Block processing failed. Do not send", (byte)0x40),
		;

		private static final Map<Byte, proc_flags_t> lookupCode = new HashMap<Byte, proc_flags_t>();
		private static final Map<String, proc_flags_t> lookupCaption = new HashMap<String, proc_flags_t>();

		static {
			for (proc_flags_t s : EnumSet
					.allOf(proc_flags_t.class))
			{ 
				lookupCode.put(s.getCode(), s);
				lookupCaption.put(s.getCaption(), s);
			}
		}

		private byte code;
		private String caption;
		private proc_flags_t(String caption, byte code) 
		{
			this.code = code;
			this.caption = caption;
		}

		public byte getCode() {
			return code;
		}

		public String getCaption() {
			return caption;
		}

		public static proc_flags_t get(byte code) {
			return lookupCode.get(code);
		}
	}


	/**
	 * Types identifying security-parameter and security-result fields. Identifies the type in the tuple: 
	 * type-length-value that represents each item. Ex: IV, key info, inegrity signature, PCB ICV. Defined in Pag 21 of sec-draft15.
	 * XXX sd. 
	 */
	public static enum ciphersuite_fields_t 
	{
		CS_reserved0                        ("CS_reserved0", (byte) 0x00), 
		CS_IV_field                         ("CS_IV_field", (byte) 0x01), 
		CS_key_ID_field                     ("CS_key_ID_field", (byte) 0x02), 
		CS_encoded_key_field                ("CS_encoded_key_field", (byte) 0x03),
		CS_fragment_offset_and_length_field ("CS_fragment_offset_and_length_field", (byte) 0x04),
		CS_signature_field					("CS_signature_field", (byte) 0x05),
		CS_reserved6						("CS_reserved6", (byte) 0x06),                       
		CS_C_block_salt						("CS_C_block_salt", (byte) 0x07),                    
		CS_C_block_ICV_field				("CS_C_block_ICV_field", (byte) 0x08),                
		CS_reserved9						("CS_reserved9", (byte) 0x09),                        
		CS_encap_block_field				("CS_encap_block_field", (byte) 0x10),                
		CS_reserved11						("CS_reserved11", (byte) 0x11)                       
		;

		private static final Map<Byte, ciphersuite_fields_t> lookup = new HashMap<Byte, ciphersuite_fields_t>();
		private static final Map<String, ciphersuite_fields_t> caption_map = new HashMap<String, ciphersuite_fields_t>();

		static {
			for (ciphersuite_fields_t s : EnumSet
					.allOf(ciphersuite_fields_t.class))
			{ 
				lookup.put(s.getCode(), s);
				caption_map.put(s.getCaption(), s);
			}

		}

		private byte code_;
		private String caption_;
		private ciphersuite_fields_t(String caption, byte code) {
			this.caption_ = caption;
			this.code_ = code;
		}

		public byte getCode() {
			return code_;
		}
		public String getCaption() {
			return caption_;
		}

		public static ciphersuite_fields_t get(byte code) {
			return lookup.get(code);
		}
	}

	/// Constructor
	public Ciphersuite()
	{
		Log.d(TAG, String.format(  "Ciphersuite()"));
		//ciphersuites_=new Ciphersuite[CS_MAX];
	}

	public static void register_ciphersuite(Ciphersuite cs)
	{
		Log.d(TAG, String.format(  "register_ciphersuite()"));
		int    num = cs.cs_num();

		if ( num <= 0 || num >= CS_MAX )
			return;            //out of range

		// don't override an existing suite
		assert(ciphersuites_[num] == null); //**********************************need to initialize the array first??? not done in the RI.
		ciphersuites_[num] = cs;
	}

	public static Ciphersuite find_suite(long suiteNum)
	{
		Ciphersuite ret = null;
		Log.d(TAG, String.format( "find_suite()"));

		if ( suiteNum > 0 && suiteNum < CS_MAX )  // entry for element zero is illegal
			ret = ciphersuites_[(int) suiteNum];
		else
			Log.e(TAG, String.format( "find_suite: Suite number: "+suiteNum +". out of bounds."));

		return ret;
	}

	public static void init_default_ciphersuites()
	{
		Log.d(TAG, String.format(  "init_default_ciphersuites()"));
		if ( ! inited ) 
		{
			// register default block processor handlers
			BundleProtocol.register_processor(new BA_BlockProcessor());
			BundleProtocol.register_processor(new PS_BlockProcessor());
			BundleProtocol.register_processor(new C_BlockProcessor());

			// register mandatory ciphersuites
			register_ciphersuite(new Ciphersuite_BA1());
			register_ciphersuite(new Ciphersuite_PS2());
			register_ciphersuite(new Ciphersuite_C3());

			inited = true;
		}
	}

	public int cs_num()
	{
		return 0;
	}

	public int result_len()  
	{ 
		return 0; 
	}


	/**
	 * Called when a bundle is received. After BundleProtocol.consume has called Blockprocessor.consume to parse 
	 * the preamble of the ASB, it calls this method to parse the rest of the ASB. 
	 * It reads from the binary data inside block.contents() and puts it into
	 * the BP_LOCAL_CS class element (that represents an ASB). 
	 * It extracts the ciphersuite-params and security-result from the raw data, and saves it into
	 * block.localcs, following the structure: type-length-value, type-length-value, ...
	 */
	public static void parse(BlockInfo block)
	{
		Ciphersuite    cs_owner = null;
		BP_Local_CS    locals = null;
		IByteBuffer          buf;
		int          len;
		long       cs_flags;
		long       suite_num;
		int             sdnv_len;
		long       security_correlator    = 0;
		long       field_length           = 0;
		boolean           has_source;    
		boolean            has_dest;      
		boolean            has_params;    
		boolean            has_correlator;
		boolean            has_result;   
		Iterator<EndpointID> iter; 

		Log.d(TAG, String.format(  "Ciphersuite.parse() block "));
		assert(block != null);

		// preamble has already been parsed and stored, so we skip over it here
		//get the type
		//get flags sdnv
		//get length sdnv



		buf = block.contents();
		buf.position( block.data_offset());
		len = block.data_length();

		
		long[] value = new long[1];

		//get ciphersuite and flags
		sdnv_len = SDNV.decode(buf, len, value);
		suite_num=value[0];
		
		len -= sdnv_len;
		Log.e(TAG, String.format( "len sdnv_len: "+sdnv_len));
		value = new long[1];
		sdnv_len = SDNV.decode(buf,len, value);
		cs_flags=value[0];
		
		
		len -= sdnv_len;
		Log.e(TAG, String.format( "len cs_flags: "+sdnv_len));
		has_source     = (cs_flags & ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE.getCode())     != 0;
		has_dest       = (cs_flags & ciphersuite_flags_t.CS_BLOCK_HAS_DEST.getCode())       != 0;
		has_params     = (cs_flags & ciphersuite_flags_t.CS_BLOCK_HAS_PARAMS.getCode())     != 0;
		has_correlator = (cs_flags & ciphersuite_flags_t.CS_BLOCK_HAS_CORRELATOR.getCode()) != 0;
		has_result     = (cs_flags & ciphersuite_flags_t.CS_BLOCK_HAS_RESULT.getCode())     != 0;
		Log.d(TAG, String.format(  "parse() suite_num %d cs_flags 0x%x",
				suite_num, cs_flags));

		
		cs_owner = find_suite(suite_num);

		if ( ciphersuites_[(int) suite_num] != null )            // get specific subclass if it's present
			cs_owner = ciphersuites_[(int) suite_num];

		if ( block.locals() == null ) 
		{
			if ( cs_owner != null )
				cs_owner.init_locals(block);                // get owning class to allocate locals
			else
			{
				
				BP_Local_CS temp = new BP_Local_CS();
				block.set_locals(temp);
			}
		}

		locals = (BP_Local_CS) block.locals();
		assert ( locals != null );

		assert ( suite_num < 65535 );
		locals.set_owner_cs_num((short) suite_num);

		//set cs_flags
		assert ( cs_flags  < 65535  );
		locals.set_cs_flags((short) cs_flags);

		//get correlator, if present
		if (has_correlator==true) 
		{   
			value = new long[1];

			sdnv_len = SDNV.decode(buf,
					len,
					value);
			security_correlator=value[0];
			
			len -= sdnv_len;
		}
		Log.d(TAG, String.format( "parse() correlator %d",(security_correlator)));
		locals.set_correlator(security_correlator);


		//	get cs params length, and data
		if ( has_params ) 
		{    
			value = new long[1];
			sdnv_len = SDNV.decode(buf, len, value);
			field_length=value[0];
			

			len -= sdnv_len;
			Log.e(TAG, String.format( "cs params length: "+sdnv_len+" + "+field_length));
			locals.set_security_params(new SerializableByteBuffer((int) field_length));


			BufferHelper.copy_data(locals.writable_security_params(), 
					locals.writable_security_params().position(),
					buf,
					buf.position(),
					(int) field_length);

			//locals.writable_security_params().position((int) field_length); //se usa para setear length

			buf.position((int) (buf.position()+field_length));

			len -= field_length;
			Log.d(TAG, String.format( "parse() security_params len %d", field_length));
		}

		//get sec-src length and data
		//Log.d(TAG, String.format( "parse() eid_list().size() %d has_source %d has_dest %d", block.eid_list().size(), has_source, has_dest));
		Log.d(TAG, String.format( "parse() eid_list().size() %d has_source: "+has_source+". has_dest:"+ has_dest, block.eid_list().size() ));

		// XXX/pl - temp fix for blocks loaded from store
		if ( block.eid_list().size() > 0 ) 
		{
			iter = block.eid_list().iterator();
			EndpointID eid;
			if ( has_source ) 
			{    
				eid = iter.next();
				locals.set_security_src( eid.toString() );
			}

			//get sec-dest length and data
			if ( has_dest ) 
			{    
				eid = iter.next();
				locals.set_security_dest( eid.toString() );
			}
		}

		//get sec-result length and data, if present
		if ( has_result ) 
		{    
			value = new long[1];
			sdnv_len = SDNV.decode(buf, len, value);
			field_length=value[0];

			locals.set_security_result(new SerializableByteBuffer((int) field_length));
			Log.e(TAG, String.format( "sec-result length: "+sdnv_len+" + "+field_length));
			len -= sdnv_len;
			// make sure the buffer has enough space, copy data in

			BufferHelper.copy_data(locals.writable_security_result(), 
					locals.writable_security_result().position(),
					buf,
					buf.position(),
					(int) field_length);

			// locals.writable_security_result().position((int) field_length);

			buf.position((int) (buf.position()+field_length));
			len -= field_length;
			Log.e(TAG, String.format( "len final: "+len));
		}
	}

	/**
	 * "First callback for parsing blocks that is expected to append a
	 * chunk of the given data to the given block. When the block is
	 * completely received, this should also parse the block into any
	 * fields in the bundle class.
	 *
	 * The base class implementation parses the block preamble fields
	 * to find the length of the block and copies the preamble and the
	 * data in the block's contents buffer.
	 *
	 * This and all derived implementations must be able to handle a
	 * block that is received in chunks, including cases where the
	 * preamble is split into multiple chunks."[DTN2]
	 *
	 * abstract method
	 * 
	 * @return "the amount of data consumed or -1 on error"[DTN2]
	 */
	public int consume(Bundle bundle, BlockInfo block, ByteBuffer buf, int len)
	{
		return -1;
	}

	public boolean reload_post_process(Bundle bundle, BlockInfoVec block_list, BlockInfo block)
	{
		// Received blocks might be stored and reloaded and
		// some fields aren't reset.
		// This allows BlockProcessors to reestablish what they
		// need. The default implementation does nothing.
		// In general that's appropriate, as the BlockProcessor
		// will have called parse() and that's the main need. 
		// Individual ciphersuites can override this if their 
		// usage requires it.

		block.set_reloaded(false);
		return true;
	}

	/**
	 * Validate the block. This is called after all blocks in the
	 * bundle have been fully received.
	 * @param bundle
	 * @param block_list
	 * @param block
	 * @param reception_reason
	 * @param deletion_reason
	 * @return true if the block passes validation
	 */
	public boolean validate(final Bundle           bundle,
			BlockInfoVec           block_list,
			BlockInfo              block,
			status_report_reason_t[] reception_reason,
			status_report_reason_t[] deletion_reason)
	{
		return false;
		//ver block processor (similar implementation)
	}

	/**
	 * "First callback to generate blocks for the output pass. The
	 * function is expected to initialize an appropriate BlockInfo
	 * structure in the given BlockInfoVec.
	 *
	 * The base class simply initializes an empty BlockInfo with the
	 * appropriate owner_ pointer."[DTN2]
	 */
	public int prepare(final Bundle    bundle,
			BlockInfoVec    xmit_blocks,
			final BlockInfo source,
			final Link   link,
			BlockInfo.list_owner_t     list)
	{
		return 0;
		//= 0;
	}

	/**
	 * "Second callback for transmitting a bundle. This pass should
	 * generate any data for the block that does not depend on other
	 * blocks' contents.  It MUST add any EID references it needs by
	 * calling block->add_eid(), then call generate_preamble(), which
	 * will add the EIDs to the primary block's dictionary and write
	 * their offsets to this block's preamble."[DTN2]
	 */
	public int generate(final Bundle  bundle,
			BlockInfoVec  xmit_blocks,
			BlockInfo     block,
			final Link link,
			boolean           last) 
	{
		return 0;
		//= 0;
	}

	/**
	 * "Third callback for transmitting a bundle. This pass should
	 * generate any data (such as security signatures) for the block
	 * that may depend on other blocks' contents.
	 *
	 * The base class implementation does nothing. 
	 * 
	 * We pass xmit_blocks explicitly to indicate that ALL blocks
	 * might be changed by finalize, typically by being encrypted.
	 * Parameters such as length might also change due to padding
	 * and encapsulation."[DTN2]
	 */
	public int finalize(final Bundle  bundle, 
			BlockInfoVec  xmit_blocks, 
			BlockInfo     block, 
			final Link link) 
	{
		return 0;
		//= 0;
	}

	/**
	 * "Check the block list for validation flags for the
	 * specified ciphersuite"[DTN2]
	 */
	static boolean check_validation(final Bundle bundle, 
			final BlockInfoVec  block_list, 
			int            num)
	{
		short       proc_flags = 0;
		BP_Local_CS    locals;
		// BlockInfoVec.const_iterator iter;
		Iterator<BlockInfo> iter=block_list.iterator() ;

		Log.d(TAG, String.format(  "Ciphersuite.check_validation(%hu)", num));
		if ( block_list == null )
			return false;

		while(iter.hasNext())
		{
			BlockInfo block_info = iter.next();

			if ( block_info.locals() == null )       // then of no interest
				continue;

			// locals = <BP_Local_CS*>(iter.locals());
			locals = (BP_Local_CS) (block_info.locals());
			if ( locals == null )
				continue;

			if (locals.owner_cs_num() != num )
				continue;

			// OK - this is one of interest
			proc_flags |= locals.proc_flags();
		}

		// Now check what we have collected
		// If we positively validated, we succeeded
		if ( (proc_flags & proc_flags_t.CS_BLOCK_PASSED_VALIDATION.getCode())>0 )
			return true;

		// If we had no positives, then any failure is failure
		if ( (proc_flags & proc_flags_t.CS_BLOCK_FAILED_VALIDATION.getCode())>0 )
			return false;

		// If no positives but no failure, then did we have 
		// a block which we couldn't test
		if ( (proc_flags & proc_flags_t.CS_BLOCK_DID_NOT_FAIL.getCode())>0 )
			return true;

		return false;   // no blocks of wanted type
	}

	/**
	 * "Create a correlator for this block-list. Include part
	 * of the fragment-offset is this bundle is a fragment."[DTN2]
	 */
	static long create_correlator(final Bundle  bundle, final BlockInfoVec  block_list)
	{
		long        result = 0;
		long        high_val = 1;
		long        value;
		BP_Local_CS    locals;
		// BlockInfoVec.const_iterator iter;
		Iterator<BlockInfo> iter = block_list.iterator();
		Log.d(TAG, String.format(  "create_correlator()"));
		if ( bundle == null )
			return 1;        // and good luck :)

		if ( block_list == null )
			return 1;

		if ( bundle.is_fragment() ) {
			result = bundle.frag_offset() << 24;
		}

		while(iter.hasNext())
		{
			BlockInfo block_info = iter.next();
			if ( block_info.locals() == null )       // then of no interest
				continue;

			locals = (BP_Local_CS) (block_info.locals());
			if ( locals == null )
				continue;

			value = locals.correlator();       // only low-order 16 bits

			value = value > high_val ? value : high_val;
		}
		result |= high_val;     // put high_val into low-order two bytes

		return result;
	}

	/**
	 * "Convenience methods to test if the security source/destination
	 * is an endpoint registered at the local node."[DTN2]
	 */
	static boolean source_is_local_node(final Bundle    bundle, final BlockInfo block)
	{
		short       cs_flags = 0;
		EndpointID        local_eid = BundleDaemon.getInstance().local_eid();
		BP_Local_CS    locals;
		boolean            result = false;     //default is "no" even in case of errors

		Log.d(TAG, String.format(  "source_is_local_node()"));
		if ( block == null )
			return false;

		if ( block.locals() == null )       // then the block is broken
			return false;

		locals = (BP_Local_CS) (block.locals());
		if ( locals == null )
			return false;

		cs_flags = locals.cs_flags();

		// this is a very clunky way to get the "base" portion of the bundle destination
		String bundle_src_str = bundle.source().uri().getScheme() + "://" +
		bundle.source().uri().getHost();
		//EndpointID        src_node(bundle_src_str);
		EndpointID        src_node= new EndpointID(bundle_src_str);

		// if this is security-src, or there isn't one and this is bundle source
		if (  (  (cs_flags & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE.getCode())>0) && (local_eid.toString().equals(locals.security_src().toString())) ||
				!((cs_flags & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE.getCode())>0) && (local_eid.toString().equals(src_node.toString()))              )     
		{  //yes - this is ours 
			result = true;
		}

		return result;
	}

	/**
	 * "Convenience methods to test if the security source/destination
	 * is an endpoint registered at the local node."[DTN2]
	 */
	static boolean destination_is_local_node(final Bundle    bundle, final BlockInfo block)
	{
		short       cs_flags = 0;
		EndpointID        local_eid = BundleDaemon.getInstance().local_eid();
		//EndpointID      local_eid = new EndpointID("dtn://android.bytewalla.com");
		BP_Local_CS    locals;
		boolean            result = false;     //default is "no" even in case of errors

		Log.d(TAG, String.format(  "destination_is_local_node()"));
		if ( block == null )
			return false;

		if ( block.locals() == null )       // then the block is broken
			return false;

		locals = (BP_Local_CS) (block.locals());
		if ( locals == null )
			return false;

		cs_flags = locals.cs_flags();

		// this is a very clunky way to get the "base" portion of the bundle destination
		String bundle_dest_str = bundle.dest().uri().getScheme() + "://" +
		bundle.dest().uri().getHost();
		//EndpointID        dest_node(bundle_dest_str);
		EndpointID        dest_node=new EndpointID(bundle_dest_str);

		// if this is security-dest, or there isn't one and this is bundle dest
		if    (((cs_flags & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST.getCode())>0) && (local_eid.toString().equals(locals.security_dest().toString())) ||
				!((cs_flags & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST.getCode())>0) && (local_eid.toString().equals(dest_node.toString()))                  ) 
		{  //yes - this is ours 
			result = true;
		}

		return result;
	}

	void init_locals(BlockInfo block)
	{
		/* Create new locals block but do not overwrite
		 * if one already exists.
		 * Derived classes may wish to change this behavior
		 * and map old-to-new, or whatever
		 */

		if ( block.locals() == null )
			block.set_locals( new BP_Local_CS() );
	}

	/**
	 * "Generate the standard preamble for the given block type, flags, EID-list
	 * and content length." [DTN2].
	 * It receives a block with loaded fields and generates the binary preamble in block.WritableContent and It creates the dictionary in xmit_blocks.dict().
	 * @param xmit_blocks (): 
	 * @param block (IN/OUT): we walk over the block.eid list and add the eids into the xmit_blocks.dict(). 
	 * This method writes the binary fields of the preamble into block.writablecontent 
	 * @param type (IN): type field, to be included into the preamble. 
	 * @param flags (IN): Block processing control flags to be included into the preamble. 
	 * @param data_length (IN): data length to be included into the preamble.
	 *  
	 * 
	 */
	protected void generate_preamble(BlockInfoVec  xmit_blocks, 
			BlockInfo block,
			BundleProtocol.bundle_block_type_t type,
			int flags,
			int data_length)
	{
		block.owner().generate_preamble(xmit_blocks, block, type,
				flags, data_length);
	}


	/**
	 * "Array of registered BlockProcessor-derived handlers for
	 * various ciphersuites -- fixed size for now [maybe make adjustable later]"[DTN2]
	 */
	private static Ciphersuite[] ciphersuites_ =new Ciphersuite[CS_MAX];


	static boolean inited=false;
}