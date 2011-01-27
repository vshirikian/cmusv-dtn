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

import java.io.Serializable;

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BP_Local;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;

import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.BufferHelper;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.SerializableByteBuffer;


/**
 * Represents an Abstract Security Block (ASB), as defined by the Bundle Security Protocol (page 9, draft-15)
 * It contains all the data required for the security functionality, like security source, security destination,
 * security parameters and security result. In addition, it contains the encoded binary data that refers to all the 
 * aforementioned security fields.
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */
public class BP_Local_CS extends BP_Local implements Serializable 
{

	/**
	 * Serial UID to support Java Serializable
	 */
	private static final long serialVersionUID = 7709307030221028000L;

	/**
	 * Constructor.
	 */
	public BP_Local_CS()
	{
		cs_flags_=0; 
		security_result_offset_=(0); 
		correlator_=0;
		security_params_= new SerializableByteBuffer(1024);
		security_result_= new SerializableByteBuffer(1024);
		owner_cs_num_=0;
	}

	/**
	 * Copy constructor.
	 */
	public BP_Local_CS(final BP_Local_CS b)
	{
		cs_flags_=(b.cs_flags_); 
		security_result_offset_=b.security_result_offset_;
		correlator_=(b.correlator_);
		security_params_=(b.security_params_);
		security_result_=(b.security_result_);
		owner_cs_num_=(b.owner_cs_num_);
	}

	/// @{ Accessors
	// need to think about which ones map to the locals and which
	// are derived
	/**
	 * accessor to return the cs_flags.
	 */  
	public short            cs_flags()                { return cs_flags_; }
	short            owner_cs_num()            { return owner_cs_num_; }
	int           security_result_offset()  { return security_result_offset_; }

	/**
	 * accessor to return the correlator of this security block.
	 */ 
	long           correlator()              
	{
		return correlator_; 
	}
	short           correlator_sequence()     { return correlator_sequence_; }
	final IByteBuffer  key()                     { return key_; }

	/**
	 * accessor to return the salt of this abstract security block. The nonce is made by salt (same in ABS) +IV (changes)
	 */ 
	final IByteBuffer  salt()                    { return salt_; }
	final IByteBuffer  iv()                      { return iv_; }
	final IByteBuffer  security_params()         { return security_params_; }
	String         security_src()            { return security_src_; }
	String         security_dest()           { return security_dest_; }
	final IByteBuffer  security_result()         { return security_result_; }
	BlockInfo.list_owner_t list_owner()          { return list_owner_; }
	short           proc_flags()              { return proc_flags_; }
	boolean                proc_flag(short f)    { return (proc_flags_ & f) != 0; }
	


	/// @{ Mutating accessors
	void         set_cs_flags(short f)            { cs_flags_ = f; }
	void         set_owner_cs_num(short n)        { owner_cs_num_ = n; }
	void         set_security_result_offset(long o){ security_result_offset_ = (int) o; }

	void         set_key(IByteBuffer  k, int len)
	{
		key_=new SerializableByteBuffer(len);

		BufferHelper.copy_data(key_, 0,k, 0,len);
	}

	void         set_salt(IByteBuffer  s, int len)
	{
		salt_=new SerializableByteBuffer(len);
		BufferHelper.copy_data(salt_, 0,s, 0,len);

	}
	void         set_iv(IByteBuffer  iv, int len)
	{
		iv_=new SerializableByteBuffer(len);
		BufferHelper.copy_data(iv_, 0,iv, 0,len);
	}

	void         set_correlator(long c)          { correlator_ = c; }
	void         set_correlator_sequence(short c) { correlator_sequence_ = c; }
	IByteBuffer writable_security_params()           { return security_params_; }    
	void         set_security_src(String s)      { security_src_ = s; }
	void         set_security_dest(String d)     { security_dest_ = d; }

	/**
	 * returns the results of the appropriate ciphersuite-specific calculation (e.g., a signature, MAC or ciphertext block key).
	 */
	IByteBuffer writable_security_result()           { return security_result_; }  

	void set_security_result(IByteBuffer sec_res)
	{
		security_result_=sec_res;
	}

	void set_security_params(IByteBuffer sec_par)
	{
		security_params_=sec_par;
	}
	void         set_list_owner(BlockInfo.list_owner_t o) { list_owner_ = o; }
	void         set_proc_flags(short f)          { proc_flags_ = f; }
	void         set_proc_flag(int f)           { proc_flags_ |= f; }
	


	/**
	 * Ciphersuite flags (SDNV), part of the Abstract Security Block
	 */
	private short cs_flags_;

	/**
	 * 
	 */
	private short correlator_sequence_;

	/**
	 * offset of the security-result field, starting from the ciphersuite field (SDNV). This value is set in prepare, and 
	 * is recovered and used in finalize.
	 */
	private int security_result_offset_;

	/**
	 * Correlator, part of the Abstract Security Block
	 */
	private long correlator_;

	/**
	 * Is one of the items in the security-paramterer or security-result field. 
	 * key information: key material encoded or protected by the key
	 *       management system, and used to transport an ephemeral key
	 *             protected by a long-term key. Must follow the CMS format (RFC 5652).
	 */
	private IByteBuffer key_;

	/**
	 * Is one of the items in the security-paramterer or security-result field. 
	 * Initialization vector(IV): random value, typically eight to sixteen bytes
	 */
	private IByteBuffer iv_;

	/**
	 * Is one of the items in the security-paramterer or security-result field. 
	 * salt: an IV-like value used by certain confidentiality suites. In C3, nonce = salt + IV
	 * 
	 */
	private IByteBuffer salt_; 

	/**
	 * Ciphersuite parameters to be used with the ciphersuite in use, e.g. a key identifier or initialization vector (IV)
	 * Contains the raw data, in binary: it is a sequence of type-length-value - type-length-value...
	 */
	private IByteBuffer security_params_;

	/**
	 * security_src_ and security_dest_ conform the EID references, part of the Abstract Security Block
	 */
	private String security_src_=null;

	/**
	 * security_src_ and security_dest_ conform the EID references, part of the Abstract Security Block
	 */
	private String security_dest_=null;

	/**
	 * contains the results of the appropriate ciphersuite-specific calculation (e.g., a signature, MAC or ciphertext block key).
	 */
	private IByteBuffer security_result_;  

	/**
	 * Source of the Block, usually XMIT (block that is prepared to be sent on the link).
	 */
	private BlockInfo.list_owner_t list_owner_;

	/**
	 * Ciphersuite ID (SDNV)[creo, or internal number?], part of the Abstract Security Block
	 */
	private short owner_cs_num_; 

	/**
	 * 
	 * XXX sd. [Internal] flags tracking processing status etc. Defined in Ciphersuite class as proc_flags_t. Ex: FAILED_VALIDATION, BLOCK_PASSED_VALIDATION, BLOCK_COMPLETED_DO_NOT_FORWARD, BLOCK_PROCESSING_FAILED_DO_NOT_SEND 
	 */
	private short proc_flags_;   

}