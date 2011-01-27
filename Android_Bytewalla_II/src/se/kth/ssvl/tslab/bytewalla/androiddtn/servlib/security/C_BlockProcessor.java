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

import android.util.Log;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfoVec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
//import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleDaemon;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.bundle_block_type_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.status_report_reason_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
//import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.naming.EndpointID;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.security.BP_Local_CS;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.BufferHelper;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;


/**
 * 
 * Block processor implementation for the Payload confidentiality block (PCB). This class extends from BlockProcessor 
 * and implements a generic PCB processor. In the current implementation, it calls the methods of Ciphersuite_C3, which
 * is an implementation of the ciphersuite PCB-RSA-AES128-PAYLOAD-PIB-PCB, as defined in the Bundle Securty Protocol Speficification
 *   
 * @author Sebastian Domancich (sdo@kth.se)
 */    
public class C_BlockProcessor extends BlockProcessor 
{
	/**
	 * Serial UID to support Java Serializable
	 */
	private static final long serialVersionUID = -2101802304411629534L;

	private static String TAG = "C_BlockProcessor";

	//constructor
	public C_BlockProcessor()
	{
		super(bundle_block_type_t.CONFIDENTIALITY_BLOCK);
	}

	/**
	 * First callback for parsing blocks that is expected to append a
	 * chunk of the given data to the given block. When the block is
	 * completely received, this should also parse the block into any
	 * fields in the bundle class.
	 * 
	 * This function consumes the C block of the bundle. It is a virtual from BlockProcessor.
	 * @param bundle (OUT): Bundle to set data after consuming
	 * @param block (OUT): security block to set data after consuming
	 * @param buf (IN): Populated buffer to read data from for consuming 
	 * @param len (IN): Number of bytes to consume
	 * @return  Return number of bytes successfully consumed, In case of error return -1
	 */    
	public int consume(Bundle bundle,  BlockInfo block,  IByteBuffer buf,  int len)
	{
		//we consume the preamble
		int cc = super.consume(bundle, block, buf, len);

		if (cc == -1) {
			return -1; // protocol error
		}


		// in on-the-fly scenario, process this data for those interested

		if (! block.complete()) {
			assert(cc == (int)len);
			return cc;
		}

		if ( block.locals() == null ) {      // then we need to parse it
			Ciphersuite.parse(block);
		}

		return cc;
	}



	/**
	 * Perform any needed action in the case where a block/bundle
	 * has been reloaded from store
	 */
	public boolean reload_post_process(Bundle bundle, BlockInfoVec block_list,BlockInfo block)
	{
		// Received blocks might be stored and reloaded and
		// some fields aren't reset.
		// This allows BlockProcessors to reestablish what they
		// need

		Ciphersuite p = null;
		boolean          err = true;
		bundle_block_type_t          type;
		BP_Local_CS locals;

		if ( ! block.reloaded() )
			return true;

		type = block.type();
		Log.d(TAG, String.format( "C_BlockProcessor.reload block type %d", type));

		Ciphersuite.parse(block);
		locals = (BP_Local_CS)(block.locals());

		if (locals==null)//CS_FAIL_IF_NULL
			return false;

		p = Ciphersuite.find_suite( locals.owner_cs_num() );
		if ( p != null ) 
			err = (p.reload_post_process(bundle, block_list, block));

		block.set_reloaded(false);
		return err;
	}

	/**
	 * Validate the block. This is called after all blocks in the
	 * bundle have been fully received.
	 *  
	 * @param bundle ():
	 * @param block_list ():
	 * @param block ():
	 * @param reception_reason ():
	 * @param deletion_reason ():
	 * @return : true if the block passes validation
	 * 
	 * 
	 * @return true if the block passes validation
	 */
	public boolean validate(final Bundle           bundle,
			BlockInfoVec           block_list,
			BlockInfo              block,
			status_report_reason_t[] reception_reason,
			status_report_reason_t[] deletion_reason)
	{
		Ciphersuite_C3 p = null;

		BP_Local_CS locals = (BP_Local_CS)(block.locals());
		boolean         result = false;

		if (locals==null) //CS_FAIL_IF_NULL
		{ 
			deletion_reason[0] = BundleProtocol.status_report_reason_t.REASON_SECURITY_FAILED;
			return false;
		}    

		Log.d(TAG, String.format( "C_BlockProcessor.validate()  ciphersuite %d",
				locals.owner_cs_num()));

		if ( Ciphersuite.destination_is_local_node(bundle, block) )
		{  //yes - this is ours 

			p = (Ciphersuite_C3) Ciphersuite.find_suite( locals.owner_cs_num() );
			if ( p != null ) 
			{
				result = p.validate(bundle, block_list, block, reception_reason, deletion_reason);
				return result;
			} 
			else 
			{
				Log.e(TAG, String.format("block failed security validation C_BlockProcessor"));
				deletion_reason[0] = BundleProtocol.status_report_reason_t.REASON_SECURITY_FAILED;
				return false;
			}
		} 
		else 
		{
			// not for here so we didn't check this block
			locals.set_proc_flag(Ciphersuite.proc_flags_t.CS_BLOCK_DID_NOT_FAIL.getCode());   
		}

		return true;
	}

	/**
	 * First callback to generate blocks for the output pass. The
	 * function is expected to initialize an appropriate BlockInfo
	 * structure in the given BlockInfoVec.
	 *
	 * The base class simply initializes an empty BlockInfo with the
	 * appropriate owner_ pointer.
	 */
	public int prepare(final Bundle    bundle,
			BlockInfoVec    xmit_blocks,
			final BlockInfo source,
			final Link   link,
			BlockInfo.list_owner_t     list)
	{
		Ciphersuite    p = null;
		int             result = BP_FAIL;
		BP_Local_CS    locals = null;
		BP_Local_CS    source_locals = null;

		if ( list == BlockInfo.list_owner_t.LIST_RECEIVED ) 
		{
			assert(source != null);
			short       cs_flags = 0;

			if ( Ciphersuite.destination_is_local_node(bundle, source) )
				return BP_SUCCESS;     //don't forward if it's for here

			xmit_blocks.add(new BlockInfo(this, source));
			BlockInfo bp = (xmit_blocks.back());
			bp.set_eid_list(source.eid_list());
			Log.d(TAG, String.format( "C_BlockProcessor.prepare() - forward received block len %d",
					source.full_length()));

			if(Ciphersuite.CS_FAIL_IF_NULL(source.locals())) // CS_FAIL_IF_NULL
			{
				if ( locals !=  null )
					locals.set_proc_flag(Ciphersuite.proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND.getCode());
				return BP_FAIL;
			}

			source_locals = (BP_Local_CS)(source.locals());

			if(Ciphersuite.CS_FAIL_IF_NULL(source_locals)) // CS_FAIL_IF_NULL
			{
				if ( locals !=  null )
					locals.set_proc_flag(Ciphersuite.proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND.getCode());
				return BP_FAIL;
			}

			bp.set_locals(new BP_Local_CS());
			locals = (BP_Local_CS)(bp.locals());

			if(Ciphersuite.CS_FAIL_IF_NULL(locals)) // CS_FAIL_IF_NULL
			{
				return BP_FAIL;
			}

			locals.set_owner_cs_num(source_locals.owner_cs_num());
			cs_flags = source_locals.cs_flags();
			locals.set_correlator(source_locals.correlator());
			locals.set_list_owner(BlockInfo.list_owner_t.LIST_RECEIVED);

			// copy security-src and -dest if they exist
			if (( source_locals.cs_flags() & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE.getCode()) >0) {
				assert(source_locals.security_src().length() > 0 );
				cs_flags |= Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_SOURCE.getCode();
				locals.set_security_src(source_locals.security_src());
				Log.d(TAG, String.format( "C_BlockProcessor.prepare() add security_src EID %s", 
						source_locals.security_src()));
			}

			if ( (source_locals.cs_flags() & Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST.getCode())>0 ) {
				assert(source_locals.security_dest().length() > 0 );
				cs_flags |= Ciphersuite.ciphersuite_flags_t.CS_BLOCK_HAS_DEST.getCode();
				locals.set_security_dest(source_locals.security_dest());
				Log.d(TAG, String.format( "C_BlockProcessor.prepare() add security_dest EID %s",
						source_locals.security_dest()));
			}
			locals.set_cs_flags(cs_flags);
			Log.d(TAG, String.format( "C_BlockProcessor.prepare() - inserted block eid_list_count %d",
					bp.eid_list().size()));
			result = BP_SUCCESS;
		} 
		else 
		{
			if ( source != null ) {
				source_locals = (BP_Local_CS)(source.locals());

				if(Ciphersuite.CS_FAIL_IF_NULL(source_locals)) // CS_FAIL_IF_NULL
				{
					if ( locals !=  null )
						locals.set_proc_flag(Ciphersuite.proc_flags_t.CS_BLOCK_PROCESSING_FAILED_DO_NOT_SEND.getCode());
					return BP_FAIL;
				}

				p = Ciphersuite.find_suite( source_locals.owner_cs_num() );
				if ( p != null ) {
					result = p.prepare(bundle, xmit_blocks, source, link, list);
				} else {
					Log.e(TAG, String.format( "C_BlockProcessor.prepare() - ciphersuite %d is missing",
							source_locals.owner_cs_num()));
				}
			}  // no msg if "source" is null, as BundleProtocol calls all BPs that way once
		}
		return result;
	}

	/**
	 * Second callback for transmitting a bundle. This pass should
	 * generate any data for the block that does not depend on other
	 * blocks' contents.  It MUST add any EID references it needs by
	 * calling block.add_eid(), then call generate_preamble(), which
	 * will add the EIDs to the primary block's dictionary and write
	 * their offsets to this block's preamble.
	 */
	public int generate(final Bundle bundle, BlockInfoVec xmit_blocks,
			BlockInfo block, final Link link, boolean last)
	{
		Ciphersuite    p = null;
		int             result = BP_FAIL;
		Log.d(TAG, String.format( "C_BlockProcessor.generate()"));
		BP_Local_CS    locals = (BP_Local_CS)(block.locals());

		if (locals==null) //CS_fail if null
			return BP_FAIL;

		p = Ciphersuite.find_suite( locals.owner_cs_num() );
		if ( p != null ) //true because owner_cs_num was initialized in ciphersuite_c3.prepare
		{
			result = p.generate(bundle, xmit_blocks, block, link, last);
		} 
		else //when is executed?
		{
			// generate the preamble and copy the data.
			int length = block.source().data_length();

			generate_preamble(xmit_blocks, 
					block,
					BundleProtocol.bundle_block_type_t.CONFIDENTIALITY_BLOCK,
					BundleProtocol.block_flag_t.BLOCK_FLAG_DISCARD_BUNDLE_ONERROR.getCode() |
					(last ? BundleProtocol.block_flag_t.BLOCK_FLAG_LAST_BLOCK.getCode() : 0),
					length);

			IByteBuffer contents = block.writable_contents();
			contents = BufferHelper.reserve (contents, block.data_offset() + length);

			BufferHelper.copy_data(contents, block.data_offset(), block.source().contents(), block.source().data_offset(), length);	

			result = BP_SUCCESS;
		}
		return result;
	}

	/**
	 * Third callback for transmitting a bundle. This pass should
	 * generate any data (such as security signatures) for the block
	 * that may depend on other blocks' contents.
	 *
	 * The base class implementation does nothing. 
	 */
	public int finalize(final Bundle  bundle, 
			BlockInfoVec  xmit_blocks, 
			BlockInfo     block, 
			final Link link)
	{
		Ciphersuite    p = null;
		int             result = BP_FAIL;
		Log.d(TAG, String.format( "C_BlockProcessor.finalize()"));

		BP_Local_CS    locals = (BP_Local_CS)(block.locals());

		if (Ciphersuite.CS_FAIL_IF_NULL(locals))
		{
			return BP_FAIL;
		}

		p = Ciphersuite.find_suite( locals.owner_cs_num() );
		if ( p != null ) {
			result = p.finalize(bundle, xmit_blocks, block, link);
		} 
		// If we are called then it means that the ciphersuite for this Bundle
		// does not exist at this node. All the work was done in generate()
		return result;


	}

	public C_BlockProcessor(bundle_block_type_t blockType) 
	{
		super(blockType);
	}
}