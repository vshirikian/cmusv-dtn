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

import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfo;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockInfoVec;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BlockProcessor;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.Bundle;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.bundling.BundleProtocol.bundle_block_type_t;
import se.kth.ssvl.tslab.bytewalla.androiddtn.servlib.contacts.Link;
import se.kth.ssvl.tslab.bytewalla.androiddtn.systemlib.util.IByteBuffer;

/**
 * This class...
 * 
 * @author Sebastian Domancich (sdo@kth.se)
 */   
public class PS_BlockProcessor extends BlockProcessor 
{

	/**
	 * UID to support Serializable
	 */
	private static final long serialVersionUID = 5279017268010457609L;

	public PS_BlockProcessor() 
	{
		super(bundle_block_type_t.PAYLOAD_SECURITY_BLOCK);
	}

	/// @{ Virtual from BlockProcessor
	/**
	 * First callback for parsing blocks that is expected to append a
	 * chunk of the given data to the given block. When the block is
	 * completely received, this should also parse the block into any
	 * fields in the bundle class.
	 * 
	 * This function consumes the PS block of the bundle. It is a virtual from BlockProcessor.
	 * @param bundle (OUT): Bundle to set data after consuming
	 * @param block (OUT): security block to set data after consuming
	 * @param buf (IN): Populated buffer to read data from for consuming 
	 * @param len (IN): Number of bytes to consume
	 * @return  Return number of bytes successfully consumed, In case of error return -1
	 */    
	public int consume(Bundle bundle,  BlockInfo block,  IByteBuffer buf,  int len)
	{
		//we consume the preamble
		int cc = super.consume(bundle, block, buf, len);//not used in original implementation, check!!!

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
	public boolean reload_post_process(Bundle bundle, BlockInfoVec block_list,
			BlockInfo block) 
	{
		return false;
	}
	
	/**
	 * Validate the block. This is called after all blocks in the
	 * bundle have been fully received.
	 *
	 * @return true if the block passes validation
	 */
	public boolean validate(final Bundle           bundle,
			BlockInfoVec           block_list,
			BlockInfo              block,
			BundleProtocol.status_report_reason_t[] reception_reason,
			BundleProtocol.status_report_reason_t[] deletion_reason)
	{
		return false;
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
		return 0;
	}

	/**
	 * Second callback for transmitting a bundle. This pass should
	 * generate any data for the block that does not depend on other
	 * blocks' contents.  It MUST add any EID references it needs by
	 * calling block->add_eid(), then call generate_preamble(), which
	 * will add the EIDs to the primary block's dictionary and write
	 * their offsets to this block's preamble.
	 */
	public int generate(final Bundle bundle, BlockInfoVec xmit_blocks,
			BlockInfo block, final Link link, boolean last)
	{
		return 0;
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
		return 0;
	}
}